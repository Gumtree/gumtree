/******************************************************************************* 
* Copyright (c) 2009 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Based on (Kowari) HistogramViewer class by Norman Xiong, 2008
*    Paul Hathaway, August 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.online;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.dae.core.util.HistogramType;
import org.gumtree.dae.core.util.ILiveDataRetriever;
import org.gumtree.dae.core.util.LiveDataRetriever;
import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.service.cli.ICommandLineOptions;

import au.gov.ansto.bragg.cicada.dam.core.DataManagerFactory;
import au.gov.ansto.bragg.cicada.dam.core.exception.NullDataObjectException;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;
import au.gov.ansto.bragg.quokka.dra.internal.Activator;

public class HistogramViewer extends ConcreteProcessor {

	/* Fields for audit trail support */
	private static final String processClass = "HistogramViewer"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009083101; 
    private String pluginID;

	/* Fields to support client-side processing */
	private static DataStructureType dataStructureType = DataStructureType.plot;
	private static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

    private static final String DEFAULT_DRA_PLUGIN = "au.gov.ansto.bragg.quokka.dra";
    private static final String OPTION_ALGO_SET = "algoSet";
    private static final String REDUCTION_DIC = "\\xml\\reduction.dic.txt";
	private static final String OPTION_SICS_INSTRUMENT = "sicsInstr";

	private Boolean inLoop;
	private Boolean outLoop;
	
	private Boolean doStop = false;
	
	private IGroup histogram;  // Processed Entry Group from HM Server dataset
	
	private Double pollInterval = 5.0; // seconds
	private Integer serverPort = 8080;
	private String serverName = "default";

	private boolean isFirst = true;
	private ILiveDataRetriever retriever;
	
    public HistogramViewer() {
		this.setReprocessable(false);
		pluginID = findPluginID();
	}

	public Boolean process() throws Exception {

		retriever = new LiveDataRetriever();
		retriever.setUser("Gumtree");
		retriever.setPassword("Gumtree");

		/* 'serverName' is one of the option strings, usually predefined by the algorithm,
		 * and including the "default" option.
		 * 'server' is the string for the actual server host name  
		 */
		String server = serverName;
		if (serverName.equals("default")){
			ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
			if(options.hasOptionValue(OPTION_SICS_INSTRUMENT)) {
				String instrumentName = options.getOptionValue(OPTION_SICS_INSTRUMENT);
				server = instrumentName.substring(instrumentName.lastIndexOf(".") + 1);
				server = "das1-" + server + ".nbi.ansto.gov.au";
			}
		}
		
		URI fileHandle = null;
		try {
			fileHandle = retriever.getHDFFileHandle(server, serverPort, HistogramType.TOTAL_HISTO_XY);
		} catch (Exception e) {
			try {
				fileHandle = retriever.getHDFFileHandle("localhost", serverPort, HistogramType.TOTAL_HISTO_XY);
			}catch (Exception e1) {
				throw e1;
			}
			debugging("Cannot establish server connection");
		}

		// Test if file is physically available
		long sleepTime;
		if (isFirst){
			sleepTime = 2000;
			isFirst = false;
		}
		else {
			sleepTime = (long) (pollInterval * 1000);
		}
		try {
			debugging("> wait for " + sleepTime +" milliseconds");
			Thread.sleep(sleepTime);
		} catch (Exception e) {
			throw e;
		}

		doStop = doStop || (null==fileHandle);
		
		if (doStop) {
			debugging("> Manual Stop or filehandle not available");
		} else {
			debugging("> filehandle: "+fileHandle);
			histogram = fetchHistogram(fileHandle);			
		}
		if (null==histogram) {
			debugging("> histogram: null");
			doStop = true;
			histogram = Factory.createGroup("empty");
		}
		outLoop = !doStop;
		return doStop;
	}
	
	private IGroup fetchHistogram(URI fileHandle) 
		throws FileAccessException, NullDataObjectException, IOException, URISyntaxException 
	{
		IGroup hm = ((NcGroup) DataManagerFactory.getDataManager(
				ConverterLib.getDictionaryPath(Activator.PLUGIN_ID))
				.getGroup(fileHandle))
				.getFirstEntryAccess();
		return hm;
	}
	
	private IGroup fetchHistogram(URI fileHandle, int entryIndex) 
		throws FileAccessException, NullDataObjectException, IOException, URISyntaxException 
	{
		IGroup entry = DataManagerFactory.getDataManager(
				ConverterLib.getDictionaryPath(Activator.PLUGIN_ID))
				.getGroup(fileHandle);
		List<IGroup>  entryList = ((NcGroup) entry).getEntries();
		IGroup hm = entryList.get(entryIndex);
		return hm;
	}
	
	private IGroup fetchHistogram(URI fileHandle, String name) 
		throws FileAccessException, NullDataObjectException, IOException, URISyntaxException 
	{
		IGroup entry = DataManagerFactory.getDataManager(
				ConverterLib.getDictionaryPath(Activator.PLUGIN_ID))
				.getGroup(fileHandle);
		IGroup hm = entry.findGroup(name);
		return hm;
	}

	private String findPluginID() {
		String pluginId = DEFAULT_DRA_PLUGIN;
		ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
		if (null!=options) {
			if (options.hasOptionValue(OPTION_ALGO_SET)) {
				pluginId = options.getOptionValue(OPTION_ALGO_SET);
			}
		}
		return pluginId;
	}

	private void debugging(String msg) {
		if (isDebugMode) {
			System.out.println(msg);
		}
	}
	
	/* Client Support methods -----------------------------------------*/	
	
	public DataStructureType getDataStructureType() {
		return dataStructureType;
	}
	
	public DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	public void setIsDebugMode(Boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}

	public Boolean getIsDebugMode() {
		return isDebugMode;
	}	
	/* Port get/set methods -----------------------------------------*/
	
    /* In-Ports -----------------------------------------------------*/

	public void setInLoop(Boolean inLoop) {
		this.inLoop = inLoop;
	}
	
	/* Out-Ports ----------------------------------------------------*/

	public IGroup getHistogram() {
		return this.histogram;
	}

	public Boolean getOutLoop() {
		if(null==this.outLoop) {
			this.outLoop = false;
		}
		return this.outLoop;
	}
	
	/* Var-Ports (options) ------------------------------------------*/

	public void setStop(Boolean doStop) {
		this.doStop = doStop;
	}
	
	public Boolean getStop() {
		return this.doStop;
	}

    /* Var-Ports (tuners) -------------------------------------------*/

	public void setPollInterval(Double interval) {
		this.pollInterval = interval;
	}

	public void setServerName(String name) {
		this.serverName = name;
	}

	public void setServerPort(Integer port) {
		this.serverPort = port;
	}

}

