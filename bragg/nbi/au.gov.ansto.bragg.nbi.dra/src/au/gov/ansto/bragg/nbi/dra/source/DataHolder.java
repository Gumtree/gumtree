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
package au.gov.ansto.bragg.nbi.dra.source;

import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 14/10/2008
 */
public class DataHolder extends ConcreteProcessor {

	private Plot dataHolder_inputPlot;
	private Plot dataHolder_outputPlot;
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		dataHolder_outputPlot = dataHolder_inputPlot;
		return false;
	}

	/**
	 * @return the dataHolder_outputPlot
	 */
	public Plot getDataHolder_outputPlot() {
		return dataHolder_outputPlot;
	}

	/**
	 * @param dataHolder_inputPlot the dataHolder_inputPlot to set
	 */
	public void setDataHolder_inputPlot(Plot dataHolder_inputPlot) {
		this.dataHolder_inputPlot = dataHolder_inputPlot;
	}

}
