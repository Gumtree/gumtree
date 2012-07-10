/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core;

import java.util.ArrayList;
import java.util.List;

import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager_;
import au.gov.ansto.bragg.cicada.core.Exporter;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.ExportException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;

/**
 * @author dak
 */
public class UIAlgorithmManager {

	
	private static AlgorithmManager algorithmManager;
	private static List<Exporter> exporterList;

	public static AlgorithmManager getAlgorithmManager() {
		if (algorithmManager == null) {
			try {
				algorithmManager = new AlgorithmManager_();
//				algorithmManager.loadConfiguration();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			} catch (LoadAlgorithmFileFailedException e) {
				e.printStackTrace();
			}
		}
		return algorithmManager;
	}
	
	public static List<Exporter> getAvailableExporterList() throws ExportException {
		if (exporterList == null) {
			final String[] exportFormats = getAlgorithmManager().getExportFormat();
			for (String format : exportFormats) {
				getAlgorithmManager().addExporter(format);
			}
			exporterList = getAlgorithmManager().getExporterList();
		}
		return exporterList;
	}
	
	public static List<Exporter> getAvailable1DExporterList() throws ExportException {
		List<Exporter> exporters = getAvailableExporterList();
		List<Exporter> oneDExporters = new ArrayList<Exporter>();
		for (Exporter exporter : exporters){
			if (exporter.is1D())
				oneDExporters.add(exporter);
		}
		return oneDExporters;
	}

	public static List<Exporter> getAvailableMultiDExporterList() throws ExportException {
		List<Exporter> exporters = getAvailableExporterList();
		List<Exporter> oneDExporters = new ArrayList<Exporter>();
		for (Exporter exporter : exporters){
			if (exporter.isMultiD())
				oneDExporters.add(exporter);
		}
		return oneDExporters;
	}

}
