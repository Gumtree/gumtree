/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.quokka.experiment.result;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.gov.ansto.bragg.quokka.experiment.model.Acquisition;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionEntry;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionSetting;
import au.gov.ansto.bragg.quokka.experiment.model.ControlledAcquisition;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironmentPreset;
import au.gov.ansto.bragg.quokka.experiment.report.ExperimentUserReport;
import au.gov.ansto.bragg.quokka.experiment.util.SampleType;

import com.thoughtworks.xstream.XStream;

public class ExperimentResultUtils {

	private static volatile XStream xStream;
	
	public static XStream getXStream() {
		if (xStream == null) {
			synchronized (ExperimentResultUtils.class) {
				if (xStream == null) {
					xStream = new XStream();
					xStream.alias("result", ExperimentResult.class);
					xStream.alias("sample", SampleResult.class);
					xStream.alias("config", ExperimentConfig.class);
					xStream.alias("transmission", TransmissionMeasurement.class);
					xStream.alias("scattering", ScatteringMeasurement.class);
					xStream.alias("controlledEnvironment", ControlledEnvironment.class);
					xStream.alias("setting", EnvironmentSetting.class);
					xStream.aliasAttribute(SampleResult.class, "position", "position");
					xStream.aliasAttribute(SampleResult.class, "name", "name");
					xStream.aliasAttribute(SampleResult.class, "thickness", "thickness");
					xStream.aliasAttribute(SampleResult.class, "type", "type");
					xStream.aliasAttribute(ExperimentConfig.class, "name", "name");
					xStream.aliasAttribute(ExperimentConfig.class, "lambda", "lambda");
					xStream.aliasAttribute(ExperimentConfig.class, "l1", "l1");
					xStream.aliasAttribute(ExperimentConfig.class, "l2", "l2");
					xStream.aliasAttribute(Measurement.class, "mode", "mode");
					xStream.aliasAttribute(Measurement.class, "preset", "preset");
					xStream.aliasAttribute(Measurement.class, "filename", "run");
					xStream.aliasAttribute(Measurement.class, "att", "att");
					xStream.aliasAttribute(EnvironmentSetting.class, "controllerId", "controller");
					xStream.aliasAttribute(EnvironmentSetting.class, "preset", "preset");
					xStream.autodetectAnnotations(true);
				}
			}
		}
		return xStream;
	}
	
