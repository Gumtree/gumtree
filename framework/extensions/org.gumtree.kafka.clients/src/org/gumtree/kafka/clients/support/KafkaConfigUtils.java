package org.gumtree.kafka.clients.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.gumtree.util.ISystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class KafkaConfigUtils {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConfigUtils.class);

	private KafkaConfigUtils() {
		super();
	}

	/**
	 * Adds the entries of the Kafka properties file named by the given setting to
	 * the config, replacing existing values. Does nothing if the setting is empty.
	 */
	static void loadConfigFile(Properties config, ISystemProperty configFile) {
		String path = configFile.getValue();
		if (path == null || path.trim().length() == 0) {
			return;
		}
		File file = new File(path.trim());
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			Properties overrides = new Properties();
			overrides.load(in);
			config.putAll(overrides);
			logger.info("Loaded Kafka settings from " + file.getAbsolutePath());
		} catch (IOException e) {
			logger.error("Cannot read Kafka settings from " + file.getAbsolutePath(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// Ignore
				}
			}
		}
	}

}
