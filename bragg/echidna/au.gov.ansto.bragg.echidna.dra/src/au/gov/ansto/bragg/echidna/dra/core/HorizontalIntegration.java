package au.gov.ansto.bragg.echidna.dra.core;

import java.io.IOException;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.echidna.dra.algolib.core.DRAStaticLibHRPD;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;


/**
 * This class is the concrete processor of the horizontal integration algorithm for echidna algorithm group.
 * It uses the stitched data of the echidna input data object. It will return a two-dimensional data
 * which has the integrated vector, the error vector and the two-theta vector.  An optional parameter allows
 * the output data to be scaled such that the maximum value is 10000 counts.  This is provided to enable
 * datasets to be compared easily
 *  
 * @author nxi,jhester
 * @version 2.0
 */
public class HorizontalIntegration extends ConcreteProcessor{

	IGroup horizontalIntegration_input = null;
	Boolean horizontalIntegration_skip = false;
	Boolean horizontalIntegration_stop = false; 
	Double horizontalIntegration_minDist = 0.;
	Double horizontalIntegration_maxDist = 127.;
	IGroup horizontalIntegration_region = null;
	IGroup horizontalIntegration_output = null;
	Boolean vertint_scale = false;              // Scale to make max value 10000
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
		if (horizontalIntegration_skip) horizontalIntegration_output = horizontalIntegration_input;
		else{
			Double xOrigin = 0. , yOrigin = 0.;
			IArray data = ((NcGroup) horizontalIntegration_input).findSignal().getData();
			int[] shape = data.getShape();
			Double pos = -1.0;
			IArray thetaArray = null;
//			try{
//			thetaArray = horizontalIntegration_input.getDataItem("two_theta_vector").getData();
//			}catch (Exception e){
//			thetaArray = horizontalIntegration_input.findDataItem("polar_angle").getData();
//			}
			IDataItem thetaDataItem;
			thetaDataItem = horizontalIntegration_input.findDataItem("two_theta_vector");
			if (thetaDataItem == null) 
				thetaDataItem = horizontalIntegration_input.findDataItem("polar_angle");
			if (thetaDataItem == null)
				if (horizontalIntegration_input instanceof Plot){
					List<?> axisList = ((Plot) horizontalIntegration_input).getAxisList();
					thetaDataItem = (IDataItem) axisList.get(axisList.size() - 1);
				}
			thetaArray = thetaDataItem.getData();
			double[][] sourceData = null;
			double[][] integration = null;
			if (horizontalIntegration_region != null)	{
				 

				IArray regionalDataArray = RegionUtils.applyRegion(horizontalIntegration_input, 
						horizontalIntegration_region);
				sourceData = ConverterLib.get2DDouble(regionalDataArray);
			}
			else 
				sourceData = ConverterLib.get2DDouble(data);
			if (shape.length == 3 && shape[0] == 1){
				Integer numSlice = Integer.valueOf(data.getShape()[2]);
				integration = DRAStaticLibHRPD.horizontalIntegration(
						sourceData,	numSlice, horizontalIntegration_minDist, horizontalIntegration_maxDist, 
//						xOrigin, yOrigin, pos, ConverterLib.get1DDouble(thetaArray));
						xOrigin, yOrigin, pos, ConverterLib.get1DDouble(thetaArray));
			}else if (shape.length == 2){
				Integer numSlice = Integer.valueOf(data.getShape()[1]);
				integration = DRAStaticLibHRPD.horizontalIntegration(
						sourceData,	numSlice, horizontalIntegration_minDist, horizontalIntegration_maxDist, 
//						xOrigin, yOrigin, pos, ConverterLib.get1DDouble(thetaArray));
						xOrigin, yOrigin, pos, ConverterLib.get1DDouble(thetaArray));
			}
			IArray resultArray = Factory.createArray(double.class, new int[]{integration[0].length}, 
					integration[0]);
			IArray errorArray = Factory.createArray(double.class, new int[]{integration[1].length}, 
					integration[1]);
//			result.add(DRAStaticLibHRPD.horizontalIntegration(data, 
//			numSlice, minDist, maxDist, xOrigin, yOrigin, pos, thetaArray));
			String resultName = "horizontalIntegration_result";
//			horizontalIntegration_output = Factory.createGroup(horizontalIntegration_input.getDataset(), horizontalIntegration_input, resultName, true);
//			horizontalIntegration_output.addLog("apply horizontal integration algorithm to get " + resultName);
//			DataItem signal = Factory.createDataItem(null, horizontalIntegration_output, resultName, resultArray);
//			DataItem twoThetaVector = Factory.createDataItem(null, horizontalIntegration_output, "twoTheta_vector", thetaArray);
//			twoThetaVector.setUnits("degrees");
//			DataItem errorVector = Factory.createDataItem(null, horizontalIntegration_output, "error_vector", errorArray);
//			errorVector.addOneAttribute(Factory.createAttribute("axes",twoThetaVector.getShortName()));
//			signal.addOneAttribute(Factory.createAttribute("error", errorVector.getShortName())); 
//			horizontalIntegration_output.buildResultGroup(signal, twoThetaVector);
//			horizontalIntegration_output.addDataItem(errorVector);
//			result.add(group);
			horizontalIntegration_output = PlotFactory.createPlot(horizontalIntegration_input, 
					resultName, dataDimensionType);
			((NcGroup) horizontalIntegration_output).addLog("apply horizontal integration algorithm to get " 
					+ resultName);
			PlotFactory.addDataToPlot(horizontalIntegration_output, resultName + "_data", resultArray, 
					"Integration Result", "counts");
			PlotFactory.addAxisToPlot(horizontalIntegration_output, "two_theta_vector",
					thetaArray, "Two Theta", "degree", 0);
			PlotFactory.addDataVarianceToPlot(horizontalIntegration_output, "variance", errorArray);
		}

