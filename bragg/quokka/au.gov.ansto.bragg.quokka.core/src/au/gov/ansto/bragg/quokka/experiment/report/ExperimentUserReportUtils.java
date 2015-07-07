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

package au.gov.ansto.bragg.quokka.experiment.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import au.gov.ansto.bragg.quokka.experiment.model.Acquisition;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionEntry;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionSetting;
import au.gov.ansto.bragg.quokka.experiment.model.ControlledAcquisition;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironmentPreset;
import au.gov.ansto.bragg.quokka.experiment.util.SampleType;

import com.thoughtworks.xstream.XStream;

public class ExperimentUserReportUtils {

	private static volatile XStream xStream;

	public static XStream getXStream() {
		if (xStream == null) {
			synchronized (ExperimentUserReportUtils.class) {
				if (xStream == null) {
					xStream = new XStream();
					xStream.alias("report", ExperimentUserReport.class);
					xStream.alias("config", ExperimentConfig.class);
					xStream.alias("sampleEnvironment", SampleEnvironmentEntry.class);
					xStream.alias("sample", SampleResult.class);
					xStream.alias("process", ProcessDetails.class);
					xStream.aliasAttribute(ExperimentConfig.class, "name", "name");
					xStream.aliasAttribute(Measurement.class, "mode", "mode");
					xStream.aliasAttribute(ExperimentConfig.class, "lambda", "lambda");
					xStream.aliasAttribute(ExperimentConfig.class, "l1", "l1");
					xStream.aliasAttribute(ExperimentConfig.class, "l2", "l2");
					xStream.aliasAttribute(ExperimentConfig.class, "transmissionMeasurement", "transmission");
					xStream.aliasAttribute(ExperimentConfig.class, "scatteringMeasurement", "scattering");
					xStream.aliasAttribute(SampleEnvironmentEntry.class, "setting", "setting");
					xStream.aliasAttribute(SampleResult.class, "position", "position");
					xStream.aliasAttribute(SampleResult.class, "name", "name");
					xStream.aliasAttribute(SampleResult.class, "thickness", "thickness");
					xStream.aliasAttribute(SampleResult.class, "type", "type");
					xStream.aliasAttribute(SampleResult.class, "att", "att");
					xStream.aliasAttribute(SampleResult.class, "preset", "preset");
					xStream.aliasAttribute(SampleResult.class, "runId", "runId");
					xStream.addImplicitCollection(ExperimentUserReport.class, "configs");
					xStream.addImplicitCollection(ExperimentUserReport.class, "sampleEnvs");
					xStream.addImplicitCollection(SampleEnvironmentEntry.class, "configs");
					xStream.addImplicitCollection(Measurement.class, "samples");
				}
			}
		}
		return xStream;
	}

	public static ExperimentUserReport createExperimentUserReport(Experiment experiment) {
		return createExperimentUserReport(experiment, null);
	}
	
	public static ExperimentUserReport createExperimentUserReport(Experiment experiment, AcquisitionSetting processRequested) {
		ExperimentUserReport report = new ExperimentUserReport();
		
		report.setTitle(experiment.getTitle());
		report.setName(experiment.getUser().getName());
		report.setEmail(experiment.getUser().getEmail());
		report.setPhone(experiment.getUser().getPhone());
		report.setStartTime(experiment.getStartTime());
		
		if (experiment.isControlledEnvironment()) {
			for(ControlledAcquisition acquisition : experiment.getAcquisitionGroups()) {
				SampleEnvironmentEntry sampleEnv = new SampleEnvironmentEntry();
				report.getSampleEnvs().add(sampleEnv);
				sampleEnv.setSetting(formatEnvironmentSetting(acquisition.getEnvSettings()));
				createExperimentUserReportForAcquisition(experiment,
						processRequested, acquisition, report,
						sampleEnv.getConfigs());
			}
		} else {
			createExperimentUserReportForAcquisition(experiment,
					processRequested, experiment.getNormalAcquisition(),
					report, report.getConfigs());
		}
		return report;
		
	}
	
