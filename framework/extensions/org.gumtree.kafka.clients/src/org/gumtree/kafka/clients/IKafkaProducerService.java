package org.gumtree.kafka.clients;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Sends messages to Kafka topics using one shared, thread-safe producer.
 * Broker address and producer settings come from {@link KafkaProperties}.
 * <p>
 * Keys are strings and may be null. Messages with the same key always go to the
 * same partition, so they are delivered in the order they were sent.
 * <p>
 * Get the service with
 * {@code ServiceUtils.getService(IKafkaProducerService.class)}.
 */
public interface IKafkaProducerService {

	/**
	 * Sends a text message (UTF-8) without waiting for the broker.
	 *
	 * @return completes when the broker acknowledges the message, or fails
	 */
	public Future<RecordMetadata> send(String topic, String key, String value);

	/**
	 * Sends a binary message without waiting for the broker.
	 *
	 * @return completes when the broker acknowledges the message, or fails
	 */
	public Future<RecordMetadata> send(String topic, String key, byte[] value);

	/**
	 * Sends a binary message without waiting for the broker. The callback runs on
	 * the producer's I/O thread when the send succeeds or fails, so it must be quick.
	 *
	 * @param callback may be null
	 * @return completes when the broker acknowledges the message, or fails
	 */
	public Future<RecordMetadata> send(String topic, String key, byte[] value, Callback callback);

	/**
	 * Sends a text message (UTF-8) and waits for the broker to acknowledge it.
	 *
	 * @return where the message was stored (partition and offset)
	 * @throws ExecutionException if the broker rejected the message or could not be reached
	 * @throws TimeoutException if there was no answer within the timeout
	 */
	public RecordMetadata sendAndWait(String topic, String key, String value, long timeoutMs)
			throws InterruptedException, ExecutionException, TimeoutException;

	/**
	 * Blocks until every message sent so far has been acknowledged or has failed.
	 */
	public void flush();

	/**
	 * @return the brokers this service sends to
	 */
	public String getBootstrapServers();

}
