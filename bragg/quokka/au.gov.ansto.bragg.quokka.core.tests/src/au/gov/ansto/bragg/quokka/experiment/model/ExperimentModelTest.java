/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.experiment.model;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import au.gov.ansto.bragg.quokka.experiment.model.AcquisitionEntry;
import au.gov.ansto.bragg.quokka.experiment.model.ControlledAcquisition;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironmentPreset;
import au.gov.ansto.bragg.quokka.experiment.model.User;
import au.gov.ansto.bragg.quokka.experiment.util.ExperimentModelUtils;

public class ExperimentModelTest extends TestCase {

	public void testSimpleSerialisation() {
		Experiment experiment = new Experiment();
		ExperimentModelUtils.getXStream().toXML(experiment, System.out);
	}
	
	public void testSerialisation() {
		// Experiment
		Experiment experiment = new Experiment();
		experiment.setTitle("Experiment");
		experiment.setDarkCurrentFile("c:/darkcurrent.hdf");
		experiment.setSensitivityFile("c:/sensitivity.hdf");
		experiment.setUserReportDirectory("c:/reports");
		
		// User
		User user = experiment.getUser();
		user.setName("Tony");
		user.setEmail("tony.lam@ansto.gov.au");
		user.setPhone("9408");
		
		// Samples
		Sample sample1 = new Sample();
		sample1.setName("H2O");
		sample1.setPosition(1);
		sample1.setRunnable(true);
		sample1.setThickness(1);
		sample1.setDescription("1mm water");
		experiment.getSamples().add(sample1);
		
		Sample sample2 = new Sample();
		sample2.setName("Paraffin");
		sample2.setPosition(2);
		sample2.setRunnable(true);
		sample2.setThickness(2);
		sample2.setDescription("Strong scatter");
		experiment.getSamples().add(sample2);
		
		Sample sample3 = new Sample();
		sample3.setPosition(3);
		sample3.setRunnable(false);
		experiment.getSamples().add(sample3);
		
		// Sample environment ids
		experiment.getSampleEnvControllerIds().add("tcl1");
		experiment.getSampleEnvControllerIds().add("vcl1");
		
		// Configs
		InstrumentConfig config1 = new InstrumentConfig();
		config1.setMode(ScanMode.TIME);
		experiment.getInstrumentConfigs().add(config1);
		
		InstrumentConfig config2 = new InstrumentConfig();
		config2.setMode(ScanMode.TIME);
		experiment.getInstrumentConfigs().add(config2);
		
		ExperimentModelUtils.getXStream().toXML(experiment, System.out);
	}
	
	// empty
	public void testEmptyEntryGeneration() {
		// Experiment
		Experiment experiment = new Experiment();
		assertFalse(experiment.isControlledEnvironment());
		assertEquals(0, experiment.getNormalAcquisition().getEntries().size());
	}
	
	// 2 runnable samples + single config
	public void testSingleConfigEntryGeneration() {
		// Experiment
		Experiment experiment = new Experiment();
		
		// Samples
		Sample sample1 = new Sample();
		sample1.setRunnable(true);
		experiment.getSamples().add(sample1);
		
		Sample sample2 = new Sample();
		sample2.setRunnable(true);
		experiment.getSamples().add(sample2);
		
		Sample sample3 = new Sample();
		sample3.setRunnable(false);
		experiment.getSamples().add(sample3);
		
		// Configs
		InstrumentConfig config1 = new InstrumentConfig();
		experiment.getInstrumentConfigs().add(config1);
		
		// Expected 2 normal entries (as the last one is non-runnable)
		assertEquals(2, experiment.getNormalAcquisition().getEntries().size());
		for (AcquisitionEntry entry : experiment.getNormalAcquisition().getEntries()) {
			assertEquals(1, entry.getConfigSettings().size());
			assertTrue(entry.getConfigSettings().containsKey(config1));
		}
		
	}
	
	public void testSingleSampleEnvironmentEntryGeneration() {
		// Experiment
		Experiment experiment = new Experiment();
		
		// Samples
		Sample sample1 = new Sample();
		sample1.setRunnable(true);
		experiment.getSamples().add(sample1);
		
		Sample sample2 = new Sample();
		sample2.setRunnable(true);
		experiment.getSamples().add(sample2);
		
		Sample sample3 = new Sample();
		sample3.setRunnable(false);
		experiment.getSamples().add(sample3);
		
		// Configs
		InstrumentConfig config1 = new InstrumentConfig();
		experiment.getInstrumentConfigs().add(config1);
		
		// Sample environment
		SampleEnvironment sampleEnv1 = new SampleEnvironment(experiment);
		experiment.getSampleEnvironments().add(sampleEnv1);
		sampleEnv1.setControllerId("tcl1");
		sampleEnv1.resetPresets(100, 500, 5, 1000);
		experiment.setControlledAcquisition(true);
		
		assertTrue(experiment.isControlledEnvironment());
		assertEquals(sampleEnv1.getPresets().size(), experiment.getAcquisitionGroups().size());
		
	}
	
