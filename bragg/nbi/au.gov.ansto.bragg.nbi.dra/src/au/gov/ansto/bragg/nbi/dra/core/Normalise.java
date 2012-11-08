package au.gov.ansto.bragg.nbi.dra.core;

import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.ISliceIterator;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.errorpropagation.ArrayOperations;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class Normalise extends ConcreteProcessor {
	IGroup normalise_groupdata = null;
	IGroup normalised_data = null;
	boolean skip_norm = false;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	public Boolean process() throws Exception {
		if (skip_norm) { 
			normalised_data = normalise_groupdata;
			return false;
		}
		// Read in needed data and prepare output arrays
		IArray dataArray = ((Plot) normalise_groupdata).findSignalArray();
		IArray variance_array = ((Plot) normalise_groupdata).getVariance().getData();
		/* If we have a 3D array, this means a series of monitor counts is available to
		 * allow normalisation at each scan step.  If the array is 2D, then no such
		 * normalisation is necessary.
		 */
		double monmax = 1.0;   // Value to which we normalise; usually maximum monitor counts
		if(dataArray.getRank()>2) {
			int [] data_dims = dataArray.getShape();
			int dlength = dataArray.getRank();
			IArray monitor_array = normalise_groupdata.getRootGroup().findDataItem("monitor_data").getData();
			//We should have a monitor array rather than a single value
			monmax = monitor_array.getArrayMath().getMaximum();
			IArray outarray = Factory.createArray(double.class, data_dims);
			IArray out_variance = Factory.createArray(double.class,data_dims);
			int [] origin_list = new int [dlength];
			int [] range_list = new int[dlength];
			int [] target_shape = new int[dlength];              //shape to make a 2D array from multi-dim array
			for(int i=0;i<dlength;i++) origin_list[i] = 0;       //take in all elements
			for(int i=0;i<dlength-2;i++) {
				range_list[i] = data_dims[i];  //last two are rank reduced
				target_shape[i] = 1;
			}
			range_list[dlength-2] = 1; range_list[dlength-1] = 1;
			target_shape[dlength-2] = data_dims[dlength-2]; target_shape[dlength-1] = data_dims[dlength-1];
			// Create an iterator over the higher dimensions.  We leave in the final two dimensions so that we can
			// use the getCurrentCounter method to create an origin.
			// Iterate over two-dimensional slices
			// Array loop_array = dataArray.sectionNoReduce(origin_list, range_list, null);
			ISliceIterator higher_dim_iter = dataArray.getSliceIterator(2);
			IArrayIterator mon_iter = monitor_array.getIterator();
			ISliceIterator high_dim_out_it = outarray.getSliceIterator(2);
			ISliceIterator var_out_it = out_variance.getSliceIterator(2);
			ISliceIterator invar_iter = variance_array.getSliceIterator(2);
			//Now loop over higher dimensional frames
			while (higher_dim_iter.hasNext()){
				double monval = mon_iter.getDoubleNext();
				if(monval==0) {
					String errorstring = "Normalisation error: zero monitor counts found";
					throw new Exception(errorstring);
				}
				double monerr = monmax*monmax/(monval*monval*monval);
				monval= monmax/monval;   //Actual value to multiply by
				IArray[] out_with_var = {high_dim_out_it.getArrayNext(), var_out_it.getArrayNext()};
				IArray [] in_with_var = {higher_dim_iter.getArrayNext(), invar_iter.getArrayNext()};
				// First create a scalar for normalisation of each frame
				double[] norm_with_err = {monval,monerr};
				ArrayOperations.multiplyByScalar(in_with_var,norm_with_err,out_with_var);
			}
			// Now build the output databag
			String resultName = "normalisation_result";
			normalised_data = PlotFactory.createPlot(normalise_groupdata, resultName, dataDimensionType);
			((NcGroup) normalised_data).addLog("Apply normalisation to get " + resultName);
			PlotFactory.addDataToPlot(normalised_data, resultName, outarray, "Normalised data", "Normalised counts",out_variance);
			// Copy axes across from previous data
			List<Axis> data_axes = ((Plot) normalise_groupdata).getAxisList();
			for (Axis oneaxis : data_axes) {
				PlotFactory.addAxisToPlot(normalised_data, oneaxis,oneaxis.getDimensionName());
			}

			// We need the value to which everything has been normalised to be available to
			// subsequent processing steps
			IAttribute norm_val = Factory.createAttribute("normalised_to_val", monmax);
			// normalised_data.buildResultGroup(signal, stthVector, channelVector, twoThetaVector); 
			normalised_data.addOneAttribute(norm_val);

		} else {
			normalised_data = normalise_groupdata;
		}
		return false;
	}
	
	public IGroup getNormalised_data() {
		return normalised_data;
	}

	public void setNorm_groupdata(IGroup indata) {
		this.normalise_groupdata = indata;
	}
	
	public void setNormalise_skip(Boolean skipval) {
		skip_norm = skipval;
	}

	public DataDimensionType getDataDimensionType() {
		// TODO Auto-generated method stub
		return dataDimensionType;
	}

	public DataStructureType getDataStructureType() {
		// TODO Auto-generated method stub
		return dataStructureType;
	}

}
