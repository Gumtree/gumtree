package au.gov.ansto.bragg.quokka.experiment.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import au.gov.ansto.bragg.quokka.experiment.model.Acquisition;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionEntry;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionSetting;
import au.gov.ansto.bragg.quokka.experiment.model.ControlledAcquisition;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironmentPreset;
import au.gov.ansto.bragg.quokka.experiment.model.ScanMode;
import au.gov.ansto.bragg.quokka.experiment.model.User;

public final class ExperimentScriptGenerator {

	// Script generation only
	public static String generate(Experiment experiment) {
		return generate(experiment, null);
	}
	
	// Generate and configure for runtime
	public static String generate(Experiment experiment, ExperimentStateManager stateManager) {
		ScriptStringBuilder builder = new ScriptStringBuilder();
		
		// Generate header
		generateHeader(builder);
		
		// Generate house keeping routines
		generateHouseKeepingRoutines(builder, experiment);
		
		// Generate instrument configuration routines
		generateConfigurationRoutines(builder, experiment);
		
		// Generate settings
		generateSettings(builder, experiment, stateManager);
		
		// Generate experiment logic
		generateExperimentLogic(builder, experiment);
		
		return builder.toString();
	}
	
	private static void generateHeader(ScriptStringBuilder builder) {
		builder.appendLine("from gumpy.commons import sics");
		builder.appendLine("from gumpy.commons import logger");
		builder.appendLine("from bragg.quokka import workflow");
		builder.appendLine("from bragg.quokka.quokka import *");
		builder.appendLine("from time import sleep");
		builder.appendLine("import logging, sys, traceback");
		builder.appendLine("log = logger.log");
		builder.appendEmptyLine();
	}
	
	private static void generateHouseKeepingRoutines(ScriptStringBuilder builder, Experiment experiment) {
		builder.appendBlockComment("House keeping routines");
		builder.appendEmptyLine();
		
		// Setup
		builder.appendComment("Setup experiment");
		builder.appendLine("def setup():");
		{
			int indentLevel = 1;
			
			// User
			User user = experiment.getUser();
			builder.appendComment("User details", indentLevel);
			builder.appendLine("print", indentLevel);
			builder.appendLine("log('Setting user details')", indentLevel);
			builder.appendLine("sics.set('user', '" + user.getName() + "')", indentLevel);
			builder.appendLine("sics.set('email', '" + user.getEmail() + "')", indentLevel);
			builder.appendLine("sics.set('phone', '" + user.getPhone() + "')", indentLevel);
			
			// Experiment
			builder.appendComment("Experiment details", indentLevel);
			builder.appendLine("print", indentLevel);
			builder.appendLine("log('Setting experiment details')", indentLevel);
			builder.appendLine("sics.set('title', '" + experiment.getTitle() + "')", indentLevel);
		}
		builder.appendEmptyLine();
		
		// Cleanup
		// Setup
		builder.appendComment("Clean up");
		builder.appendLine("def cleanUp():");
		{
			int indentLevel = 1;
			
			builder.appendLine("print", indentLevel);
			builder.appendLine("log('Clean up')", indentLevel);
			builder.appendLine("try:", indentLevel);
			builder.appendLine("sleep(1)", indentLevel + 1);
			builder.appendLine("driveAtt(330)", indentLevel + 1);
			builder.appendLine("if workflow.isDriveSampleStage():", indentLevel + 1);
			builder.appendLine("driveToLoadPosition()", indentLevel + 2);
			builder.appendLine("except SicsExecutionException :", indentLevel);
			builder.appendLine("traceback.print_exc(file=__context__.errorWriter)", indentLevel + 1);
			builder.appendLine("logger.__global_writer__ = None", indentLevel + 1);
			builder.appendLine("raise SicsExecutionException, 'SICS Interrupted'", indentLevel + 1);
			builder.appendLine("except:", indentLevel);
			builder.appendLine("traceback.print_exc(file=__context__.errorWriter)", indentLevel + 1);
			builder.appendLine("logger.__global_writer__ = None", indentLevel + 1);
			builder.appendLine("print", indentLevel);
			builder.appendLine("log('Experiment has been completed')", indentLevel);
		            
		}
		builder.appendEmptyLine();
	}
	
