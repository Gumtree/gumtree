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

package org.gumtree.app.runtime;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the config environment manager.
 * 
 * @author Tony Lam
 * @since 1.3
 */
public class ConfigEnvironmentManager implements IConfigEnvironmentManager {

	private static final String PREFIX_CONFIG_ENV = "gumtree.runtime.configEnv.";

	private static final Logger logger = LoggerFactory
			.getLogger(ConfigEnvironmentManager.class);

	private static Pattern propertiesPattern = Pattern
			.compile("\\$\\{(\\w|\\.)+\\}");

	private Map<String, String> configEnvironments;
	
	public ConfigEnvironmentManager() {
		configEnvironments = new HashMap<String, String>();
		loadConfigEnvironment();
	}

	/**
	 * Loads config environment details from the JVM.
	 */
	protected void loadConfigEnvironment() {
		for (Entry<Object, Object> property : System.getProperties().entrySet()) {
			String propertyKey = (String) property.getKey();
			if (propertyKey.startsWith(PREFIX_CONFIG_ENV)) {
				String envKey = propertyKey.substring(PREFIX_CONFIG_ENV
						.length());
				if (envKey.contains(".")) {
					// Environment key should be a single string with no "."
					// delimator
					logger.warn("Invalid config environment: " + envKey);
				} else {
					// Store as config environment
					configEnvironments
							.put(envKey, (String) property.getValue());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gumtree.runtime.config.IConfigEnvironmentManager#getConfigEnvironments
	 * ()
	 */
	public Map<String, String> getConfigEnvironments() {
		return Collections.unmodifiableMap(configEnvironments);
	}

	/* (non-Javadoc)
	 * @see org.gumtree.runtime.config.IConfigEnvironmentManager#loadProperties(java.net.URL)
	 */
	public Properties loadProperties(URL fileURL) throws Exception {
		InputStream in = null;
		Properties properties = new Properties();
		try {
			in = fileURL.openStream();
			properties.load(in);
			properties = resolveProperties(properties);
		} catch (Exception e) {
			logger.error("Failed to load properties file " + fileURL + ".");
		} finally {
			in.close();
		}
		return properties;
	}

	/* (non-Javadoc)
	 * @see org.gumtree.runtime.config.IConfigEnvironmentManager#resolveProperties(java.util.Properties)
	 */
	public Properties resolveConfigs(PropertiesConfiguration config) {
		Map<String, List<ConfigEnvProperty>> buffer = new HashMap<String, List<ConfigEnvProperty>>();
		Iterator<String> keys = config.getKeys();
		while (keys.hasNext()) {
			String key = keys.next();
			// Take away "[", "]" and "@"
			String[] tokens = key.split("[\\[\\]\\@]");

			// Create new property holder
			ConfigEnvProperty configEnvProperty = new ConfigEnvProperty();
			configEnvProperty.key = tokens[0];
			configEnvProperty.value = config.getString(key);

			// Token patterns:
			// xx(index:0)[xx(index:1)@xx(index:2)](index:3)[xx(index:4)@xx(index:5)]...
			boolean skip = false;
			for (int i = 1; i < tokens.length; i += 3) {
				// Don't bother if the config env is not currently specified in
				// the System property
				if (!configEnvironments.containsKey(tokens[i])) {
					skip = true;
					break;
				}
				configEnvProperty.configEnv.put(tokens[i],
						Arrays.asList(tokens[i + 1].split(",")));
			}
			if (skip) {
				continue;
			}

			// Store result into buffer
			if (!buffer.containsKey(configEnvProperty.key)) {
				buffer.put(configEnvProperty.key,
						new ArrayList<ConfigEnvProperty>(2));
			}
			buffer.get(configEnvProperty.key).add(configEnvProperty);
		}
		return resolveProperties(buffer);
	}
	
	/* (non-Javadoc)
	 * @see org.gumtree.runtime.config.IConfigEnvironmentManager#resolveProperties(java.util.Properties)
	 */
	public Properties resolveProperties(Properties properties) {
		Map<String, List<ConfigEnvProperty>> buffer = new HashMap<String, List<ConfigEnvProperty>>();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String) entry.getKey();
			// Take away "[", "]" and "@"
			String[] tokens = key.split("[\\[\\]\\@]");

			// Create new property holder
			ConfigEnvProperty configEnvProperty = new ConfigEnvProperty();
			configEnvProperty.key = tokens[0];
			configEnvProperty.value = (String) entry.getValue();

			// Token patterns:
			// xx(index:0)[xx(index:1)@xx(index:2)](index:3)[xx(index:4)@xx(index:5)]...
			boolean skip = false;
			for (int i = 1; i < tokens.length; i += 3) {
				// Don't bother if the config env is not currently specified in
				// the System property
				if (!configEnvironments.containsKey(tokens[i])) {
					skip = true;
					break;
				}
				configEnvProperty.configEnv.put(tokens[i],
						Arrays.asList(tokens[i + 1].split(",")));
			}
			if (skip) {
				continue;
			}

			// Store result into buffer
			if (!buffer.containsKey(configEnvProperty.key)) {
				buffer.put(configEnvProperty.key,
						new ArrayList<ConfigEnvProperty>(2));
			}
			buffer.get(configEnvProperty.key).add(configEnvProperty);
		}
		return resolveProperties(buffer);
	}
	
	// Client may override this method
	protected Properties resolveProperties(
			Map<String, List<ConfigEnvProperty>> buffer) {
		Properties properties = new Properties();

		// Step one: resolve properties based on current config

		// 1. Traverse each property that was previously loaded
		for (Entry<String, List<ConfigEnvProperty>> bufferEntry : buffer
				.entrySet()) {
			String key = bufferEntry.getKey();

			List<ConfigEnvProperty> candidateProperties = new ArrayList<ConfigEnvProperty>(
					bufferEntry.getValue());

			// 2. For each current config environment, remove non-matching
			// properties
			for (Entry<String, String> configEnvEntry : configEnvironments
					.entrySet()) {
				List<ConfigEnvProperty> candidateBuffer = new ArrayList<ConfigEnvProperty>(
						2);
				for (ConfigEnvProperty configEnvProperty : candidateProperties) {
					String envKey = configEnvEntry.getKey();
					String envValue = configEnvEntry.getValue();
					if (configEnvProperty.configEnv.containsKey(envKey)) {
						if (configEnvProperty.configEnv.get(envKey).contains(
								envValue)) {
							configEnvProperty.matchingScore++;
						} else {
							configEnvProperty.invalid = true;
						}
					}
					candidateBuffer.add(configEnvProperty);
				}
				candidateProperties = candidateBuffer;
			}

			// 3. Find the best match
			ConfigEnvProperty result = null;
			for (ConfigEnvProperty candidateProperty : candidateProperties) {
				if (result == null) {
					if (!candidateProperty.invalid) {
						result = candidateProperty;
					}
				} else {
					if (candidateProperty.matchingScore > result.matchingScore
							&& !candidateProperty.invalid) {
						result = candidateProperty;
					}
				}
			}
			if (result != null) {
				properties.put(result.key, result.value);
				logger.debug("Resolved property " + result.key + " = "
						+ result.value + " (score: " + result.matchingScore
						+ ")");
			} else {
				logger.debug("Cannot resolve property " + key);
			}
		}

		// Step two: properties substitution
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			substituteProperty(key, value, properties);
		}

		return properties;
	}

	private void substituteProperty(String key, String value,
			Properties propertySource) {
		Matcher matcher = propertiesPattern.matcher(value);
		String result = value;
		while (matcher.find()) {
			String propertyName = value.substring(matcher.start() + 2,
					matcher.end() - 1);
			String propertyValue = propertySource.getProperty(propertyName);
			if (propertyValue != null) {
				// Recursively replace if necessary
				Matcher internalMatcher = propertiesPattern
						.matcher(propertyValue);
				if (internalMatcher.find()) {
					substituteProperty(propertyName, propertyValue,
							propertySource);
					propertyValue = propertySource.getProperty(propertyName);
				}
				// Replace
				logger.debug("key={}, value={}, propertyValue={}",
						new Object[] { key, value, propertyValue });
				result = result.replaceFirst("\\$\\{(\\w|\\.)+\\}",
						propertyValue);
			}
			propertySource.put(key, result);
		}
	}
	
	// Internal data structure
	private static class ConfigEnvProperty {

		// Property key with out config environment information
		private String key;

		// Property value before substitution
		private String value;

		// Config environment information
		private Map<String, List<String>> configEnv;

		// Used in resolution phrase
		private int matchingScore;
		
		// Used in resolution phrase (when env mismatch found)
		private boolean invalid;

		private ConfigEnvProperty() {
			configEnv = new HashMap<String, List<String>>(2);
		}

		@Override
		public String toString() {
			return "ConfigEnvProperty [configEnv=" + configEnv + ", key=" + key
					+ ", matchingScore=" + matchingScore + ", invalid="
					+ invalid + ", value=" + value + "]";
		}

	}

}
