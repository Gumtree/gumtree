package org.gumtree.kafka.clients;

import java.util.List;
import java.util.Map;

/**
 * Creates Kafka producers from settings given by the caller, for example a
 * Jython dict. Producers still open when this service stops are closed.
 * <p>
 * Get the service with
 * {@code ServiceUtils.getService(IKafkaProducerService.class)}.
 */
public interface IKafkaProducerService {

	/**
	 * Creates a producer. It connects to the broker on the first send.
	 *
	 * @param config Kafka producer settings under their Kafka names, for example
	 *            bootstrap.servers, acks or compression.type (see
	 *            https://kafka.apache.org/39/documentation.html#producerconfigs).
	 *            bootstrap.servers is required. Serializers are set by the producer.
	 * @throws org.apache.kafka.common.KafkaException if a setting is missing or invalid
	 */
	public IKafkaProducer createProducer(Map<String, ?> config);

	/**
	 * @return the producers that are still open
	 */
	public List<IKafkaProducer> getProducers();

}
