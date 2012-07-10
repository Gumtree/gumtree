package org.gumtree.app.runtime.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Properties;

import org.gumtree.app.runtime.ConfigEnvironmentManager;
import org.gumtree.app.runtime.IConfigEnvironmentManager;
import org.junit.Test;

public class ConfigEnvironmentManagerTest {

	private static final String PROP_CONFIG_ENV = "gumtree.runtime.configEnv.";
	private static final String DIR_TEST_DATA = "data";
	private static final String FILE_TEST = "test.properties";

	@Test
	public void testGetConfigEnvironments() {
		System.getProperties()
				.setProperty(PROP_CONFIG_ENV + "instr", "echidna");
		System.getProperties().setProperty(PROP_CONFIG_ENV + "env", "prod");
		System.getProperties().setProperty(PROP_CONFIG_ENV + "mode",
				"experiment");

		IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
		assertEquals("echidna", manager.getConfigEnvironments().get("instr"));
		assertEquals("prod", manager.getConfigEnvironments().get("env"));
		assertEquals("experiment", manager.getConfigEnvironments().get("mode"));
	}

	@Test
	public void testResolution() throws Exception {
		System.getProperties().setProperty(PROP_CONFIG_ENV + "instr", "kowari");
		System.getProperties().setProperty(PROP_CONFIG_ENV + "env", "prod");
		System.getProperties().setProperty(PROP_CONFIG_ENV + "mode",
				"experiment");

		URL propertyFile = Activator.getContext().getBundle()
				.findEntries(DIR_TEST_DATA, FILE_TEST, false).nextElement();
		IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
		Properties properties = manager.loadProperties(propertyFile);

		assertEquals("Kowari", properties.getProperty("instrument.shortName"));
		assertEquals("Kowari Experiment Workbench",
				properties.getProperty("gumtree.productName"));
		assertEquals("false", properties.getProperty("intro.showNavigationBar"));
		assertEquals("9876", properties.getProperty("restlet.serverPort"));
		assertEquals("1.3.0", properties.getProperty("gumtree.build.version"));
		assertEquals("Kowari Experiment Workbench - 1.3.0",
				properties.getProperty("gumtree.titleBar"));
	}

	@Test
	public void testResolutionWithEmptyConfigEnvironment() throws Exception {
		System.getProperties().remove(PROP_CONFIG_ENV + "instr");
		System.getProperties().remove(PROP_CONFIG_ENV + "env");
		System.getProperties().remove(PROP_CONFIG_ENV + "mode");
		IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
		assertTrue(manager.getConfigEnvironments().isEmpty());

		URL propertyFile = Activator.getContext().getBundle()
				.findEntries(DIR_TEST_DATA, FILE_TEST, false).nextElement();
		Properties properties = manager.loadProperties(propertyFile);
		assertNull(properties.getProperty("instrument.shortName"));
		assertEquals("1.3.0", properties.getProperty("gumtree.build.version"));
		assertEquals("true", properties.getProperty("intro.showNavigationBar"));
	}

	@Test
	public void testMultiSubstitution() throws Exception {
		URL propertyFile = Activator.getContext().getBundle()
				.findEntries(DIR_TEST_DATA, FILE_TEST, false).nextElement();
		IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
		Properties properties = manager.loadProperties(propertyFile);

		assertEquals("hello", properties.getProperty("string1"));
		assertEquals("hello_world", properties.getProperty("string2"));
		assertEquals("hello_world_everyone", properties.getProperty("string3"));

		// Test if substitution works in reverse order
		assertEquals("hello_world_everyone", properties.getProperty("string4"));
		assertEquals("hello_world", properties.getProperty("string5"));
		assertEquals("hello", properties.getProperty("string6"));
	}

	@Test
	public void testMismatch() {
		// Set config environment to kowari
		System.getProperties().setProperty(PROP_CONFIG_ENV + "instr", "kowari");
		// Set properties
		Properties properties = new Properties();
		properties.setProperty("sics.name", "unknown");
		properties.setProperty("sics.name[instr@quokka]", "quokkka");
		// Process properties
		IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
		Properties processedProperties = manager.resolveProperties(properties);
		// The mismatched case should be ignored
		assertEquals("unknown", processedProperties.getProperty("sics.name"));

		// Try again in reversed order
		properties = new Properties();
		properties.setProperty("sics.name[instr@quokka]", "quokkka");
		properties.setProperty("sics.name", "unknown");
		processedProperties = manager.resolveProperties(properties);
		assertEquals("unknown", processedProperties.getProperty("sics.name"));

		properties = new Properties();
		properties.setProperty("sics.name[instr@quokka]", "quokkka");
		processedProperties = manager.resolveProperties(properties);
		// General case was unavailable, so no property should be processed
		assertNull(processedProperties.getProperty("sics.name"));
	}

	@Test
	public void testMixingWithMismatch() {
		// Set config environment to kowari
		System.getProperties().setProperty(PROP_CONFIG_ENV + "instr", "kowari");
		// Set properties
		Properties properties = new Properties();
		properties.setProperty("sics.name", "unknown");
		properties.setProperty("sics.name[instr@kowari]", "kowari");
		properties.setProperty("sics.name[instr@quokka]", "quokkka");
		// Process properties
		IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
		Properties processedProperties = manager.resolveProperties(properties);
		// The mismatched case should be ignored
		assertEquals("kowari", processedProperties.getProperty("sics.name"));
	}

}
