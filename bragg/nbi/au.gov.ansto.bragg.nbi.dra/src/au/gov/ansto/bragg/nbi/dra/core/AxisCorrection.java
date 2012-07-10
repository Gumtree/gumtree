package au.gov.ansto.bragg.nbi.dra.core;

import java.util.List;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.common.dra.algolib.math.BinTypeChange;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class AxisCorrection extends ConcreteProcessor {

	IGroup axisCorrection_input = null;
	Boolean axisCorrection_skip = false;
	Boolean axisCorrection_stop = false; 
	IGroup axisCorrection_output = null;
	Boolean axis_flip = false;  //two-theta is in wrong direction

	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public final static DataDimensionType dataDimensionType = DataDimensionType.map;

/** This processor corrects the 2-theta and vertical axes to give physical values.
 * This is important for proper data output and geometry correction.
 * 
 * The wombat 2-theta and vertical axis corrections simply involve changing from bin boundaries to
 * bin centres.
 * 
 */
	public Boolean process() throws Exception {
		if(axisCorrection_skip) {
			axisCorrection_output = axisCorrection_input;
		} else
		{
			//First do 2-theta axis: this should be the fastest-varying, but we need to know the rank
			int drank = ((Plot) axisCorrection_input).getRank();
			int[] dshape = ((NcGroup) axisCorrection_input).getSignalArray().getShape();
			IArray thetaArray = null;
			try{
				thetaArray = ((Plot) axisCorrection_input).getAxis(drank-1).getData();
			}catch (Exception ex){
				throw new Exception("Axis correction fails: no theta axis in file");
			}
			IArray verticalOffsetArray = null;
			try {
				verticalOffsetArray = ((Plot) axisCorrection_input).getAxis(drank-2).getData();
			} catch (Exception e) {
				String errorstring = "Axis correction fails: no vertical pixel location data in file";
				throw new Exception(errorstring);
			}
			int trank = thetaArray.getRank();
			if(axis_flip) thetaArray = thetaArray.getArrayUtils().flip(thetaArray.getRank()-1).getArray();
			// Only fiddle if we have a data shape mismatch
			thetaArray = thetaArray.getArrayUtils().reduce().getArray();  //This gets around a bug in the ucar 4.0 code whereby section 
			                                            //doesn't work for flipped arrays
			trank = thetaArray.getRank();
			IArray new_two_theta = thetaArray;
			if(dshape[drank-1]==thetaArray.getShape()[trank-1]-1) new_two_theta = BinTypeChange.ToCentres(thetaArray);
			IArray new_vertical = verticalOffsetArray;
			if(dshape[drank-2]==verticalOffsetArray.getShape()[0]-1)   //Assume 1D vertical offset array
				new_vertical = BinTypeChange.ToCentres(verticalOffsetArray);
			// Create a new output object with the input data all there
			axisCorrection_output  = PlotFactory.createPlot(axisCorrection_input, "Corrected_axes", dataDimensionType);
			((NcGroup) axisCorrection_output).addLog("Corrected axis values");
			PlotFactory.addDataToPlot(axisCorrection_output,"axis_correction", ((NcGroup) axisCorrection_input).getSignalArray(),
					"Corrected axes",((Plot)axisCorrection_input).getDataUnits(),((Plot) axisCorrection_input).getVariance().getData());
			// Now add any other axes that are present on file
			PlotFactory.addAxisToPlot(axisCorrection_output, "two_theta", new_two_theta, "Two theta", "degrees", drank-1);
			PlotFactory.addAxisToPlot(axisCorrection_output, "vert_offset", new_vertical, "Vertical offset", "mm", drank-2);
			// Now add any other axes that are present on file
			List<Axis> data_axes = ((Plot) axisCorrection_input).getAxisList();
			for (Axis thisaxis: data_axes) {
				int thisdim = Integer.parseInt(thisaxis.getAttribute("dimension").getStringValue());
				if(thisdim<drank-2) {
				    PlotFactory.addAxisToPlot(axisCorrection_output, thisaxis, thisdim);
				    System.out.printf("Added axis %d(%s) to output",thisdim,thisaxis.getDescription());
				}
			}
		}
		return axisCorrection_stop;
	}

	public void setAxis_flip(Boolean flipval) {
		axis_flip = flipval;
	}
	
	public void setAxisCorrection_input(IGroup indata) {
		axisCorrection_input = indata;
	}
	
	public IGroup getAxisCorrection_output() {
		return axisCorrection_output;
	}
	
	public void setAxisCorrection_skip(Boolean skipval) {
		axisCorrection_skip = skipval;
	}
	
	public DataStructureType getDataStructureType() {
		return dataStructureType;
		
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

}
