package au.gov.ansto.bragg.echidna.dra.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * This class is the concrete processor of the stitching algorithm for echidna algorithm group.
 * It uses the stitched data of the echidna input data object. It will return a two-dimensional data
 * which has the integrated vector, the error vector and the two-theta vector.
 *  
 * @author nxi
 * @author jxh
 * @version 3.0
 */
public class Stitching extends ConcreteProcessor {

	private static String ANGULAR_OFFSET_FILE;
	IGroup stitch_input = null;
	Boolean stitch_skip = false;
	Boolean stitch_stop = false;
	Double stitch_binSize = 0.;
	IGroup stitch_output = null;
	Boolean stitch_reversed = true;   // The tube angle order is incorrect (should be reversed)
	Boolean ang_correct = false;      // Apply angular corrections
	private double [] new_twotheta = null;
	private double [] correction_data = null;
	private double max_correction = 0.0; //For informational purposes
	private double [][] stitch_result = null;
	private double [][] stitch_variance = null;
	private int [][] origin_tube = null; //Original tube for each angle in new_twotheta
	private int [][] new_contribs = null;  // A new contributor map for the stitched data
	private String currentAngularFile;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	/* This method is called when we want to "stitch" the data, that is, rearrange the n data from m tubes
	 * into a single continuous array with n x m data points.  If stitch_reversed is set, we reverse the
	 * order of the array elements listing the tube angles.
	 */
	public Boolean process() throws Exception{
		if (stitch_skip || stitch_input == null) {
			stitch_output = stitch_input;
		} else{
			String metadata_string = "";
			IArray inDat = ((NcGroup) stitch_input).findSignal().getData();
			int [] dataShape = inDat.getShape();
			int drank = inDat.getRank();
			IArray inVar = ((Plot) stitch_input).getVariance().getData();
			IDataItem cbs = stitch_input.findDataItem("contributors");
			IDataItem bad_frames = stitch_input.findDataItem("bad_frames");
			IArray contribs = null;
			if(cbs != null) {
				contribs = cbs.getData();
			} else   // fill with ones
			{
				int [] revShape = {dataShape[2],dataShape[1]};  /* contributor map is tube first index, vertical pixel second index */
				contribs = Factory.createArray(int.class, revShape);
				for (IArrayIterator j = contribs.getIterator();j.hasNext();j.next().setIntCurrent(1)) {};
			}
			IArray bad_frame_data = null;
			if(bad_frames!=null) bad_frame_data = bad_frames.getData();
			// Obtain angles using NeXuS conventions
			IArray scanStep = null;
			IArray array2theta = null;
			IArray vOffset = null;
			String tube_units = "degrees";
			List<Axis> axes_di = ((Plot) stitch_input).getAxisList();
			double radius = stitch_input.findDataItem("radius").readScalarDouble();
			if(radius<0.1) radius = 1250.0; //for one week in April 2009 it was zero
			if(axes_di.size()!=3) {       //we have bad input NeXuS data
				scanStep = stitch_input.findDataItem("scanStep").getData();
				array2theta = stitch_input.findDataItem("xoffset").getData();
				tube_units = "mm";        //We just know this, unit attribute isn't set
				vOffset = stitch_input.findDataItem("verticalOffset").getData();
			} else {
				// The step positions will be the most slowly-varying element, which is earliest in the array
				scanStep = ((Plot) stitch_input).getAxis(0).getData();
				// The tube positions at each step we assume to be the fastest varying element
				array2theta = ((Plot) stitch_input).getAxis(2).getData();
				// Units may be degrees or mm depending on when they were generated...
				tube_units = ((Plot) stitch_input).getAxis(2).getUnitsString();
				vOffset = ((Plot) stitch_input).getAxis(1).getData(); //vertical pixel: 2nd dimension
			}
			metadata_string = metadata_string + String.format("Data were collected over a total of %d detector steps%n", scanStep.getSize());
			// Deal with historical issues.  Until April 2009, the array2theta array was two-dimensional.  For 6 days at the beginning
			// of April, it disappeared altogether.  When it reappeared, it was one-dimensional.  Following discussion with Ferdi, it
			// reappeared in May as a 2D array, but this time with the correct orientation.
			if(array2theta.getRank()==2) {       //Prior to April 2009; must convert to 1D array
				int [] origin = {0,0};
				int [] shape = {1,array2theta.getShape()[1]};
				array2theta = array2theta.getArrayUtils().section(origin, shape).getArray();  //remove step component
				IIndex a2tind = array2theta.getIndex();
				a2tind.set(0);
				double botbin = array2theta.getDouble(a2tind);
				a2tind.set(1);
				double topbin = array2theta.getDouble(a2tind);
				double binmid = botbin + (topbin-botbin)/2;
				array2theta = array2theta.getArrayMath().add(binmid*-1.0).getArray(); //set first tube to zero offset
			}
			// Up until May 2008, the angular values corresponded to the centre of each tube.  Now they correspond
			// to the edges of each tube, so we need to find the centres as that is how we have written our stitching algorithm.
			// TODO: revisit stitching algorithm and see if using bin boundaries would help us
			if(array2theta.getShape()[0]==dataShape[drank-1]+1) array2theta = findBinCentres(array2theta);
			// Now convert to degrees, if necessary
			if(tube_units.equals("mm")) array2theta.getArrayMath().scale(180.0/(radius*Math.PI));
			// Now prepare the other dimension as well
			if(vOffset.getShape()[0]==dataShape[drank-2]+1) {     //using bin boundaries and bottom left as 0,0
				vOffset = findBinCentres(vOffset);
				//Get the detector active height to convert to height relative to central position
				double det_height = stitch_input.getRootGroup().findDataItem("active_height").readScalarDouble();
				vOffset.getArrayMath().add(-1.0*det_height/2.0);
			} else {                                    // 2007 data using vertical channel number; convert to mm
				double det_height = stitch_input.getRootGroup().findDataItem("height").readScalarDouble();
				double height_per_pixel = det_height/vOffset.getSize();
				vOffset.getArrayMath().scale(height_per_pixel);
				vOffset.getArrayMath().add(-1.0*det_height/2.0);
			}
			if (stitch_reversed) {
				array2theta = array2theta.getArrayUtils().flip(0).getArray();  // tube positions are listed in the second dimension	
			}
			//  Apply angular corrections to polar angle and stth array
			if (ANGULAR_OFFSET_FILE != null && ANGULAR_OFFSET_FILE.length() > 0){
				if (!ANGULAR_OFFSET_FILE.equals(currentAngularFile)) {
					correction_data = read_correction_file(new File(ANGULAR_OFFSET_FILE));
					currentAngularFile = ANGULAR_OFFSET_FILE;
				}
			} else {
				if (currentAngularFile != null) {
					String filename = System.getProperty("dav.offsets.angular","");
					IFileStore fileStore = EFS.getStore(new URI(filename));
					correction_data = read_correction_file(fileStore.toLocalFile(EFS.NONE, new NullProgressMonitor()));
					currentAngularFile = null;
				}
			}
			if (ang_correct) {
				array2theta = do_angular_correction(array2theta,correction_data);
				metadata_string = String.format("Angular correction applied to nominal detector tube positions. Maximum " +
						"correction %8.3f%n",max_correction);
			}
			IIndex index = array2theta.getIndex();
			index.set(0);
			Double twotheta0 = array2theta.getDouble(index); //first value of left/rightmost tube
			double[] scanStepData = ConverterLib.get1DDouble(scanStep);
			double roughbinSize = Math.abs(scanStepData[0]-scanStepData[scanStepData.length-1])/(scanStepData.length-1);//assume monotonic increasing
			System.out.println("First tube, first position: "+ (twotheta0+scanStepData[0]));
			IArray out_array = null;
			IArray twoThetaArray = null;
			IArray out_variance = null;
			IArray out_contribs = null;
			if(drank==2) {
				out_array = inDat; 
				out_variance = inVar;
				twoThetaArray = array2theta.getArrayMath().toAdd(scanStepData[0]).getArray();
				out_contribs = contribs.getArrayUtils().transpose(1, 0).getArray();
				if(ang_correct)
				metadata_string = "Two theta angles corrected%n";
				else
					metadata_string = "Two theta angles set to ideal values%n";
			} else {
				if(ang_correct) {
					ExactDataStitch(ConverterLib.get3DDouble(inDat), ConverterLib.get1DDouble(array2theta), 
							scanStepData,ConverterLib.get3DDouble(inVar),(int [][]) contribs.getArrayUtils().copyToNDJavaArray(),
							(int[]) bad_frame_data.getArrayUtils().copyTo1DJavaArray());
					metadata_string = metadata_string + "Data from each detector tube sorted into a single two-dimensional " +
					"intensity array ordered by corrected 2-theta angle\n";
				} else {
					IdealDataStitch(ConverterLib.get3DDouble(inDat), ConverterLib.get1DDouble(array2theta), 
							scanStepData,ConverterLib.get3DDouble(inVar),(int [][]) contribs.getArrayUtils().copyToNDJavaArray());
					metadata_string = metadata_string + "Data from each detector tube sorted and merged into a single two-dimensional " +
					"array assuming ideal angular positions of the detectors";
				}
				// Output results to databag object
				out_array = Factory.createArray(stitch_result);
				twoThetaArray = Factory.createArray(new_twotheta);
				out_variance = Factory.createArray(stitch_variance);
				out_contribs= Factory.createArray(new_contribs);
			}
			String resultName = "stitch_result";
			stitch_output = PlotFactory.createPlot(stitch_input, resultName, dataDimensionType);
			((NcGroup) stitch_output).addLog("apply stitching algorithm to get " + resultName);
			PlotFactory.addDataToPlot(stitch_output, resultName, out_array, "Stitched data", "Counts", out_variance);
			PlotFactory.addAxisToPlot(stitch_output, "verticalOffset", vOffset, "Vertical pixel offset", "mm", 0);
			PlotFactory.addAxisToPlot(stitch_output, "two_theta_vector", twoThetaArray, "Two theta", "degrees", 1);
			IDataItem pixel_ok = Factory.createDataItem(stitch_output, "contributors", out_contribs);
			// Add data for visualisation subsystem
			//			stitch_output.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
			//					StaticDefinition.DataStructureType.plot.name());
			//			stitch_output.addStringAttribute(StaticDefinition.DATA_DIMENSION_TYPE, StaticDefinition.DataDimensionType.map.name());
			stitch_output.addDataItem(pixel_ok);
			((NcGroup) stitch_output).addMetadata("CIF", "_pd_proc_info_data_reduction", metadata_string, false);
			((NcGroup) stitch_output).addMetadata("CIF", "_[local]_scan_step_size", String.format("%.5f", roughbinSize));
			//			result.add(group);
		}
		// blank out fields of the object to save memory, as the object will remain instantiated as long as this algorithm
		// chain is operating
		new_twotheta = null;
		//		correction_data = null;
		stitch_result = null;
		stitch_variance = null;
		origin_tube = null;
		new_contribs = null;  // A new contributor map for the stitched data

		return stitch_stop;
	}

