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

package au.gov.ansto.bragg.quokka.core.tests;

import au.gov.ansto.bragg.quokka.experiment.model.Acquisition;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionEntry;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionSetting;
import au.gov.ansto.bragg.quokka.experiment.model.ControlledAcquisition;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.User;
import au.gov.ansto.bragg.quokka.experiment.util.SampleType;

public class ExperimentModelFactory {

	public static Experiment createSimpleNormalExperiment() {
		/*********************************************************************
		 * Prepares experiment model
		 *********************************************************************/
		Experiment experiment = new Experiment();
		experiment.setControlledAcquisition(false);
		experiment.setTitle("Experiment");
		
		User user = new User();
		user.setName("User");
		experiment.setUser(user);
		
		// Samples (2 running samples + MT cell + MT beam)
		Sample sample1 = new Sample();
		sample1.setName("Sample 1");
		sample1.setPosition(1);
		sample1.setThickness(1.0f);
		sample1.setDescription("First Sample");
		sample1.setRunnable(true);
		experiment.getSamples().add(sample1);
		
		Sample sample2 = new Sample();
		sample2.setRunnable(false);
		experiment.getSamples().add(sample2);
		
		Sample sample3 = new Sample();
		sample3.setName("Sample 3");
		sample3.setPosition(3);
		sample3.setThickness(2.0f);
		sample3.setDescription("Third Sample");
		sample3.setRunnable(true);
		experiment.getSamples().add(sample3);
		
		Sample emptyCell = new Sample();
		emptyCell.setType(SampleType.EMPTY_CELL);
		emptyCell.setName("MT Cell");
		emptyCell.setPosition(19);
		emptyCell.setDescription("Empty cell");
		emptyCell.setRunnable(true);
		experiment.getSamples().add(emptyCell);
		
		Sample emptyBeam = new Sample();
		emptyBeam.setType(SampleType.EMPTY_BEAM);
		emptyBeam.setName("MT Beam");
		emptyBeam.setPosition(20);
		emptyBeam.setDescription("Empty beam");
		emptyBeam.setRunnable(true);
		experiment.getSamples().add(emptyBeam);
		
		// Configs
		InstrumentConfig config1 = new InstrumentConfig();
		config1.setName("High Q");
		config1.setMode("Timer");
		experiment.getInstrumentConfigs().add(config1);

		InstrumentConfig config2 = new InstrumentConfig();
		config2.setName("Low Q");
		config2.setMode("Monitor");
		experiment.getInstrumentConfigs().add(config2);
		
		// Mock result files
		int fileCounter = 1;
		Acquisition acquisition = experiment.getNormalAcquisition();
		// Config 1 transmission
		for (AcquisitionEntry entry : acquisition.getEntries()) {
			AcquisitionSetting setting = entry.getConfigSettings().get(config1);
			setting.setTransmissionWavelength(5.0f);
			setting.setTransmissionDataFile(fileCounter++ + "");
		}
		// Config 1 scattering
		for (AcquisitionEntry entry : acquisition.getEntries()) {
			AcquisitionSetting setting = entry.getConfigSettings().get(config1);
			setting.setScatteringWavelength(5.0f);
			setting.setScatteringDataFile(fileCounter++ + "");
		}
		// Config 2 transmission
		for (AcquisitionEntry entry : acquisition.getEntries()) {
			AcquisitionSetting setting = entry.getConfigSettings().get(config2);
			setting.setTransmissionWavelength(5.0f);
			setting.setTransmissionDataFile(fileCounter++ + "");
		}
		// Config 2 scattering
		for (AcquisitionEntry entry : acquisition.getEntries()) {
			AcquisitionSetting setting = entry.getConfigSettings().get(config2);
			setting.setScatteringWavelength(5.0f);
			setting.setScatteringDataFile(fileCounter++ + "");
		}
		return experiment;
	}
	
