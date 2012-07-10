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

import java.net.URI;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
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
		ExtendedPropertiesConfiguration config = new ExtendedPropertiesConfiguration(
				RuntimeUtils.getFilename(bundle, getPropertiesFile()));
		
		IConfigEnvironmentManager manager = new ConfigEnvironmentManager();
		Properties processedProperties = manager.resolveProperties(config.getProperties());
		for (String key : processedProperties.stringPropertyNames()) {
			// Do not change existing system properties
			if (getProperties().containsKey(key)) {
				continue;
			}
			getProperties().setProperty(key, processedProperties.getProperty(key));
			logger.info("Loaded properties {} = {}", key, processedProperties.getProperty(key));
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
	
	private static class ExtendedPropertiesConfiguration extends PropertiesConfiguration {
		
		public ExtendedPropertiesConfiguration(String fileName) throws ConfigurationException {
			super(fileName);
		}
		
		// We do not need this feature in the parsing level
		public boolean isDelimiterParsingDisabled() {
			return true;
		}
		
		protected String interpolate(String base) {
			// Special hack: we assume the "include" properties need special care under the following conditions:
			if (StringUtils.isNotEmpty(base) && base.startsWith("bundle://") && base.endsWith(".properties")) {
				try {
					URI uri = new URI(base);
					Bundle bundle = RuntimeUtils.findLatestBundle(uri.getAuthority());
					return RuntimeUtils.getFilename(bundle, uri.getPath());
				} catch (Exception e) {
					logger.error("Failed to load properties file from " + base, e);
				}
			}
			return super.interpolate(base);
	    }
		
		public Properties getProperties() {
			Properties props = new Properties();
			Iterator<?> keys = getKeys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				props.setProperty(key, getString(key));
			}
			return props;
		}
		
	}
	
}
