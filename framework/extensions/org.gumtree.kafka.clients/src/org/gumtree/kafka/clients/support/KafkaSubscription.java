package org.gumtree.kafka.clients.support;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.gumtree.kafka.clients.IKafkaMessageListener;
import org.gumtree.kafka.clients.IKafkaSubscription;
import org.gumtree.kafka.clients.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One subscription: a KafkaConsumer used only by its own polling thread, as
 * KafkaConsumer is not thread-safe. Other threads only call wakeup().
 */
class KafkaSubscription implements IKafkaSubscription, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(KafkaSubscription.class);

	private static final Duration POLL_TIMEOUT = Duration.ofMillis(500);

	private static final Duration CLOSE_TIMEOUT = Duration.ofSeconds(5);

	private static final long ERROR_BACKOFF_MS = 5000;

	private static final long JOIN_TIMEOUT_MS = 10000;

	private static final Duration POSITION_TIMEOUT = Duration.ofSeconds(5);

	private final List<String> topics;

	private final String groupId;

	private final IKafkaMessageListener listener;

	private final KafkaConsumerService owner;

	private final Consumer<String, byte[]> consumer;

	private final Thread thread;

	private final Object backoffLock = new Object();

	private volatile boolean active = true;

	/** Set by the rebalance listener; used only on the polling thread. */
	private boolean assigned;

	private volatile boolean ready;

	/** Released once ready, or on close so that waiters do not hang. */
	private final CountDownLatch readyLatch = new CountDownLatch(1);

	KafkaSubscription(Map<String, Object> config, List<String> topics, IKafkaMessageListener listener,
			KafkaConsumerService owner) {
		this.topics = Collections.unmodifiableList(topics);
		this.groupId = String.valueOf(config.get(ConsumerConfig.GROUP_ID_CONFIG));
		this.listener = listener;
		this.owner = owner;
		// Created here so that bad settings fail in the caller, not in the thread.
		// Kafka loads classes named in its config through the context class
		// loader; inside OSGi that loader may not see the Kafka bundle.
		Thread current = Thread.currentThread();
		ClassLoader previous = current.getContextClassLoader();
		current.setContextClassLoader(KafkaConsumer.class.getClassLoader());
		try {
			consumer = new KafkaConsumer<String, byte[]>(config, new StringDeserializer(),
					new ByteArrayDeserializer());
		} finally {
			current.setContextClassLoader(previous);
		}
		thread = new Thread(this, "Kafka consumer " + this.topics);
		thread.setDaemon(true);
	}

	void start() {
		thread.start();
	}

	@Override
	public void run() {
		logger.info("Subscribing to " + topics + " in group " + groupId);
		try {
			consumer.subscribe(topics, new ConsumerRebalanceListener() {
				@Override
				public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
					assigned = true;
				}

				@Override
				public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
				}
			});
			while (active) {
				try {
					ConsumerRecords<String, byte[]> records = consumer.poll(POLL_TIMEOUT);
					for (ConsumerRecord<String, byte[]> record : records) {
						if (!active) {
							break;
						}
						deliver(record);
					}
					if (assigned && !ready) {
						markReady();
					}
				} catch (WakeupException e) {
					// close() was called
				} catch (Exception e) {
					if (active) {
						logger.error("Error receiving from " + topics + ", retrying in " + ERROR_BACKOFF_MS + " ms", e);
						backoff();
					}
				}
			}
		} finally {
			readyLatch.countDown();
			try {
				consumer.close(CLOSE_TIMEOUT);
			} catch (Exception e) {
				logger.warn("Error while closing Kafka consumer for " + topics, e);
			}
			owner.removeSubscription(this);
			logger.info("Unsubscribed from " + topics + " in group " + groupId);
		}
	}

	/**
	 * Fixes the starting position on every assigned partition, then releases
	 * waiters. Kafka otherwise looks the position up lazily, so with
	 * auto.offset.reset=latest a message sent just after assignment could fall
	 * before the position and be skipped. Called on the polling thread; if a
	 * lookup fails, the loop's error handling retries on the next poll.
	 */
	private void markReady() {
		for (TopicPartition partition : consumer.assignment()) {
			consumer.position(partition, POSITION_TIMEOUT);
		}
		ready = true;
		readyLatch.countDown();
		logger.info("Subscription to " + topics + " in group " + groupId + " is ready");
	}

	private void deliver(ConsumerRecord<String, byte[]> record) {
		KafkaMessage message = new KafkaMessage(record.topic(), record.partition(), record.offset(),
				record.timestamp(), record.key(), record.value());
		try {
			listener.onMessage(message);
		} catch (Exception e) {
			logger.error("Listener failed on " + message, e);
		}
	}

	private void backoff() {
		synchronized (backoffLock) {
			try {
				backoffLock.wait(ERROR_BACKOFF_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public List<String> getTopics() {
		return topics;
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public boolean waitUntilReady(long timeoutMs) {
		try {
			readyLatch.await(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return ready && active;
	}

	@Override
	public boolean isReady() {
		return ready && active;
	}

	@Override
	public void close() {
		if (!active) {
			return;
		}
		active = false;
		readyLatch.countDown();
		consumer.wakeup();
		synchronized (backoffLock) {
			backoffLock.notifyAll();
		}
		// A listener may close its own subscription; the loop then ends after
		// the listener returns.
		if (Thread.currentThread() != thread) {
			try {
				thread.join(JOIN_TIMEOUT_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

}