	private static void generateConfigurationRoutines(ScriptStringBuilder builder, Experiment experiment) {
		for (int i = 0; i < experiment.getInstrumentConfigs().size(); i++) {
			InstrumentConfig config = experiment.getInstrumentConfigs().get(i);
			builder.appendBlockComment(config.getName() + " config routines");
			builder.appendEmptyLine();
			
			// Initial script
			builder.appendComment("Initial rountine for " + config.getName() + " config");
			builder.appendLine("def driveToConfig" + i + "():");
			
			for (String line : config.getInitScript().split("\n")) {
				builder.appendLine(line.trim(), 1);
			}
			// Add this to avoid empty content inside the method
			builder.appendLine("pass", 1);
			builder.appendEmptyLine();
			
			// Pre-transmission script
			builder.appendComment("Pre-transmission rountine for " + config.getName() + " config");
			builder.appendLine("def driveToConfig" + i + "Transmission():");
			for (String line : config.getPreTransmissionScript().split("\n")) {
				builder.appendLine(line.trim(), 1);
			}
			builder.appendLine("pass", 1);
			builder.appendEmptyLine();
			
			// Pre-scattering script
			builder.appendComment("Pre-scattering rountine for " + config.getName() + " config");
			builder.appendLine("def driveToConfig" + i + "Scattering():");
			for (String line : config.getPreScatteringScript().split("\n")) {
				builder.appendLine(line.trim(), 1);
			}
			builder.appendLine("pass", 1);
			builder.appendEmptyLine();
		}
	}
	
	private static void generateSettings(ScriptStringBuilder builder, Experiment experiment, ExperimentStateManager stateManager) {
		builder.appendBlockComment("Settings");
		builder.appendEmptyLine();
		
		// Instrument config settings
		generateInstrumentConfigSettings(builder, experiment);
		
		// Sample settings
		generateSampleSettings(builder, experiment);
		
		// Sample environment settings
		if (experiment.isControlledEnvironment()) {
			generateSampleEnvironmentSettings(builder, experiment);
		}
		
		// Acquisition dictionary
		generateAcqusitionEntries(builder, experiment, stateManager);
	}
	
	private static void generateInstrumentConfigSettings(ScriptStringBuilder builder, Experiment experiment) {
		builder.appendComment("Instrument configuration dictionary");
		builder.appendLine("configs = {");
		for (int i = 0; i < experiment.getInstrumentConfigs().size(); i++) {
			InstrumentConfig config = experiment.getInstrumentConfigs().get(i);
			builder.appendLine("'" + i + "-" + config.getName() + "':{", 1);
			{
				builder.appendLine("'name':'" + config.getName() + "',", 2);
				builder.appendLine("'mainRoutine':driveToConfig" + i + ",", 2);
				if (config.isUseManualAttenuationAlgorithm()) {
					builder.appendLine("'manualAttenuationAlgorithm':True,", 2);
				} else {
					builder.appendLine("'manualAttenuationAlgorithm':False,", 2);
				}
				builder.appendLine("'startingAttenuation':" + config.getStartingAttenuation() + ",", 2);
				builder.appendLine("'transmissionRoutine':driveToConfig" + i + "Transmission,", 2);
				
				if (config.getTransmissionMode()        == ScanMode.TIME) {
					builder.appendLine("'transmissionMode':scanMode.time,", 2);	
				} else if (config.getTransmissionMode() == ScanMode.COUNTS) {
					builder.appendLine("'transmissionMode':scanMode.count,", 2);	
				} else if (config.getTransmissionMode() == ScanMode.BM1) {
					builder.appendLine("'transmissionMode':scanMode.monitor,", 2);
				}
				builder.appendLine("'transmissionPreset':" + config.getTransmissionPreset() + ",", 2);
				builder.appendLine("'scatteringRoutine':driveToConfig" + i + "Scattering,", 2);
				
				if (config.getMode()                  == ScanMode.TIME) {
					builder.appendLine("'scatteringMode':scanMode.time,", 2);	
				} else if (config.getMode()           == ScanMode.COUNTS) {
					builder.appendLine("'scatteringMode':scanMode.count,", 2);	
				} else if (config.getMode()           == ScanMode.BM1) {
					builder.appendLine("'scatteringMode':scanMode.monitor,", 2);
				}
			}
			builder.appendLine("},", 1);
		}
		builder.appendLine("}");
		builder.appendEmptyLine();
	}
	