	/**
	 * This  algorithm is developed to stitch together possibly overlapped multiple scan data sets. It is "ideal" in that it
	 * assumes on-average evenly-spaced steps and makes no attempt to account for deviations of a given tube at a given
	 * step from the ideal position. The data calculated include a new
	 * 2 theta vector, stitched data, stitched variances, and a stitched pixel_ok map. To avoid complexity in the return data 
	 * structure, private fields in the object are set instead of returning anything.
	 * @param inDat      Input 3D file with nScan * nCounts * nTubes
	 * @param array2theta  Tube position 2theta array nTubes
	 * @param steplist  List of steps nScan elements
	 * @param variances variance array for data in inDat
	 * @param contribs  pixel ok map
	 * @return 
	 */

	public void IdealDataStitch( double[][][] inDat, double[] tube2theta, double[] steplist, 
			double[][][] variances,int [][] contribs){
		int nScan = inDat.length;
		int nCount = inDat[0].length;
		int nTubes =inDat[0][0].length;
		double dbinSize = 0.0;

		System.out.println("Data Dimensions: " + nScan + "; " + nCount +"; " + nTubes);

		/* The starting value of 2-theta in the output array will always be the lowest-angle position.  Rather than assume a particular
		 * tube numbering low->high or scan direction low-> high, we search for the lowest value in the 4 possible extreme positions
		 */
		double twotheta0 = tube2theta[nTubes-1];    // most likely in old style tube numbering scheme
		if (tube2theta[0]+steplist[0]< twotheta0) twotheta0 = tube2theta[0]+steplist[0];
		if (tube2theta[0]+steplist[nScan-1]< twotheta0) twotheta0 = tube2theta[0]+steplist[nScan-1];
		if (tube2theta[nTubes-1]+steplist[nScan-1]< twotheta0) twotheta0 = tube2theta[nTubes-1]+steplist[nScan-1];	

		/* extract the tube separation from the data.*/
		double tube_sep = Math.abs(tube2theta[0]-tube2theta[nTubes-1])/(tube2theta.length-1);

		/* create a 2D array of tube position at each step */
		double [][] array2theta = new double[nScan][nTubes];
		for(int ntube=0;ntube<nTubes;ntube++)
			for(int nscan=0;nscan<nScan;nscan++)
				array2theta[nscan][ntube] = tube2theta[ntube]+steplist[nscan];

		/* The total length will be the difference between the first angle of the lowest-angle tube and the last
		 * angle of the highest-angle tube, for scanning low->high.  We divide by the average stepsize to get the number of bins.  
		 * Note that
		 * the first tube could be at highest angle, so the scan direction is opposite the tube listing
		 * direction, or the first tube could be at the lowest angle, in which case the scan direction and
		 * the tube listing direction would coincide for a normal low->high scan.  We catch both cases by
		 * searching for the largest difference between first and last tube values. 
		 */
		double theta_len = Math.abs(array2theta[0][0] - twotheta0);
		theta_len = Math.max(theta_len,Math.abs(array2theta[0][nTubes-1]-twotheta0));
		theta_len = Math.max(theta_len,Math.abs(array2theta[nScan-1][0]-twotheta0));
		theta_len = Math.max(theta_len,Math.abs(array2theta[nScan-1][nTubes-1] -twotheta0));

		/* To work out bin size, we consider each tube to be centered at the reported angle, and the bin size will be the step
		 * size (not the (fixed) angular range of the Soller slits, although that would be most correct).
		 * So for 50 scan points there are 50 bins covering a range equal to 50xstepsize. 
		 * 
		 * The values in the list of stth steps will contain some jitter, which will transfer into our estimate of
		 * the average stepsize if we simply divide by the number of scan steps.  This error will then be magnified if we
		 * multiply by the total number of steps including all tubes.  Therefore, we use the roundabout way of
		 * estimating the number of steps a single tube takes before overlapping with data from the next neighbour, and
		 * multiply this value by the number of tubes to get the total steps.
		 */
		double roughbinSize = Math.abs(steplist[0]-steplist[steplist.length-1])/(steplist.length-1);//assume monotonic increasing
		int non_overlap_points = (int) Math.round(tube_sep/roughbinSize);
		int total_points = non_overlap_points*(nTubes-1) + nScan;  // one tube has no overlapped values
		theta_len = theta_len + roughbinSize; //total length from maximum to minimum has an extra half width at each end
		dbinSize = theta_len/(double) total_points; 

		System.out.println("average stepsize = " + dbinSize );
		System.out.println("average tube separation: " + tube_sep);
		System.out.println("Non overlapping points per tube: " + non_overlap_points);

		int nBins = (int) Math.round(theta_len/dbinSize);
		System.out.println("nBin and length of curve: " + nBins + ", " + theta_len);
		new_twotheta = new double[nBins];      

		/* create a scan step array with ideal positions. */
		for (int k = 0; k < nBins; k++) new_twotheta[k] = twotheta0 + k * dbinSize;	
		stitch_result = new double[nCount][nBins];
		stitch_variance = new double[nCount][nBins];
		new_contribs = new int[nCount][nBins];
		int ncontrBin[] = new int[nBins];  /* count contributions at each value */
		/* loop over each of the input datapoints and assign to a bin. For efficiency,
		 * we keep track of whether or not there is more than one contribution to any
		 * point.  In the likely case that there is not, we can skip dividing by the
		 * number of contributions later.  If contribs is available, we use it to
		 * restrict contributions */
		boolean more_than_one = false;
		double low_bin = twotheta0-dbinSize/2.0; /* low side boundary of low angle bin so we can use floor instead of round */
		for(int k = 0; k < nScan; k++){
			for(int j = 0; j < nTubes; j++){	
				/* calculate target bin for this angle */
				double obs_angle = array2theta[k][j];
				int tobin = (int) Math.floor((obs_angle - low_bin)/dbinSize);  //floor may be more efficient than round?
				/* increment contributions to this bin and copy vertical pixels into output array */
				ncontrBin[tobin]++;	
				more_than_one = (ncontrBin[tobin]>1);
				/* loop over vertical pixels as they are all at the same nominal angle */
				for  (int nc =0; nc < nCount; nc++) {
					if(contribs[j][nc]==1)   /* if these pixels are OK: the contribs array is indexed first by tube, then vert. pixel */
					{
						stitch_result[nc][tobin] += inDat[k][nc][j];
						stitch_variance[nc][tobin] += variances[k][nc][j];  /* variances add, how convenient */
						new_contribs[nc][tobin] = 1;                        /* a contributor */
					}
				}
			}
		}
		/* For debugging: find any bins with no contributions */
		for(int i=0;i<nBins;i++) if(ncontrBin[i]<1) System.out.printf("Bin %d has zero contributions%n",i);
		/* now divide by the number of contributions.  For efficiency, if they are all 1, we skip this
		 * step.
		 */
		if (more_than_one) {
			for(int ith = 0; ith < nBins; ith++)
				for(int k = 0; k < nCount; k++)
					if (ncontrBin[ith] > 1) {
						System.out.printf("Stitching: %d contributions to bin %d",ncontrBin[ith],ith);
						stitch_result[k][ith] = stitch_result[k][ith]/ncontrBin[ith];
						stitch_variance[k][ith] = stitch_variance[k][ith]/ncontrBin[ith];
					}
		}

	}

