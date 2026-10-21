package org.gumtree.kafka.clients;

import java.util.List;

/**
 * A running subscription created by {@link IKafkaConsumerService}. Close it when
 * the messages are no longer needed.
 */
public interface IKafkaSubscription {

	public List<String> getTopics();

	/**
	 * @return the consumer group this subscription belongs to
	 */
	public String getGroupId();

	/**
	 * @return true until the subscription is closed
	 */
	public boolean isActive();

	/**
	 * Stops receiving messages and releases the connection. Waits briefly for a
	 * listener call in progress to finish. Safe to call more than once.
	 */
	public void close();

}
