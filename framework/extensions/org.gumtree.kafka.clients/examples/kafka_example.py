# Example: send and receive Kafka messages from a Gumtree Jython script, using
# the services in org.gumtree.kafka.clients.
#
# Needs a running Kafka broker. The address comes from the setting
# gumtree.kafka.bootstrapServers (default localhost:9092); for a local dev
# broker see D:/GumtreeAI/test/README.md.
#
# Run it from the Gumtree Jython console:
#     execfile('<path>/kafka_example.py')

import json
import time

from org.gumtree.core.service import ServiceUtils
from org.gumtree.kafka.clients import IKafkaConsumerService, IKafkaProducerService

TOPIC = 'gumtree-jython-example'

producer = ServiceUtils.getService(IKafkaProducerService)
consumer = ServiceUtils.getService(IKafkaConsumerService)
print('Kafka brokers: ' + producer.getBootstrapServers())


# --- Receive -----------------------------------------------------------------

received = []

def on_message(message):
    # Runs on the subscription's own background thread, so keep it short.
    # A plain function works wherever Java expects an IKafkaMessageListener.
    received.append(message)
    if message.getKey() == 'raw':
        value = list(message.getValue())
    else:
        value = message.getValueAsString()
    print('received key=%s value=%s (partition %d, offset %d)' % (
        message.getKey(), value, message.getPartition(), message.getOffset()))

# No group id: this subscription gets every message sent from now on.
# Pass a group id as the second argument to share the work with other
# subscribers instead: consumer.subscribe(TOPIC, 'my-group', on_message)
subscription = consumer.subscribe(TOPIC, on_message)

try:
    # The subscription only sees messages sent after it has connected.
    time.sleep(3)

    # --- Send --------------------------------------------------------------

    # Wait for the broker to confirm, up to 5 seconds.
    metadata = producer.sendAndWait(TOPIC, 'status', 'scan started', 5000)
    print('sent to partition %d, offset %d' % (metadata.partition(), metadata.offset()))

    # Send without waiting. Messages with the same key keep their order.
    for point in range(3):
        producer.send(TOPIC, 'point', 'measured point %d' % point)

    # Structured data: encode it as JSON text.
    reading = {'sample': 'Al2O3', 'temperature': 295.3, 'counts': [10, 42, 17]}
    producer.send(TOPIC, 'reading', json.dumps(reading))

    # Called when the broker confirms or the send fails. A plain function works
    # wherever Java expects a Kafka Callback; it runs on Kafka's I/O thread.
    def on_sent(metadata, error):
        if error is None:
            print('binary message stored at offset %d' % metadata.offset())
        else:
            print('binary message failed: %s' % error)

    # Binary data goes as a Java byte array.
    from jarray import array
    producer.send(TOPIC, 'raw', array([1, 2, 3, 4], 'b'), on_sent)

    # Block until everything sent so far is confirmed.
    producer.flush()

    # --- Wait for the messages to come back ---------------------------------

    deadline = time.time() + 10
    while len(received) < 6 and time.time() < deadline:
        time.sleep(0.2)
    print('received %d of 6 messages' % len(received))

    for message in received:
        if message.getKey() == 'reading':
            data = json.loads(message.getValueAsString())
            print('reading: %s at %.1f K, counts %s' % (
                data['sample'], data['temperature'], data['counts']))
        elif message.getKey() == 'raw':
            print('raw bytes: %s' % list(message.getValue()))

finally:
    # Always close subscriptions you no longer need; each one keeps a thread
    # and a connection open.
    subscription.close()
    print('subscription closed')