	/**
	 * Export experiment in report form into a specified directory.
	 * 
	 * @param reportFolder
	 * @param experiment
	 */
	public static void exportReport(File reportFolder, ExperimentResult result) throws IOException {
		if (!reportFolder.exists()) {
			reportFolder.mkdir();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		
		// Write report
		File report = new File(reportFolder, "QKK_" + format.format(Calendar.getInstance().getTime()) + "_report.xml");
		if (!report.exists()) {
			report.createNewFile();
		}
		FileWriter writer = new FileWriter(report);
		getXStream().toXML(result, writer);
		writer.flush();
		writer.close();
	}
	
	public static ExperimentResult createExperimentResult(Experiment experiment) {
		return createExperimentResult(experiment, new ArrayList<AcquisitionSetting>(0));
	}
	
	public static ExperimentResult createExperimentResult(Experiment experiment, List<AcquisitionSetting> processRequestedList) {
		ExperimentResult result = new ExperimentResult();
		result.setControlledEnvironment(experiment.isControlledEnvironment());
		
		for (Sample sample : experiment.getSamples()) {
			if (sample.isRunnable()) {
				SampleResult sampleResult = new SampleResult();
				sampleResult.setPosition(sample.getPosition());
				sampleResult.setName(sample.getName());
				sampleResult.setDescription(sample.getDescription());
				sampleResult.setThickness(sample.getThickness());
				sampleResult.setType(sample.getType());
				
				if (experiment.isControlledEnvironment()) {
					for (ControlledAcquisition acquisition : experiment.getAcquisitionGroups()) {
						ControlledEnvironment env = new ControlledEnvironment();
						for (SampleEnvironment sampleEnvironment : experiment.getSampleEnvironments()) {
							EnvironmentSetting setting = new EnvironmentSetting();
							SampleEnvironmentPreset preset = acquisition.getEnvSettings().get(sampleEnvironment);
							setting.setControllerId(sampleEnvironment.getControllerId());
							setting.setPreset(preset.getPreset());
							env.getEnvironments().add(setting);
						}
						sampleResult.getControlledEnvs().add(env);
						configExperimentResult(acquisition, sample, experiment.getInstrumentConfigs(), env.getConfigs(), processRequestedList);
					}
				} else {
					Acquisition acquisition = experiment.getNormalAcquisition();
					configExperimentResult(acquisition, sample, experiment.getInstrumentConfigs(), sampleResult.getConfigs(), processRequestedList);
				}
				
				result.getSampleResults().add(sampleResult);
			}
		}
		
		return result;
	}

	private static void configExperimentResult(Acquisition acquisition, Sample sample, List<InstrumentConfig> configs,
			List<ExperimentConfig> experimentConfigs, List<AcquisitionSetting> processRequestedList) {
		
		Map<InstrumentConfig, ExperimentConfig> configLookup = new HashMap<InstrumentConfig, ExperimentConfig>();
		Map<SampleType, AcquisitionEntry> specialEntryLookup = new HashMap<SampleType, AcquisitionEntry>();
		
		for (AcquisitionEntry entry : acquisition.getEntries()) {
			if (entry.getSample().equals(sample)) {
				
				for (InstrumentConfig config : configs) {
					ExperimentConfig experimentConfig = configLookup.get(config);
					if (experimentConfig == null) {
						experimentConfig = new ExperimentConfig();
						experimentConfig.setName(config.getName());
						configLookup.put(config, experimentConfig);
						experimentConfigs.add(experimentConfig);
					}
					AcquisitionSetting setting = entry.getConfigSettings().get(config);
					
					TransmissionMeasurement transmissionMeasurement = new TransmissionMeasurement();
					transmissionMeasurement.setMode(config.getTransmissionMode());
					transmissionMeasurement.setPreset(config.getTransmissionPreset());
					// Ensure run attribute always exist
					if (setting.getTransmissionDataFile() == null || setting.getTransmissionDataFile().length() == 0) {
						transmissionMeasurement.setFilename("");
					} else {
						transmissionMeasurement.setFilename(setting.getTransmissionDataFile());
					}
					transmissionMeasurement.setAtt(setting.getTransmissionAttenuation());
					experimentConfig.getTransmissionMeasurements().add(transmissionMeasurement);
					
					ScatteringMeasurement scatteringMeasurement = new ScatteringMeasurement();
					scatteringMeasurement.setMode(config.getMode());
					scatteringMeasurement.setPreset(setting.getPreset());
					// Ensure run attribute always exist
					if (setting.getScatteringDataFile() == null || setting.getScatteringDataFile().length() == 0) {
						scatteringMeasurement.setFilename("");
					} else {
						scatteringMeasurement.setFilename(setting.getScatteringDataFile());
					}
					scatteringMeasurement.setAtt(setting.getScatteringAttenuation());
					// Mark as process requested based on the supplied arguments
					if (processRequestedList.contains(setting)) {
						scatteringMeasurement.setProcessRequested(true);
					}
					
					experimentConfig.getScatteringMeasurements().add(scatteringMeasurement);
					
					// Update config details (assume these numbers are same for all setting
					// within this config group, so we only keep the last transmission one)
					experimentConfig.setLambda(setting.getTransmissionWavelength());
					experimentConfig.setL1(setting.getTransmissionL1());
					experimentConfig.setL2(setting.getTransmissionL2());
					
					if (!sample.getType().equals(SampleType.SAMPLE)) {
						// Skip
						continue;
					}
					
					// Background
					if (!specialEntryLookup.containsKey(SampleType.DARK_CURRENT)) {
						// Find background entry
						AcquisitionEntry firstBackgroundEntry = findFirstSpecialEntry(acquisition, SampleType.DARK_CURRENT);
						specialEntryLookup.put(SampleType.DARK_CURRENT, firstBackgroundEntry);
					}
					AcquisitionEntry firstBackgroundEntry = specialEntryLookup.get(SampleType.DARK_CURRENT);
					if (firstBackgroundEntry != null) {
						AcquisitionSetting backgroundSetting = firstBackgroundEntry.getConfigSettings().get(config);
						experimentConfig.setBackgroundTransmission(backgroundSetting.getTransmissionDataFile());
						experimentConfig.setBackgroundScattering(backgroundSetting.getScatteringDataFile());
					}
					
					// Emtpy cell
					if (!specialEntryLookup.containsKey(SampleType.EMPTY_CELL)) {
						// Find background entry
						AcquisitionEntry firstEmptyCellEntry = findFirstSpecialEntry(acquisition, SampleType.EMPTY_CELL);
						specialEntryLookup.put(SampleType.EMPTY_CELL, firstEmptyCellEntry);
					}
					AcquisitionEntry firstEmptyCellEntry = specialEntryLookup.get(SampleType.EMPTY_CELL);
					if (firstEmptyCellEntry != null) {
						AcquisitionSetting emptyCellSetting = firstEmptyCellEntry.getConfigSettings().get(config);
						experimentConfig.setEmptyCellTransmission(emptyCellSetting.getTransmissionDataFile());
						experimentConfig.setEmptyCellScattering(emptyCellSetting.getScatteringDataFile());
					}
					
					// Emtpy beam
					if (!specialEntryLookup.containsKey(SampleType.EMPTY_BEAM)) {
						// Find background entry
						AcquisitionEntry firstEmptyBeamEntry = findFirstSpecialEntry(acquisition, SampleType.EMPTY_BEAM);
						specialEntryLookup.put(SampleType.EMPTY_BEAM, firstEmptyBeamEntry);
					}
					AcquisitionEntry firstEmptyBeamEntry = specialEntryLookup.get(SampleType.EMPTY_BEAM);
					if (firstEmptyBeamEntry != null) {
						AcquisitionSetting emptyBeamSetting = firstEmptyBeamEntry.getConfigSettings().get(config);
						experimentConfig.setEmptyBeamTransmission(emptyBeamSetting.getTransmissionDataFile());
						experimentConfig.setEmptyBeamScattering(emptyBeamSetting.getScatteringDataFile());
					}
				}
			}
		}
	}
	
	private static AcquisitionEntry findFirstSpecialEntry(Acquisition acquisition, SampleType type) {
		for (AcquisitionEntry entry : acquisition.getEntries()) {
			if (entry.getSample().getType().equals(type)) {
				return entry;
			}
		}
		return null;
	}
	
	private ExperimentResultUtils() {
		super();
	}
	
	public static ScatteringMeasurement getScatteringForAnalysis(ExperimentResult result){
		List<SampleResult> samples = result.getSampleResults();
		for (SampleResult sample : samples){
			for (ExperimentConfig config : sample.getConfigs()){
				for (ScatteringMeasurement scatteringMeasurement : config.getScatteringMeasurements()){
					if (scatteringMeasurement.isProcessRequested())
						return scatteringMeasurement;
				}
			}
		}
		return null;
	}
	
	public static ScatteringMeasurement getScatteringAOL(ExperimentResult result){
		List<SampleResult> samples = result.getSampleResults();
		ScatteringMeasurement measurement = null;
		for (SampleResult sample : samples){
			for (ExperimentConfig config : sample.getConfigs()){
				for (ScatteringMeasurement scatteringMeasurement : config.getScatteringMeasurements()){
					if (scatteringMeasurement.isProcessRequested())
						return scatteringMeasurement;
					else {
						if (measurement == null)
							measurement = scatteringMeasurement;
						else{
							try {
								if (Double.valueOf(scatteringMeasurement.getFilename()) > 
									Double.valueOf(measurement.getFilename())){
									measurement = scatteringMeasurement;
								}
							}catch (Exception e) {
								// TODO: handle exception
							}							
						}
					}
				}
			}
		}
		return measurement;
	}
	
	public static SampleResult getSampleResult(ExperimentResult experimentResult, ScatteringMeasurement measurement){
		for (SampleResult sample : experimentResult.getSampleResults()){
			for (ExperimentConfig config : sample.getConfigs()){
				if (config.getScatteringMeasurements().contains(measurement))
					return sample;
			}
		}
		return null;
	}

	public static ExperimentConfig getSampleResult(SampleResult sample,
			ScatteringMeasurement measurement) {
		for (ExperimentConfig config : sample.getConfigs()){
			if (config.getScatteringMeasurements().contains(measurement))
				return config;
		}
		return null;
	}

}
