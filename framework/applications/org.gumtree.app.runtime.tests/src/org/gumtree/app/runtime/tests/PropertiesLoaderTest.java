package org.gumtree.app.runtime.tests;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.gumtree.app.runtime.loader.PropertiesLoader;
import org.junit.Test;

public class PropertiesLoaderTest {

	@Test
	public void testParseProperties() throws Exception {
		// Create
		PropertiesLoader loader = new PropertiesLoader();
		Properties props = new Properties();
		
		// Set
		loader.setBundleName(Activator.ID_PLUGIN);
		loader.setPropertiesFile("data/custom_gumtree.properties");
		loader.setProperties(props);
		
		// Run
		loader.load(Activator.getContext());
		
		// Assert
		assertEquals("hello", props.getProperty("text"));
	}
	
	@Test
	public void testDefaultParseProperties() throws Exception {
		// Create
		PropertiesLoader loader = new PropertiesLoader();
		
		// Set
		System.setProperty("gumtree.runtime.configBundle", Activator.ID_PLUGIN);
		
		// Run
		loader.load(Activator.getContext());
		
		// Assert
		assertEquals("hello", System.getProperty("text"));
	}
	
}
