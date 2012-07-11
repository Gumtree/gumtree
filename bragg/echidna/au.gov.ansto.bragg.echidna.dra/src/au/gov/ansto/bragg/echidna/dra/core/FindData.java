/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.echidna.dra.core;

import java.awt.color.ProfileDataException;
import java.io.IOException;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 30/05/2008
 */
public class FindData extends ConcreteProcessor {

	IGroup source_groupData = null;
	String source_dataName = "data";
	Plot source_outdata = null;
	private String dimensionType = "pattern";
	
	public IGroup getSource_outdata() {
		return source_outdata;
	}

	public void setSource_groupData(IGroup source_groupData) {
		this.source_groupData = source_groupData;
	}

	public void setSource_dataName(String source_dataName) {
		this.source_dataName = source_dataName;
	}

	public Boolean process() throws Exception {
		if (source_groupData == null) {
			return false;
		}
		IGroup data_group = null;
		try {
			data_group = source_groupData.getGroup(source_dataName);
			if (data_group == null)
				data_group = source_groupData.getDataItem(source_dataName).getParentGroup();			
		} catch (Exception e) {
			throw new ProfileDataException("can not find the data: " + source_dataName);
		}
		if (data_group == null)
			throw new ProfileDataException("can not find the data: " + source_dataName);
		/* As a number of routines check for the signal attribute, we make sure that our data have this
		 * attribute.  This will not be the case if we are interested in 'total_counts' and we have also
		 * a histogram in the same group
		 */
		IDataItem real_data = data_group.findDataItem(source_dataName);
		if(real_data == null) {
			throw new ProfileDataException("Cannot find " + source_dataName + " in group " + data_group.getName());
		}
		source_outdata = PlotFactory.createPlot("Scan data", DataDimensionType.valueOf(dimensionType));
		source_outdata.addData("Scan results", real_data.getData().getArrayMath().toScale(1.00).getArray(), "Intensity", "Counts");
		guess_scan_axis(source_groupData,source_outdata);
		return false;
	}

	/*
	 *  This routine runs through known potential scan axes in an attempt to find the one that has been
	 *  scanned.  It returns an Axis object which can be directly inserted into a result Group
	 */
	
	private void guess_scan_axis(IGroup target_group, Plot target_plot) throws IOException, InvalidRangeException, ShapeNotMatchException, InvalidArrayTypeException, PlotFactoryException {
		// A list of possible scan axes
		String[][] locations= {{"/entry1/sample/azimuthal_angle","Two theta"},
	                           {"/entry1/instrument/crystal/chi","Mono chi"},
				               {"/entry1/sample/chi","Chi"},
				               {"/entry1/sample/dummy_motor","Dummy"},
				               {"/entry1/sample/phi","Phi"},
				               {"/entry1/sample/rotate","Rotation"},
				               {"/entry1/instrument/collimator/secondary_collimator_rotation","Secondary collimator"},
				               {"/entry1/sample/translate_x","X translation"},
				               {"/entry1/sample/translate_y","Y translation"},
				               {"/entry1/instrument/crystal/omega","Mono omega"},
				               {"/entry1/data/run_number","Run number"}};
		for (String[] location:locations) {
			IDataItem axis_data = null;
			try {
				axis_data = (IDataItem) target_group.findContainerByPath(location[0]);
			} catch (Exception e) { }
			if(axis_data!=null) {
				//We use the following heuristic to tell if an axis is scanned: the step size between the
				//individual points has the same sign and approximate size
				IArray axis_points = axis_data.getData();
				IArray axis_steps = axis_points.getArrayUtils().section(new int[] {1}, new int[] {(int) axis_points.getSize()-1}).getArray();
				IArray axis_jump = axis_points.getArrayUtils().section(new int[] {0}, new int[] {(int) axis_points.getSize()-1}).getArray();
				IArray steps = axis_jump.getArrayMath().toScale(-1.0).getArray();
				steps.getArrayMath().add(axis_steps);  //this contains the steps between each point
				//Test if all steps have same sign
				if(Math.abs(steps.getArrayMath().getMaximum()+steps.getArrayMath().getMinimum())<Math.abs(steps.getArrayMath().getMaximum())
						&& location != locations[locations.length - 1]) continue;
				double ave_step = -1.0*(steps.getArrayMath().sum()/steps.getSize());
				IArray step_diff = steps.getArrayMath().add(ave_step).getArray();
				//Test if step is greater than zero
				if(Math.abs(ave_step)<1E-6) continue;
				//Test if variation in step size is greater than a third of step size (too big)
				double min = Math.abs(step_diff.getArrayMath().getMaximum());
				double max = Math.abs(step_diff.getArrayMath().getMinimum());
				if(Math.abs(step_diff.getArrayMath().getMaximum())-Math.abs(step_diff.getArrayMath().getMinimum())>Math.abs(ave_step/3.0)) continue;
				System.out.printf("Found axis! %s, average step %f%n", location[0], ave_step);
				//Now create the axis object with the necessary information
				target_plot.addAxis(location[1], axis_points, location[1], "Unknown", 0);
				return;
			}
		}
		return;
	}

}
