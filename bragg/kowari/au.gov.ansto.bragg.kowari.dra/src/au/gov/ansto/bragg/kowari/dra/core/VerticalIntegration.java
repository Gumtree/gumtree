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
package au.gov.ansto.bragg.kowari.dra.core;

import org.gumtree.data.impl.netcdf.NcGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotMathException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotUtil;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 29/09/2008
 */
public class VerticalIntegration extends ConcreteProcessor {

	private Plot verticalIntegration_inputPlot;
	private Plot verticalIntegration_outputPlot;
	private Boolean verticalIntegration_skip = false;
	private Boolean verticalIntegration_stop = false;
	public final static DataStructureType dataStructureType = DataStructureType.plot;
	public DataDimensionType dataDimensionType = DataDimensionType.patternset;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		if (verticalIntegration_skip){
			verticalIntegration_outputPlot = verticalIntegration_inputPlot;
//			verticalIntegration_outputPlot.addLog(verticalIntegration_inputPlot.getProcessingLog(), null);
			return verticalIntegration_stop;
		}
		if (verticalIntegration_inputPlot.getRank() < 2)
			throw new PlotMathException("failed to do integration, not enough rank");
//		verticalIntegration_outputGroup = verticalIntegration_inputGroup.sumForDimension(0);
		verticalIntegration_outputPlot = 
			verticalIntegration_inputPlot.enclosedIntegrateDimension(
					verticalIntegration_inputPlot.getRank() - 2);
		((NcGroup) verticalIntegration_outputPlot).setLocation(verticalIntegration_inputPlot.getLocation());
//		verticalIntegration_outputPlot.addLog(verticalIntegration_inputPlot.getProcessingLog(), null);
		verticalIntegration_outputPlot.setTitle("Vertical Integration");
		verticalIntegration_outputPlot.findSingal().setUnits("counts");
//		PlotUtil.removeNaN(verticalIntegration_outputPlot);
		PlotUtil.removeZeroVariance(verticalIntegration_outputPlot);
		verticalIntegration_inputPlot.getGroupList().clear();
		return verticalIntegration_stop;
	}
	/**
	 * @return the verticalIntegration_outputPlot
	 */
	public Plot getVerticalIntegration_outputPlot() {
		return verticalIntegration_outputPlot;
	}
	/**
	 * @param verticalIntegration_inputPlot the verticalIntegration_inputPlot to set
	 */
	public void setVerticalIntegration_inputPlot(Plot verticalIntegration_inputPlot) {
		this.verticalIntegration_inputPlot = verticalIntegration_inputPlot;
	}
	/**
	 * @param verticalIntegration_skip the verticalIntegration_skip to set
	 */
	public void setVerticalIntegration_skip(Boolean verticalIntegration_skip) {
		this.verticalIntegration_skip = verticalIntegration_skip;
	}
	/**
	 * @param verticalIntegration_stop the verticalIntegration_stop to set
	 */
	public void setVerticalIntegration_stop(Boolean verticalIntegration_stop) {
		this.verticalIntegration_stop = verticalIntegration_stop;
	}
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

}
