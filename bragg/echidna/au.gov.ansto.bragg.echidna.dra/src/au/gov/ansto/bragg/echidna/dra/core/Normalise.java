package au.gov.ansto.bragg.echidna.dra.core;

import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.plot.*;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.errorpropagation.*;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * This class is the concrete processor of the normalisation algorithm for echidna algorithm group.
 * 
 * @author jhester
 * @version 1.1
 *
 */
public class Normalise extends ConcreteProcessor {
	IGroup normalise_groupdata = null;
	IGroup normalised_data = null;
	boolean skip_norm = false;
	private ArrayOperations ao = new ArrayOperations();
	private String norm_reference =  null;
	public final static DataStructureType source_dataStructureType = DataStructureType.plot;
	public final static DataDimensionType source_dataDimensionType = DataDimensionType.map;

	public Boolean process() throws Exception{
			if (skip_norm || normalise_groupdata == null) { 
				normalised_data = normalise_groupdata;
				return false;
			}
			String metadata_string = "";
			// Read in needed data and prepare output arrays
		    IArray dataArray = ((NcGroup) normalise_groupdata).findSignal().getData();
			IArray normed_data = Factory.createArray(double.class,dataArray.getShape());
			IArray error_array = ((Plot) normalise_groupdata).getVariance().getData();
			IArray norm_error_array = error_array.copy();
			/* If we have a 3D array, this means a series of monitor counts is available to
			 * allow normalisation at each scan step.  If the array is 2D, then no such
			 * normalisation is necessary.
			 */
			double monmax = 1.0;   // Value to which we normalise; usually maximum monitor counts
			IArray bad_frames = null;
			if(dataArray.getRank()==3) {
				int [] data_dims = dataArray.getShape();
				bad_frames = Factory.createArray(Integer.class,new int[] {data_dims[0]});
				IArray monitor_array = null;
				if(norm_reference==null) {
				monitor_array = normalise_groupdata.getRootGroup().getDataItem("monitor_data").getData();
				} else {
				monitor_array = normalise_groupdata.getRootGroup().getDataItem(norm_reference).getData();	
				}
				monmax = monitor_array.getArrayMath().getMaximum();
				System.out.printf("Normalising to %f monitor counts\n", monmax);
				IIndex data_ind = dataArray.getIndex();
				IIndex mon_ind = monitor_array.getIndex();
				IIndex normed_ind = normed_data.getIndex();
				IIndex bad_frame_ind = bad_frames.getIndex();
				int bad_frame_count = 0;
				// We loop over the input data, calculating the normalised value at each point and
				// the variance
				for(int step_no=0;step_no<data_dims[0];step_no++) {
					mon_ind.set(step_no);
					normed_ind.set(step_no);
					data_ind.set(step_no);
					bad_frame_ind.set(step_no);
					int [] section_origin = {step_no,0,0}; // get this section of complete data
					int [] section_range = {1,data_dims[1],data_dims[2]}; //all data for one step
					double this_mon = monitor_array.getDouble(mon_ind);
					if(this_mon < 1.0) {
						String errorstring = String.format("Zero monitor counts at step %d: normalisation not possible for this step", step_no);
						System.out.print(errorstring);
						// Store this step in our bad_frame list
						bad_frames.setInt(bad_frame_ind, 1);        //flag for bad
						bad_frame_count++;
						continue;
					}
					double mult_val = monmax/monitor_array.getDouble(mon_ind);
					// Error below derived from normal partial derivative expression
					// assuming counting statistics for the monitor counts, but remembering that the
					// value to be normalised to has no associated error (as it is arbitrary).
					double mult_err = (mult_val*mult_val)/monitor_array.getDouble(mon_ind);
					IArray [] dat_with_err = {dataArray.getArrayUtils().section(section_origin, section_range).getArray(),
							                 error_array.getArrayUtils().section(section_origin,section_range).getArray()};
					double [] mult_with_err = {mult_val, mult_err};
					IArray [] comp_result = ArrayOperations.multiplyByScalar(dat_with_err, mult_with_err);
					IIndex res_index = comp_result[0].getIndex();
					IIndex res_err_ind = comp_result[1].getIndex();
					res_index.set(8,0);
					IIndex temp_index = dat_with_err[0].getIndex();
					temp_index.set(8,0);
					System.out.printf("%d Normalisation value,error: %f %f%n", step_no,mult_val,mult_err);
					for(int tube_no=0;tube_no<data_dims[1];tube_no++) {
						for(int vert_pix=0;vert_pix<data_dims[2];vert_pix++) {
							res_index.set(tube_no,vert_pix);
							normed_ind.set(step_no,tube_no,vert_pix);
							res_err_ind.set(tube_no,vert_pix);
							normed_data.setDouble(normed_ind,comp_result[0].getDouble(res_index));
							norm_error_array.setDouble(normed_ind,comp_result[1].getDouble(res_err_ind));						
						}
					}
				}
				// Now produce our metadata
				metadata_string = String.format("Each detector step normalised to a monitor count value of %10.1f%n",monmax);
				if(bad_frame_count>0) 
					metadata_string += String.format("%2d bad frames were detected and flagged%n",bad_frame_count);
			}
			else {     //A simple 2D array, which means no internal normalisation necessary
				    bad_frames = Factory.createArray(Integer.class,new int[] {1});
					normed_data = dataArray.copy();
					norm_error_array = error_array.copy();
					metadata_string = String.format("No normalisation performed as only one detector step in file%n");
				}
			// Now build the output databag
				String resultName = "normalisation_result";
				normalised_data = PlotFactory.createPlot(normalise_groupdata, resultName, source_dataDimensionType);
				((NcGroup) normalised_data).addLog("Apply normalisation to get " + resultName);
				PlotFactory.addDataToPlot(normalised_data, resultName, normed_data, "Normalised data", "Normalised counts",norm_error_array);
				// Copy axes across from previous data
				List<Axis> data_axes = ((Plot) normalise_groupdata).getAxisList();
				for (Axis oneaxis : data_axes) {
					PlotFactory.addAxisToPlot(normalised_data, oneaxis,oneaxis.getDimensionName());
				}
				((NcGroup) normalised_data).addMetadata("CIF","_pd_proc_info_data_reduction", metadata_string,false);
        // We need the value to which everything has been normalised to be available to
		// subsequent processing steps
		IAttribute norm_val = Factory.createAttribute("normalised_to_val", monmax);
		// normalised_data.buildResultGroup(signal, stthVector, channelVector, twoThetaVector); 
		normalised_data.addOneAttribute(norm_val);
		IDataItem bad_frame_di = Factory.createDataItem(normalised_data, "bad_frames", bad_frames);
		normalised_data.addDataItem(bad_frame_di);
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
	
	public String getNorm_reference() {
		return norm_reference;
	}

	public void setNorm_reference(String refloc) {
		norm_reference = refloc;
	}
	
	public DataStructureType getDataStructureType() {
		return source_dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return source_dataDimensionType;
	}

}

