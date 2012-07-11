package au.gov.ansto.bragg.echidna.dra.core;
import java.io.File;
import java.net.URI;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.utils.Utilities;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.echidna.dra.internal.Activator;
import au.gov.ansto.bragg.errorpropagation.ScalarOperations;
import au.gov.ansto.bragg.errorpropagation.ScalarWithVariance;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * This class is the concrete processor of the background correction algorithm for echidna algorithm group.
 * It uses the databag of the echidna input data object. 
 *  
 * @author nxi,jxh
 * @version 1.1
 * @since V2.2
 */
public class BackgroundCorrection extends ConcreteProcessor {
	IGroup backgroundCorrection_scanData = null;
	Boolean backgroundCorrection_skip = false;
	Boolean backgroundCorrection_stop = false; 
	Double backgroundCorrection_ratio = 1.;
	IGroup backgroundCorrection_output = null;
	URI backgroundCorrection_backgroundFilename = null;	
	private IGroup backgroundData = null;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

	/* This is the method which is called to run the algorithm */
	
	public Boolean process() throws Exception{
		if (backgroundCorrection_skip || backgroundCorrection_scanData == null){
			backgroundCorrection_output = backgroundCorrection_scanData;
		}else{
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
			IArray backgroundArray = ((NcGroup) backgroundData).findSignal().getData();
			// Remove any dimension-1 axes
			backgroundArray = backgroundArray.getArrayUtils().reduce().getArray();
			IArray backmonitor = backgroundData.getRootGroup().getDataItem("monitor_data").getData();
			// Set up Index objects to loop through the arrays
			IIndex back_mon_index = backmonitor.getIndex();
			IIndex back_data_index = backgroundArray.getIndex();
			IIndex data_index = dataArray.getIndex();
			// Now check that all shapes match; otherwise return true (which means failure)
			int[] dataShape = dataArray.getShape();
			int[] back_mon_shape = backmonitor.getShape();
			int[] backdata_shape = backgroundArray.getShape();
			if(backdata_shape.length!=dataShape.length) {
				String errorstring = "Background and data files of different dimension";
				throw new Exception(errorstring);
			}
			for(int si = 0;si<backdata_shape.length;si++) if (backdata_shape[si]!=dataShape[si]) 
				{
				String errorstring = String.format("Background correction fails: Background and data of different lengths at dimension %d",si);
				System.out.print(errorstring);
				throw new Exception(errorstring);
				}
			if(back_mon_shape[0]!= backdata_shape[0]) {
				String errorstring = String.format("Background correction fails: background monitor array different length to background data array");
				System.out.print(errorstring);
				throw new Exception(errorstring);
			}
			// Now we do the actual calculation.  We need to create a new Array to hold the result
			IArray outarray = Factory.createArray(double.class, dataShape);
			IArray out_variance = Factory.createArray(double.class,dataShape);
			IIndex out_index = outarray.getIndex();
			IIndex variance_ind = varianceArray.getIndex();
			IIndex out_var_ind = out_variance.getIndex();
			ScalarWithVariance back_data,curr_data,result_data;
			// Loop over the input data and the background data, normalising as we go. 
			if (dataShape.length == 3){    /* length 2 not yet implemented */
				for(int i=0;i< dataShape[0];i++) {
					back_mon_index.set0(i);
					data_index.set(i);
					back_data_index.set(i);
					out_index.set(i);
					variance_ind.set(i);
					out_var_ind.set(i);
					double back_mon_val = backmonitor.getDouble(back_mon_index);
					// assume background monitor counts obey counting statistics
					ScalarWithVariance monitor_ratio = new ScalarWithVariance(back_mon_val,back_mon_val);
					monitor_ratio = ScalarOperations.reciprocal(monitor_ratio);
					monitor_ratio = ScalarOperations.multiply(norm_counts, monitor_ratio);
					for(int j=0;j<dataShape[1];j++) {
						data_index.set1(j);
						back_data_index.set1(j);
						out_index.set1(j);
						variance_ind.set1(j);
						out_var_ind.set1(j);
						for(int k=0;k<dataShape[2];k++){
							data_index.set2(k);
							back_data_index.set2(k);
							out_index.set2(k);
							variance_ind.set2(k);
							out_var_ind.set2(k);
							// for efficiency store data in temporary variables
							curr_data = new ScalarWithVariance(dataArray.getDouble(data_index),varianceArray.getDouble(variance_ind));
							double back_val = backgroundArray.getDouble(back_data_index);
							back_data = new ScalarWithVariance(back_val,back_val);
							// Now we do the maths using our error propagation routines
							// if background would be negative, set it to zero
							result_data = ScalarOperations.multiply(back_data, monitor_ratio);
							result_data = ScalarOperations.subtract(curr_data, result_data);
							if(result_data.getData()<0d) outarray.setDouble(out_index,0.0);
							else outarray.setDouble(out_index, result_data.getData());
							out_variance.setDouble(out_var_ind, result_data.getVariance());
						}
					}
				}
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
			URI backgroundCorrection_backgroundFilename) 
	throws Exception {
		this.backgroundCorrection_backgroundFilename = backgroundCorrection_backgroundFilename;
		if (backgroundCorrection_backgroundFilename != null){
			File backgroundFile = null;
			URI uri = backgroundCorrection_backgroundFilename;
			try {
			    backgroundFile = new File(backgroundCorrection_backgroundFilename);
			} catch (Exception e) {
				backgroundFile = null;
			}
			if (backgroundFile == null || ! backgroundFile.exists()){
				System.out.println("loading default background file");
				backgroundFile = ConverterLib.findFile(Activator.PLUGIN_ID, "data/backgroundFile.hdf");
			}
			// Done this way to enable tests to run without using bundle technology.  The user.dir seen by the
			// tests is just the plugin home directory.
			
			File dict_file = new File("xml/path_table.txt");
            if (!dict_file.exists()) {
				dict_file = new File(ConverterLib.getDictionaryPath(Activator.PLUGIN_ID));
			}
			uri = ConverterLib.path2URI(backgroundFile.getPath());
		    IGroup backgroundRootGroup = (IGroup) Utilities.findObject(uri, dict_file.getAbsolutePath());
			backgroundData = backgroundRootGroup.getGroup("data");
		}
	}
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

}