	/**
	 * This  algorithm is developed to stitch together possibly overlapped multiple scan data sets. It is "exact" in that
	 * deviations from the ideal tube positions are preserved.  Note that, for a typical scan where the step
	 * size is the same as the collimation angle, any deviation from the ideal will imply a partial overlap with one of
	 * the neighbouring data points, but no correction or attempt to rebin is performed.  The final result is just a 2D list
	 * of vertical intensities, with a 1D list of angles that are ordered. To avoid complexity in the return data 
	 * structure, private fields in the processor block object are set instead of communicating via the return value.
	 * @param inDat      Input 3D file with nScan * nCounts * nTubes
	 * @param tube2theta   Scan 2D 2theta array nTubes
	 * @param stepsize  Multiple scan step size 1D array with nScan elements
	 * @param variances variance array for data in inDat
	 * @param contribs  pixel ok map
	 * @return 
	 */

	public void ExactDataStitch( double[][][] inDat, double [] tube2theta, double[] steplist, 
			double[][][] variances,int [][] contribs, int[] bad_frames){
		int nScan = inDat.length;
		int nCount = inDat[0].length;
		int nTubes =inDat[0][0].length;
		origin_tube = new int [nTubes*nScan][10];  //maximum possible number of angles, with maximum 10 overlapping perfectly at each point
		SortedMap<Double,Double[][]> sorted_data = new TreeMap<Double,Double[][]>(); // for storing sorted data
		HashMap<Double,Integer> count_contribs = new HashMap<Double,Integer>(); //for storing number of contributions
		SortedMap<Double,ArrayList<Integer>> origin_tubes = new TreeMap<Double,ArrayList<Integer>>(); //for storing origin tubes

		System.out.println("Data Dimensions: " + nScan + "; " + nCount +"; " + nTubes);

		/* We wish to create a 2D array of data points and a 1D list of matching angles.  We could make the angle list simply by sorting array2theta
		 * into strictly ascending order, but we need to make all the sort operations act simultaneously on the data as well. We produce this
		 * behaviour by using a SortedMap, where the sorted keys are angles and the values are the lists of vertical pixels.
		 * We simultaneously keep track of which angles belong to which tubes, so that the vertical integration step can re-derive
		 * scale factors between tubes where overlap has been used.
		 */
		boolean more_than_one;                   // flag that we don't have to initialize output values
		for(int k = 0; k < nScan; k++){
			if(bad_frames[k]!=0) {
				System.out.printf("Ignoring frame %d as it has been flagged bad%n", k);
				continue;       // this frame will have a bad angular value
			}
			for(int j = 0; j < nTubes; j++){	
				/* Get angle corresponding to this value */
				double obs_angle = steplist[k]+tube2theta[j];
				Double[][] data_values = new Double[3][nCount];
				if(sorted_data.containsKey(obs_angle)) {    // Exactly the same angle!
					// We cannot keep a track of multiple tubes producing exactly the same angle
					System.out.printf("Exact overlap during exact stitching at %f, step %d, tube %d %n",obs_angle,k,j);
					data_values = sorted_data.get(obs_angle);
					count_contribs.put(obs_angle, count_contribs.get(obs_angle)+1);
					origin_tubes.get(obs_angle).add(j);
					more_than_one = true;
				} else { 
					more_than_one = false;
					count_contribs.put(obs_angle,1);
					origin_tubes.put(obs_angle, new ArrayList<Integer>());
				}
				origin_tubes.get(obs_angle).add(j);
				/* loop over vertical pixels as they are all at the same nominal angle. Contribs are indexed by tube then
				 * vertical pixel */
				for  (int nc =0; nc < nCount; nc++) {
					{
						if (more_than_one) {        // should add; this condition is unlikely
							data_values[0][nc] += inDat[k][nc][j];
							data_values[1][nc] += variances[k][nc][j];
							if (contribs[j][nc] == 1) data_values[2][nc] = 1.0; 
						} else
						{
							data_values[0][nc] = inDat[k][nc][j];
							data_values[1][nc] = variances[k][nc][j];
							data_values[2][nc] = (double) contribs[j][nc];
						}
					}
				}
				sorted_data.put(obs_angle,data_values);
			}
		}

		/* now divide by the number of contributions and put back into our plain 2D arrays.
		 * Note the redundancy implied by using both new_twotheta and next_twotheta.  The problem
		 * is that the ucar array Factory does not properly create an array from an array of Doubles,
		 * even though it tries hard: it ends up calling a method of a null pointer in reflectArrayCopyIn as
		 * it determines that Double is not a primitive type and recurses, but obviously cannot get a component
		 * type of Double and fails miserably.  So we have to copy our Double array into double so that the
		 * Array can be built properly later on.  We are forced to use Double, not double, as the TreeMap 
		 * constructor needs a proper object.
		 */
		int total_len = sorted_data.size();
		Double[] next_twotheta = sorted_data.keySet().toArray(new Double[total_len]);
		new_twotheta = new double[total_len];
		stitch_result = new double[nCount][total_len];
		stitch_variance = new double[nCount][total_len];
		new_contribs = new int[nCount][total_len];
		for(int k=0;k<total_len;k++) {
			for(int ith=0;ith<nCount;ith++)
			{
				stitch_result[ith][k] = sorted_data.get(next_twotheta[k])[0][ith]/count_contribs.get(next_twotheta[k]);
				stitch_variance[ith][k] = sorted_data.get(next_twotheta[k])[1][ith]/count_contribs.get(next_twotheta[k]);
				new_contribs[ith][k] = sorted_data.get(next_twotheta[k])[2][ith].intValue();
			}
			new_twotheta[k] = next_twotheta[k];
		}
	}

