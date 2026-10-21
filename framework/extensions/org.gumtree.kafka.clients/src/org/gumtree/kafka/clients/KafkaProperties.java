package org.gumtree.kafka.clients;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

/**
 * Settings for the Kafka services. Set them as system properties (for example
 * -Dgumtree.kafka.bootstrapServers=host:9092 in the product ini) or through
 * Gumtree preferences.
 */
public final class KafkaProperties {

	/**
	 * Comma-separated host:port list of Kafka brokers.
	 */
	public static final ISystemProperty BOOTSTRAP_SERVERS = new SystemProperty(
			"gumtree.kafka.bootstrapServers", "localhost:9092");

	/**
	 * Client id the producer reports to the broker (shows in broker logs and metrics).
	 */
	public static final ISystemProperty PRODUCER_CLIENT_ID = new SystemProperty(
			"gumtree.kafka.producer.clientId", "gumtree-producer");

	/**
	 * Acknowledgements required before a send counts as done: 0, 1 or all.
	 */
	public static final ISystemProperty PRODUCER_ACKS = new SystemProperty(
			"gumtree.kafka.producer.acks", "all");

	/**
	 * Compression for sent batches: none, gzip, snappy, lz4 or zstd.
	 */
	public static final ISystemProperty PRODUCER_COMPRESSION_TYPE = new SystemProperty(
			"gumtree.kafka.producer.compressionType", "lz4");

	/**
	 * Longest time in milliseconds a send may block, for example while the broker is
	 * unreachable. Kafka's own default is 60000, too long for a UI thread.
	 */
	public static final ISystemProperty PRODUCER_MAX_BLOCK_MS = new SystemProperty(
			"gumtree.kafka.producer.maxBlockMs", "10000");

	/**
	 * Optional path to a Kafka producer properties file. Its entries override all
	 * settings above, and can set anything else Kafka supports (SSL, SASL, etc.).
	 */
	public static final ISystemProperty PRODUCER_CONFIG_FILE = new SystemProperty(
			"gumtree.kafka.producer.configFile", "");

	/**
	 * Prefix for consumer client ids; each subscription adds a number to it.
	 */
	public static final ISystemProperty CONSUMER_CLIENT_ID = new SystemProperty(
			"gumtree.kafka.consumer.clientId", "gumtree-consumer");

	/**
	 * Where a subscription starts when its group has no saved position: latest
	 * (only new messages) or earliest (everything still kept on the broker).
	 */
	public static final ISystemProperty CONSUMER_AUTO_OFFSET_RESET = new SystemProperty(
			"gumtree.kafka.consumer.autoOffsetReset", "latest");

	/**
	 * Optional path to a Kafka consumer properties file. Its entries override all
	 * consumer settings, and can set anything else Kafka supports (SSL, SASL, etc.).
	 */
	public static final ISystemProperty CONSUMER_CONFIG_FILE = new SystemProperty(
			"gumtree.kafka.consumer.configFile", "");

	private KafkaProperties() {
		super();
	}

}
