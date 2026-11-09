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
	 * Waits until the subscription has joined its group, been given its
	 * partitions and fixed its starting position on each of them. Messages sent
	 * after this returns true are received, even with auto.offset.reset=latest.
	 * In a shared group with more members than partitions, this subscription may
	 * be given no partitions; it is then ready but receives nothing.
	 *
	 * @param timeoutMs
	 *            the longest time to wait, in milliseconds
	 * @return true if ready, false if the time ran out or the subscription was
	 *         closed first
	 */
	public boolean waitUntilReady(long timeoutMs);

	/**
	 * @return true once the subscription is ready (see
	 *         {@link #waitUntilReady(long)})
	 */
	public boolean isReady();

	/**
	 * Stops receiving messages and releases the connection. Waits briefly for a
	 * listener call in progress to finish. Safe to call more than once.
	 */
	public void close();

}