	public void testDoubleSampleEnvironmentEntryGeneration() {
		// Experiment
		Experiment experiment = new Experiment();
		
		// Samples
		Sample sample1 = new Sample();
		sample1.setRunnable(true);
		experiment.getSamples().add(sample1);
		
		Sample sample2 = new Sample();
		sample2.setRunnable(true);
		experiment.getSamples().add(sample2);
		
		Sample sample3 = new Sample();
		sample3.setRunnable(false);
		experiment.getSamples().add(sample3);
		
		// Configs
		InstrumentConfig config1 = new InstrumentConfig();
		experiment.getInstrumentConfigs().add(config1);
		
		// Sample environment
		SampleEnvironment sampleEnv1 = new SampleEnvironment(experiment);
		experiment.getSampleEnvironments().add(sampleEnv1);
		sampleEnv1.setControllerId("tcl1");
		sampleEnv1.resetPresets(100, 500, 5, 1000);

		SampleEnvironment sampleEnv2 = new SampleEnvironment(experiment);
		experiment.getSampleEnvironments().add(sampleEnv2);
		sampleEnv2.setControllerId("vcl1");
		sampleEnv1.resetPresets(10, 15, 2, 0);
		
		experiment.setControlledAcquisition(true);
		
		assertTrue(experiment.isControlledEnvironment());
		assertEquals(sampleEnv1.getPresets().size() * sampleEnv2.getPresets().size(), experiment.getAcquisitionGroups().size());
		
	}
	
	public void testTripleSampleEnvironmentEntryGeneration() {
		// Experiment
		Experiment experiment = new Experiment();
		
		// Samples
		Sample sample1 = new Sample();
		sample1.setRunnable(true);
		experiment.getSamples().add(sample1);
		
		Sample sample2 = new Sample();
		sample2.setRunnable(true);
		experiment.getSamples().add(sample2);
		
		Sample sample3 = new Sample();
		sample3.setRunnable(false);
		experiment.getSamples().add(sample3);
		
		// Configs
		InstrumentConfig config1 = new InstrumentConfig();
		experiment.getInstrumentConfigs().add(config1);
		
		// Sample environment
		SampleEnvironment sampleEnv1 = new SampleEnvironment(experiment);
		experiment.getSampleEnvironments().add(sampleEnv1);
		sampleEnv1.setControllerId("tcl1");
		sampleEnv1.resetPresets(100, 300, 3, 1000);

		SampleEnvironment sampleEnv2 = new SampleEnvironment(experiment);
		experiment.getSampleEnvironments().add(sampleEnv2);
		sampleEnv2.setControllerId("vcl1");
		sampleEnv2.resetPresets(10, 15, 2, 0);
		
		SampleEnvironment sampleEnv3 = new SampleEnvironment(experiment);
		experiment.getSampleEnvironments().add(sampleEnv3);
		sampleEnv3.setControllerId("mcl1");
		sampleEnv3.resetPresets(0.1f, 0.3f, 3, 0);
		
		experiment.setControlledAcquisition(true);
		
		assertTrue(experiment.isControlledEnvironment());
		assertEquals(sampleEnv1.getPresets().size() * sampleEnv2.getPresets().size() * sampleEnv3.getPresets().size(), experiment.getAcquisitionGroups().size());
		
	}
	
	public void testFindControlledAcquisition() {
		// Experiment
		Experiment experiment = new Experiment();

		// Sample environment
		SampleEnvironment sampleEnv1 = new SampleEnvironment(experiment);
		experiment.getSampleEnvironments().add(sampleEnv1);
		sampleEnv1.setControllerId("tcl1");
		sampleEnv1.resetPresets(100, 300, 3, 1000);
		
		experiment.setControlledAcquisition(true);
		assertEquals(sampleEnv1.getPresets().size(), experiment.getAcquisitionGroups().size());
		
		Map<SampleEnvironment, SampleEnvironmentPreset> target = new HashMap<SampleEnvironment, SampleEnvironmentPreset>();
		SampleEnvironmentPreset preset = new SampleEnvironmentPreset(100f, 1000);
		target.put(sampleEnv1, preset);
		
		ControlledAcquisition acqusition = experiment.findControlledAcquisition(target);
		assertNotNull(acqusition);
		
	}
	
}