	private static ExperimentUserReport createExperimentUserReportForAcquisition(
			Experiment experiment, AcquisitionSetting processRequested,
			Acquisition acquisition, ExperimentUserReport report,
			List<ExperimentConfig> configs) {
		for (InstrumentConfig config : experiment.getInstrumentConfigs()) {
			// Creates config node
			ExperimentConfig experimentConfig = new ExperimentConfig();
			experimentConfig.setName(config.getName());
			experimentConfig.setEmptyBeamTransmissionRunId(config.getEmptyBeamTransmissionDataFile());
			experimentConfig.setEmptyCellTransmissionRunId(config.getEmptyCellTransmissionDataFile());
			experimentConfig.setEmptyCellScatteringRunId(config.getEmptyCellScatteringDataFile());
			configs.add(experimentConfig);
			
			// Creates transmission node
			TransmissionMeasurement transMeas = new TransmissionMeasurement();
			transMeas.setMode(config.getTransmissionMode());
			experimentConfig.setTransmissionMeasurement(transMeas);
			
			// Creates scattering node
			ScatteringMeasurement scattMeas = new ScatteringMeasurement();
			scattMeas.setMode(config.getMode());
			experimentConfig.setScatteringMeasurement(scattMeas);
			
			for (AcquisitionEntry entry : acquisition.getEntries()) {
				AcquisitionSetting setting = entry.getConfigSettings().get(config);
				// We assume both l1, l2, lambda values are the same for any sample
				// and any mode (trans or scatt) across the same configuration.
				experimentConfig.setLambda(setting.getTransmissionWavelength());
				experimentConfig.setL1(setting.getTransmissionL1());
				experimentConfig.setL2(setting.getTransmissionL2());
				
				Sample sample = entry.getSample();
				SampleResult sampleTransResult = new SampleResult();
				sampleTransResult.setPosition(sample.getPosition());
				sampleTransResult.setName(sample.getName());
				sampleTransResult.setThickness(sample.getThickness());
				sampleTransResult.setType(sample.getType().toString());
				// Ensure run attribute always exist
				if (setting.getTransmissionDataFile() == null || setting.getTransmissionDataFile().length() == 0) {
					sampleTransResult.setRunId("");
				} else {
					sampleTransResult.setRunId(setting.getTransmissionDataFile());
				}
				sampleTransResult.setAtt(setting.getTransmissionAttenuation());
				sampleTransResult.setPreset(config.getTransmissionPreset());
				transMeas.getSamples().add(sampleTransResult);
				
				SampleResult sampleScattResult = new SampleResult();
				sampleScattResult.setPosition(sample.getPosition());
				sampleScattResult.setName(sample.getName());
				sampleScattResult.setThickness(sample.getThickness());
				sampleScattResult.setType(sample.getType().toString());
				// Ensure run attribute always exist
				if (setting.getScatteringDataFile() == null || setting.getScatteringDataFile().length() == 0) {
					sampleScattResult.setRunId("");
				} else {
					sampleScattResult.setRunId(setting.getScatteringDataFile());
				}
				sampleScattResult.setAtt(setting.getScatteringAttenuation());
				sampleScattResult.setPreset(setting.getPreset());
				scattMeas.getSamples().add(sampleScattResult);
				
				// Mark to process if necessary (check by reference equal)
				if (processRequested == setting) {
					ProcessDetails processDetails = new ProcessDetails();
					processDetails.setToProcess(setting.getScatteringDataFile());
					report.setProcessDetails(processDetails);
				}
			}
		}
		return report;
	}
	
