package org.gumtree.kafka.clients.support;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.gumtree.kafka.clients.IKafkaConsumerService;
import org.gumtree.kafka.clients.IKafkaMessageListener;
import org.gumtree.kafka.clients.IKafkaSubscription;
import org.gumtree.kafka.clients.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumerService implements IKafkaConsumerService {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

	private final List<KafkaSubscription> subscriptions = new CopyOnWriteArrayList<KafkaSubscription>();

	private boolean active;

	public synchronized void activate() {
		active = true;
	}

	public void deactivate() {
		synchronized (this) {
			active = false;
		}
		if (!subscriptions.isEmpty()) {
			logger.info("Closing " + subscriptions.size() + " Kafka subscription(s)");
		}
		for (KafkaSubscription subscription : subscriptions) {
			subscription.close();
		}
		subscriptions.clear();
	}

	@Override
	public IKafkaSubscription subscribe(Map<String, ?> config, String topic, IKafkaMessageListener listener) {
		return subscribe(config, Collections.singletonList(topic), listener);
	}

	@Override
	public IKafkaSubscription subscribe(Map<String, ?> config, Collection<String> topics,
			IKafkaMessageListener listener) {
		if (topics == null || topics.isEmpty()) {
			throw new IllegalArgumentException("No topic given");
		}
		if (listener == null) {
			throw new IllegalArgumentException("No listener given");
		}
		synchronized (this) {
			if (!active) {
				throw new IllegalStateException("Kafka consumer service is not active");
			}
			KafkaSubscription subscription = new KafkaSubscription(createConfig(config),
					new ArrayList<String>(topics), listener, this);
			subscriptions.add(subscription);
			subscription.start();
			return subscription;
		}
	}

	@Override
	public List<IKafkaSubscription> getSubscriptions() {
		return new ArrayList<IKafkaSubscription>(subscriptions);
	}

	@Override
	public List<KafkaMessage> readLatest(Map<String, ?> settings, String topic, int count, long timeoutMs) {
		if (topic == null || topic.trim().length() == 0) {
			throw new IllegalArgumentException("No topic given");
		}
		List<KafkaMessage> messages = new ArrayList<KafkaMessage>();
		if (count <= 0) {
			return messages;
		}
		Map<String, Object> config = KafkaConfigUtils.copy(settings);
		// Partitions are assigned directly, so no group and nothing to commit.
		config.remove(ConsumerConfig.GROUP_ID_CONFIG);
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		// Reading should not create a missing topic as a side effect.
		if (!config.containsKey(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG)) {
			config.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
		}
		long deadline = System.currentTimeMillis() + timeoutMs;
		KafkaConsumer<String, byte[]> consumer = createConsumer(config);
		try {
			List<PartitionInfo> infos = consumer.partitionsFor(topic, remaining(deadline));
			if (infos == null || infos.isEmpty()) {
				return messages;
			}
			List<TopicPartition> partitions = new ArrayList<TopicPartition>();
			for (PartitionInfo info : infos) {
				partitions.add(new TopicPartition(topic, info.partition()));
			}
			consumer.assign(partitions);
			Map<TopicPartition, Long> begin = consumer.beginningOffsets(partitions, remaining(deadline));
			Map<TopicPartition, Long> end = consumer.endOffsets(partitions, remaining(deadline));
			List<TopicPartition> pending = new ArrayList<TopicPartition>();
			for (TopicPartition partition : partitions) {
				long first = Math.max(begin.get(partition), end.get(partition) - count);
				if (first < end.get(partition)) {
					consumer.seek(partition, first);
					pending.add(partition);
				}
			}
			consumer.assign(pending);
			// Read up to the end offsets taken above; later messages are left
			// for a subscription to deliver.
			while (!pending.isEmpty() && System.currentTimeMillis() < deadline) {
				for (ConsumerRecord<String, byte[]> record : consumer.poll(remaining(deadline))) {
					TopicPartition partition = new TopicPartition(record.topic(), record.partition());
					if (record.offset() < end.get(partition)) {
						messages.add(new KafkaMessage(record.topic(), record.partition(), record.offset(),
								record.timestamp(), record.key(), record.value()));
					}
				}
				for (int i = pending.size() - 1; i >= 0; i--) {
					TopicPartition partition = pending.get(i);
					if (consumer.position(partition, remaining(deadline)) >= end.get(partition)) {
						pending.remove(i);
					}
				}
			}
			if (!pending.isEmpty()) {
				logger.warn("Read of " + topic + " timed out after " + timeoutMs + " ms; returning "
						+ messages.size() + " message(s)");
			}
		} finally {
			consumer.close(Duration.ofSeconds(5));
		}
		Collections.sort(messages, new Comparator<KafkaMessage>() {
			@Override
			public int compare(KafkaMessage a, KafkaMessage b) {
				if (a.getTimestamp() != b.getTimestamp()) {
					return a.getTimestamp() < b.getTimestamp() ? -1 : 1;
				}
				if (a.getPartition() != b.getPartition()) {
					return a.getPartition() < b.getPartition() ? -1 : 1;
				}
				return a.getOffset() < b.getOffset() ? -1 : (a.getOffset() == b.getOffset() ? 0 : 1);
			}
		});
		return messages;
	}

	private static Duration remaining(long deadline) {
		return Duration.ofMillis(Math.max(0, deadline - System.currentTimeMillis()));
	}

	private static KafkaConsumer<String, byte[]> createConsumer(Map<String, Object> config) {
		// As in KafkaSubscription: Kafka loads classes named in its config
		// through the context class loader, which inside OSGi may not see Kafka.
		Thread current = Thread.currentThread();
		ClassLoader previous = current.getContextClassLoader();
		current.setContextClassLoader(KafkaConsumer.class.getClassLoader());
		try {
			return new KafkaConsumer<String, byte[]>(config, new StringDeserializer(), new ByteArrayDeserializer());
		} finally {
			current.setContextClassLoader(previous);
		}
	}

	void removeSubscription(KafkaSubscription subscription) {
		subscriptions.remove(subscription);
	}

	protected Map<String, Object> createConfig(Map<String, ?> settings) {
		Map<String, Object> config = KafkaConfigUtils.copy(settings);
		Object groupId = config.get(ConsumerConfig.GROUP_ID_CONFIG);
		if (groupId == null || groupId.toString().trim().length() == 0) {
			// Private group: nobody else will resume it, so do not save positions
			// unless asked to. The broker drops such a group once it is empty.
			config.put(ConsumerConfig.GROUP_ID_CONFIG, "gumtree-" + UUID.randomUUID());
			if (!config.containsKey(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)) {
				config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
			}
		} else {
			config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId.toString().trim());
		}
		return config;
	}

}
