package org.gumtree.kafka.clients;

import java.util.Collection;
import java.util.List;

/**
 * Delivers messages from Kafka topics to listeners. Each subscription has its
 * own connection and background thread, and runs until it is closed or this
 * service stops. Broker address and consumer settings come from
 * {@link KafkaProperties}.
 * <p>
 * Consumer groups decide who gets which message:
 * <ul>
 * <li>Without a group id, the subscription gets a private group, so it sees
 * every message sent to the topic while it runs. Where it starts follows
 * {@link KafkaProperties#CONSUMER_AUTO_OFFSET_RESET} (default: only new messages).
 * Use this for live monitoring.</li>
 * <li>Subscriptions sharing a group id split the topic's messages between them,
 * and the group remembers how far it got, so a restarted subscription carries on
 * where it stopped. Use this for processing each message once.</li>
 * </ul>
 * Get the service with
 * {@code ServiceUtils.getService(IKafkaConsumerService.class)}.
 */
public interface IKafkaConsumerService {

	/**
	 * Subscribes to one topic with a private group.
	 */
	public IKafkaSubscription subscribe(String topic, IKafkaMessageListener listener);

	/**
	 * Subscribes to one topic as a member of a consumer group.
	 *
	 * @param groupId null or empty for a private group
	 */
	public IKafkaSubscription subscribe(String topic, String groupId, IKafkaMessageListener listener);

	/**
	 * Subscribes to several topics as a member of a consumer group.
	 *
	 * @param groupId null or empty for a private group
	 */
	public IKafkaSubscription subscribe(Collection<String> topics, String groupId, IKafkaMessageListener listener);

	/**
	 * @return the subscriptions that are still active
	 */
	public List<IKafkaSubscription> getSubscriptions();

	/**
	 * @return the brokers this service receives from
	 */
	public String getBootstrapServers();

}
