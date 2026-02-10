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
package au.gov.ansto.bragg.cicada.core.internal;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.exception.FileAccessException;
import org.jdom2.JDOMException;

import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.extension.AlgorithmRegistration;
import au.gov.ansto.bragg.cicada.core.extension.AlgorithmSet;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.process.common.Common_;
import au.gov.ansto.bragg.process.parse.ConfigurationItem;
import au.gov.ansto.bragg.process.parse.Parse;

public class Configuration extends Common_{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_DRA_PLUGIN = "au.gov.ansto.bragg.cicada.dra";
	
//	private static final String OPTION_ALGO_SET = "algoSet";
	
	private List<ConfigurationItem> configurationList = null;
	private ConfigurationItem defaultLoaded = null;
	private String pluginXMLPath = null;
	List<AlgorithmSet> algorithmSetList = null;
	
	public final static String FILENAME = "au.gov.ansto.bragg.cicada/plugin.xml";
	
	/**
	 * Created on 3 May 07
	 * Load configuration file from the plugin project folder. 
	 * This method take a relative path for the plugin.xml
	 * @throws IOException 
	 */
	public Configuration() throws IOException {
//		Bundle bundle = Platform.getBundle(Activator.getDefault().PLUGIN_ID);
//		URL workspaceURL = FileLocator.toFileURL(FileLocator.find(bundle, new Path("plugin.xml"), null));
//		workspaceURL = FileLocator.toFileURL(FileLocator.find(bundle, new Path("java space.xml"), null));
//		String urlString = workspaceURL.toExternalForm();
//		File file = new File(workspaceURL.toURI());
//		File file = null;
//		file = ConverterLib.findFile(Activator.getDefault().PLUGIN_ID, "plugin.xml");
//
////		try {
////				file = new File(workspaceURL.toURI());
////			} catch(URISyntaxException e) {
////				file = new File(workspaceURL.getPath());
////			}
//		configurationList = Parse.parseConfiguration(file);
//		for (Iterator<ConfigurationItem> iter = configurationList.iterator(); iter.hasNext();){
//			ConfigurationItem item = iter.next();
//			if (item.isDefault()) defaultLoaded = item;
//		}
//		Bundle bundle = Platform.getBundle(defaultLoaded.getClassName());
//		URL pluginXMLURL = FileLocator.toFileURL(FileLocator.find(bundle, new Path("xml"), null));
////		pluginXMLPath = ConverterLib.findFile(, filename)
//		try {
//			pluginXMLPath = pluginXMLURL.toURI().getPath();
//		} catch(URISyntaxException e) {
//			pluginXMLPath = pluginXMLURL.getPath();
//		}
//		pluginXMLPath = pluginXMLURL.toURI().getPath();
		
		
//		pluginXMLPath = pluginXMLURL.getFile();
//		System.out.println("config path: " + pluginXMLPath);
//		pluginXMLPath = file.getParentFile().getParent() + "/" + defaultLoaded.getClassName() + "/xml/";
		
		String pluginId = DEFAULT_DRA_PLUGIN;
		AlgorithmRegistration algorithmRegistration = AlgorithmRegistration.getInstance();
		algorithmSetList = algorithmRegistration.getAlgorithmSetList();
		


//		for (Iterator<String> iterator = algorithmSetList.iterator(); iterator
//				.hasNext();) {
//			String algorithmSetId = iterator.next();
//			System.out.println(algorithmSetId);
//		}
		try {
//			ICommandLineOptions options = GTPlatform.getCommandLineOptions();
//			if(options.hasOptionValue(OPTION_ALGO_SET)) {
//				pluginId = options.getOptionValue(OPTION_ALGO_SET);
//			}
			for (Iterator<?> iterator = algorithmSetList.iterator(); iterator
					.hasNext();) {
				AlgorithmSet algorithmSet = (AlgorithmSet) iterator.next();
				if (algorithmSet.isDefault()) pluginId = algorithmSet.getId();
			}
//			IFileStore fileStore = null;
//			try{
//				fileStore = GTPlatform.find(pluginId, "/xml");
//			}catch (Exception e1){
//				pluginId = DEFAULT_DRA_PLUGIN;
//				fileStore = GTPlatform.find(pluginId, "/xml");
//			}
//			pluginXMLPath = fileStore.toLocalFile(EFS.NONE, new NullProgressMonitor()).getAbsolutePath() + "/";
//			pluginXMLPath = fileStore.toURI().getPath();
			pluginXMLPath = ConverterLib.path2URI(ConverterLib.findFile(pluginId, "/xml").getPath()).getPath();
		} catch (FileAccessException e) {
			throw new IOException("Cannot load from plugin " + pluginId);
		}
	}
	
	public Configuration(String workspacePath) throws ConfigurationException {
		String algorithmDir = workspacePath + FILENAME;
		File file = new File(algorithmDir);
		try {
			configurationList = Parse.parseConfiguration(file);
		} catch (IOException e) {
			throw new ConfigurationException("failed to load configuration from " + file.getAbsolutePath() 
					+ ": " + e.getMessage(), e);
		} catch (JDOMException e) {
			throw new ConfigurationException("failed to load configuration from " + file.getAbsolutePath() 
					+ ": " + e.getMessage(), e);
		} 
		for (Iterator<ConfigurationItem> iter = configurationList.iterator(); iter.hasNext();){
			ConfigurationItem item = iter.next();
			if (item.isDefault()) defaultLoaded = item;
		}
		pluginXMLPath = workspacePath + defaultLoaded.getClassName() + "/xml/";
	}
	
	public ConfigurationItem getLoadedPlugin() {
		return defaultLoaded;
	}

	public String getPluginPath(){
		return pluginXMLPath;
	}
	
	@Override
	public String toString(){
		String result = "";
		for (Iterator<ConfigurationItem> iter = configurationList.iterator(); iter.hasNext();){
			result += iter.next().toString();
		}
		return result;
	}

	public List<AlgorithmSet> getAlgorithmSetList() {
		return algorithmSetList;
	}
	
	public AlgorithmSet getDefaultAlgorithmSet(){
		for (AlgorithmSet algorithmSet : algorithmSetList) {
			if (algorithmSet.isDefault())
				return algorithmSet;
		}
		for (AlgorithmSet algorithmSet : algorithmSetList) {
			if (algorithmSet.getId().equals(DEFAULT_DRA_PLUGIN))
				return algorithmSet;
		}
		return null;
	}
}
