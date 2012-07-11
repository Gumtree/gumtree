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

import java.net.URI;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.dae.core.util.HistogramType;
import org.gumtree.dae.core.util.ILiveDataRetriever;
import org.gumtree.dae.core.util.LiveDataRetriever;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.service.cli.ICommandLineOptions;

import au.gov.ansto.bragg.cicada.dam.core.DataManagerFactory;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kowari.dra.internal.Activator;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 04/06/2008
 */
public class HistogramViewer extends ConcreteProcessor {

	private Boolean histogramViewer_loopIn;
	private Boolean histogramViewer_loopOut;
	private IGroup histogramViewer_data;
	private Double histogramViewer_interval = 5.0;
	private String histogramViewer_server = "default";
	private Integer histogramViewer_port = 8081;
	public void setHistogramViewer_server(String histogramViewer_server) {
		this.histogramViewer_server = histogramViewer_server;
	}
	public void setHistogramViewer_port(Integer histogramViewer_port) {
		this.histogramViewer_port = histogramViewer_port;
	}
	private Boolean histogramViewer_stop = false;
	private boolean isFirst = true;
	private ILiveDataRetriever retriever;
	private static final String OPTION_SICS_INSTRUMENT = "sicsInstr";
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.quokka.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		// TODO Auto-generated method stub
//		if (histogramViewer_interval.doubleValue() != interval.doubleValue()){
//			System.out.println("oldValue=" + interval + " newValue=" + histogramViewer_interval);
//			setInterval(histogramViewer_interval);
//			System.out.println("set the interval to " + interval);
//			return true;
//		}
		retriever = new LiveDataRetriever();
		retriever.setUser("Gumtree");
		retriever.setPassword("Gumtree");

		String serverName = histogramViewer_server;
		if (histogramViewer_server.equals("default")){
			ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
			if(options.hasOptionValue(OPTION_SICS_INSTRUMENT)) {
				String instrumentName = options.getOptionValue(OPTION_SICS_INSTRUMENT);
				serverName = instrumentName.substring(instrumentName.lastIndexOf(".") + 1);
				serverName = "das1-" + serverName + ".nbi.ansto.gov.au";
			}
		}
		
		URI fileHandle = null;
		try {
//			fileHandle = retriever.getHDFFileHandle("localhost", 8081, HistogramType.TOTAL_HISTO_XY);
			fileHandle = retriever.getHDFFileHandle(serverName, histogramViewer_port, HistogramType.TOTAL_HISTO_XY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			try {
				fileHandle = retriever.getHDFFileHandle("localhost", histogramViewer_port, HistogramType.TOTAL_HISTO_XY);
			}catch (Exception e1) {
				// TODO: handle exception
				throw e1;
			}
//			e.printStackTrace();
//			return "can not make the connection";
		}
		// Test if file is physically available
		long sleepTime;
		if (isFirst){
			sleepTime = 2000;
			isFirst = false;
		}
		else
			sleepTime = (long) (histogramViewer_interval * 1000);
		System.out.println("wait for " + sleepTime +" milsec");
		try {
			Thread.sleep(sleepTime);
		} catch (Exception e) {
			// TODO: handle exception
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
