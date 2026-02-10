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

package org.gumtree.app.runtime.loader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.gumtree.app.runtime.ConfigEnvironmentManager;
import org.gumtree.app.runtime.IConfigEnvironmentManager;
import org.gumtree.app.runtime.RuntimeProperties;
import org.gumtree.app.runtime.RuntimeUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PropertiesLoader2 loads properties based on the IConfigEnvironmentManager
 * API.
 * 
 * @author Tony Lam
 * @since 1.6
 */
public class PropertiesLoader implements IRuntimeLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(PropertiesLoader.class);
	
	private static final String FILE_GUMTREE_PROPERTIES = "gumtree.properties";

	private String bundleName;
	
	private String propertiesFile;
	
	private Properties props;
		
	public void load(BundleContext context) throws Exception {
		Bundle bundle = RuntimeUtils.findLatestBundle(getBundleName());
		if (bundle == null) {
			logger.info("No config bundle has been specified.");
			return;
		}
		
		logger.debug("Processing config from bundle {}", getBundleName());
//		ExtendedPropertiesConfiguration config = new ExtendedPropertiesConfiguration(
//				RuntimeUtils.getFilename(bundle, getPropertiesFile()));
		
//		Parameters params = new Parameters();
//		FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
//		    new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
//		    .configure(params.properties()
//		        .setFileName(RuntimeUtils.getFilename(bundle, getPropertiesFile())));
////		PropertiesConfiguration config = builder.getConfiguration();
//		
//		IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
////		Properties processedProperties = manager.resolveProperties(getProperties(config));
//		Properties processedProperties = manager.resolveProperties(getProperties(
//				builder.getConfiguration()
//				));
//		Map<String, String> sortedProperties = convertPropertiesToMap(processedProperties);
//		for (String key : sortedProperties.keySet()) {
//			// Do not change existing system properties
//			if (getProperties().containsKey(key)) {
//				continue;
//			}
//			getProperties().setProperty(key, sortedProperties.get(key));
//			logger.info("Loaded properties {} = {}", key, sortedProperties.get(key));
//		}
		
//		Configurations configs = new Configurations();
//        try {
//            // Load properties file (absolute or relative path)
//            PropertiesConfiguration config =
//                    configs.properties(
//							RuntimeUtils.getFilename(bundle, getPropertiesFile())
//							);
//
//    		IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
//    		Properties processedProperties = manager.resolveConfigs(config);
//    		Map<String, String> sortedProperties = convertPropertiesToMap(processedProperties);
//    		for (String key : sortedProperties.keySet()) {
//    			// Do not change existing system properties
//    			if (getProperties().containsKey(key)) {
//    				continue;
//    			}
//    			getProperties().setProperty(key, sortedProperties.get(key));
//    			logger.info("Loaded properties {} = {}", key, sortedProperties.get(key));
//    		}
//
//        } catch (Exception e) {
//            logger.error("Failed to load properties file: " + getPropertiesFile(), e);
//        }
		
		Properties props = new Properties();

        try (InputStream fis = Files.newInputStream(Path.of(new URI(
        		RuntimeUtils.getFilename(bundle, getPropertiesFile()))
        		))) {
            props.load(fis);

            IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
            Properties processedProperties = manager.resolveProperties(props);
            
            Map<String, String> sortedProperties = convertPropertiesToMap(processedProperties);
            for (String key : sortedProperties.keySet()) {
				// Do not change existing system properties
				if (getProperties().containsKey(key)) {
					continue;
				}
				getProperties().setProperty(key, sortedProperties.get(key));
				logger.info("Loaded properties {} = {}", key, sortedProperties.get(key));
			}
        } catch (IOException e) {
        	logger.error("Failed to load properties file: " + getPropertiesFile(), e);
        }
	}

	public void unload(BundleContext context) throws Exception {
	}

	public String getBundleName() {
		if (bundleName == null) {
			bundleName = System
					.getProperty(RuntimeProperties.GUMTREE_CONFIG_BUNDLE);
		}
		return bundleName;
	}
	
	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}
	
	public String getPropertiesFile() {
		if (propertiesFile == null) {
			propertiesFile = FILE_GUMTREE_PROPERTIES;
		}
		return propertiesFile;
	}
	
	public void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}
	
	public Properties getProperties() {
		if (props == null) {
			props = System.getProperties();
		}
		return props;
	}
	
	public void setProperties(Properties props) {
		this.props = props;
	}
	
	private Map<String, String> convertPropertiesToMap(Properties properties) {
		Map<String, String> map = new TreeMap<String, String>();
		for (String key : properties.stringPropertyNames()) {
			map.put(key, properties.getProperty(key));
		}
		return map;
	}
	
	private Properties getProperties(PropertiesConfiguration config) {
		Properties props = new Properties();
		Iterator<?> keys = config.getKeys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			props.setProperty(key, config.getString(key));
		}
		return props;
	}
	
}
