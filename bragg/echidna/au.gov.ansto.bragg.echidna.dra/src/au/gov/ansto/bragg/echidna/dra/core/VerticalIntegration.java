package au.gov.ansto.bragg.echidna.dra.core;

import java.io.IOException;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcArray;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import ucar.ma2.IndexIterator;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;


/**
 * This class is the concrete processor of the vertical integration algorithm for echidna algorithm group.
 * It uses the stitched data of the echidna input data object. It will return a two-dimensional data
 * which has the integrated vector, the error vector and the two-theta vector.  An optional parameter allows
 * the output data to be scaled such that the maximum value is 10000 counts.  This is provided to enable
 * datasets to be compared easily
 *  
 * @author nxi,jhester
 * @version 2.0
 */
public class VerticalIntegration extends ConcreteProcessor{

	IGroup verticalIntegration_input = null;
	private IGroup alignment_input = null;
	Boolean verticalIntegration_skip = false;
	Boolean verticalIntegration_stop = false; 
	Double verticalIntegration_minDist = 0.;
	Double verticalIntegration_maxDist = 127.;
	Double vIdebunch = 0.0;                  // average data that are close together
	Boolean vertintsamplestats = false;         // Assume sampling statistics
	IGroup verticalIntegration_region = null;
	IGroup verticalIntegration_output = null;
	Boolean vertint_scale = false;              // Scale to make max value 10000
	private String metadata_string = "";        // Report our actions
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.pattern;
	
	private double [] totals;
	private double [] total_variances;

	/**
	 * This method is called when the processor process. 
	 * @param source_groupData stitched data in two-dimensional double array type
	 * @param thetaArray  in one-dimensional double array type
	 * @param numSlice  in Integer type
	 * @param pos  in Double type
	 * @param verticalIntegration_minDist  in Double type
	 * @param verticalIntegration_maxDist  in Double type
	 * @return  List of return results objects
	 * @throws IOException 
	 */
	public Boolean process() throws Exception{
		int[][] contrib_mask = null;
		metadata_string = "";  //reinitialise
		if (verticalIntegration_skip) {
			verticalIntegration_output = verticalIntegration_input;
			return false;
		} if (verticalIntegration_input == null) {
			if (alignment_input != null) {
				verticalIntegration_output = alignment_input;
			} else {
				return false;
			}
		} else{
			IArray data = ((NcGroup) verticalIntegration_input).findSignal().getData();
			IArray thetaArray = null;
			try{
				thetaArray = ((Plot) verticalIntegration_input).getAxis(1).getData();
			}catch (Exception e){
				thetaArray = verticalIntegration_input.findDataItem("polar_angle").getData();
			}
			try {
				contrib_mask = (int[][]) verticalIntegration_input.findDataItem("contributors").getData().getArrayUtils().copyToNDJavaArray();
			} catch (Exception e) {
				System.out.println("No contributor mask found");
			}
			double[][] sourceData = null;
			IArray variance_array = null;
			try {
				variance_array = ((Plot) verticalIntegration_input).getVariance().getData();
			} catch (Exception e) {
				// TODO: handle exception
				variance_array = ((NcGroup) verticalIntegration_input).getSignalArray();
			}
			if (verticalIntegration_region != null)	{
				IArray regionalDataArray = 
					RegionUtils.applyRegion(verticalIntegration_input, 
							verticalIntegration_region);
				metadata_string = metadata_string + "Before final vertical integration step, parts of the 2D detector map "+
				"were masked off.";
				sourceData = ConverterLib.get2DDouble(regionalDataArray);
			}
			else 
				sourceData = ConverterLib.get2DDouble(data);
			verticalIntegrate(
					sourceData, ConverterLib.get2DDouble(variance_array),verticalIntegration_minDist, verticalIntegration_maxDist,
					contrib_mask);
			// Rescale if necessary
			if (vertint_scale) rescale(10000.0);
			// rescale(data.getMaximum());
			
			IArray resultArray = Factory.createArray(totals);
			IArray errorArray = Factory.createArray(total_variances);
			if (vIdebunch > 0.0) {
				IArray [] result = debunch(thetaArray,resultArray,errorArray);
				thetaArray = result[0];
				resultArray = result[1];
				errorArray = result[2];
				metadata_string += String.format("Data points within %8.3f degrees of one another were merged.",vIdebunch);
			}
			String resultName = "horizontalIntegration_result";
			String plot_title = ((IDataItem) verticalIntegration_input.findContainerByPath("$entry/sample/name")).getData().toString();
			verticalIntegration_output = PlotFactory.createPlot(verticalIntegration_input, resultName, dataDimensionType);
			PlotFactory.addDataToPlot(verticalIntegration_output, resultName, resultArray, plot_title, "Counts", errorArray);
			((NcGroup) verticalIntegration_output).addLog("apply horizontal integration algorithm to get " + resultName);
			PlotFactory.addAxisToPlot(verticalIntegration_output, "two_theta_vector", thetaArray, "Two theta", "degrees", 0);
			((NcGroup) verticalIntegration_output).addMetadata("CIF", "_pd_proc_info_data_reduction", metadata_string, false);
		}
		return verticalIntegration_stop;
//		return result;
	}
	