	/* Apply angular corrections to the polar angle and stth arrays.  Correcting all 6400
	 * entries is not optimal, as the stitching routine only looks at a few particular
	 * angular values.  However, doing it this way guards against future changes in the 
	 * stitching routine failing to account for angular correction
	 */
	private IArray do_angular_correction(IArray polar_angles, double [] corrections) {
		IIndex old_array_index = polar_angles.getIndex();
		int [] angles_shape = polar_angles.getShape();
		IArray new_positions = Factory.createArray(double.class, angles_shape);
		IIndex new_array_index = new_positions.getIndex();
		/* The polar angle array is a set of positions for all the tubes at each step, that is,
		 * the slowly-varying index is step number, and the fast-varying index is the tube no.
		 */
		max_correction = 0.0;
		for(int tube_no=0;tube_no<angles_shape[0];tube_no++) {
			double this_correction = correction_data[tube_no];
			if(Math.abs(this_correction)>max_correction) max_correction=this_correction;
			old_array_index.set(tube_no);
			new_array_index.set(tube_no);
			new_positions.setDouble(new_array_index, polar_angles.getDouble(old_array_index)+this_correction);
		}
		return new_positions;
	}

	/* The angular correction file is a sequence of numbers corresponding to a correction
	 * to be applied to the positions held in the input file under 'polar angle' and 'stth'.  
	 */
	private double[] read_correction_file(File ang_file) {
		double [] correction_data=new double[200];
		BufferedReader getdata;
		try {
			getdata = new BufferedReader(new FileReader(ang_file));
		} catch (FileNotFoundException e) {
			return correction_data;   //all zero at this stage
		}
		String next_line;
		String [] these_numbers;
		int current=0;           //pointer to current position in double array
		try {
			// Find the next line with data, ignoring empty lines and comments
			for(next_line=getdata.readLine();next_line!=null;) {
				if (next_line.equals("")) {   /* watch out for blank lines */
					next_line=getdata.readLine();
					continue;
				}
				int comment_start = next_line.indexOf("#");   /* anything after a hash is ignored */
				if(comment_start>0) {
					next_line = next_line.substring(0, comment_start);
				} else if (comment_start==0) {                /* nothing on this line at all */
					next_line=getdata.readLine();
					continue;
				}
				these_numbers = next_line.trim().split(" +");
				for(String this_number: these_numbers) {
					correction_data[current++]=Double.parseDouble(this_number);
				}
				next_line=getdata.readLine();
			}
		} catch (IOException e) {
			return correction_data;
		}
		return correction_data;
	}

