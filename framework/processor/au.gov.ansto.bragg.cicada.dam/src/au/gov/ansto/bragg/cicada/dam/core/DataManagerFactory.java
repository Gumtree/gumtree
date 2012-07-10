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
package au.gov.ansto.bragg.cicada.dam.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.cli.ICommandLineOptions;

import au.gov.ansto.bragg.cicada.dam.core.exception.DataManagerException;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;

public class DataManagerFactory {

	public static final String OPTION_ALGO_SET = "algoSet";

	private static List<DataManager> dataManagers;
	
	public static DataManager getDataManager(String dictionaryPath){
		if (dataManagers == null) {
			dataManagers = new ArrayList<DataManager>();
		}
		DataManager dataManager = searchDataManagers(dataManagers, dictionaryPath);
		if (dataManager != null) return dataManager;
		dataManager = new DataManager(dictionaryPath);
		dataManagers.add(dataManager);
		return dataManager;
	}
	
	private static DataManager searchDataManagers(List<DataManager> dataManager, String dictionaryPath){
		if (dictionaryPath == null) 
			return null;
		for (Iterator<DataManager> iter = dataManagers.iterator(); iter.hasNext();){
			DataManager manager = iter.next();
			try{
				if (manager.getDictionaryPath().equals(dictionaryPath)) return manager;
			}catch (Exception e) {
				// TODO: handle exception
			}
		}
		return null;
	}
	
	public static String getAlgorithmDictionaryPath() throws IOException, URISyntaxException{
		String pluginId = null;
		ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
		if(options.hasOptionValue(OPTION_ALGO_SET)) {
			pluginId = options.getOptionValue(OPTION_ALGO_SET);
			File dict_file = new File(ConverterLib.getDictionaryPath(pluginId));
			return dict_file.getAbsolutePath();
		}
		return null;
	}

	public static DataManager getDataManager() throws DataManagerException {
		try {
			return getDataManager(getAlgorithmDictionaryPath());
		} catch (Exception e) {
			throw new DataManagerException("failed to get dictionary path");
		} 
	}
}