	/**
     * Makes the horizontal integration about a selected region.  Masked out areas will have values of Double.NaN.
     * Set private fields rather than return structured arrays.  We accept a 'mask' argument for more complicated
     * masks that are not handled by the current Region datastructures.
     * 
     * When generalising for Wombat etc. will need to figure out how to return both errors and variances from a general 
     * routine
     * 
     * We calculate the sample population variance where we have more than two observations at a given angle
     * 
     * @param data The two D double data to be integrated.
	 * @param minYi  The minimum value (bottom side) for integration.
	 * @param maxYi The maximum value (top side) for integration.
     */

	public void verticalIntegrate(double[][] data, double [][] variances,
            double minDist, double maxDist, int [][] mask){
		
		int yheight = data.length;
		int xwidth = data[0].length;
		int maxi,mini;       // max, min as integers
		int[] nentry= new int[xwidth];   // count contributors for later normalisation
		int i, j;
		// sanity check for minimum,maximum limits
		if (minDist < 0) minDist = 0;
		if (maxDist > yheight ) maxDist = yheight;
		mini = (int) Math.floor(minDist);
		maxi = (int) Math.ceil(maxDist);
		int maxcontribs = 0;        //remember maximum contributors
		totals = new double [xwidth];
		total_variances = new double [xwidth];
		double [] total_sq_var = new double[xwidth];
		//loop over our 2 theta bins
		for(i=0; i < xwidth; i++) {
			nentry[i] = 0;   // initialise
			//loop over vertical pixels
			for (j = mini; j < maxi; j++) 
			{
				String velem = String.valueOf(data[j][i]);
				if (velem.equals("NaN") || (mask != null && mask[j][i]==0)){   //This value is masked out
					continue;		   
				}
				else  {
					nentry[i]++;            // count contributions
					totals[i] += data[j][i];      
					total_variances[i] += variances[j][i];
					total_sq_var[i]+= data[j][i]*data[j][i];
				}
			}
			if(nentry[i]>maxcontribs) maxcontribs=nentry[i];
		}
		// Now normalise to counts per maxcontribs bin; this will resemble actual observed counts and avoid
		// users worrying about small counts
		System.out.printf("%d max contribs, normalising to this number of bins%n",maxcontribs);
		for(i=0;i< xwidth;i++) {
			// Now normalise to number of counts
			if(nentry[i] == 0) {
//				System.out.printf("Bin %d: no contribs%n",i);
				totals[i] = 0;
				total_variances[i]=0;
			}
			else {
				double scale_factor = maxcontribs/nentry[i];  //convenience definition
				// Now get the population variance; choose 5 points as the cutoff.  This is arbitrary
				if(vertintsamplestats) { //assume all pixels are observations of the same quantity
					if(nentry[i]<5) {
						total_variances[i] *= scale_factor*scale_factor;   
					} else {
						total_variances[i] = (total_sq_var[i]/nentry[i]) - totals[i]*totals[i]/(nentry[i]*nentry[i]);  //population variance of counts per pixel value
						total_variances[i] *= maxcontribs*maxcontribs;   //variance of counts per maxcontribs pixels
					}
					totals[i]*=scale_factor;
				} else {
					total_variances[i] *= scale_factor*scale_factor;
				}
				//			System.out.printf("%d: %d contributions%n",i,nentry);
			}
		}
			metadata_string = metadata_string + "2D data were summed vertically to produce 1D data. These sums were "+
			   String.format("adjusted for variable number of vertical pixels by normalising to %d bins. ",maxcontribs);
			// System.out.printf("%d: %f(%f)%n",i,totals[i]/maxcontribs,total_variances[i]/maxcontribs/maxcontribs);
	}
	
	/* Rescale the data and variance arrays such that the maximum value in the data array is newmax */
	
	public void rescale (double newmax) {
		// Find the current maximum 
		double curmax = 0;
		for (double curval : totals)
			if (curval > curmax) curmax = curval;
		// Calculate the necessary scale factor
		double rescale_fact = newmax/curmax;
		System.out.printf("Maximum value %f; rescaling by %f",curmax,rescale_fact);
		// Apply the scale factor to the totals
		for (int i=0;i<totals.length;i++) {
			totals[i] *= rescale_fact;
			total_variances[i] *= rescale_fact*rescale_fact;
		}
		metadata_string = metadata_string + String.format("All data and corresponding variances were rescaled by %8.3f to give a maximum intensity of %d counts.",rescale_fact,newmax);
	}
		
