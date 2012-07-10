package au.gov.ansto.bragg.nbi.dra.core;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.ISliceIterator;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.nexus.*;
import au.gov.ansto.bragg.errorpropagation.ArrayOperations;
import au.gov.ansto.bragg.errorpropagation.ScalarOperations;
import au.gov.ansto.bragg.errorpropagation.ScalarWithVariance;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * This class is the concrete processor of a general background subtraction algorithm. Both background and the
 * data may be arbitrary-dimensional. Multi-slice data should have been normalised, but the background data may
 * be unnormalised. 
 *  
 * @author nxi,jxh
 * @version 1.1
 * @since V2.2
 */
public class BackgroundCorrection extends ConcreteProcessor {
	IGroup backgroundCorrection_scanData = null;
	Boolean backgroundCorrection_skip = false;
	Boolean backgroundCorrection_stop = false; 
	Boolean background_is_2d = false;          //Flag that do not have multi-dimensional background
	Double backgroundCorrection_ratio = 1.;
	IGroup backgroundCorrection_output = null;
	URI backgroundCorrection_backgroundURI = null;	
	private IGroup backgroundData = null;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	/* This is the method which is called to run the algorithm */
	
	public Boolean process() throws Exception{
		if (backgroundCorrection_skip){
			backgroundCorrection_output = backgroundCorrection_scanData;
		}else{
			readBackgroundFile();
			// Extract information from the input databag: we need data, variance, background and normalisation counts
			IArray dataArray = ((Plot) backgroundCorrection_scanData).findSignalArray();
			IArray varianceArray = ((Plot) backgroundCorrection_scanData).getVariance().getData();
			double norm_counts;
			// If normalisation has been skipped, we won't have an attribute available; substitute a guess
			try {
			     norm_counts = ((NcGroup) backgroundCorrection_scanData).findSignal().getParentGroup().findAttribute("normalised_to_val").getNumericValue().doubleValue();
			} catch (NullPointerException e) {
				IArray mon_data = backgroundCorrection_scanData.getRootGroup().getDataItem("monitor_data").getData();
				IIndex mon_data_index = mon_data.getIndex();
				mon_data_index.set(0);
				norm_counts =  mon_data.getDouble(mon_data_index);
				System.out.printf("Data not normalised: normalisation value assumed to be %f%n",norm_counts);
			}
			System.out.printf("Input data normalised to %f monitor counts%n",norm_counts);
			//In reducing the background data, we are assuming that the input data has also been reduced
			//Note that the background data may not be a Plot object so we use the Group interface
			IArray backgroundArray = ((NcGroup) backgroundData).findSignal().getData().getArrayUtils().reduce().getArray(); //remove unused dimensions
			IArray backmonitor = backgroundData.getRootGroup().getDataItem("monitor_data").getData();
			// We may have a variable number of dimensions, but we assume that the largest two are the detector
			// dimensions, and these should match our background dimensions.
			// Now check that all shapes match; otherwise return true (which means failure)
			int[] dataShape = dataArray.getShape();
			int[] back_mon_shape = backmonitor.getShape();
			int[] backdata_shape = backgroundArray.getShape();
			// Check that last blength dimensions are the same size: note that we are assuming that we do not need to do anything to
			// remove empty dimensions from the background data. Note that this algorithm does not apply to Echidna, where we truly
			// need to have 3 dimensional background data.  We also check that the background monitor has a rank two less than the
			// data, and the same values in the dimensions that are the same.
			int blength = backdata_shape.length;
			int dlength = dataShape.length;
			for(int si = 0;si<blength;si++) if (backdata_shape[blength-1-si]!=dataShape[dlength-1-si]) 
				{
				String errorstring = String.format("Error: Background and data of different lengths in dimension %d: %d != %d%n",si,backdata_shape[blength-1-si],dataShape[dlength-1-si]);
				throw new Exception(errorstring);
				}
			// Check dimensionality
			if(backgroundArray.getRank()==2) background_is_2d = true;   //so we iterate differently
			else {
			    for(int si=0;si<dlength-2;si++) if(back_mon_shape[si]!=dataShape[si]) {
				    String errorstring = String.format("Error: Background monitor data of wrong size for dimension %d: should be %d not %d%n", si,dataShape[si],back_mon_shape[si]);
				    throw new Exception(errorstring);
			    }
			}
			// Now we do the actual calculation.  We need to create a new Array to hold the result. 
			IArray outarray = Factory.createArray(double.class, dataShape);
			IArray out_variance = Factory.createArray(double.class,dataShape);
			// Loop over the input data and the background data, normalising as we go.  Note that even if the background 
			// data have more than two dimensions, the background
			// monitor counts apply to exactly two dimensions, so we have to do everything in two-dimensional chunks.
			ISliceIterator higher_dim_iter = dataArray.getSliceIterator(2);
			ISliceIterator high_dim_out_it = outarray.getSliceIterator(2);
			ISliceIterator var_iter = varianceArray.getSliceIterator(2);
			ISliceIterator var_out_iter = out_variance.getSliceIterator(2);
			ISliceIterator back_iter = backgroundArray.getSliceIterator(2);
			IArrayIterator mon_iter = backmonitor.getIterator();
			int[] target_shape = higher_dim_iter.getSliceShape();
			IArray [] temp_array = {Factory.createArray(double.class,target_shape).getArrayUtils().reduce().getArray(), Factory.createArray(double.class,target_shape).getArrayUtils().reduce().getArray()};
			double back_mon_val = 0d;
			if(background_is_2d) back_mon_val = mon_iter.getDoubleNext();
			while (higher_dim_iter.hasNext()){
				IArray[] in_with_var = {higher_dim_iter.getArrayNext(),var_iter.getArrayNext()};
				IArray[] out_with_var = {high_dim_out_it.getArrayNext(),var_out_iter.getArrayNext()};
				IArray back_section = backgroundArray;
				if(!background_is_2d) {
					back_mon_val = mon_iter.getDoubleNext();
					back_section = back_iter.getArrayNext();
				}
				IArray [] back_with_var = {back_section,back_section};  //assuming counting stats for background
				// Now we have some 2D arrays to operate on, lets go to it!
				// First create a scalar for normalisation of each background value
				ScalarWithVariance monitor_ratio = new ScalarWithVariance(back_mon_val,back_mon_val);
				monitor_ratio = ScalarOperations.reciprocal(monitor_ratio);
				monitor_ratio = ScalarOperations.multiply(norm_counts, monitor_ratio);
				ArrayOperations.multiplyByScalar(back_with_var,monitor_ratio,temp_array);
				ArrayOperations.subtractandzero(in_with_var,temp_array, out_with_var);
			}
			// We now have a result in variable 'outarray'.  We bundle this into a databag for output, and store it as a new group in the
			// input databag.
			String resultName = "backgroundCorrection_result";
			backgroundCorrection_output = PlotFactory.createPlot(backgroundCorrection_scanData, resultName,dataDimensionType);
			((NcGroup) backgroundCorrection_output).addLog("apply background correction algorithm to get " + resultName);
			PlotFactory.addDataToPlot(backgroundCorrection_output, resultName, outarray, "Background corrected data", "Intensity", out_variance);
			// We need to store x,y axis information in the same group as the data so we copy across this information from the
			// previous location
			List<Axis> data_axes = ((Plot) backgroundCorrection_scanData).getAxisList();
			for (Axis oneaxis : data_axes) {
				PlotFactory.addAxisToPlot(backgroundCorrection_output, oneaxis,oneaxis.getDimensionName());
			}
		}
		return backgroundCorrection_stop;
	}