		return horizontalIntegration_stop;
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
	 * @param data The two D double data to be integrated.
	 * @param minYi  The minimum value (bottom side) for integration.
	 * @param maxYi The maximum value (top side) for integration.
	 */

	public void horizontalIntegration(double[][] data, double [][] variances,
			double minDist, double maxDist, int [][] mask){

		int yheight = data.length;
		int xwidth = data[0].length;
		int maxi,mini;       // max, min as integers
		int nentry;
		int i, j;
		// sanity check for minimum,maximum limits
		if (minDist < 0) minDist = 0;
		if (maxDist > yheight ) maxDist = yheight;
		mini = (int) Math.floor(minDist);
		maxi = (int) Math.ceil(maxDist);
		totals = new double [xwidth];
		total_variances = new double [xwidth];
		//loop over our 2 theta bins
		for(i=0; i < xwidth; i++) {
			nentry = 0;   // initialise
			//loop over vertical pixels
			for (j = mini; j < maxi; j++) 
			{
				String velem = String.valueOf(data[j][i]);
				if (velem.equals("NaN") || (mask != null && mask[j][i]==0)){   //This value is masked out
					continue;		   
				}
				else  {
					nentry++;            // count contributions
					totals[i] += data[j][i];      
					total_variances[i] += variances[j][i];
				}
			}
			// Now normalise to number of counts
			if(nentry == 0) totals[i] = Double.NaN;
			else { 
				totals[i] /= nentry;
				total_variances[i] /= nentry;   
			}
			System.out.printf("%d: %d contributions%n",i,nentry);
		}
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
	}

	public IGroup getHorizontalIntegration_output() {
		return horizontalIntegration_output;
	}

	public void setHorizontalIntegration_input(IGroup horizontalIntegration_input) {
		this.horizontalIntegration_input = horizontalIntegration_input;
	}

	public void setHorizontalIntegration_skip(Boolean horizontalIntegration_skip) {
		this.horizontalIntegration_skip = horizontalIntegration_skip;
	}

	public void setHorizontalIntegration_stop(Boolean horizontalIntegration_stop) {
		this.horizontalIntegration_stop = horizontalIntegration_stop;
	}

	public void setHorizontalIntegration_minDist(
			Double horizontalIntegration_minDist) {
		this.horizontalIntegration_minDist = horizontalIntegration_minDist;
	}

	public void setHorizontalIntegration_maxDist(
			Double horizontalIntegration_maxDist) {
		this.horizontalIntegration_maxDist = horizontalIntegration_maxDist;
	}

	public void setHorizontalIntegration_region(IGroup horizontalIntegration_region) {
		this.horizontalIntegration_region = horizontalIntegration_region;
	}

	public void setVertInt_scale (Boolean newval) {
		this.vertint_scale = newval;
	}

	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

}