	/*If we have scanned with overlapping datasets, we might have a cluster of observations at each
	 * step.  If the vIdebunch value is greater than zero, we average all observations within this
	 * interval.  We don't allow our interval to become larger than the total size.
	 * 
	 */
	private IArray[] debunch(IArray angles, IArray data, IArray variances) {
		IndexIterator angles_i = ((NcArray) angles).getArray().getIndexIterator();
		IArray out_angles = Factory.createArray(Double.class, angles.getShape());
		IArray out_intensities = Factory.createArray(Double.class, angles.getShape());
		IArray out_variances = Factory.createArray(Double.class, angles.getShape());
		IndexIterator out_a_i = ((NcArray) out_angles).getArray().getIndexIterator();
		IndexIterator out_t_i = ((NcArray) out_intensities).getArray().getIndexIterator();
		IndexIterator out_v_i = ((NcArray) out_variances).getArray().getIndexIterator();
		IndexIterator data_i = ((NcArray) data).getArray().getIndexIterator();
		IndexIterator var_i = ((NcArray) variances).getArray().getIndexIterator();
		double cluster_begin = angles_i.getDoubleNext();  //Get first point in angle
		int total_points = 0; //find length of new array
		double total_intensity = 0;
		double total_variance = 0;
		double mean_angle = 0.0;
		int bunch_points = 0;
		int in_points = 0;
		double new_angle = cluster_begin;
		while(true) {
			//note we assume that the incoming data are sorted by angle
			double distance = new_angle-cluster_begin;
			//The less than or equals is important: we often compare a point with itself
			if(distance<=vIdebunch) {   //accumulate this in current bin
				total_intensity+=data_i.getDoubleNext();
				total_variance+=var_i.getDoubleNext();
				mean_angle+=new_angle;
				bunch_points++;
				/* if(total_points>100 && total_points<200) {  //Debug output
					System.out.printf("Point %d: %8.4f %8.4f%n",total_points,new_angle,data_i.getDoubleCurrent());
				} */
				try {
					new_angle = angles_i.getDoubleNext();
				} catch (Exception e) {
					break;
				}
			} else {  //Finish this point
				out_t_i.setDoubleNext(total_intensity/bunch_points);
				out_v_i.setDoubleNext(total_variance/(bunch_points*bunch_points));
				out_a_i.setDoubleNext(mean_angle/bunch_points);
				total_points++;
				in_points += bunch_points;
				/*if(total_points>100 && total_points<200) {
					System.out.printf("%d points: %8.4f %8.4f%n",bunch_points,out_a_i.getDoubleCurrent(),out_t_i.getDoubleCurrent());
					System.out.printf("========================%n");
				}*/
				//Reinitialise our counters
				total_intensity=0.0;
				total_variance=0.0;
				mean_angle=0.0;
				bunch_points=0;
				//The while loop has not stepped the input iterators forward, so we now treat the same
				//point as we have just tested, but as last_point will now be the same, we will accumulate
				//it.
				cluster_begin = new_angle;
			}				
		}  // Now finish off the last point
		out_t_i.setDoubleNext(total_intensity);
		out_v_i.setDoubleNext(total_variance);
		out_a_i.setDoubleNext(mean_angle/bunch_points);
		total_points++;
		// Trim our output arrays
		try {
			out_angles = out_angles.getArrayUtils().section(new int []{0}, new int[]{total_points}).getArray();
			out_intensities = out_intensities.getArrayUtils().section(new int []{0}, new int[]{total_points}).getArray();
			out_variances = out_variances.getArrayUtils().section(new int []{0}, new int[]{total_points}).getArray();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.printf("Unbunching finished: unbunch factor %8.3f%n", angles.getSize()/(double) total_points);
		return new IArray[] {out_angles,out_intensities,out_variances};
	}
	
	public IGroup getVerticalIntegration_output() {
		return verticalIntegration_output;
	}

	public void setVerticalIntegration_input(IGroup verticalIntegration_input) {
		this.verticalIntegration_input = verticalIntegration_input;
	}

	public void setVerticalIntegration_skip(Boolean verticalIntegration_skip) {
		this.verticalIntegration_skip = verticalIntegration_skip;
	}

	public void setVerticalIntegration_stop(Boolean verticalIntegration_stop) {
		this.verticalIntegration_stop = verticalIntegration_stop;
	}

	public void setVIdebunch(Double debunch_val) {
		vIdebunch = debunch_val;
		System.out.printf("debunch set to %f%n", debunch_val);
	}
	
	public void setVerticalIntegration_minDist(
			Double verticalIntegration_minDist) {
		this.verticalIntegration_minDist = verticalIntegration_minDist;
	}

	public void setVerticalIntegration_maxDist(
			Double verticalIntegration_maxDist) {
		this.verticalIntegration_maxDist = verticalIntegration_maxDist;
	}

	public void setVerticalIntegration_region(IGroup verticalIntegration_region) {
		this.verticalIntegration_region = verticalIntegration_region;
	}
	
	public void setVertInt_scale (Boolean newval) {
		this.vertint_scale = newval;
	}
	
	public void setVertSampling (Boolean newval) {
		this.vertintsamplestats = newval;
	}
	
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	/**
	 * @param alignment_input the alignment_input to set
	 */
	public void setAlignment_input(IGroup alignment_input) {
		this.alignment_input = alignment_input;
	}

	/**
	 * @return the alignment_input
	 */
	public IGroup getAlignment_input() {
		return alignment_input;
	}

}
