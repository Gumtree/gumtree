package au.gov.ansto.bragg.quokka.experiment.util;

import junit.framework.TestCase;
import au.gov.ansto.bragg.quokka.experiment.model.ControlledAcquisition;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironmentPreset;

public class ExperimentStateManagerTest extends TestCase {

	public void testNormalEnvironmentRunnable() {
		Experiment experiment = new Experiment();
		experiment.setControlledAcquisition(false);
		experiment.getUser().setName("tla");
		experiment.getUser().setEmail("tony.lam@ansto.gov.au");
		experiment.getUser().setPhone("9408");
		experiment.setTitle("Experiment");
		
		Sample sample1 = new Sample();
		sample1.setPosition(19);
		sample1.setName("Water");
		sample1.setDescription("H2O");
		sample1.setRunnable(true);
		experiment.getSamples().add(sample1);
		
		Sample sample2 = new Sample();
		sample2.setPosition(20);
		sample2.setName("Paraffin");
		sample2.setDescription("2mm paraffin");
		sample2.setRunnable(true);
		experiment.getSamples().add(sample2);
		
		InstrumentConfig config0 = new InstrumentConfig();
		config0.setName("Long");
		config0.setInitScript("print 'To long'");
		config0.setPreTransmissionScript("print 'To long transmission'");
		config0.setPreScatteringScript("print 'To long scattering'");
		experiment.getInstrumentConfigs().add(config0);
		
		InstrumentConfig config1 = new InstrumentConfig();
		config1.setName("Medium");
		config1.setInitScript("print 'To medium'");
		config1.setPreTransmissionScript("print 'To medium transmission'");
		config1.setPreScatteringScript("print 'To medium scattering'");
		experiment.getInstrumentConfigs().add(config1);
		
		InstrumentConfig config2 = new InstrumentConfig();
		config2.setName("Short");
		config2.setInitScript("print 'To short'");
		config2.setPreTransmissionScript("print 'To short transmission'");
		config2.setPreScatteringScript("print 'To short scattering'");
		experiment.getInstrumentConfigs().add(config2);
		
		// Config run setting
		// Set sample 0 @ medium config transmission off
		experiment.getNormalAcquisition().getEntries().get(0).getConfigSettings().get(config1).setRunTransmission(false);
		
		
//		System.out.println();
//		System.out.println("*******************************************************************************");
//		String script = ExperimentScriptGenerator.generate(experiment);
//		System.out.println(script);
//		System.out.println("*******************************************************************************");
//		System.out.println();
		
		ExperimentStateManager stateManager = new ExperimentStateManager(experiment);
		ExperimentScriptGenerator.generate(experiment, stateManager);
		
		// Long
		assertTrue(stateManager.checkTransmissionRunnable(0));
		assertTrue(stateManager.checkTransmissionRunnable(1));
		// Medium
		assertFalse(stateManager.checkTransmissionRunnable(2));
		assertTrue(stateManager.checkTransmissionRunnable(3));
		// Short
		assertTrue(stateManager.checkTransmissionRunnable(4));
		assertTrue(stateManager.checkTransmissionRunnable(5));
		try {
			// Out of range test
			assertTrue(stateManager.checkTransmissionRunnable(6));
			fail();
		} catch (Exception e) {
		}
	}
	
	public void testControlledEnvironmentRunnable() {
		Experiment experiment = new Experiment();
		experiment.setControlledAcquisition(true);
		experiment.getUser().setName("tla");
		experiment.getUser().setEmail("tony.lam@ansto.gov.au");
		experiment.getUser().setPhone("9408");
		experiment.setTitle("Experiment");
		
		Sample sample1 = new Sample();
		sample1.setPosition(19);
		sample1.setName("Water");
		sample1.setDescription("H2O");
		sample1.setRunnable(true);
		experiment.getSamples().add(sample1);
		
		Sample sample2 = new Sample();
		sample2.setPosition(20);
		sample2.setName("Paraffin");
		sample2.setDescription("2mm paraffin");
		sample2.setRunnable(true);
		experiment.getSamples().add(sample2);
		
		InstrumentConfig config0 = new InstrumentConfig();
		config0.setName("Long");
		config0.setInitScript("print 'To long'");
		config0.setPreTransmissionScript("print 'To long transmission'");
		config0.setPreScatteringScript("print 'To long scattering'");
		experiment.getInstrumentConfigs().add(config0);
		
		InstrumentConfig config1 = new InstrumentConfig();
		config1.setName("Medium");
		config1.setInitScript("print 'To medium'");
		config1.setPreTransmissionScript("print 'To medium transmission'");
		config1.setPreScatteringScript("print 'To medium scattering'");
		experiment.getInstrumentConfigs().add(config1);
		
		InstrumentConfig config2 = new InstrumentConfig();
		config2.setName("Short");
		config2.setInitScript("print 'To short'");
		config2.setPreTransmissionScript("print 'To short transmission'");
		config2.setPreScatteringScript("print 'To short scattering'");
		experiment.getInstrumentConfigs().add(config2);
		
		SampleEnvironment env0 = new SampleEnvironment(experiment);
		env0.setControllerId("dummy_motor");
		env0.getPresets().add(new SampleEnvironmentPreset(100, 1));
		env0.getPresets().add(new SampleEnvironmentPreset(200, 1));
		env0.getPresets().add(new SampleEnvironmentPreset(300, 1));
		env0.getPresets().add(new SampleEnvironmentPreset(400, 1));
		env0.getPresets().add(new SampleEnvironmentPreset(500, 1));
		experiment.getSampleEnvironments().add(env0);
		
		SampleEnvironment env1 = new SampleEnvironment(experiment);
		env1.setControllerId("dummy_motor");
		env1.getPresets().add(new SampleEnvironmentPreset(10, 1));
		env1.getPresets().add(new SampleEnvironmentPreset(20, 1));
		experiment.getSampleEnvironments().add(env1);
		
		// Config run setting
		// Set sample 20 (seq 1) @ medium config @ env (200, 10) transmission off
		for (ControlledAcquisition acquisition : experiment.getAcquisitionGroups()) {
			float preset1 = acquisition.getEnvSettings().get(env0).getPreset();
			float preset2 = acquisition.getEnvSettings().get(env1).getPreset();
			if (preset1 == 200 && preset2 == 10) {
				acquisition.getEntries().get(1).getConfigSettings().get(config1).setRunTransmission(false);
			}
		}
		
		ExperimentStateManager stateManager = new ExperimentStateManager(experiment);
		ExperimentScriptGenerator.generate(experiment, stateManager);
		
		for (int i = 0; i < 60; i++) {
			if (i == 15) {
				assertFalse(stateManager.checkTransmissionRunnable(i));
				assertTrue(stateManager.checkScatteringRunnable(i));
			} else {
				assertTrue("RunID: " + i, stateManager.checkTransmissionRunnable(i));
				assertTrue("RunID: " + i, stateManager.checkScatteringRunnable(i));
			}
		}
		
	}
	
}