	/* A routine to convert from bin boundaries to bin centres.  Proper resting place would be not in this
	 * concrete processor but in a general library.
	 */
	public IArray findBinCentres(IArray bin_borders) {
		double bin_lowedge = 0;
		int[] out_len = {bin_borders.getShape()[0]-1};
		IArray centre_array = Factory.createArray(double.class, out_len);
		IIndex c_index = centre_array.getIndex();
		IIndex o_index = bin_borders.getIndex();
		double bin_highedge = 0;                       //allocate memory outside the loop
		o_index.set(0);
		bin_lowedge = bin_borders.getDouble(o_index);
		for(int c_i=0;c_i<out_len[0];c_i++) {
			c_index.set0(c_i);
			o_index.set0(c_i+1);
			bin_highedge = bin_borders.getDouble(o_index);
			centre_array.setDouble(c_index, bin_lowedge + (bin_highedge-bin_lowedge)/2.0);
			bin_lowedge = bin_highedge;                 //set ready for next time through the loop
		}
		return centre_array;
	}

	public IGroup getStitch_output() {
		return stitch_output;
	}

	public void setStitch_input(IGroup stitch_input) {
		this.stitch_input = stitch_input;
	}

	public void setStitch_skip(Boolean stitch_skip) {
		this.stitch_skip = stitch_skip;
	}

