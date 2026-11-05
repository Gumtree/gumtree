package org.gumtree.kafka.clients.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.gumtree.kafka.clients.IKafkaProducer;
import org.gumtree.kafka.clients.IKafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerService implements IKafkaProducerService {

	private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

	private final List<ManagedKafkaProducer> producers = new CopyOnWriteArrayList<ManagedKafkaProducer>();

	private boolean active;

	public synchronized void activate() {
		active = true;
	}

	public void deactivate() {
		synchronized (this) {
			active = false;
		}
		if (!producers.isEmpty()) {
			logger.info("Closing " + producers.size() + " Kafka producer(s)");
		}
		for (ManagedKafkaProducer producer : producers) {
			producer.close();
		}
		producers.clear();
	}

	@Override
	public synchronized IKafkaProducer createProducer(Map<String, ?> config) {
		if (!active) {
			throw new IllegalStateException("Kafka producer service is not active");
		}
		ManagedKafkaProducer producer = new ManagedKafkaProducer(KafkaConfigUtils.copy(config), this);
		producers.add(producer);
		return producer;
	}

	@Override
	public List<IKafkaProducer> getProducers() {
		return new ArrayList<IKafkaProducer>(producers);
	}

	void removeProducer(ManagedKafkaProducer producer) {
		producers.remove(producer);
	}

}
