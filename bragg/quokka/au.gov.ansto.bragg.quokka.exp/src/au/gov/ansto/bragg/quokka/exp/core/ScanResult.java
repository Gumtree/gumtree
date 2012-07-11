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
package au.gov.ansto.bragg.quokka.exp.core;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.kakadu.dom.PlotDOM;
import au.gov.ansto.bragg.quokka.exp.core.exception.GetDataFailedException;
import au.gov.ansto.bragg.quokka.exp.core.exception.PlotErrorException;
import au.gov.ansto.bragg.quokka.exp.core.scanfunction.Function;

/**
 * @author nxi
 * Created on 14/04/2008
 */
public class ScanResult extends Function {

	/**
	 * 
	 */
	public ScanResult() {
		// TODO Auto-generated constructor stub
		super();
	}

	public ScanResult(Function function) {
		plotTitle = function.getPlotTitle();
		peak = function.getPeak();
		max = function.getStatistic(FunctionalStatistic.max);
		min = function.getStatistic(FunctionalStatistic.min);
		centroid = function.getStatistic(FunctionalStatistic.centroid);
		mean = function.getStatistic(NonFunctionalStatistic.mean);
		RMS = function.getStatistic(NonFunctionalStatistic.RMS);
		total = function.getStatistic(NonFunctionalStatistic.total);
		device = function.getDevice();
	}
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.scanfunction.Function#addData(org.gumtree.data.gdm.core.Group)
	 */
	@Override
	public void addData(IGroup databag) throws GetDataFailedException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.exp.core.scanfunction.Function#getShortDescription()
	 */
	@Override
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDoubleData(Double data) throws GetDataFailedException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void clearDataHistory() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void clearPeak() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public PlotDOM plot(Composite composite)
			throws PlotErrorException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void plotLastMarker() throws PlotErrorException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void rePlot() throws PlotErrorException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void setEntryArray(double[] entryArray) {
		// TODO Auto-generated method stub
	}
	
}