	/* Earlier approaches read the background file when the field was set.  This is not a good idea if
	 * the Cicada framework wants to set field values even for skipped processor blocks.  So we provide
	 * a separate method to perform the work that was originally performed in the 'set' method
	 */
	
	public void readBackgroundFile() throws URISyntaxException, StructureTypeException, CoreException {
		URI defuri = new URI("default");
		if (backgroundCorrection_backgroundURI != null && !backgroundCorrection_backgroundURI.equals(defuri)){
			backgroundData = NexusUtils.getNexusData(backgroundCorrection_backgroundURI);
		}
		else {  //Locate a default file
			    File backgroundFile = null;
				String filename = System.getProperty("dav.background.default","");
				IFileStore fileStore = EFS.getStore(new URI(filename));
				backgroundFile = fileStore.toLocalFile(EFS.NONE, new NullProgressMonitor());
				System.out.println("loading default background file from "+backgroundFile.toString());
			//IFileStore path_fs = EFS.getStore(new URI(path_fn));
			backgroundData = NexusUtils.getNexusData(backgroundFile.toURI());
		}
	}
	
	public IGroup getBackgroundCorrection_output() {
		return backgroundCorrection_output;
	}

	public void setBackgroundCorrection_scanData(
			IGroup backgroundCorrection_scanData) {
		this.backgroundCorrection_scanData = backgroundCorrection_scanData;
	}

	public void setBackgroundCorrection_skip(Boolean backgroundCorrection_skip) {
		this.backgroundCorrection_skip = backgroundCorrection_skip;
	}

	public void setBackgroundCorrection_stop(Boolean backgroundCorrection_stop) {
		this.backgroundCorrection_stop = backgroundCorrection_stop;
	}

	public void setBackgroundCorrection_ratio(Double backgroundCorrection_ratio) {
		this.backgroundCorrection_ratio = backgroundCorrection_ratio;
	}

	public void setBackgroundCorrection_backgroundFilename(
			URI backgroundCorrection_backgroundURI) {
		this.backgroundCorrection_backgroundURI = backgroundCorrection_backgroundURI;
	}
	
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

}