	private static String formatEnvironmentSetting(Map<SampleEnvironment, SampleEnvironmentPreset> envSettings) {
		StringBuilder builder = new StringBuilder();
		Iterator<Entry<SampleEnvironment, SampleEnvironmentPreset>> iterator = envSettings.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<SampleEnvironment, SampleEnvironmentPreset> entry = iterator.next();
			builder.append(entry.getKey().getControllerId());
			builder.append("=");
			builder.append(entry.getValue().getPreset());
			if (iterator.hasNext()) {
				builder.append(",");
			}
		}
		return builder.toString();
	}
	
	public static void exportUserReport(File reportFolder, ExperimentUserReport report) throws IOException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		exportUserReport(reportFolder, report, "QKK_" + format.format(Calendar.getInstance().getTime()) + "_report.xml");
	}
	
	public static void exportUserReport(File reportFolder, ExperimentUserReport report, String filename) throws IOException {
		if (!reportFolder.exists()) {
			reportFolder.mkdir();
		}
		
		// Write report
		File reportFile = new File(reportFolder, filename);
		if (!reportFile.exists()) {
			reportFile.createNewFile();
		}
		FileWriter writer = new FileWriter(reportFile);
		getXStream().toXML(report, writer);
		writer.flush();
		writer.close();
	}
	
	public static String exportAcquisitionTable(Acquisition acquisition, InstrumentConfig config) {
		StringBuilder html = new StringBuilder("<table align='center' " + 
				"border='1' class='xmlTable'>\r\n");
		String envText = "";
		if (acquisition.getExperiment().isControlledEnvironment() && acquisition instanceof ControlledAcquisition) {
			Map<SampleEnvironment, SampleEnvironmentPreset> envSettings = ((ControlledAcquisition) acquisition).getEnvSettings();
			int index = 0;
			for (SampleEnvironment env : envSettings.keySet()) {
				String envId = env.getControllerId();
				float envValue = envSettings.get(env).getPreset();
				envText += (index++ > 0 ? ";" : "") + envId + " = " + envValue;
			}
		}
//		html.append("<tr><th colspan=\"4\"></th>\r\n<th colspan=\"3\">" + config.getName() + "</th></tr>");
//		html.append("<tr><th>Sequence</th>\r\n<th>Position</th>\r\n<th>Sample Name</th>\r\n<th>Thickness</th>" 
		html.append("<tr><th colspan=\"3\">" + envText + "</th>\r\n<th colspan=\"4\">" + config.getName() + "</th></tr>");
		html.append("<tr><th>Position</th>\r\n<th>Sample Name</th>\r\n<th>Thickness</th>"
				+ "<th>Transmission</th><th>Scattering</th><th>Preset</th><th>Comment</th></tr>");
//		int index = 0;
		for (AcquisitionEntry entry : acquisition.getEntries()){
			Sample sample = entry.getSample();
			AcquisitionSetting acSetting = entry.getConfigSettings().get(config);
			
//			html.append("<tr><td>" + (index++) + "</td><td>" + sample.getPosition() + "</td>" 
			html.append("<tr><td>" + sample.getPosition() + "</td>"
					+ "<td>" + sample.getName() + "</td><td>" + sample.getThickness() + "</td>\r\n"
					+ "<td>" + (acSetting.getTransmissionDataFile() != null ? acSetting.getTransmissionDataFile() : "") + "</td>"
					+ "<td>" + (acSetting.getScatteringDataFile() != null ? acSetting.getScatteringDataFile() : "") + "</td>"
					+ "<td>" + acSetting.getPreset() + "</td>\r\n<td></td>\r\n</tr>\r\n");
		}
		if (config.getEmptyBeamTransmissionDataFile() != null) {
			html.append("<tr><th colspan=\"3\">Empty Beam Transmission ID</td><td colspan=\"4\">" 
					+ config.getEmptyBeamTransmissionDataFile() + "</td></tr>");
		}
		if (config.getEmptyCellScatteringDataFile() != null) {
			html.append("<tr><th colspan=\"3\">Empty Cell Scattering ID</td><td colspan=\"4\">" 
					+ config.getEmptyCellScatteringDataFile() + "</td></tr>");
		}
		if (config.getEmptyCellTransmissionDataFile() != null) {
			html.append("<tr><th colspan=\"3\">Empty Cell Transmission ID</td><td colspan=\"4\">" 
					+ config.getEmptyCellTransmissionDataFile() + "</td></tr>");
		}
		if (acquisition.getExperiment().getDarkCurrentFile() != null) {
			html.append("<tr><th colspan=\"3\">Dark Current File Name</td><td colspan=\"4\">" 
					+ acquisition.getExperiment().getDarkCurrentFile() + "</td></tr>");
		}
		html.append("</table>");

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		html.append("<span class=\"class_span_tablefoot\">Scan report created at " 
				+ dateFormat.format(new Date()) + "; ");
		AcquisitionSetting setting = acquisition.getEntries().get(0).getConfigSettings().get(config);
		// We assume both l1, l2, lambda values are the same for any sample
		// and any mode (trans or scatt) across the same configuration.
		html.append("lambda=" + setting.getTransmissionWavelength() + ", ");
		html.append("L1=" + setting.getTransmissionL1() + ", ");
		html.append("L2=" + setting.getTransmissionL2() + " ");
		html.append("</span><br>");
		return html.toString();
	}
	
	public static String createExperimentInfoTable(Experiment experiment) {
		StringBuilder html = new StringBuilder("<table align='center' " + 
				"border='1' class='xmlTable'>\r\n");
		String title = experiment.getTitle();
		String user = experiment.getUser().getName();
		String email = experiment.getUser().getEmail();
		String phone = experiment.getUser().getPhone();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String startTime = dateFormat.format(experiment.getStartTime());
		html.append("<tr><th colspan=\"2\">Multi-Sample Workflow Started</th>\r\n</tr>");
		if (title != null && title.trim().length() > 0) {
			html.append("<tr><th>Title</th>\r\n<td>" + title + "</td>\r\n");
		}
		if (user != null && user.trim().length() > 0) {
			html.append("<tr><th>User</th>\r\n<td>" + user + "</td>\r\n");
		}
		if (email != null && email.trim().length() > 0) {
			html.append("<tr><th>Email</th>\r\n<td>" + email + "</td>\r\n");
		}
		if (phone != null && phone.trim().length() > 0) {
			html.append("<tr><th>Phone</th>\r\n<td>" + phone + "</td>\r\n");
		}
		html.append("<tr><th>Time Started</th>\r\n<td>" + startTime + "</td>\r\n");
		html.append("</table>");
		return html.toString();
	}
	
	private ExperimentUserReportUtils() {
		super();
	}

	public static SampleResult getScatteringForAnalysis(ExperimentUserReport result){
		ProcessDetails processRequire = result.getProcessDetails();
		if (processRequire == null || processRequire.getToProcess() == null || processRequire.getToProcess().trim().length() == 0)
			return null;
		List<ExperimentConfig> configs = result.getConfigs();
		for (ExperimentConfig config : configs){
			ScatteringMeasurement scattering = config.getScatteringMeasurement();
			for (SampleResult sample : scattering.getSamples()){
				if (sample.getType().equals(SampleType.SAMPLE.toString()))
					if (sample.getRunId().equals(processRequire.getToProcess()))
						return sample;
			}
		}
		for (Object objectEntry : result.getSampleEnvs()) {
			if (objectEntry instanceof ExperimentConfig) {
				ScatteringMeasurement scattering = ((ExperimentConfig) 
						objectEntry).getScatteringMeasurement();
				for (SampleResult sample : scattering.getSamples()){
					if (sample.getType().equals(SampleType.SAMPLE.toString()))
						if (sample.getRunId().equals(processRequire.getToProcess()))
							return sample;
				}
			} else if (objectEntry instanceof SampleEnvironmentEntry) {
				List<ExperimentConfig> environmentConfigs = ((SampleEnvironmentEntry) 
						objectEntry).getConfigs();
				if (environmentConfigs != null) {
					for (ExperimentConfig config : environmentConfigs){
						ScatteringMeasurement scattering = config.getScatteringMeasurement();
						for (SampleResult sample : scattering.getSamples()){
							if (sample.getType().equals(SampleType.SAMPLE.toString()))
								if (sample.getRunId().equals(processRequire.getToProcess()))
									return sample;
						}
					}
				}
			}
		}
		return null;
	}
	
	public static List<SampleResult> getAllSampleScatterings(ExperimentUserReport result){
		List<SampleResult> scatteringList = new ArrayList<SampleResult>();
		List<ExperimentConfig> configs = result.getConfigs();
		for (ExperimentConfig config : configs){
			ScatteringMeasurement scattering = config.getScatteringMeasurement();
			for (SampleResult sample : scattering.getSamples()){
				if (sample.getType().equals(SampleType.SAMPLE.toString()))
					scatteringList.add(sample);
			}
		}
//		for (SampleEnvironmentEntry environmentEntry : result.getSampleEnvs()) {
//			List<ExperimentConfig> environmentConfigs = environmentEntry.getConfigs();
//			if (environmentConfigs != null) {
//				for (ExperimentConfig config : environmentConfigs){
//					ScatteringMeasurement scattering = config.getScatteringMeasurement();
//					for (SampleResult sample : scattering.getSamples()){
//						if (sample.getType().equals(SampleType.SAMPLE.toString()))
//							scatteringList.add(sample);
//					}
//				}
//			}
//		}
		for (Object objectEntry : result.getSampleEnvs()) {
			if (objectEntry instanceof ExperimentConfig) {
				ScatteringMeasurement scattering = ((ExperimentConfig) 
						objectEntry).getScatteringMeasurement();
				for (SampleResult sample : scattering.getSamples()){
					if (sample.getType().equals(SampleType.SAMPLE.toString()))
						scatteringList.add(sample);
				}
			} else if (objectEntry instanceof SampleEnvironmentEntry) {
				List<ExperimentConfig> environmentConfigs = ((SampleEnvironmentEntry) 
						objectEntry).getConfigs();
				if (environmentConfigs != null) {
					for (ExperimentConfig config : environmentConfigs){
						ScatteringMeasurement scattering = config.getScatteringMeasurement();
						for (SampleResult sample : scattering.getSamples()){
							if (sample.getType().equals(SampleType.SAMPLE.toString()))
								scatteringList.add(sample);
						}
					}
				}
			}
		}
		return scatteringList;
	}

	public static SampleResult getScatteringAOL(ExperimentUserReport result){
		ProcessDetails processRequire = result.getProcessDetails();
		boolean isProcessRequired = (processRequire != null && processRequire.getToProcess() != null
				&& processRequire.getToProcess().trim().length() > 0);
		SampleResult sampleResult = null;
		for (ExperimentConfig config : result.getConfigs()){
			ScatteringMeasurement scatteringMeasurement = config.getScatteringMeasurement();
			if (scatteringMeasurement == null)
				continue;
			for (SampleResult sample : scatteringMeasurement.getSamples()){
				String runId = sample.getRunId();
				if (runId == null || runId.trim().length() == 0 || !sample.getType().equals(SampleType.SAMPLE.toString()))
					continue;
				if (isProcessRequired && processRequire.getToProcess().equals(runId))
					return sample;
				if (sampleResult == null)
					sampleResult = sample;
				else{
					try {
						if (Double.valueOf(runId) > 
						Double.valueOf(sampleResult.getRunId())){
							sampleResult = sample;
						}
					}catch (Exception e) {
					}	
				}
			}
		}
		for (Object objectEntry : result.getSampleEnvs()) {
			if (objectEntry instanceof ExperimentConfig) {
				ScatteringMeasurement scatteringMeasurement = ((ExperimentConfig) 
						objectEntry).getScatteringMeasurement();
				if (scatteringMeasurement == null)
					continue;
				for (SampleResult sample : scatteringMeasurement.getSamples()){
					String runId = sample.getRunId();
					if (runId == null || runId.trim().length() == 0 || !sample.getType().equals(SampleType.SAMPLE.toString()))
						continue;
					if (isProcessRequired && processRequire.getToProcess().equals(runId))
						return sample;
					if (sampleResult == null)
						sampleResult = sample;
					else{
						try {
							if (Double.valueOf(runId) > 
							Double.valueOf(sampleResult.getRunId())){
								sampleResult = sample;
							}
						}catch (Exception e) {
						}	
					}
				}
			} else if (objectEntry instanceof SampleEnvironmentEntry) {
				List<ExperimentConfig> environmentConfigs = ((SampleEnvironmentEntry) 
						objectEntry).getConfigs();
				if (environmentConfigs != null) {
					for (ExperimentConfig config : environmentConfigs){
						ScatteringMeasurement scatteringMeasurement = config.getScatteringMeasurement();
						if (scatteringMeasurement == null)
							continue;
						for (SampleResult sample : scatteringMeasurement.getSamples()){
							String runId = sample.getRunId();
							if (runId == null || runId.trim().length() == 0 || !sample.getType().equals(SampleType.SAMPLE.toString()))
								continue;
							if (isProcessRequired && processRequire.getToProcess().equals(runId))
								return sample;
							if (sampleResult == null)
								sampleResult = sample;
							else{
								try {
									if (Double.valueOf(runId) > 
									Double.valueOf(sampleResult.getRunId())){
										sampleResult = sample;
									}
								}catch (Exception e) {
								}	
							}
						}
					}
				}
			}
		}
		return sampleResult;
	}
	
	public static ExperimentConfig getExperimentConfig(ExperimentUserReport report, SampleResult sample){
		if (sample == null)
			return null;
		for (ExperimentConfig config : report.getConfigs()){
			ScatteringMeasurement scattering = config.getScatteringMeasurement();
			if (scattering.getSamples().contains(sample))
				return config;
		}
//		for (SampleEnvironmentEntry environmentEntry : report.getSampleEnvs()) {
//			List<ExperimentConfig> environmentConfigs = environmentEntry.getConfigs();
//			if (environmentConfigs != null) {
//				for (ExperimentConfig config : environmentConfigs){
//					ScatteringMeasurement scattering = config.getScatteringMeasurement();
//					if (scattering.getSamples().contains(sample))
//						return config;
//				}
//			}
//		}
		for (Object objectEntry : report.getSampleEnvs()) {
			if (objectEntry instanceof ExperimentConfig) {
				ScatteringMeasurement scattering = ((ExperimentConfig) 
						objectEntry).getScatteringMeasurement();
				if (scattering.getSamples().contains(sample))
					return (ExperimentConfig) objectEntry;
			} else if (objectEntry instanceof SampleEnvironmentEntry) {
				List<ExperimentConfig> environmentConfigs = ((SampleEnvironmentEntry) 
						objectEntry).getConfigs();
				if (environmentConfigs != null) {
					for (ExperimentConfig config : environmentConfigs){
						ScatteringMeasurement scattering = config.getScatteringMeasurement();
						if (scattering.getSamples().contains(sample))
							return config;
					}
				}
			}
		}
		return null;
	}

	public static SampleResult getTransmission(ExperimentConfig config, SampleResult sampleResult, SampleType type){
		if (sampleResult == null)
			return null;
		TransmissionMeasurement transmissionMeasurement = config.getTransmissionMeasurement();
		if (transmissionMeasurement == null)
			return null;
		for (SampleResult sample : transmissionMeasurement.getSamples()){
			if (type == SampleType.SAMPLE && sample.getPosition() == sampleResult.getPosition())
				return sample;
			if (type != SampleType.SAMPLE && sample.getType().equals(type.toString()))
				return sample;
		}
		return null;
	}

	public static SampleResult getSampleScatteringResult(ExperimentUserReport model, String runId){
		if (model == null || runId == null || runId.trim().length() == 0)
			return null;
		for (ExperimentConfig config : model.getConfigs()){
			ScatteringMeasurement scattering = config.getScatteringMeasurement();
			for (SampleResult sample : scattering.getSamples()){
				if (sample.getRunId() != null && sample.getRunId().equals(runId))
					return sample;
			}
		}
		for (Object objectEntry : model.getSampleEnvs()) {
			if (objectEntry instanceof ExperimentConfig) {
				ScatteringMeasurement scattering = ((ExperimentConfig) 
						objectEntry).getScatteringMeasurement();
				for (SampleResult sample : scattering.getSamples()){
					if (sample.getRunId() != null && sample.getRunId().equals(runId))
						return sample;
				}
			} else if (objectEntry instanceof SampleEnvironmentEntry) {
				List<ExperimentConfig> environmentConfigs = ((SampleEnvironmentEntry) 
						objectEntry).getConfigs();
				if (environmentConfigs != null) {
					for (ExperimentConfig config : environmentConfigs){
						ScatteringMeasurement scattering = config.getScatteringMeasurement();
						for (SampleResult sample : scattering.getSamples()){
							if (sample.getRunId() != null && sample.getRunId().equals(runId))
								return sample;
						}
					}
				}
			}
		}
		return null;
	}

	public static SampleResult getScattering(ExperimentConfig config, SampleResult sampleResult, SampleType type){
		if (sampleResult == null)
			return null;
		ScatteringMeasurement scatteringMeasurement = config.getScatteringMeasurement();
		if (scatteringMeasurement == null)
			return null;
		for (SampleResult sample : scatteringMeasurement.getSamples()){
			if (type == SampleType.SAMPLE && sample.getPosition() == sampleResult.getPosition())
				return sample;
			if (type != SampleType.SAMPLE && sample.getType().equals(type.toString()))
				return sample;
		}
		return null;
	}
	
}
