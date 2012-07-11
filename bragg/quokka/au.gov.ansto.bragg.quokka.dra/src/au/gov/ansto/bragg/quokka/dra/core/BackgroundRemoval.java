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
package au.gov.ansto.bragg.quokka.dra.core;

import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.quokka.dra.core.internal.ConcreteProcessor;

/**
 * @author nxi
 * Created on 17/10/2008
 */
public class BackgroundRemoval implements ConcreteProcessor {

	private Plot backgroundRemoval_inputPlot;
	private Plot backgroundRemoval_outputPlot;
	private IGroup backgroundRemoval_region;
	private Boolean backgroundRemoval_skip = true;
	private Boolean backgroundRemoval_stop = false;
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.dra.core.internal.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
		if (backgroundRemoval_skip || backgroundRemoval_region == null){
			backgroundRemoval_outputPlot = backgroundRemoval_inputPlot;
			return backgroundRemoval_stop;
		}
		IArray regionalData = RegionUtils.applyRegion(backgroundRemoval_inputPlot, 
				backgroundRemoval_region);
		IArrayIterator regionalIterator = regionalData.getIterator();
		int counter = 0;
		double sum = 0;
		while (regionalIterator.hasNext()) {
			double value = regionalIterator.getDoubleNext();
			if (! Double.isNaN(value)){
				sum += value;
				counter ++;
			}
		}
		backgroundRemoval_outputPlot = backgroundRemoval_inputPlot.toAdd(- sum / counter, 
				- sum / counter);
		try{
			IArrayIterator outputInterator = backgroundRemoval_outputPlot.findSignalArray().
			getIterator();
			while(outputInterator.hasNext()){
				double value = outputInterator.getDoubleNext();
				if (value <= 0)
					outputInterator.setDoubleCurrent(0);
			}
			IArrayIterator varianceInterator = backgroundRemoval_outputPlot.getVariance().
			getData().getIterator();
			while(varianceInterator.hasNext()){
				double value = varianceInterator.getDoubleNext();
				if (value < 0)
					varianceInterator.setDoubleCurrent(0);
			}				
		}catch (Exception e) {
			// TODO: handle exception
		}
		return backgroundRemoval_stop;
	}

	/**
	 * @return the backgroundRemoval_outputPlot
	 */
	public Plot getBackgroundRemoval_outputPlot() {
		return backgroundRemoval_outputPlot;
	}

	/**
	 * @param backgroundRemoval_inputPlot the backgroundRemoval_inputPlot to set
	 */
	public void setBackgroundRemoval_inputPlot(Plot backgroundRemoval_inputPlot) {
		this.backgroundRemoval_inputPlot = backgroundRemoval_inputPlot;
	}

	/**
	 * @param backgroundRemoval_region the backgroundRemoval_region to set
	 */
	public void setBackgroundRemoval_region(IGroup backgroundRemoval_region) {
		this.backgroundRemoval_region = backgroundRemoval_region;
	}

	/**
	 * @param backgroundRemoval_skip the backgroundRemoval_skip to set
	 */
	public void setBackgroundRemoval_skip(Boolean backgroundRemoval_skip) {
		this.backgroundRemoval_skip = backgroundRemoval_skip;
	}

	/**
	 * @param backgroundRemoval_stop the backgroundRemoval_stop to set
	 */
	public void setBackgroundRemoval_stop(Boolean backgroundRemoval_stop) {
		this.backgroundRemoval_stop = backgroundRemoval_stop;
	}

}
