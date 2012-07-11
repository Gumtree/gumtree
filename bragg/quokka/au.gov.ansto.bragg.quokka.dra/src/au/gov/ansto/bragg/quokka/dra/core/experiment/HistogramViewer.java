/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*    Paul Hathaway (4/6/2009)
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core.experiment;

import java.net.URI;

import org.gumtree.dae.core.util.HistogramType;
import org.gumtree.dae.core.util.ILiveDataRetriever;
import org.gumtree.dae.core.util.LiveDataRetriever;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.dam.core.DataManagerFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.quokka.dra.core.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.internal.Activator;

public class HistogramViewer implements ConcreteProcessor {

	private Boolean histogramViewer_loopIn;
	private Boolean histogramViewer_loopOut;
	private IGroup histogramViewer_data;
	private Double histogramViewer_interval = 5.0;
	private Boolean histogramViewer_stop = false;
	private ILiveDataRetriever retriever;
	
	public Boolean process() throws Exception {

		retriever = new LiveDataRetriever();
		retriever.setUser("Gumtree");
		retriever.setPassword("Gumtree");

		URI fileHandle = null;
		try {
			fileHandle = retriever.getHDFFileHandle("localhost", 8081, HistogramType.TOTAL_HISTO_XY);
		} catch (Exception e) {
			throw e;
		}

		// Test if file is physically available
		System.out.println("wait for " + histogramViewer_interval +"sec");
		try {
			Thread.sleep( (long) (histogramViewer_interval * 1000));
		} catch (Exception e) {
			throw e;
		}
		System.out.println(fileHandle);
		histogramViewer_data = ((NcGroup) DataManagerFactory.getDataManager(ConverterLib.getDictionaryPath(
				Activator.PLUGIN_ID)).getGroup(fileHandle)).getFirstEntryAccess();
		histogramViewer_loopOut = ! histogramViewer_stop;
		return histogramViewer_stop;
	}

	public Boolean getHistogramViewer_loopOut() {
		return histogramViewer_loopOut;
	}
	
	public IGroup getHistogramViewer_data() {
		return histogramViewer_data;
	}
	
	public void setHistogramViewer_loopIn(Boolean histogramViewer_loopIn) {
		this.histogramViewer_loopIn = histogramViewer_loopIn;
	}
	
	public void setHistogramViewer_interval(Double histogramViewer_interval) {
		this.histogramViewer_interval = histogramViewer_interval;
	}
	
	public void setHistogramViewer_stop(Boolean histogramViewer_stop) {
		this.histogramViewer_stop = histogramViewer_stop;
	}

}