	private static void generateSampleSettings(ScriptStringBuilder builder, Experiment experiment) {
		builder.appendComment("Sample dictionary");
		builder.appendLine("samples = {");
		for (Sample sample : experiment.getSamples()) {
			if (!sample.isRunnable()) {
				continue;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(sample.getPosition());
			sb.append(":{");
			sb.append("'position':");
			sb.append(sample.getPosition());
			sb.append(", ");
			sb.append("'name':'");
			if (sample.getName() == "") {
				sb.append("UNKNOWN");
			} else {
				sb.append(sample.getName());
			}
			sb.append("', ");
			sb.append("'type':'");
			if (sample.getType() == null) {
				sb.append("UNKNOWN");
			} else {
				sb.append(sample.getType().toString());
			}
			sb.append("', ");
			sb.append("'description':'");
			if (sample.getDescription() == "") {
				sb.append("UNKNOWN");
			} else {
				sb.append(sample.getDescription());
			}
			
			sb.append("', ");
			sb.append("'thickness':'");
			sb.append(sample.getThickness());
			sb.append("'},");
			builder.appendLine(sb.toString(), 1);
		}
		builder.appendLine("}");
		builder.appendEmptyLine();
	}
	
	private static void generateSampleEnvironmentSettings(ScriptStringBuilder builder, Experiment experiment) {
		builder.appendComment("Sample environment dictionary");
		builder.appendLine("sampleEnvironments = {");
		{
			for (int i = 0; i < experiment.getSampleEnvironments().size(); i++) {
				SampleEnvironment env = experiment.getSampleEnvironments().get(i);
				builder.appendComment("Sample environment " + env.getControllerId(), 1);
				builder.appendLine("'" + i + "-" + env.getControllerId() + "':{", 1);
				{
					// Writes controller id
					builder.appendLine("'controller':'" + env.getControllerId() + "',", 2);
					for (int j = 0; j < env.getPresets().size(); j++) {
						SampleEnvironmentPreset preset = env.getPresets().get(j);
						// Writes preset and wait time
						builder.appendLine(j + ":{'preset':" + preset.getPreset() + ", 'wait':" +  preset.getWaitTime() + "},", 2);
					}
				}
				builder.appendLine("},", 1);
			}
		}
		builder.appendLine("}");
		builder.appendEmptyLine();
	}
	
	private static void generateAcqusitionEntries(ScriptStringBuilder builder, Experiment experiment, ExperimentStateManager stateManager) {
		builder.appendComment("Acquisition dictionary");
		// Prepares tuple
		builder.appendLine("acqusitionEntries = (");
		if (experiment.isControlledEnvironment()) {
			generateSampleEnvEntries(builder, experiment, new LinkedList<ControlledAcquisition>(experiment.getAcquisitionGroups()), 0, 1, 0, stateManager);
			
		} else {
			generateConfigEntries(builder, experiment, experiment.getNormalAcquisition(), 0, 1, stateManager);
		}
		builder.appendLine(")");
		builder.appendEmptyLine();
	}
	
	private static int generateSampleEnvEntries(ScriptStringBuilder builder, Experiment experiment, Queue<ControlledAcquisition> acqGroup, int runId, int baseIndentLevel, int sampleEnvIndex, ExperimentStateManager stateManager) {
		if (sampleEnvIndex == experiment.getSampleEnvironments().size()) {
			ControlledAcquisition acquisition = acqGroup.poll();
			return generateConfigEntries(builder, experiment, acquisition, runId, baseIndentLevel, stateManager);
		}
		SampleEnvironment env = experiment.getSampleEnvironments().get(sampleEnvIndex);
		for (int presetIndex = 0; presetIndex < env.getPresets().size(); presetIndex++) {
			builder.appendLine("{", baseIndentLevel);
			StringBuilder sb = new StringBuilder();
			sb.append("'type':'sampleEnv', 'target':'");
			sb.append(sampleEnvIndex + "-" + env.getControllerId() + "', ");
			sb.append("'setting':" + presetIndex + ", ");
			sb.append("'contents':(");
			// Writes metadata
			builder.appendLine(sb.toString(), baseIndentLevel + 1);
			// Recurrsively generate entries 
			runId = generateSampleEnvEntries(builder, experiment, acqGroup, runId, baseIndentLevel + 2, sampleEnvIndex + 1, stateManager);
			builder.appendLine(")", baseIndentLevel + 1);
			builder.appendLine("},", baseIndentLevel);
		}
		return runId;
	}
	
	private static int generateConfigEntries(ScriptStringBuilder builder, Experiment experiment, Acquisition acquisition, int runId, int baseIndentLevel, ExperimentStateManager stateManager) {
		for (int configIndex = 0; configIndex < experiment.getInstrumentConfigs().size(); configIndex++) {
			InstrumentConfig config = experiment.getInstrumentConfigs().get(configIndex);
			{
				// Prepares config entries
				builder.appendLine("{", baseIndentLevel);
				{
					StringBuilder sb = new StringBuilder();
					sb.append("'type':'config', ");
					sb.append("'target':'" + configIndex + "-" + config.getName() + "', ");
					// Prepares sequence list
					sb.append("'contents':(");
					builder.appendLine(sb.toString(), baseIndentLevel + 1);
					List<AcquisitionEntry> acquisitionEntries = acquisition.getEntries();
					for (int seqIndex = 0; seqIndex < acquisitionEntries.size(); seqIndex++) {
						AcquisitionEntry entry = acquisitionEntries.get(seqIndex);
						Sample sample = entry.getSample();
						AcquisitionSetting setting = entry.getConfigSettings().get(config);
						sb = new StringBuilder();
						sb.append("{'seq':");
						sb.append(seqIndex);
						sb.append(", 'sample':");
						sb.append(sample.getPosition());
						sb.append(", 'runId':");
						sb.append(runId);
						sb.append(", 'preset':");
						sb.append(setting.getPreset());
						sb.append("},");
						builder.appendLine(sb.toString(), baseIndentLevel + 2);
						// Register for state tracking
						if (stateManager != null) {
							stateManager.registerSetting(runId, setting);
							stateManager.registerAcquisition(runId, acquisition);
							stateManager.registerSample(runId, sample);
						}
						runId++;
					}
					builder.appendLine(")", baseIndentLevel + 1);
				}
				builder.appendLine("},", baseIndentLevel);
			}
		}
		return runId;
	}
	
	private static void generateExperimentLogic(ScriptStringBuilder builder, Experiment experiment) {
		builder.appendBlockComment("Experiment logic");
		builder.appendEmptyLine();
		builder.appendLine("if __name__ == '__main__':");
		{
			int indentLevel = 1;
			
			// Set up
			builder.appendEmptyLine(indentLevel);
			builder.appendLine("logger.__global_writer__ = __context__.writer", indentLevel);
			builder.appendComment("Set up", indentLevel);
			builder.appendLine("setup()", indentLevel);
			builder.appendEmptyLine(indentLevel);
			
			// Set settings
			builder.appendComment("Set global settings", indentLevel);
			builder.appendLine("workflow.configs = configs", indentLevel);
			builder.appendLine("workflow.samples = samples", indentLevel);
			if (experiment.isControlledEnvironment()) {
				builder.appendLine("workflow.sampleEnvironments = sampleEnvironments", indentLevel);
			}
			builder.appendLine("workflow.engineContext = __context__", indentLevel);
			builder.appendEmptyLine(indentLevel);
			
			// Loop
			builder.appendComment("Start looping through configurations", indentLevel);
			builder.appendLine("try:", indentLevel);
			{
				builder.appendLine("workflow.startAcquistion()", indentLevel + 1);
				builder.appendLine("workflow.runQuokkaScan(acqusitionEntries)", indentLevel + 1);
			}
			builder.appendLine("except:", indentLevel);
			{
				builder.appendLine("logging.error('Exception occured. It will now go to clean up mode.')", indentLevel + 1);
				builder.appendLine("log('Exception occured. It will now go to clean up mode.')", indentLevel + 1);
				// [GUMTREE-615] save intermediate result
				builder.appendComment("Save result", indentLevel + 1);
				builder.appendLine("log('Saving intermediate result')", indentLevel + 1);
				builder.appendLine("sics.execute('newfile HISTOGRAM_XY')", indentLevel + 1);
				builder.appendLine("sics.execute('save')", indentLevel + 1);
				builder.appendLine("traceback.print_exc(file=__context__.errorWriter)", indentLevel + 1);
				builder.appendLine("log(str(sys.exc_info()))", indentLevel + 1);
			}
			builder.appendEmptyLine(indentLevel);
			
			// Clean up
			builder.appendComment("Clean up", indentLevel);
			builder.appendLine("cleanUp()", indentLevel);
			builder.appendLine("logger.__global_writer__ = None", indentLevel);
		}
		builder.appendEmptyLine();
	}
	
	private ExperimentScriptGenerator() {
		super();
	}

	
}
