package org.gumtree.kafka.clients.support;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.gumtree.kafka.clients.IKafkaProducerService;
import org.gumtree.kafka.clients.KafkaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerService implements IKafkaProducerService {

	private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

	private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(5);

	private Producer<String, byte[]> producer;

	private boolean active;

	public synchronized void activate() {
		active = true;
	}

	public synchronized void deactivate() {
		active = false;
		if (producer != null) {
			logger.info("Closing Kafka producer");
			try {
				producer.close(CLOSE_TIMEOUT);
			} catch (Exception e) {
				logger.warn("Error while closing Kafka producer", e);
			}
			producer = null;
		}
	}

	@Override
	public Future<RecordMetadata> send(String topic, String key, String value) {
		return send(topic, key, toBytes(value), null);
	}

	@Override
	public Future<RecordMetadata> send(String topic, String key, byte[] value) {
		return send(topic, key, value, null);
	}

	@Override
	public Future<RecordMetadata> send(String topic, String key, byte[] value, Callback callback) {
		return getProducer().send(new ProducerRecord<String, byte[]>(topic, key, value), callback);
	}

	@Override
	public RecordMetadata sendAndWait(String topic, String key, String value, long timeoutMs)
			throws InterruptedException, ExecutionException, TimeoutException {
		return send(topic, key, value).get(timeoutMs, TimeUnit.MILLISECONDS);
	}

	@Override
	public void flush() {
		Producer<String, byte[]> current;
		synchronized (this) {
			current = producer;
		}
		// Nothing sent yet means nothing to flush.
		if (current != null) {
			current.flush();
		}
	}

	@Override
	public String getBootstrapServers() {
		return KafkaProperties.BOOTSTRAP_SERVERS.getValue();
	}

	/**
	 * Creates the producer on first use, so that starting Gumtree never waits for
	 * or depends on a Kafka broker.
	 */
	protected synchronized Producer<String, byte[]> getProducer() {
		if (!active) {
			throw new IllegalStateException("Kafka producer service is not active");
		}
		if (producer == null) {
			Properties config = createConfig();
			logger.info("Creating Kafka producer for " + config.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
			// Kafka loads classes named in its config through the context class
			// loader; inside OSGi that loader may not see the Kafka bundle.
			Thread thread = Thread.currentThread();
			ClassLoader previous = thread.getContextClassLoader();
			thread.setContextClassLoader(KafkaProducer.class.getClassLoader());
			try {
				producer = new KafkaProducer<String, byte[]>(config, new StringSerializer(),
						new ByteArraySerializer());
			} finally {
				thread.setContextClassLoader(previous);
			}
		}
		return producer;
	}

	protected Properties createConfig() {
		Properties config = new Properties();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaProperties.BOOTSTRAP_SERVERS.getValue());
		config.put(ProducerConfig.CLIENT_ID_CONFIG, KafkaProperties.PRODUCER_CLIENT_ID.getValue());
		config.put(ProducerConfig.ACKS_CONFIG, KafkaProperties.PRODUCER_ACKS.getValue());
		config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, KafkaProperties.PRODUCER_COMPRESSION_TYPE.getValue());
		config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, KafkaProperties.PRODUCER_MAX_BLOCK_MS.getValue());
		KafkaConfigUtils.loadConfigFile(config, KafkaProperties.PRODUCER_CONFIG_FILE);
		return config;
	}

	private static byte[] toBytes(String value) {
		return value == null ? null : value.getBytes(StandardCharsets.UTF_8);
	}

}
