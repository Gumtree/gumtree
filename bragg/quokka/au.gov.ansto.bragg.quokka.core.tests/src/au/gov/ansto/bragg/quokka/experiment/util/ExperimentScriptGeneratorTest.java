package au.gov.ansto.bragg.quokka.experiment.util;

import junit.framework.TestCase;
import au.gov.ansto.bragg.quokka.experiment.model.Experiment;
import au.gov.ansto.bragg.quokka.experiment.model.InstrumentConfig;
import au.gov.ansto.bragg.quokka.experiment.model.Sample;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironment;
import au.gov.ansto.bragg.quokka.experiment.model.SampleEnvironmentPreset;

public class ExperimentScriptGeneratorTest extends TestCase {
//
//	public void testNormalEmptyExperiment() {
//		Experiment experiment = new Experiment();
//		experiment.setControlledAcquisition(false);
//		experiment.getUser().setName("tla");
//		experiment.getUser().setEmail("tony.lam@ansto.gov.au");
//		experiment.getUser().setPhone("9408");
//		experiment.setTitle("Experiment");
//		
//		System.out.println();
//		System.out.println("*******************************************************************************");
//		String script = ExperimentScriptGenerator.generate(experiment);
//		System.out.println(script);
//		System.out.println("*******************************************************************************");
//		System.out.println();
//	}
//	
//	public void testNormalWithConfigsOnlyExperiment() {
//		Experiment experiment = new Experiment();
//		experiment.setControlledAcquisition(false);
//		
//		InstrumentConfig config0 = new InstrumentConfig();
//		config0.setName("Long");
//		config0.setInitScript("print 'To long'");
//		config0.setPreTransmissionScript("print 'To long transmission'");
//		config0.setPreScatteringScript("print 'To long scattering'");
//		experiment.getInstrumentConfigs().add(config0);
//		
//		InstrumentConfig config1 = new InstrumentConfig();
//		config1.setName("Medium");
//		experiment.getInstrumentConfigs().add(config1);
//		
//		InstrumentConfig config2 = new InstrumentConfig();
//		config2.setName("Short");
//		config2.setInitScript("print 'To short'");
//		config2.setPreTransmissionScript("print 'To short transmission'");
//		config2.setPreScatteringScript("print 'To short scattering'");
//		experiment.getInstrumentConfigs().add(config2);
//		
//		System.out.println();
//		System.out.println("*******************************************************************************");
//		String script = ExperimentScriptGenerator.generate(experiment);
//		System.out.println(script);
//		System.out.println("*******************************************************************************");
//		System.out.println();
//	}
//	
//	public void testNormalWithSamplesOnlyExperiment() {
//		Experiment experiment = new Experiment();
//		experiment.setControlledAcquisition(false);
//		
//		Sample sample1 = new Sample();
//		sample1.setPosition(1);
//		sample1.setName("Water");
//		sample1.setRunnable(true);
//		experiment.getSamples().add(sample1);
//		
//		Sample sample2 = new Sample();
//		sample2.setPosition(2);
//		sample2.setRunnable(true);
//		experiment.getSamples().add(sample2);
//		
//		Sample sample3 = new Sample();
//		sample3.setPosition(3);
//		sample3.setRunnable(false);
//		experiment.getSamples().add(sample3);
//		
//		System.out.println();
//		System.out.println("*******************************************************************************");
//		String script = ExperimentScriptGenerator.generate(experiment);
//		System.out.println(script);
//		System.out.println("*******************************************************************************");
//		System.out.println();
//	}
//	
//	public void testNormalExperiment() {
//		Experiment experiment = new Experiment();
//		experiment.setControlledAcquisition(false);
//		experiment.getUser().setName("tla");
//		experiment.getUser().setEmail("tony.lam@ansto.gov.au");
//		experiment.getUser().setPhone("9408");
//		experiment.setTitle("Experiment");
//		
//		Sample sample1 = new Sample();
//		sample1.setPosition(19);
//		sample1.setName("Water");
//		sample1.setDescription("H2O");
//		sample1.setRunnable(true);
//		experiment.getSamples().add(sample1);
//		
//		Sample sample2 = new Sample();
//		sample2.setPosition(20);
//		sample2.setName("Paraffin");
//		sample2.setDescription("2mm paraffin");
//		sample2.setRunnable(true);
//		experiment.getSamples().add(sample2);
//		
//		InstrumentConfig config0 = new InstrumentConfig();
//		config0.setName("Long");
//		config0.setInitScript("print 'To long'");
//		config0.setPreTransmissionScript("print 'To long transmission'");
//		config0.setPreScatteringScript("print 'To long scattering'");
//		experiment.getInstrumentConfigs().add(config0);
//		
//		InstrumentConfig config1 = new InstrumentConfig();
//		config1.setName("Medium");
//		config1.setInitScript("print 'To medium'");
//		config1.setPreTransmissionScript("print 'To medium transmission'");
//		config1.setPreScatteringScript("print 'To medium scattering'");
//		experiment.getInstrumentConfigs().add(config1);
//		
//		InstrumentConfig config2 = new InstrumentConfig();
//		config2.setName("Short");
//		config2.setInitScript("print 'To short'");
//		config2.setPreTransmissionScript("print 'To short transmission'");
//		config2.setPreScatteringScript("print 'To short scattering'");
//		experiment.getInstrumentConfigs().add(config2);
//		
//		System.out.println();
//		System.out.println("*******************************************************************************");
//		String script = ExperimentScriptGenerator.generate(experiment);
//		System.out.println(script);
//		System.out.println("*******************************************************************************");
//		System.out.println();
//	}
	
	public void testControlledExperiment() {
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
		
		System.out.println();
		System.out.println("*******************************************************************************");
		String script = ExperimentScriptGenerator.generate(experiment);
		System.out.println(script);
		System.out.println("*******************************************************************************");
		System.out.println();
	}
	
}
