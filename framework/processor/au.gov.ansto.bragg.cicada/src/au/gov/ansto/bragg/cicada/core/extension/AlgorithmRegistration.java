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
package au.gov.ansto.bragg.cicada.core.extension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;

import au.gov.ansto.bragg.cicada.core.CicadaCoreProperties;

public class AlgorithmRegistration {

	private static List<AlgorithmSet> algorithmSetList;
	private static final String DEFAULT_DRA_PLUGIN = "au.gov.ansto.bragg.cicada.dra";
	private static final String DEFAULT_UI_PLUGIN = "au.gov.ansto.bragg.kakadu.ui";
	private static final String OPTION_ALGO_SET = "algoSet";
	
	private static AlgorithmRegistration registration;

	public static AlgorithmRegistration getInstance(){
		if (registration == null) {
			registration = new AlgorithmRegistration();
		}
		return registration;
	}

	protected AlgorithmRegistration(){
		if (algorithmSetList == null){
			algorithmSetList = new ArrayList<AlgorithmSet>();
			AlgorithmSetExtensionReader reader = new AlgorithmSetExtensionReader(this);
			reader.readCicadaConfigurationExtensions();
			applyPreference();
			applyDefault();
		}
	}

	private void applyDefault() {
		// [2011-04-14][TONY] Use the new system property mechanism instead of
		// reading from the command line
		String pluginId = CicadaCoreProperties.AlGORITHM_SET_PLUGIN.getValue();
//		String pluginId = DEFAULT_DRA_PLUGIN;
//		ICommandLineOptions options = ServiceUtils.getService(ICommandLineOptions.class);
//		if (options == null){
//			pluginId = "au.gov.ansto.bragg.kowari.dra";
//		}else if(options.hasOptionValue(OPTION_ALGO_SET)) {
//			pluginId = options.getOptionValue(OPTION_ALGO_SET);
//		}
		for (Iterator<AlgorithmSet> iterator = algorithmSetList.iterator(); iterator.hasNext();) {
			AlgorithmSet algorithmSet = iterator.next();
			if (algorithmSet.getId().equals(pluginId))
				algorithmSet.setDefault(true);
		}

	}

	public void applyDefault(String algorithmSetID) {
		
		for (Iterator<AlgorithmSet> iterator = algorithmSetList.iterator(); iterator.hasNext();) {
			AlgorithmSet algorithmSet = iterator.next();
			if (algorithmSet.getId().equals(algorithmSetID)) {
				algorithmSet.setDefault(true);
			} else {
				algorithmSet.setDefault(false);
			}
		}
	}
	
	// TODO: Refactor this to remove dependency to
	// org.eclipse.core.runtime.compatibility and
	// org.eclipse.update.configurator
	private void applyPreference() {
		Preferences prefs = Platform.getPlugin(DEFAULT_UI_PLUGIN).getPluginPreferences();
		for (Iterator<AlgorithmSet> iterator = algorithmSetList.iterator(); iterator.hasNext();) {
			AlgorithmSet algorithmSet = iterator.next();
			if (prefs.contains(algorithmSet.getName())){
				algorithmSet.setAvailability(prefs.getBoolean(algorithmSet.getName()));
			}
		}
	}

	public void addAlgorithmSet(String id, String name, String version){
		AlgorithmSet algorithmSet = new AlgorithmSet(id, name, version);
		algorithmSetList.add(algorithmSet);
	}

	public void addAlgorithmSet(IConfigurationElement element) {
		AlgorithmSetDescriptor descriptor = new AlgorithmSetDescriptor(element);
		addAlgorithmSet(descriptor.getId(), descriptor.getName(), descriptor.getVersion());
	}

	public List<AlgorithmSet> getAlgorithmSetList() {
		return algorithmSetList;
	}

	public List<String> getAlgorithmSetIDList(){
		List<String> algorithmSetIDList = new ArrayList<String>();
		for (Iterator<AlgorithmSet> iterator = algorithmSetList.iterator(); iterator
		.hasNext();) {
			algorithmSetIDList.add(iterator.next().getId());		
		}
		return algorithmSetIDList;
	}

	public List<String> getAlgorithmSetNameList(){
		List<String> algorithmSetNameList = new ArrayList<String>();
		for (Iterator<AlgorithmSet> iterator = algorithmSetList.iterator(); iterator
		.hasNext();) {
			algorithmSetNameList.add(iterator.next().getName());		
		}
		return algorithmSetNameList;
	}

}