	public static Experiment createSimpleControlledExperiment() {
		/*********************************************************************
		 * Prepares experiment model
		 *********************************************************************/
		Experiment experiment = new Experiment();
		experiment.setControlledAcquisition(true);
		experiment.setTitle("Experiment");
		
		User user = new User();
		user.setName("User");
		experiment.setUser(user);
		
		// Samples (2 running samples + MT cell + MT beam)
		Sample sample1 = new Sample();
		sample1.setName("Sample 1");
		sample1.setPosition(1);
		sample1.setThickness(1.0f);
		sample1.setDescription("First Sample");
		sample1.setRunnable(true);
		experiment.getSamples().add(sample1);
		
		Sample sample2 = new Sample();
		sample2.setRunnable(false);
		experiment.getSamples().add(sample2);
		
		Sample sample3 = new Sample();
		sample3.setName("Sample 3");
		sample3.setPosition(3);
		sample3.setThickness(2.0f);
		sample3.setDescription("Third Sample");
		sample3.setRunnable(true);
		experiment.getSamples().add(sample3);
		
		Sample emptyCell = new Sample();
		emptyCell.setType(SampleType.EMPTY_CELL);
		emptyCell.setName("MT Cell");
		emptyCell.setPosition(19);
		emptyCell.setDescription("Empty cell");
		emptyCell.setRunnable(true);
		experiment.getSamples().add(emptyCell);
		
		Sample emptyBeam = new Sample();
		emptyBeam.setType(SampleType.EMPTY_BEAM);
		emptyBeam.setName("MT Beam");
		emptyBeam.setPosition(20);
		emptyBeam.setDescription("Empty beam");
		emptyBeam.setRunnable(true);
		experiment.getSamples().add(emptyBeam);
		
		// Configs
		InstrumentConfig config1 = new InstrumentConfig();
		config1.setName("High Q");
		config1.setMode("Timer");
		experiment.getInstrumentConfigs().add(config1);

		InstrumentConfig config2 = new InstrumentConfig();
		config2.setName("Low Q");
		config2.setMode("Monitor");
		experiment.getInstrumentConfigs().add(config2);
		
		// Sample environments
		SampleEnvironment env1 = new SampleEnvironment(experiment);
		// Must add this to experiment model before updating the model
		experiment.getSampleEnvironments().add(env1);
		env1.setControllerId("tc1");
		// Must use this helper function to update the experiment model
		env1.resetPresets(100, 200, 2, 10);
		
		SampleEnvironment env2 = new SampleEnvironment(experiment);
		experiment.getSampleEnvironments().add(env2);
		env2.setControllerId("mc1");
		env2.resetPresets(0.7f, 0.9f, 2, 10);
		
		// Mock result files
		int fileCounter = 1;
		for (ControlledAcquisition acquisition : experiment.getAcquisitionGroups()) {
			// Config 1 transmission
			for (AcquisitionEntry entry : acquisition.getEntries()) {
				AcquisitionSetting setting = entry.getConfigSettings().get(config1);
				setting.setTransmissionWavelength(5.0f);
				setting.setTransmissionDataFile(fileCounter++ + "");
			}
			// Config 1 scattering
			for (AcquisitionEntry entry : acquisition.getEntries()) {
				AcquisitionSetting setting = entry.getConfigSettings().get(config1);
				setting.setScatteringWavelength(5.0f);
				setting.setScatteringDataFile(fileCounter++ + "");
			}
			// Config 2 transmission
			for (AcquisitionEntry entry : acquisition.getEntries()) {
				AcquisitionSetting setting = entry.getConfigSettings().get(config2);
				setting.setTransmissionWavelength(5.0f);
				setting.setTransmissionDataFile(fileCounter++ + "");
			}
			// Config 2 scattering
			for (AcquisitionEntry entry : acquisition.getEntries()) {
				AcquisitionSetting setting = entry.getConfigSettings().get(config2);
				setting.setScatteringWavelength(5.0f);
				setting.setScatteringDataFile(fileCounter++ + "");
			}
		}
		
		return experiment;
	}
	
	private ExperimentModelFactory() {
		super();
	}
	
}
