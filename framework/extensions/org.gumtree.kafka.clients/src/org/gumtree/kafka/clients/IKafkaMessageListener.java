package org.gumtree.kafka.clients;

/**
 * Receives messages for a Kafka subscription.
 * <p>
 * Called on the subscription's own background thread, one message at a time and
 * in order per partition. Return quickly: a slow listener holds up later messages.
 * To update SWT widgets, hand the work to the UI thread (Display.asyncExec).
 * <p>
 * In Jython a plain function can be passed wherever this listener is expected.
 */
public interface IKafkaMessageListener {

	/**
	 * Called for each message. An exception thrown here is logged and the
	 * subscription carries on with the next message.
	 */
	public void onMessage(KafkaMessage message);

}
