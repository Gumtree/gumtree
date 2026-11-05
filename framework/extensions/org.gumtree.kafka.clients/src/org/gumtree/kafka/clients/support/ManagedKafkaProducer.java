package org.gumtree.kafka.clients.support;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
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
import org.gumtree.kafka.clients.IKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A KafkaProducer that its service closes when the service stops.
 */
class ManagedKafkaProducer implements IKafkaProducer {

	private static final Logger logger = LoggerFactory.getLogger(ManagedKafkaProducer.class);

	private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(5);

	private final Producer<String, byte[]> producer;

	private final KafkaProducerService owner;

	private volatile boolean active = true;

	ManagedKafkaProducer(Map<String, Object> config, KafkaProducerService owner) {
		this.owner = owner;
		logger.info("Creating Kafka producer for " + config.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
		// Kafka loads classes named in its config through the context class
		// loader; inside OSGi that loader may not see the Kafka bundle.
		Thread thread = Thread.currentThread();
		ClassLoader previous = thread.getContextClassLoader();
		thread.setContextClassLoader(KafkaProducer.class.getClassLoader());
		try {
			producer = new KafkaProducer<String, byte[]>(config, new StringSerializer(), new ByteArraySerializer());
		} finally {
			thread.setContextClassLoader(previous);
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
		checkActive();
		return producer.send(new ProducerRecord<String, byte[]>(topic, key, value), callback);
	}

	@Override
	public RecordMetadata sendAndWait(String topic, String key, String value, long timeoutMs)
			throws InterruptedException, ExecutionException, TimeoutException {
		return send(topic, key, value).get(timeoutMs, TimeUnit.MILLISECONDS);
	}

	@Override
	public void flush() {
		checkActive();
		producer.flush();
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public synchronized void close() {
		if (!active) {
			return;
		}
		active = false;
		try {
			producer.close(CLOSE_TIMEOUT);
		} catch (Exception e) {
			logger.warn("Error while closing Kafka producer", e);
		}
		owner.removeProducer(this);
	}

	private void checkActive() {
		if (!active) {
			throw new IllegalStateException("Kafka producer is closed");
		}
	}

	private static byte[] toBytes(String value) {
		return value == null ? null : value.getBytes(StandardCharsets.UTF_8);
	}

}