	public void setStitch_stop(Boolean stitch_stop) {
		this.stitch_stop = stitch_stop;
	}

	public void setStitch_reverse(Boolean is_reversed) {
		this.stitch_reversed = is_reversed;
	}

	public void setStitch_correct(Boolean do_correction) throws IOException {
		this.ang_correct = do_correction;
		File correctionFile = null;
		System.out.println("loading default angular correction");
		try {
			if (ANGULAR_OFFSET_FILE != null && ANGULAR_OFFSET_FILE.length() > 0) {
				correctionFile = new File(ANGULAR_OFFSET_FILE);
				currentAngularFile = ANGULAR_OFFSET_FILE;
			} else {
				String filename = System.getProperty("dav.offsets.angular","");
				IFileStore fileStore = EFS.getStore(new URI(filename));
				correctionFile = fileStore.toLocalFile(EFS.NONE, new NullProgressMonitor());
				currentAngularFile = null;
			}
			System.out.println("loading default angular offsets from "+correctionFile.getPath());
			//			correctionFile = ConverterLib.findFile(Activator.getDefault().PLUGIN_ID, "data/echidna.ang");
		} catch (Exception e) {
			System.out.println("Failed to open default angle correction file");
			throw new IOException("Unable to open default angle correction file",e);
		}
		this.correction_data = read_correction_file(correctionFile);
	}

	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	/**
	 * @return the aNGULAR_OFFSET_FILE
	 */
	public static String getANGULAR_OFFSET_FILE() {
		return ANGULAR_OFFSET_FILE;
	}

	/**
	 * @param aNGULAR_OFFSET_FILE the aNGULAR_OFFSET_FILE to set
	 */
	public static void setANGULAR_OFFSET_FILE(String aNGULAR_OFFSET_FILE) {
		ANGULAR_OFFSET_FILE = aNGULAR_OFFSET_FILE;
	}
}
