package org.gumtree.kafka.clients;

import java.nio.charset.StandardCharsets;

/**
 * A message received from Kafka.
 */
public final class KafkaMessage {

	private final String topic;

	private final int partition;

	private final long offset;

	private final long timestamp;

	private final String key;

	private final byte[] value;

	public KafkaMessage(String topic, int partition, long offset, long timestamp, String key, byte[] value) {
		this.topic = topic;
		this.partition = partition;
		this.offset = offset;
		this.timestamp = timestamp;
		this.key = key;
		this.value = value;
	}

	public String getTopic() {
		return topic;
	}

	public int getPartition() {
		return partition;
	}

	/**
	 * @return position of the message within its partition
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * @return milliseconds since the epoch, normally when the message was sent
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the key, may be null
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the raw value, may be null
	 */
	public byte[] getValue() {
		return value;
	}

	/**
	 * @return the value decoded as UTF-8 text, or null if there is no value
	 */
	public String getValueAsString() {
		return value == null ? null : new String(value, StandardCharsets.UTF_8);
	}

	@Override
	public String toString() {
		return "KafkaMessage[" + topic + "-" + partition + "@" + offset + ", key=" + key + "]";
	}

}
