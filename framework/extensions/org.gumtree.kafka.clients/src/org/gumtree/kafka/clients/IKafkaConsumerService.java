package org.gumtree.kafka.clients;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Delivers messages from Kafka topics to listeners. Each subscription has its
 * own connection and background thread, and runs until it is closed or this
 * service stops.
 * <p>
 * Settings are given by the caller, for example a Jython dict, under their
 * Kafka names (see
 * https://kafka.apache.org/39/documentation.html#consumerconfigs).
 * bootstrap.servers is required; deserializers are set by the subscription.
 * The group.id setting decides who gets which message:
 * <ul>
 * <li>Without group.id, the subscription gets a private group, so it sees every
 * message sent to the topic while it runs. Where it starts follows
 * auto.offset.reset (Kafka's default, latest: only new messages). Use this for
 * live monitoring.</li>
 * <li>Subscriptions sharing a group.id split the topic's messages between them,
 * and the group remembers how far it got, so a restarted subscription carries on
 * where it stopped. Use this for processing each message once.</li>
 * </ul>
 * Get the service with
 * {@code ServiceUtils.getService(IKafkaConsumerService.class)}.
 */
public interface IKafkaConsumerService {

	/**
	 * Subscribes to one topic.
	 *
	 * @throws org.apache.kafka.common.KafkaException if a setting is missing or invalid
	 */
	public IKafkaSubscription subscribe(Map<String, ?> config, String topic, IKafkaMessageListener listener);

	/**
	 * Subscribes to several topics.
	 *
	 * @throws org.apache.kafka.common.KafkaException if a setting is missing or invalid
	 */
	public IKafkaSubscription subscribe(Map<String, ?> config, Collection<String> topics,
			IKafkaMessageListener listener);

	/**
	 * @return the subscriptions that are still active
	 */
	public List<IKafkaSubscription> getSubscriptions();

	/**
	 * Reads the most recent messages of a topic once, without subscribing: up to
	 * {@code count} messages from the end of each partition, as the topic stands
	 * when called. Useful to find the current state, such as the last event,
	 * before following new messages with {@link #subscribe}. Uses a temporary
	 * connection outside any group, so nothing is committed; group.id is ignored.
	 *
	 * @param count
	 *            the most messages to read from each partition
	 * @param timeoutMs
	 *            the longest time to spend; messages read by then are returned
	 * @return the messages, oldest first by timestamp; empty if the topic does
	 *         not exist or holds no messages
	 * @throws org.apache.kafka.common.KafkaException if a setting is missing or invalid
	 */
	public List<KafkaMessage> readLatest(Map<String, ?> config, String topic, int count, long timeoutMs);

}
