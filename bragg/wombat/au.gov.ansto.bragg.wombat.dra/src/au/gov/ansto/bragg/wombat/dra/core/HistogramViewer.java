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
package au.gov.ansto.bragg.wombat.dra.core;

import java.net.URI;

import org.gumtree.dae.core.util.HistogramType;
import org.gumtree.dae.core.util.ILiveDataRetriever;
import org.gumtree.dae.core.util.LiveDataRetriever;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.dam.core.DataManagerFactory;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.wombat.dra.internal.Activator;

/**
 * @author nxi
 * Created on 04/06/2008
 */
public class HistogramViewer implements ConcreteProcessor {

	private Boolean histogramViewer_loopIn;
	private Boolean histogramViewer_loopOut;
	private IGroup histogramViewer_data;
	private Double histogramViewer_interval = 10.0;
	private Boolean histogramViewer_stop = false;
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

		String serverName = "localhost";
//		ICommandLineOptions options = GTPlatform.getCommandLineOptions();
//		if(options.hasOptionValue(OPTION_SICS_INSTRUMENT)) {
//			String instrumentName = options.getOptionValue(OPTION_SICS_INSTRUMENT);
//			serverName = instrumentName.substring(instrumentName.lastIndexOf(".") + 1);
//			serverName = "das1-" + serverName + ".nbi.ansto.gov.au";
//		}
//		
		URI fileHandle = null;
		try {
//			fileHandle = retriever.getHDFFileHandle("localhost", 8081, HistogramType.TOTAL_HISTO_XY);
			fileHandle = retriever.getHDFFileHandle(serverName, 8081, HistogramType.TOTAL_HISTO_XY);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			try {
				fileHandle = retriever.getHDFFileHandle("localhost", 8081, HistogramType.TOTAL_HISTO_XY);
			}catch (Exception e1) {
				// TODO: handle exception
				throw e1;
			}
//			e.printStackTrace();
//			return "can not make the connection";
		}
		// Test if file is physically available
		System.out.println("wait for " + histogramViewer_interval +"sec");
		try {
			Thread.sleep( (long) (histogramViewer_interval * 1000));
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
	public DataDimensionType getDataDimensionType() {
		// TODO Auto-generated method stub
		return DataDimensionType.map;
	}
	public DataStructureType getDataStructureType() {
		// TODO Auto-generated method stub
		return DataStructureType.plot;
	}

}
