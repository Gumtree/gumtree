package org.gumtree.kafka.clients.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

final class KafkaConfigUtils {

	private KafkaConfigUtils() {
		super();
	}

	/**
	 * Copies caller settings (for example a Jython dict) into a map Kafka can use,
	 * so later changes by the caller have no effect.
	 */
	static Map<String, Object> copy(Map<String, ?> config) {
		if (config == null) {
			throw new IllegalArgumentException("No Kafka settings given");
		}
		Map<String, Object> copy = new HashMap<String, Object>();
		for (Entry<String, ?> entry : config.entrySet()) {
			// Keys may not be Java strings when the map comes from a script.
			Object key = entry.getKey();
			copy.put(String.valueOf(key), entry.getValue());
		}
		return copy;
	}

}
