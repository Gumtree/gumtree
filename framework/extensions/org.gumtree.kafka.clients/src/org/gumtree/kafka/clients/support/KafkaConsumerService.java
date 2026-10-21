package org.gumtree.kafka.clients.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.gumtree.kafka.clients.IKafkaConsumerService;
import org.gumtree.kafka.clients.IKafkaMessageListener;
import org.gumtree.kafka.clients.IKafkaSubscription;
import org.gumtree.kafka.clients.KafkaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaConsumerService implements IKafkaConsumerService {

	private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

	private final List<KafkaSubscription> subscriptions = new CopyOnWriteArrayList<KafkaSubscription>();

	// Numbers client ids, which Kafka expects to be unique within a process.
	private final AtomicInteger clientCount = new AtomicInteger();

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
	public IKafkaSubscription subscribe(String topic, IKafkaMessageListener listener) {
		return subscribe(Collections.singletonList(topic), null, listener);
	}

	@Override
	public IKafkaSubscription subscribe(String topic, String groupId, IKafkaMessageListener listener) {
		return subscribe(Collections.singletonList(topic), groupId, listener);
	}

	@Override
	public IKafkaSubscription subscribe(Collection<String> topics, String groupId, IKafkaMessageListener listener) {
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
			KafkaSubscription subscription = new KafkaSubscription(createConfig(groupId),
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
	public String getBootstrapServers() {
		return KafkaProperties.BOOTSTRAP_SERVERS.getValue();
	}

	void removeSubscription(KafkaSubscription subscription) {
		subscriptions.remove(subscription);
	}

	protected Properties createConfig(String groupId) {
		Properties config = new Properties();
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaProperties.BOOTSTRAP_SERVERS.getValue());
		config.put(ConsumerConfig.CLIENT_ID_CONFIG,
				KafkaProperties.CONSUMER_CLIENT_ID.getValue() + "-" + clientCount.incrementAndGet());
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, KafkaProperties.CONSUMER_AUTO_OFFSET_RESET.getValue());
		if (groupId == null || groupId.trim().length() == 0) {
			// Private group: nobody else will resume it, so do not save positions.
			// The broker drops such a group once it is empty.
			config.put(ConsumerConfig.GROUP_ID_CONFIG, "gumtree-" + UUID.randomUUID());
			config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		} else {
			config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId.trim());
			config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		}
		KafkaConfigUtils.loadConfigFile(config, KafkaProperties.CONSUMER_CONFIG_FILE);
		return config;
	}

}
