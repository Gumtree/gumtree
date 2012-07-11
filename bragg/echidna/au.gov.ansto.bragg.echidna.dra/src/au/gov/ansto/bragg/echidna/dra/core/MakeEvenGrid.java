package au.gov.ansto.bragg.echidna.dra.core;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class MakeEvenGrid extends ConcreteProcessor {

	/*
	 * This processor block moves the input 1D data onto an even scale, suitable
	 * for output to GSAS, which requires evenly-spaced data. (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
	 */
	IGroup even_grid_input;
	IGroup even_grid_output;
	double grid_spacing = 0.05;  /* this is the grid spacing of the output data */
	double collimator_width = 5.0/60.0;
	Boolean even_grid_skip = false;
	Boolean even_grid_stop = false;
	private String metadata_string;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.pattern;

	@Override
	public Boolean process() throws Exception {
		metadata_string = "";  //reinitialise
		if (even_grid_skip) even_grid_output = even_grid_input;
		else{
			IArray data = ((NcGroup) even_grid_input).findSignal().getData();
			IArray thetaArray = null;
			try{
				thetaArray = ((Plot) even_grid_input).getAxis(0).getData();
			}catch (Exception e){
				thetaArray = even_grid_input.findDataItem("polar_angle").getData();
			}
			double[][] sourceData = null;
			IArray variance_array = null;
			try {
				variance_array = ((Plot) even_grid_input).getVariance().getData();
			} catch (Exception e) {
				variance_array = ((NcGroup) even_grid_input).getSignalArray();
			}
			// TODO automatically determine the appropriate grid spacing
			
			// values for working out pixel location in our array. As discussed in the stitching algorithm,
			// we assume that the 2theta value corresponds to the centre of the pixel.  Therefore, we expect 
			// thlen-1 bins from thlen points.
			IIndex thetaind = thetaArray.getIndex();
			IIndex dataind = data.getIndex();
			IIndex varind = variance_array.getIndex();
			double [] thetapoints = ConverterLib.get1DDouble(thetaArray);
			double [] datapoints = ConverterLib.get1DDouble(data);
			double [] varpoints = ConverterLib.get1DDouble(variance_array);
			//The +1 in the following expression arises because we have a halfwidth at either end to account for the bin width
			int thlen = (int) Math.ceil((thetaArray.getArrayMath().getMaximum() -thetaArray.getArrayMath().getMinimum()) / grid_spacing) + 1;  //negative if order is reversed
			System.out.printf("Theta grid regularised to spacing %f, %d points\n", grid_spacing,thlen);
			double minpoint = thetaArray.getArrayMath().getMinimum();
			double [] outcounts = new double[thlen];
			double [] outvar = new double[thlen];
			double [] outangles = new double[thlen];
			/* now we loop over the output points, doing a linear interpolation between the closest observed points.  We improve
			 * efficiency by not searching our point array from the beginning each time.  As the observed point array is sorted,
			 * we know that we will never need to step back in it to find the nearest point.
			 */
			int upper_neighbour = 0;
			for(int pointno=0;pointno<thlen;pointno++) {
				double thispoint = pointno*grid_spacing+minpoint;
				outangles[pointno]=thispoint;
				//Find closest point that is less than this point
				try {
				while(thetapoints[upper_neighbour]<=thispoint) upper_neighbour++;
				} catch (ArrayIndexOutOfBoundsException e) {
					break;
				}
				//So by definition, the next point is greater than this point. Perform the
				//linear interpolation.
				double lowerpoint = thetapoints[upper_neighbour-1];
				double upperpoint = thetapoints[upper_neighbour];
				double slope = (datapoints[upper_neighbour]-datapoints[upper_neighbour-1])/(upperpoint-lowerpoint);//slope
				double distance = thispoint - lowerpoint;              //distance from lower point
				double error_fac = distance/(upperpoint-lowerpoint);   //this appears in the error equation
				outcounts[pointno] = datapoints[upper_neighbour-1] + slope*distance;
				outvar[pointno] = varpoints[upper_neighbour-1]*(1-error_fac)*(1-error_fac) + varpoints[upper_neighbour]*error_fac;
				// Some debugging output
				if(pointno>100 && pointno < 200) {
					System.out.printf("%8.4f %8.4f(%8.4f) from (%8.4f,%8.4f) (%8.4f %8.4f)%n", thispoint,outcounts[pointno],outvar[pointno],
							lowerpoint,datapoints[upper_neighbour-1],upperpoint,datapoints[upper_neighbour]);
				}
			}
		metadata_string = String.format("Points were interpolated onto an even grid with spacing %5.2f by pointwise linear interpolation. %n",grid_spacing);
		/* package it all up for output */
		IArray resultArray = Factory.createArray(outcounts);
		IArray errorArray = Factory.createArray(outvar);
		String resultName = "even_grid_result";
		String plot_title = ((IDataItem) even_grid_input.findContainerByPath("$entry/sample/name")).getData().toString();
		even_grid_output = PlotFactory.createPlot(even_grid_input, resultName, dataDimensionType);
		PlotFactory.addDataToPlot(even_grid_output, resultName, resultArray, plot_title, "Counts", errorArray);
		((NcGroup) even_grid_output).addLog("apply interpolation algorithm to get " + resultName);
		PlotFactory.addAxisToPlot(even_grid_output, "two_theta_vector", Factory.createArray(outangles), "Two theta", "degrees", 0);
		// Now fiddle with the metadata: as we have an even grid, we can use different tags for the angle array.
		// We will check these tags in the GSAS output module to make sure that everything is correct.
		((NcGroup) even_grid_output).addMetadata("CIF", "_pd_proc_info_data_reduction", metadata_string, false);
		((NcGroup) even_grid_output).addMetadata("CIF", "_pd_proc_2theta_range_min",String.format("%.5f",outangles[0]));
		((NcGroup) even_grid_output).addMetadata("CIF", "_pd_proc_2theta_range_max",String.format("%.5f",outangles[outangles.length-1]));
		((NcGroup) even_grid_output).addMetadata("CIF", "_pd_proc_2theta_range_inc",String.format("%.5f",grid_spacing));
		((NcGroup) even_grid_output).addMetadata("CIF", "_pd_proc_number_of_points", String.format("%d",resultArray.getSize()));
	}
	return even_grid_stop;
	}


	/* This is a rejected method of rebinning which works by assigning the intensity in an input bin into overlapping output bins proportionally to the
	 * degree of overlap.  Rejected because it implicitly assumes that the intensity over the whole width of the bin is uniform, which leads to a
	 * contradiction if neighbouring input bins overlap.
	 */
	private void rebin(IArray thetaArray, IArray data, IArray variance_array) {
		IIndex thetaind = thetaArray.getIndex();
		IIndex dataind = data.getIndex();
		IIndex varind = variance_array.getIndex();
		double [] thetapoints = ConverterLib.get1DDouble(thetaArray);
		//The +1 in the following expression arises because we have a halfwidth at either end to account for the bin width
		int thlen = (int) Math.ceil((thetaArray.getArrayMath().getMaximum() -thetaArray.getArrayMath().getMinimum()) / grid_spacing) + 1;  //negative if order is reversed
		double halfwidth = collimator_width/2.0;
		System.out.printf("Theta grid regularised to spacing %f, %d points\n", grid_spacing,thlen);
		double minpoint = thetaArray.getArrayMath().getMinimum()-grid_spacing/2.0;
		double [] outcounts = new double[thlen];
		double [] outvar = new double[thlen];
		double [] outangles = new double[thlen];
		double [] outcont = new double[thlen];
		for(int pointno = 0; pointno<thetapoints.length; pointno++) {
			thetaind.set(pointno);
			dataind.set(pointno);
			varind.set(pointno);
			double onepoint = thetaArray.getDouble(thetaind);
			double dataval = data.getDouble(dataind);
			double varval = variance_array.getDouble(varind);
			/* For each point, partition between all overlapping grid points.  The input data correspond to the centres 
			 * of tubes, which accept data from a fixed angular range.  Therefore the input bin size is fixed by the
			 * detector construction.
			 * 
			 * Our output bin array starts at min(thetapoints), which corresponds to the centre of a bin of width
			 * grid_spacing.  So the first bin goes from min(thetapoints)-grid_spacing/2.
			 * 
			 * The lowest bin which this point contributes to will be located at the lower boundary, which is
			 * onepoint - halfwidth, and the distance to the beginning is the difference with minpoint
			 * 
			 * The contribution is proportional to the overlapping length.  As the total length of the input
			 * bin is given by halfwidth*2, we divide the length of overlap by this.  Finally, we have to normalise
			 * for the number of contributions, which means we need to keep a track of the total contributions
			 * to each bin, where 1 would be a full contribution.
			*/
			int minbin = (int) Math.floor((onepoint - halfwidth - minpoint)/grid_spacing);
			minbin = Math.max(minbin, 0);
			int maxbin = (int) Math.floor((onepoint + halfwidth - minpoint)/grid_spacing);
			maxbin = Math.min(maxbin, thlen-1);
			if(pointno<100) {
				System.out.printf("Inbin %f-%f: %f counts%n",onepoint-halfwidth,onepoint+halfwidth,dataval);
			}
			/* allocate counts in proportion to bin width */
			double total_cont = 0;  // a sanity check
			for(int binno=minbin;binno<=maxbin;binno++) {
				/* bottom boundary for contribution: maximum (input bin lower boundary,output bin lower boundary) */
				double binbot = Math.max(binno*grid_spacing+minpoint, onepoint-halfwidth);
				/* top boundary for contribution: minimum (input bin upper boundary, output bin upper boundary) */
				double bintop = Math.min((binno+1)*grid_spacing+minpoint, onepoint+halfwidth);
				double ratio = (bintop-binbot)/(halfwidth*2);
				double thiscontribution = dataval*ratio;
				double varcontribution = varval*ratio*ratio;
				total_cont += thiscontribution;  /* should add up to counts in bin by the end */
				outcounts[binno] += thiscontribution;
				outvar[binno] += varcontribution;
				outcont[binno] += ratio;  //This is the contribution in units of the incoming bin size
				// following angle calculated here as a check; in final version shift to own loop
				outangles[binno] = minpoint+(grid_spacing/2.0) + (grid_spacing*binno);
				if(pointno<100) {   //Debugging printout
					System.out.printf("Bin: %f-%f Contribution: %f -> %f, total count %f%n",binno*grid_spacing+minpoint,(binno+1)*grid_spacing+minpoint,
							 ratio,outcont[binno],outcounts[binno]);
				}
			}
			if(Math.abs(total_cont - dataval)>0.001) {
				System.out.printf("Warning: point %d: %f counts interpolated to %f counts%n", pointno,dataval,total_cont);
			}
		}
		for(int binno=0;binno<thlen;binno++) {
			if(outcont[binno]<0.1) {
				System.out.printf("Warning: bin %d has %f contributions %n",binno,outcont[binno]);
				outcounts[binno] = 0;
				outvar[binno] = 0;
			} else {
			outcounts[binno]/= outcont[binno];
			outvar[binno]/= outcont[binno]*outcont[binno];
			}
		}
	}
	
	public IGroup getEven_grid_output() {
		return even_grid_output;
	}

	public void setEven_grid_input(IGroup evenGridInput) {
		even_grid_input = evenGridInput;
	}

	public void setGrid_spacing(double gridSpacing) {
		grid_spacing = gridSpacing;
	}

	public void setEven_grid_skip(Boolean evenGridSkip) {
		even_grid_skip = evenGridSkip;
	}

	public void setEven_grid_stop(Boolean evenGridStop) {
		even_grid_stop = evenGridStop;
	}

}
