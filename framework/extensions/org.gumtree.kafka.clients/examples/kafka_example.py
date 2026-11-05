# Example: send and receive Kafka messages from a Gumtree Jython script.
#
# Needs a running Kafka broker; for a local dev broker see
# D:/GumtreeAI/test/README.md. Run it from the Gumtree Jython console:
#     execfile('<path>/kafka_example.py')

import time

from org.gumtree.core.service import ServiceUtils
from org.gumtree.kafka.clients import IKafkaConsumerService, IKafkaProducerService

# --- Settings ----------------------------------------------------------------
# Any Kafka client setting can go in these dicts, under its Kafka name:
#   producer: https://kafka.apache.org/39/documentation.html#producerconfigs
#   consumer: https://kafka.apache.org/39/documentation.html#consumerconfigs

BROKERS = 'localhost:9092'
TOPIC = 'gumtree-jython-example'

PRODUCER_CONFIG = {
    'bootstrap.servers': BROKERS,
    'client.id': 'gumtree-producer',
    'acks': 'all',              # wait until every replica has the message
    'compression.type': 'lz4',  # none, gzip, snappy, lz4 or zstd
    'max.block.ms': 10000,      # give up a send after 10 s if the broker is down
}

CONSUMER_CONFIG = {
    'bootstrap.servers': BROKERS,
    'auto.offset.reset': 'latest',  # only messages sent after subscribing
    # 'group.id': 'my-group',       # share the messages with other subscribers
}

# --- Receive -----------------------------------------------------------------

def on_message(message):
    # Runs on the subscription's own thread, so keep it short.
    print('received %s: %s' % (message.getKey(), message.getValueAsString()))

consumer_service = ServiceUtils.getService(IKafkaConsumerService)
subscription = consumer_service.subscribe(CONSUMER_CONFIG, TOPIC, on_message)

# --- Send --------------------------------------------------------------------

producer = ServiceUtils.getService(IKafkaProducerService).createProducer(PRODUCER_CONFIG)
try:
    time.sleep(3)  # the subscription only sees messages sent after it connects

    metadata = producer.sendAndWait(TOPIC, 'status', 'scan started', 5000)
    print('stored at partition %d, offset %d' % (metadata.partition(), metadata.offset()))

    for point in range(3):
        producer.send(TOPIC, 'point', 'measured point %d' % point)
    producer.flush()

    time.sleep(2)  # let the messages come back
finally:
    # Each producer and subscription keeps a connection open until closed.
    producer.close()
    subscription.close()
