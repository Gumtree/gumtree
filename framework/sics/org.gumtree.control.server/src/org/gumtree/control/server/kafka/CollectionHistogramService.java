package org.gumtree.control.server.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.gumtree.core.service.ServiceUtils;
import org.gumtree.kafka.clients.IKafkaConsumerService;
import org.gumtree.kafka.clients.IKafkaMessageListener;
import org.gumtree.kafka.clients.IKafkaProducer;
import org.gumtree.kafka.clients.IKafkaProducerService;
import org.gumtree.kafka.clients.IKafkaSubscription;
import org.gumtree.kafka.clients.KafkaMessage;
import org.gumtree.kafka.clients.da00.Da00DataArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Follows the histogram accumulated over the current collection, as the AESS
 * instrument backend's Active view does (a-ess-data-acquisition-frontend-project,
 * aess-instrument-backend: histogram_runtime.py and kafka_producer.py):
 * <ol>
 * <li>A StartCollection ControlEvent on the event topic launches a histogram
 * task on the histogram service, starting at the event's time, open-ended,
 * never cleared, published to the output topic every publish period.</li>
 * <li>While the collection runs, the task's lease is renewed on the heartbeat
 * topic.</li>
 * <li>Each da00 frame the service publishes for the task goes to the
 * {@link Listener}s and is kept as {@link #getLatestHistogram()}.</li>
 * <li>A StopCollection ControlEvent sets the task's end time; the service then
 * publishes the final histogram and finishes.</li>
 * </ol>
 * In addition:
 * <ul>
 * <li>A collection already running at {@link #start()} is picked up from its
 * start time (the last Start/StopCollection on the event topic is a start).</li>
 * <li>When the task's frames stop arriving, as after a histogram service
 * restart, the same launch (same ID and start time) is sent again, with waits
 * that double up to a maximum. After a stop, the task is relaunched with its end
 * time until the final frame arrives, a few times at most.</li>
 * <li>{@link #stop()} terminates a task whose collection is still running.</li>
 * </ul>
 * All state is kept on one worker thread; the Kafka listeners only parse
 * messages and hand them over. Listeners are called on that worker thread, so
 * they must return quickly.
 * <p>
 * Needs the org.gumtree.kafka.clients bundle, which this bundle requires
 * optionally: without it, this class cannot be loaded but the rest of the
 * bundle works. Settings come from {@link Settings}, by default from system
 * properties starting with {@value #PROPERTY_PREFIX}.
 */
public class CollectionHistogramService {

	private static final Logger logger = LoggerFactory.getLogger(CollectionHistogramService.class);

	public static final String PROPERTY_BROKERS = "gumtree.kafka.histogram.brokers";

	public static final String PROPERTY_PREFIX = "gumtree.kafka.histogram.";

	private static final String TASK_TYPE = "HistogramTask";

	private static final String TASK_SCHEMA = "https://www.nbi.ansto.gov.au/daq/test/schema/hist/histservice.json";

	private static final String START_COLLECTION = "StartCollection";

	private static final String STOP_COLLECTION = "StopCollection";

	private static final long SEND_TIMEOUT_MS = 10000;

	private static final long READY_TIMEOUT_MS = 15000;

	private static final long TICK_MS = 500;

	private static final long STOP_TIMEOUT_MS = 15000;

	/**
	 * Receives the collection's histograms. Called on the service's worker
	 * thread, so implementations must return quickly.
	 */
	public interface Listener {

		/**
		 * A collection started, or was found running at start-up, and its
		 * histogram task was launched.
		 *
		 * @param startTimeNs
		 *            the collection's start, nanoseconds since the Unix epoch
		 */
		public void collectionStarted(String taskId, long startTimeNs);

		/**
		 * A histogram frame of the current collection arrived. Frames
		 * accumulate from the collection's start, so each replaces the last.
		 *
		 * @param collectionRunning
		 *            false once the collection has stopped: the frame is then the
		 *            final one, or close to it
		 */
		public void histogramReceived(String taskId, Da00DataArray histogram, boolean collectionRunning);

		/**
		 * The collection stopped. The final frame normally follows.
		 *
		 * @param endTimeNs
		 *            the collection's end, nanoseconds since the Unix epoch
		 */
		public void collectionStopped(String taskId, long endTimeNs);

	}

	/**
	 * Empty {@link Listener}, to override only what is needed.
	 */
	public static class ListenerAdapter implements Listener {

		public void collectionStarted(String taskId, long startTimeNs) {
		}

		public void histogramReceived(String taskId, Da00DataArray histogram, boolean collectionRunning) {
		}

		public void collectionStopped(String taskId, long endTimeNs) {
		}

	}

	/**
	 * Settings, with defaults matching the AESS deployment on
	 * geishm-vm4 and Echidna's 128 x 128 detector. Each can be set with the
	 * system property {@value CollectionHistogramService#PROPERTY_PREFIX} plus
	 * the name given, on the command line or in the web server's
	 * gumtree.properties, which Gumtree loads as system properties at start;
	 * see {@link #fromSystemProperties()}.
	 */
	public static class Settings {

		/**
		 * "brokers": Kafka bootstrap servers, host:port[,host:port...]. Set in
		 * gumtree.properties of au.gov.ansto.bragg.nbi.webserver; this default
		 * applies only when the property is not set.
		 */
		public String brokers = System.getProperty(PROPERTY_BROKERS);

		/** "eventTopic": ControlEvent StartCollection/StopCollection */
		public String eventTopic = "instrument.events";

		/** "commandTopic": histogram service launch/update/terminate */
		public String commandTopic = "binit_command";

		/** "heartbeatTopic": histogram service keep-alive renewals */
		public String heartbeatTopic = "binit_heartbeat";

		/** "outputTopic": where the service publishes this task's histogram */
		public String outputTopic = "histogram.gumtree";

		/**
		 * "histogram": the histogram to accumulate, as JSON: the instrument's X-Y
		 * task from config/instruments/&lt;instrument&gt;/histogramtasks.json in
		 * the AESS project. publish_topic, start_time, end_time, keep_alive and
		 * publish_clear are set by the service.
		 */
		public String histogram = "{\"panel\": {\"base_id\": 0, \"nx\": 128, \"ny\": 128, \"along_y\": true},"
				+ " \"hz_axis\": \"X\","
				+ " \"hz_bins\": {\"minimum\": -0.5, \"step\": 1, \"maximum\": 127.5, \"units\": \"posn\"},"
				+ " \"vt_axis\": \"Y\","
				+ " \"vt_bins\": {\"minimum\": -0.5, \"step\": 1, \"maximum\": 127.5, \"units\": \"posn\"},"
				+ " \"dp_axis\": null, \"dp_bins\": null, \"publish_period\": 2.0}";

		/** "source": the Source field of the requests sent */
		public String source = "Gumtree";

		/** "keepAliveSeconds": lease of the open-ended task */
		public double keepAliveSeconds = 30;

		/** "renewSeconds": renew well inside the lease */
		public double renewSeconds = 10;

		/** "catchUp": pick up a collection already running at start */
		public boolean catchUp = true;

		/** "catchUpEvents": recent events to look through for it */
		public int catchUpEvents = 50;

		/**
		 * "catchUpMaxHours": ignore an older start (a lost StopCollection, or
		 * too much data for the service to replay)
		 */
		public double catchUpMaxHours = 12;

		/** "relaunch": relaunch the task when its frames stop */
		public boolean relaunch = true;

		/** "firstFrameSeconds": wait for the first frame after a launch */
		public double firstFrameSeconds = 60;

		/** "silenceSeconds": wait between frames once they flow */
		public double silenceSeconds = 15;

		/** "relaunchMaxSeconds": longest wait between relaunches */
		public double relaunchMaxSeconds = 300;

		/** "finishAttempts": relaunches to get the final frame after a stop */
		public int finishAttempts = 3;

		/**
		 * @return the defaults, overridden by any system properties set
		 */
		public static Settings fromSystemProperties() {
			Settings settings = new Settings();
			settings.brokers = string("brokers", settings.brokers);
			settings.eventTopic = string("eventTopic", settings.eventTopic);
			settings.commandTopic = string("commandTopic", settings.commandTopic);
			settings.heartbeatTopic = string("heartbeatTopic", settings.heartbeatTopic);
			settings.outputTopic = string("outputTopic", settings.outputTopic);
			settings.histogram = string("histogram", settings.histogram);
			settings.source = string("source", settings.source);
			settings.keepAliveSeconds = number("keepAliveSeconds", settings.keepAliveSeconds);
			settings.renewSeconds = number("renewSeconds", settings.renewSeconds);
			settings.catchUp = Boolean.parseBoolean(string("catchUp", String.valueOf(settings.catchUp)));
			settings.catchUpEvents = (int) number("catchUpEvents", settings.catchUpEvents);
			settings.catchUpMaxHours = number("catchUpMaxHours", settings.catchUpMaxHours);
			settings.relaunch = Boolean.parseBoolean(string("relaunch", String.valueOf(settings.relaunch)));
			settings.firstFrameSeconds = number("firstFrameSeconds", settings.firstFrameSeconds);
			settings.silenceSeconds = number("silenceSeconds", settings.silenceSeconds);
			settings.relaunchMaxSeconds = number("relaunchMaxSeconds", settings.relaunchMaxSeconds);
			settings.finishAttempts = (int) number("finishAttempts", settings.finishAttempts);
			return settings;
		}

		private static String string(String name, String defaultValue) {
			String value = System.getProperty(PROPERTY_PREFIX + name);
			return value == null || value.trim().length() == 0 ? defaultValue : value.trim();
		}

		private static double number(String name, double defaultValue) {
			String value = string(name, null);
			if (value == null) {
				return defaultValue;
			}
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				logger.warn("Ignoring " + PROPERTY_PREFIX + name + "=" + value + ": not a number");
				return defaultValue;
			}
		}

	}

	private final Settings settings;

	private final List<Listener> listeners = new CopyOnWriteArrayList<Listener>();

	private JSONObject histogramSpec;

	private IKafkaConsumerService consumerService;

	private IKafkaProducer producer;

	private IKafkaSubscription histogramSubscription;

	private IKafkaSubscription eventSubscription;

	// Volatile so that Kafka listeners hand over without taking this object's
	// lock, which stop() holds while it closes their subscriptions.
	private volatile ScheduledExecutorService worker;

	/** Released once start() has connected (or failed), see initialise() */
	private CountDownLatch connected;

	// Read by other threads
	private volatile String taskId;

	private volatile boolean collectionRunning;

	private volatile Da00DataArray latestHistogram;

	// Used only on the worker thread
	private Task task;

	/** Events up to these offsets, per partition, were handled by the catch-up */
	private final Map<Integer, Long> catchUpReadTo = new HashMap<Integer, Long>();

	private IKafkaProducerService producerService;

	/** Uses settings from system properties and the OSGi Kafka services. */
	public CollectionHistogramService() {
		this(Settings.fromSystemProperties());
	}

	/** Uses the OSGi Kafka services. */
	public CollectionHistogramService(Settings settings) {
		this(settings, null, null);
	}

	/**
	 * @param consumerService
	 *            null to use the OSGi service
	 * @param producerService
	 *            null to use the OSGi service
	 */
	public CollectionHistogramService(Settings settings, IKafkaConsumerService consumerService,
			IKafkaProducerService producerService) {
		if (settings == null) {
			throw new IllegalArgumentException("No settings given");
		}
		this.settings = settings;
		this.consumerService = consumerService;
		this.producerService = producerService;
	}

	/**
	 * Connects, picks up a running collection if there is one, and follows
	 * collections until {@link #stop()}. Returns once the connections are set
	 * up; the catch-up continues in the background.
	 *
	 * @throws IllegalStateException
	 *             if already started, or the Kafka services are not available
	 * @throws IllegalArgumentException
	 *             if the histogram setting is not valid JSON
	 */
	public synchronized void start() {
		if (worker != null) {
			throw new IllegalStateException("Collection histogram service already started");
		}
		try {
			histogramSpec = new JSONObject(settings.histogram);
		} catch (JSONException e) {
			throw new IllegalArgumentException("Histogram setting is not valid JSON: " + settings.histogram, e);
		}
		if (consumerService == null) {
			consumerService = ServiceUtils.getService(IKafkaConsumerService.class);
		}
		if (producerService == null) {
			producerService = ServiceUtils.getService(IKafkaProducerService.class);
		}
		if (consumerService == null || producerService == null) {
			throw new IllegalStateException("Kafka client services are not available");
		}
		worker = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "Collection histogram");
				thread.setDaemon(true);
				return thread;
			}
		});
		// Queued first, so it runs before anything a listener hands over: the
		// worker has one thread. It waits until the connections below exist.
		connected = new CountDownLatch(1);
		final CountDownLatch startConnected = connected;
		worker.execute(new Runnable() {
			public void run() {
				try {
					startConnected.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
				initialise();
			}
		});
		try {
			Map<String, Object> producerConfig = new HashMap<String, Object>();
			producerConfig.put("bootstrap.servers", settings.brokers);
			producerConfig.put("client.id", "gumtree-collection-histogram");
			producerConfig.put("acks", "all");
			producerConfig.put("max.block.ms", 10000);
			producer = producerService.createProducer(producerConfig);
			histogramSubscription = consumerService.subscribe(consumerConfig(), settings.outputTopic,
					new IKafkaMessageListener() {
						public void onMessage(KafkaMessage message) {
							onHistogram(message);
						}
					});
			eventSubscription = consumerService.subscribe(consumerConfig(), settings.eventTopic,
					new IKafkaMessageListener() {
						public void onMessage(KafkaMessage message) {
							onEvent(message);
						}
					});
		} catch (RuntimeException e) {
			worker.shutdownNow();
			worker = null;
			connected.countDown();
			closeConnections();
			throw e;
		}
		connected.countDown();
		worker.scheduleWithFixedDelay(new Runnable() {
			public void run() {
				tick();
			}
		}, TICK_MS, TICK_MS, TimeUnit.MILLISECONDS);
		logger.info("Collection histogram service started: events from " + settings.eventTopic
				+ ", histograms to " + settings.outputTopic + " on " + settings.brokers);
	}

	/**
	 * Terminates the task of a collection still running, and disconnects.
	 * Safe to call more than once.
	 */
	public synchronized void stop() {
		if (worker == null) {
			return;
		}
		Future<?> done = worker.submit(new Runnable() {
			public void run() {
				if (task != null && task.isRunning()) {
					// Nobody follows it any more; it would otherwise lapse
					// only after its lease.
					sendQuietly(settings.commandTopic, body("terminate", task.id), "terminate histogram " + task.id);
				}
			}
		});
		try {
			done.get(STOP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.warn("Could not terminate the histogram task while stopping", e);
		}
		worker.shutdownNow();
		worker = null;
		closeConnections();
		task = null;
		taskId = null;
		collectionRunning = false;
		logger.info("Collection histogram service stopped");
	}

	public synchronized boolean isStarted() {
		return worker != null;
	}

	public void addListener(Listener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	/**
	 * @return the newest frame of the current (or last) collection, or null
	 */
	public Da00DataArray getLatestHistogram() {
		return latestHistogram;
	}

	/**
	 * @return the ID of the current (or last) collection's histogram task, also
	 *         the source name of its frames; null before the first collection
	 */
	public String getTaskId() {
		return taskId;
	}

	public boolean isCollectionRunning() {
		return collectionRunning;
	}

	public Settings getSettings() {
		return settings;
	}

	// ---- Kafka listeners: parse, then hand over to the worker -------------

	private void onEvent(final KafkaMessage message) {
		final Event event = parseEvent(message);
		if (event == null) {
			return;
		}
		execute(new Runnable() {
			public void run() {
				handleEvent(event);
			}
		});
	}

	private void onHistogram(KafkaMessage message) {
		byte[] value = message.getValue();
		String current = taskId;
		if (current == null || !Da00DataArray.isDa00(value)) {
			return;
		}
		final Da00DataArray histogram;
		try {
			histogram = Da00DataArray.deserialise(value);
		} catch (IllegalArgumentException e) {
			logger.warn("Ignoring a corrupt da00 message on " + settings.outputTopic + ": " + e.getMessage());
			return;
		}
		// Other tasks may publish to the same topic; take only ours.
		if (!current.equals(histogram.getSourceName())) {
			return;
		}
		execute(new Runnable() {
			public void run() {
				handleHistogram(histogram);
			}
		});
	}

	private void execute(Runnable runnable) {
		ExecutorService current = worker;
		if (current != null && !current.isShutdown()) {
			try {
				current.execute(runnable);
			} catch (RuntimeException e) {
				// Stopping
			}
		}
	}

	// ---- Worker thread ------------------------------------------------------

	private void initialise() {
		for (IKafkaSubscription subscription : new IKafkaSubscription[] { histogramSubscription,
				eventSubscription }) {
			if (!subscription.waitUntilReady(READY_TIMEOUT_MS)) {
				// It keeps trying; messages arrive once it is ready.
				logger.warn("Subscription to " + subscription.getTopics() + " not ready after "
						+ READY_TIMEOUT_MS + " ms");
			}
		}
		if (settings.catchUp) {
			try {
				catchUp();
			} catch (RuntimeException e) {
				logger.error("Could not look for a running collection on " + settings.eventTopic, e);
			}
		}
		if (task == null) {
			logger.info("Waiting for " + START_COLLECTION + " on " + settings.eventTopic);
		}
	}

	private void catchUp() {
		// The event subscription is ready, so no event falls between it and
		// this read; events seen by both are told apart by offset.
		Event last = null;
		for (KafkaMessage message : consumerService.readLatest(consumerConfig(), settings.eventTopic,
				settings.catchUpEvents, READY_TIMEOUT_MS)) {
			Long readTo = catchUpReadTo.get(message.getPartition());
			if (readTo == null || readTo.longValue() < message.getOffset()) {
				catchUpReadTo.put(message.getPartition(), message.getOffset());
			}
			Event event = parseEvent(message);
			if (event != null) {
				last = event;
			}
		}
		if (last == null || !START_COLLECTION.equals(last.action)) {
			return;
		}
		double ageHours = (nowNs() - last.timestampNs) / 3.6e12;
		if (ageHours > settings.catchUpMaxHours) {
			logger.info(String.format("Not catching up: the last %s was %.1f hours ago", START_COLLECTION, ageHours));
			return;
		}
		logger.info("Collection already running; catching up");
		startTask(last.timestampNs);
	}

	private void handleEvent(Event event) {
		Long readTo = catchUpReadTo.get(event.partition);
		if (readTo != null && event.offset <= readTo.longValue()) {
			return; // already seen by the catch-up
		}
		if (START_COLLECTION.equals(event.action)) {
			if (task != null && task.isRunning()) {
				finishTask(event.timestampNs); // a new start ends the last one
			}
			startTask(event.timestampNs);
		} else if (task != null && task.isRunning()) {
			finishTask(event.timestampNs);
		} else {
			logger.info("Ignoring " + STOP_COLLECTION + ": no collection running");
		}
	}

	private void handleHistogram(Da00DataArray histogram) {
		Task current = task;
		if (current == null || !current.id.equals(histogram.getSourceName())) {
			return; // a late frame of a previous task
		}
		current.frameArrived();
		latestHistogram = histogram;
		for (Listener listener : listeners) {
			try {
				listener.histogramReceived(current.id, histogram, current.isRunning());
			} catch (RuntimeException e) {
				logger.error("Collection histogram listener failed", e);
			}
		}
	}

	private void tick() {
		try {
			if (task != null) {
				task.check();
			}
		} catch (RuntimeException e) {
			// An exception would cancel the schedule.
			logger.error("Collection histogram check failed", e);
		}
	}

	private void startTask(long startTimeNs) {
		Task newTask = new Task(startTimeNs);
		task = newTask;
		taskId = newTask.id; // frames of any previous task are ignored from now on
		latestHistogram = null;
		collectionRunning = true;
		newTask.launch();
		logger.info("Launched histogram " + newTask.id + " from " + startTimeNs);
		for (Listener listener : listeners) {
			try {
				listener.collectionStarted(newTask.id, startTimeNs);
			} catch (RuntimeException e) {
				logger.error("Collection histogram listener failed", e);
			}
		}
	}

	private void finishTask(long endTimeNs) {
		task.finish(endTimeNs);
		collectionRunning = false;
		logger.info("Collection stopped; ended histogram " + task.id + " at " + endTimeNs);
		for (Listener listener : listeners) {
			try {
				listener.collectionStopped(task.id, endTimeNs);
			} catch (RuntimeException e) {
				logger.error("Collection histogram listener failed", e);
			}
		}
	}

	/**
	 * The histogram task of one collection: launches it, keeps it alive while
	 * the collection runs, and relaunches it when its frames stop arriving.
	 * Used only on the worker thread. Times in ms are System.currentTimeMillis.
	 */
	private class Task {

		final String id = UUID.randomUUID().toString();

		final long startNs;

		long endNs; // 0 while the collection runs

		long endSentMs; // 0 until the end is sent

		boolean done; // final frame received, or given up

		long launchedMs;

		long lastRenewMs;

		long lastFrameMs;

		double waitSeconds = settings.firstFrameSeconds;

		int finishTries;

		Task(long startNs) {
			this.startNs = startNs;
		}

		boolean isRunning() {
			return endNs == 0;
		}

		void launch() {
			launchedMs = lastRenewMs = System.currentTimeMillis();
			sendQuietly(settings.commandTopic, launchBody(id, startNs, endNs), "launch histogram " + id);
		}

		void finish(long endTimeNs) {
			// The service then publishes the histogram up to the end, and
			// finishes.
			endNs = endTimeNs;
			endSentMs = System.currentTimeMillis();
			JSONObject update = new JSONObject();
			put(update, "end_time", endTimeNs);
			JSONObject body = body("update", id);
			put(body, "update", update);
			sendQuietly(settings.commandTopic, body, "end histogram " + id);
		}

		void frameArrived() {
			lastFrameMs = System.currentTimeMillis();
			waitSeconds = settings.silenceSeconds;
			if (endSentMs != 0) {
				// Taken as the final frame. One published just before the end
				// arrived may be taken instead; it is at most a publish period
				// short.
				done = true;
			}
		}

		void check() {
			if (done) {
				return;
			}
			long now = System.currentTimeMillis();
			if (isRunning() && now - lastRenewMs >= settings.renewSeconds * 1000) {
				lastRenewMs = now; // on failure, try again next interval
				JSONObject update = new JSONObject();
				put(update, "keep_alive", settings.keepAliveSeconds);
				JSONObject body = body("update", id);
				put(body, "update", update);
				sendQuietly(settings.heartbeatTopic, body, "renew histogram " + id);
			}
			if (!settings.relaunch) {
				return;
			}
			long since = Math.max(launchedMs, Math.max(lastFrameMs, endSentMs));
			if (now - since < waitSeconds * 1000) {
				return;
			}
			if (!isRunning()) {
				if (finishTries >= settings.finishAttempts) {
					logger.warn("No final histogram from " + id + " after " + finishTries
							+ " relaunches; giving up");
					done = true;
					return;
				}
				finishTries++;
			}
			logger.warn(String.format("No histogram from %s for %.0f s (service restarted?); relaunching", id,
					(now - since) / 1000.0));
			launch();
			// Allow for a replay after each launch, and back off while it
			// fails.
			waitSeconds = Math.min(Math.max(waitSeconds * 2, settings.firstFrameSeconds),
					settings.relaunchMaxSeconds);
		}

	}

	// ---- Requests to the histogram service ----------------------------------

	private JSONObject launchBody(String id, long startNs, long endNs) {
		JSONObject histogram;
		try {
			histogram = new JSONObject(histogramSpec.toString());
		} catch (JSONException e) {
			throw new IllegalStateException(e); // was valid at start
		}
		put(histogram, "publish_topic", settings.outputTopic);
		put(histogram, "start_time", startNs);
		put(histogram, "end_time", endNs); // 0: open-ended until StopCollection
		put(histogram, "publish_clear", false); // accumulate over the collection
		if (endNs == 0) {
			put(histogram, "keep_alive", settings.keepAliveSeconds);
		} else {
			histogram.remove("keep_alive");
		}
		JSONObject body = body("launch", id);
		put(body, "histogram", histogram);
		return body;
	}

	private static JSONObject body(String action, String id) {
		JSONObject body = new JSONObject();
		put(body, "action", action);
		put(body, "id", id);
		put(body, "task", 0);
		return body;
	}

	/** Sends a request; failures are logged, the watchdog recovers. */
	private void sendQuietly(String topic, JSONObject body, String what) {
		JSONObject message = new JSONObject();
		put(message, "Source", settings.source);
		put(message, "ID", UUID.randomUUID().toString());
		put(message, "Type", TASK_TYPE);
		put(message, "Schema", TASK_SCHEMA);
		put(message, "Body", body);
		try {
			producer.sendAndWait(topic, null, message.toString(), SEND_TIMEOUT_MS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Interrupted while trying to " + what);
		} catch (Exception e) {
			logger.error("Could not " + what + " on " + topic + ": " + e.getMessage());
		}
	}

	private static void put(JSONObject object, String key, Object value) {
		try {
			object.put(key, value);
		} catch (JSONException e) {
			throw new IllegalArgumentException(e); // only for NaN or a null key
		}
	}

	private static void put(JSONObject object, String key, long value) {
		try {
			object.put(key, value);
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static void put(JSONObject object, String key, double value) {
		try {
			object.put(key, value);
		} catch (JSONException e) {
			throw new IllegalArgumentException(e);
		}
	}

	// ---- Helpers ------------------------------------------------------------

	private static final class Event {

		final int partition;

		final long offset;

		final String action;

		final long timestampNs;

		Event(int partition, long offset, String action, long timestampNs) {
			this.partition = partition;
			this.offset = offset;
			this.action = action;
			this.timestampNs = timestampNs;
		}

	}

	/**
	 * @return the Start/StopCollection ControlEvent in a message, else null
	 */
	private static Event parseEvent(KafkaMessage message) {
		JSONObject event;
		try {
			event = new JSONObject(message.getValueAsString());
		} catch (JSONException e) {
			return null; // not JSON
		} catch (RuntimeException e) {
			return null; // no value
		}
		if (!"ControlEvent".equals(event.optString("Type"))) {
			return null;
		}
		JSONObject body = event.optJSONObject("Body");
		if (body == null) {
			return null;
		}
		String action = body.optString("Action");
		if (!START_COLLECTION.equals(action) && !STOP_COLLECTION.equals(action)) {
			return null;
		}
		Object stamp = body.opt("Timestamp");
		if (!(stamp instanceof Number)) {
			logger.warn("Ignoring " + action + " without a numeric Timestamp");
			return null;
		}
		long timestampNs = ((Number) stamp).longValue();
		return new Event(message.getPartition(), message.getOffset(), action,
				timestampNs != 0 ? timestampNs : nowNs());
	}

	private Map<String, Object> consumerConfig() {
		Map<String, Object> config = new HashMap<String, Object>();
		config.put("bootstrap.servers", settings.brokers);
		config.put("auto.offset.reset", "latest"); // only events and frames from now on
		// The output topic may not exist until the service first publishes to
		// it; check for it often instead of every 5 minutes (Kafka's default).
		config.put("metadata.max.age.ms", 5000);
		return config;
	}

	private void closeConnections() {
		if (eventSubscription != null) {
			eventSubscription.close();
			eventSubscription = null;
		}
		if (histogramSubscription != null) {
			histogramSubscription.close();
			histogramSubscription = null;
		}
		if (producer != null) {
			producer.close();
			producer = null;
		}
	}

	private static long nowNs() {
		return System.currentTimeMillis() * 1000000L;
	}

}
