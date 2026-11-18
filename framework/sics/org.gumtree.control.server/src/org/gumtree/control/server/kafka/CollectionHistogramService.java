package org.gumtree.control.server.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
 * Follows the histograms of the current collection: X-Y, accumulated since the
 * collection's start, and X-T and Y-T, each frame the counts of the last 2 s
 * (100 frames of 20 ms) (see {@link Settings#xyHistogram},
 * {@link Settings#xtHistogram} and {@link Settings#ytHistogram}), as the AESS
 * instrument backend's Active view does for one histogram
 * (a-ess-data-acquisition-frontend-project, aess-instrument-backend:
 * histogram_runtime.py and kafka_producer.py):
 * <ol>
 * <li>A StartCollection ControlEvent on the event topic launches one histogram
 * task per histogram on the histogram service, starting at the event's time,
 * open-ended, published to the output topic every publish period.</li>
 * <li>While the collection runs, the tasks' leases are renewed on the heartbeat
 * topic.</li>
 * <li>Each da00 frame the service publishes for a task goes to the
 * {@link Listener}s with its kind ({@link #XY}, {@link #XT} or {@link #YT}),
 * and is kept as {@link #getLatestHistogram(String)}.</li>
 * <li>A StopCollection ControlEvent sets the tasks' end time; the service then
 * publishes the final histograms and finishes.</li>
 * </ol>
 * In addition:
 * <ul>
 * <li>A collection already running at {@link #start()} is picked up from its
 * start time (the last Start/StopCollection on the event topic is a start).</li>
 * <li>When a task's frames stop arriving, as after a histogram service
 * restart, the same launch (same ID and start time) is sent again, with waits
 * that double up to a maximum. After a stop, a task is relaunched with its end
 * time until its final frame arrives, a few times at most.</li>
 * <li>{@link #stop()} terminates the tasks of a collection still running.</li>
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

	/** The X-Y histogram: X across, Y up */
	public static final String XY = "xy";

	/** The X-T histogram: X across, T up */
	public static final String XT = "xt";

	/** The Y-T histogram: T across, Y up */
	public static final String YT = "yt";

	/** The kinds of histogram collected, in this order */
	public static final List<String> KINDS = Collections.unmodifiableList(Arrays.asList(XY, XT, YT));

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
		 * histogram tasks were launched.
		 *
		 * @param collectionId
		 *            identifies the collection in the calls that follow
		 * @param startTimeNs
		 *            the collection's start, nanoseconds since the Unix epoch
		 */
		public void collectionStarted(String collectionId, long startTimeNs);

		/**
		 * A histogram frame of the current collection arrived. Frames
		 * accumulate from the collection's start, so each replaces the last of
		 * its kind.
		 *
		 * @param kind
		 *            {@link CollectionHistogramService#XY},
		 *            {@link CollectionHistogramService#XT} or
		 *            {@link CollectionHistogramService#YT}
		 * @param collectionRunning
		 *            false once the collection has stopped: the frame is then the
		 *            final one, or close to it
		 */
		public void histogramReceived(String collectionId, String kind, Da00DataArray histogram,
				boolean collectionRunning);

		/**
		 * The collection stopped. The final frames normally follow.
		 *
		 * @param endTimeNs
		 *            the collection's end, nanoseconds since the Unix epoch
		 */
		public void collectionStopped(String collectionId, long endTimeNs);

	}

	/**
	 * Empty {@link Listener}, to override only what is needed.
	 */
	public static class ListenerAdapter implements Listener {

		public void collectionStarted(String collectionId, long startTimeNs) {
		}

		public void histogramReceived(String collectionId, String kind, Da00DataArray histogram,
				boolean collectionRunning) {
		}

		public void collectionStopped(String collectionId, long endTimeNs) {
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

		// Parts of the default histograms: Echidna's 128 x 128 detector, and T
		// over a 20 ms frame in 1000 bins of 20 microseconds.
		private static final String PANEL = "\"panel\": {\"base_id\": 0, \"nx\": 128, \"ny\": 128, \"along_y\": true}";

		private static final String POSITION_BINS = "{\"minimum\": -0.5, \"step\": 1, \"maximum\": 127.5, \"units\": \"posn\"}";

		private static final String TIME_BINS = "{\"minimum\": 0, \"step\": 20, \"maximum\": 20000, \"units\": \"usec\"}";

		private static final String X_ACROSS = "\"hz_axis\": \"X\", \"hz_bins\": " + POSITION_BINS;

		private static final String T_ACROSS = "\"hz_axis\": \"T\", \"hz_bins\": " + TIME_BINS;

		private static final String Y_UP = "\"vt_axis\": \"Y\", \"vt_bins\": " + POSITION_BINS;

		private static final String T_UP = "\"vt_axis\": \"T\", \"vt_bins\": " + TIME_BINS;

		private static final String NO_DEPTH = "\"dp_axis\": null, \"dp_bins\": null";

		/** Published every 2 s, counting from the collection's start */
		private static final String ACCUMULATED = NO_DEPTH + ", \"publish_period\": 2.0, \"publish_clear\": false";

		/** Published every 2 s (100 frames of 20 ms), each the counts of those 2 s only */
		private static final String WINDOW = NO_DEPTH + ", \"publish_period\": 2.0, \"publish_clear\": true";

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

		/** "outputTopic": where the service publishes this service's histograms */
		public String outputTopic = "histogram.gumtree";

		/**
		 * "xyHistogram": the X-Y histogram, as JSON, in the histogram service's
		 * HistogramSpec format (as in
		 * config/instruments/&lt;instrument&gt;/histogramtasks.json of the AESS
		 * project): X across (hz_axis), Y up (vt_axis), accumulated since the
		 * collection's start (publish_clear false). publish_topic, start_time,
		 * end_time and keep_alive are set by this service. Set it empty to not
		 * collect this histogram.
		 * <p>
		 * publish_clear decides how a histogram is launched: false accumulates
		 * from the collection's start; true makes each frame the counts of one
		 * publish_period only, a window that starts when the task is launched
		 * (see {@link Task#launch()}).
		 */
		public String xyHistogram = "{" + PANEL + ", " + X_ACROSS + ", " + Y_UP + ", " + ACCUMULATED + "}";

		/**
		 * "xtHistogram": the X-T histogram, as for {@link #xyHistogram}: X
		 * across in 128 bins, T up in 1000 bins of 20 microseconds over a 20 ms
		 * frame. Each frame integrates the last 100 such 20 ms frames: it is
		 * published every 2 s and then cleared. 128 x 1000 32-bit counts make
		 * 512 kB per frame, inside Kafka's default message limit of 1 MB.
		 */
		public String xtHistogram = "{" + PANEL + ", " + X_ACROSS + ", " + T_UP + ", " + WINDOW + "}";

		/**
		 * "ytHistogram": the Y-T histogram, as {@link #xtHistogram} but T across
		 * in 1000 bins and Y up in 128 bins.
		 */
		public String ytHistogram = "{" + PANEL + ", " + T_ACROSS + ", " + Y_UP + ", " + WINDOW + "}";

		/** "source": the Source field of the requests sent */
		public String source = "Gumtree";

		/** "keepAliveSeconds": lease of the open-ended tasks */
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

		/**
		 * "catchUpStopped": when no collection is running at start, ask for the
		 * final histograms of the last one, so that clients show them until the
		 * next collection
		 */
		public boolean catchUpStopped = true;

		/** "relaunch": relaunch a task when its frames stop */
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
			settings.xyHistogram = histogram("xyHistogram", settings.xyHistogram);
			settings.xtHistogram = histogram("xtHistogram", settings.xtHistogram);
			settings.ytHistogram = histogram("ytHistogram", settings.ytHistogram);
			settings.source = string("source", settings.source);
			settings.keepAliveSeconds = number("keepAliveSeconds", settings.keepAliveSeconds);
			settings.renewSeconds = number("renewSeconds", settings.renewSeconds);
			settings.catchUp = Boolean.parseBoolean(string("catchUp", String.valueOf(settings.catchUp)));
			settings.catchUpEvents = (int) number("catchUpEvents", settings.catchUpEvents);
			settings.catchUpMaxHours = number("catchUpMaxHours", settings.catchUpMaxHours);
			settings.catchUpStopped = Boolean.parseBoolean(string("catchUpStopped",
					String.valueOf(settings.catchUpStopped)));
			settings.relaunch = Boolean.parseBoolean(string("relaunch", String.valueOf(settings.relaunch)));
			settings.firstFrameSeconds = number("firstFrameSeconds", settings.firstFrameSeconds);
			settings.silenceSeconds = number("silenceSeconds", settings.silenceSeconds);
			settings.relaunchMaxSeconds = number("relaunchMaxSeconds", settings.relaunchMaxSeconds);
			settings.finishAttempts = (int) number("finishAttempts", settings.finishAttempts);
			return settings;
		}

		/**
		 * @return the histograms to collect, by kind, in the order of
		 *         {@link CollectionHistogramService#KINDS}; those set empty are
		 *         left out
		 */
		public Map<String, String> histograms() {
			Map<String, String> histograms = new LinkedHashMap<String, String>();
			String[] requests = { xyHistogram, xtHistogram, ytHistogram };
			for (int i = 0; i < requests.length; i++) {
				if (requests[i] != null && requests[i].trim().length() > 0) {
					histograms.put(KINDS.get(i), requests[i].trim());
				}
			}
			return histograms;
		}

		private static String string(String name, String defaultValue) {
			String value = System.getProperty(PROPERTY_PREFIX + name);
			return value == null || value.trim().length() == 0 ? defaultValue : value.trim();
		}

		/** Unlike other settings, a histogram set empty stays empty: not collected. */
		private static String histogram(String name, String defaultValue) {
			String value = System.getProperty(PROPERTY_PREFIX + name);
			return value == null ? defaultValue : value.trim();
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

	/** The histograms to collect, by kind, set at start */
	private Map<String, JSONObject> histogramSpecs;

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
	private volatile String collectionId;

	/** The current collection's task IDs (its frames' source names) */
	private volatile Map<String, String> taskKinds = Collections.emptyMap();

	private volatile boolean collectionRunning;

	private final Map<String, Da00DataArray> latestHistograms = new ConcurrentHashMap<String, Da00DataArray>();

	// Used only on the worker thread
	private Collection collection;

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
	 *             if a histogram setting is not valid JSON, or all are empty
	 */
	public synchronized void start() {
		if (worker != null) {
			throw new IllegalStateException("Collection histogram service already started");
		}
		Map<String, JSONObject> specs = new LinkedHashMap<String, JSONObject>();
		for (Map.Entry<String, String> histogram : settings.histograms().entrySet()) {
			try {
				specs.put(histogram.getKey(), new JSONObject(histogram.getValue()));
			} catch (JSONException e) {
				throw new IllegalArgumentException("The " + histogram.getKey()
						+ " histogram setting is not valid JSON: " + histogram.getValue(), e);
			}
		}
		if (specs.isEmpty()) {
			throw new IllegalArgumentException("No histogram to collect: the histogram settings are all empty");
		}
		histogramSpecs = specs;
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
		logger.info("Collection histogram service started: events from " + settings.eventTopic + ", "
				+ histogramSpecs.keySet() + " histograms to " + settings.outputTopic + " on " + settings.brokers);
	}

	/**
	 * Terminates the tasks of a collection still running, and disconnects.
	 * Safe to call more than once.
	 */
	public synchronized void stop() {
		if (worker == null) {
			return;
		}
		Future<?> done = worker.submit(new Runnable() {
			public void run() {
				if (collection != null && collection.isRunning()) {
					// Nobody follows them any more; they would otherwise lapse
					// only after their lease.
					for (Task task : collection.tasks) {
						sendQuietly(settings.commandTopic, body("terminate", task.id), "terminate " + task);
					}
				}
			}
		});
		try {
			done.get(STOP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			logger.warn("Could not terminate the histogram tasks while stopping", e);
		}
		worker.shutdownNow();
		worker = null;
		closeConnections();
		collection = null;
		collectionId = null;
		taskKinds = Collections.emptyMap();
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
	 * @param kind
	 *            {@link #XY}, {@link #XT} or {@link #YT}
	 * @return the newest frame of that kind of the current (or last)
	 *         collection, or null
	 */
	public Da00DataArray getLatestHistogram(String kind) {
		return kind == null ? null : latestHistograms.get(kind);
	}

	/**
	 * @return the ID of the current (or last) collection; null before the first
	 */
	public String getCollectionId() {
		return collectionId;
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
		Map<String, String> current = taskKinds;
		if (current.isEmpty() || !Da00DataArray.isDa00(value)) {
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
		if (!current.containsKey(histogram.getSourceName())) {
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
		if (collection == null) {
			logger.info("Waiting for " + START_COLLECTION + " on " + settings.eventTopic);
		}
	}

	private void catchUp() {
		// The event subscription is ready, so no event falls between it and
		// this read; events seen by both are told apart by offset.
		List<Event> events = new ArrayList<Event>();
		for (KafkaMessage message : consumerService.readLatest(consumerConfig(), settings.eventTopic,
				settings.catchUpEvents, READY_TIMEOUT_MS)) {
			Long readTo = catchUpReadTo.get(message.getPartition());
			if (readTo == null || readTo.longValue() < message.getOffset()) {
				catchUpReadTo.put(message.getPartition(), message.getOffset());
			}
			Event event = parseEvent(message);
			if (event != null) {
				events.add(event);
			}
		}
		if (events.isEmpty()) {
			return;
		}
		Collections.sort(events, new Comparator<Event>() {
			public int compare(Event a, Event b) {
				return a.timestampNs < b.timestampNs ? -1 : a.timestampNs > b.timestampNs ? 1 : 0;
			}
		});
		Event last = events.get(events.size() - 1);
		if (START_COLLECTION.equals(last.action)) {
			if (tooOld(last)) {
				return;
			}
			logger.info("Collection already running; catching up");
			startCollection(last.timestampNs);
			return;
		}
		if (!settings.catchUpStopped) {
			return;
		}
		// No collection running: show the last one, from its start to its stop.
		Event start = null;
		for (int i = events.size() - 2; i >= 0 && start == null; i--) {
			if (START_COLLECTION.equals(events.get(i).action)) {
				start = events.get(i);
			}
		}
		if (start == null) {
			logger.info("Not showing the last collection: its " + START_COLLECTION + " is not in the last "
					+ settings.catchUpEvents + " events");
			return;
		}
		if (tooOld(start) || last.timestampNs <= start.timestampNs) {
			return;
		}
		logger.info("No collection running; asking for the histograms of the last one");
		startStoppedCollection(start.timestampNs, last.timestampNs);
	}

	private boolean tooOld(Event start) {
		double ageHours = (nowNs() - start.timestampNs) / 3.6e12;
		if (ageHours > settings.catchUpMaxHours) {
			logger.info(String.format("Not catching up: the last %s was %.1f hours ago", START_COLLECTION, ageHours));
			return true;
		}
		return false;
	}

	private void handleEvent(Event event) {
		Long readTo = catchUpReadTo.get(event.partition);
		if (readTo != null && event.offset <= readTo.longValue()) {
			return; // already seen by the catch-up
		}
		if (START_COLLECTION.equals(event.action)) {
			if (collection != null && collection.isRunning()) {
				finishCollection(event.timestampNs); // a new start ends the last one
			}
			startCollection(event.timestampNs);
		} else if (collection != null && collection.isRunning()) {
			finishCollection(event.timestampNs);
		} else {
			logger.info("Ignoring " + STOP_COLLECTION + ": no collection running");
		}
	}

	private void handleHistogram(Da00DataArray histogram) {
		Collection current = collection;
		Task task = current == null ? null : current.task(histogram.getSourceName());
		if (task == null) {
			return; // a late frame of a previous collection
		}
		task.frameArrived();
		latestHistograms.put(task.kind, histogram);
		for (Listener listener : listeners) {
			try {
				listener.histogramReceived(current.id, task.kind, histogram, current.isRunning());
			} catch (RuntimeException e) {
				logger.error("Collection histogram listener failed", e);
			}
		}
	}

	private void tick() {
		try {
			if (collection != null) {
				for (Task task : collection.tasks) {
					task.check();
				}
			}
		} catch (RuntimeException e) {
			// An exception would cancel the schedule.
			logger.error("Collection histogram check failed", e);
		}
	}

	/**
	 * Makes a new collection current; frames of any previous one are ignored
	 * from now on.
	 *
	 * @param endTimeNs
	 *            0 for a collection still running
	 */
	private Collection newCollection(long startTimeNs, long endTimeNs) {
		Collection newCollection = new Collection(startTimeNs, endTimeNs);
		Map<String, String> kinds = new HashMap<String, String>();
		for (Task task : newCollection.tasks) {
			kinds.put(task.id, task.kind);
		}
		collection = newCollection;
		collectionId = newCollection.id;
		taskKinds = Collections.unmodifiableMap(kinds);
		latestHistograms.clear();
		collectionRunning = endTimeNs == 0;
		return newCollection;
	}

	private void startCollection(long startTimeNs) {
		Collection newCollection = newCollection(startTimeNs, 0);
		for (Task task : newCollection.tasks) {
			task.launch();
		}
		logger.info("Launched histograms " + newCollection.tasks + " from " + startTimeNs);
		for (Listener listener : listeners) {
			try {
				listener.collectionStarted(newCollection.id, startTimeNs);
			} catch (RuntimeException e) {
				logger.error("Collection histogram listener failed", e);
			}
		}
	}

	/**
	 * Launches the histograms of a collection that has already stopped, for
	 * their final frames. Clients see it start and stop at once, then get the
	 * frames.
	 */
	private void startStoppedCollection(long startTimeNs, long endTimeNs) {
		Collection newCollection = newCollection(startTimeNs, endTimeNs);
		for (Task task : newCollection.tasks) {
			task.endSentMs = System.currentTimeMillis(); // the first frame is the final one
			task.launch();
		}
		logger.info("Launched histograms " + newCollection.tasks + " from " + startTimeNs + " to " + endTimeNs);
		for (Listener listener : listeners) {
			try {
				listener.collectionStarted(newCollection.id, startTimeNs);
				listener.collectionStopped(newCollection.id, endTimeNs);
			} catch (RuntimeException e) {
				logger.error("Collection histogram listener failed", e);
			}
		}
	}

	private void finishCollection(long endTimeNs) {
		collection.endNs = endTimeNs;
		for (Task task : collection.tasks) {
			task.finish(endTimeNs);
		}
		collectionRunning = false;
		logger.info("Collection stopped; ended histograms " + collection.tasks + " at " + endTimeNs);
		for (Listener listener : listeners) {
			try {
				listener.collectionStopped(collection.id, endTimeNs);
			} catch (RuntimeException e) {
				logger.error("Collection histogram listener failed", e);
			}
		}
	}

	/**
	 * One collection: a histogram task for each kind collected, sharing its
	 * start and end. Used only on the worker thread.
	 */
	private class Collection {

		final String id = UUID.randomUUID().toString();

		final long startNs;

		long endNs; // 0 while the collection runs

		final List<Task> tasks = new ArrayList<Task>();

		Collection(long startNs, long endNs) {
			this.startNs = startNs;
			this.endNs = endNs;
			for (String kind : histogramSpecs.keySet()) {
				tasks.add(new Task(this, kind));
			}
		}

		boolean isRunning() {
			return endNs == 0;
		}

		/**
		 * @return the task whose frames have this source name, or null
		 */
		Task task(String sourceName) {
			for (Task task : tasks) {
				if (task.id.equals(sourceName)) {
					return task;
				}
			}
			return null;
		}

	}

	/**
	 * The histogram task of one kind for a collection: launches it, keeps it
	 * alive while the collection runs, and relaunches it when its frames stop
	 * arriving. Used only on the worker thread. Times in ms are
	 * System.currentTimeMillis.
	 */
	private class Task {

		final String id = UUID.randomUUID().toString();

		final Collection collection;

		/** xy, xt or yt */
		final String kind;

		long endSentMs; // 0 until the end is sent

		boolean done; // final frame received, or given up

		long launchedMs;

		long lastRenewMs;

		long lastFrameMs;

		double waitSeconds = settings.firstFrameSeconds;

		int finishTries;

		Task(Collection collection, String kind) {
			this.collection = collection;
			this.kind = kind;
		}

		boolean isRunning() {
			return collection.isRunning();
		}

		/**
		 * Asks for the histogram from the collection's start, or, for a window
		 * histogram (publish_clear), from now: its frames hold only recent
		 * counts, so replaying the collection so far would only delay them. For a
		 * collection already stopped, a window histogram covers its last
		 * publish period.
		 */
		void launch() {
			launchedMs = lastRenewMs = System.currentTimeMillis();
			JSONObject spec = histogramSpecs.get(kind);
			long startNs = collection.startNs;
			if (spec.optBoolean("publish_clear", false)) {
				long windowNs = (long) (spec.optDouble("publish_period", 0) * 1e9);
				startNs = collection.isRunning() ? Math.max(startNs, nowNs())
						: Math.max(startNs, collection.endNs - windowNs);
			}
			sendQuietly(settings.commandTopic, launchBody(kind, id, startNs, collection.endNs), "launch " + this);
		}

		void finish(long endTimeNs) {
			// The service then publishes the histogram up to the end, and
			// finishes.
			endSentMs = System.currentTimeMillis();
			JSONObject update = new JSONObject();
			put(update, "end_time", endTimeNs);
			JSONObject body = body("update", id);
			put(body, "update", update);
			sendQuietly(settings.commandTopic, body, "end " + this);
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
				sendQuietly(settings.heartbeatTopic, body, "renew " + this);
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
					logger.warn("No final frame from " + this + " after " + finishTries + " relaunches; giving up");
					done = true;
					return;
				}
				finishTries++;
			}
			logger.warn(String.format("No frame from %s for %.0f s (service restarted?); relaunching", this,
					(now - since) / 1000.0));
			launch();
			// Allow for a replay after each launch, and back off while it
			// fails.
			waitSeconds = Math.min(Math.max(waitSeconds * 2, settings.firstFrameSeconds),
					settings.relaunchMaxSeconds);
		}

		@Override
		public String toString() {
			return kind + " histogram " + id;
		}

	}

	// ---- Requests to the histogram service ----------------------------------

	private JSONObject launchBody(String kind, String id, long startNs, long endNs) {
		JSONObject histogram;
		try {
			histogram = new JSONObject(histogramSpecs.get(kind).toString());
		} catch (JSONException e) {
			throw new IllegalStateException(e); // was valid at start
		}
		put(histogram, "publish_topic", settings.outputTopic);
		put(histogram, "start_time", startNs);
		put(histogram, "end_time", endNs); // 0: open-ended until StopCollection
		if (!histogram.has("publish_clear")) {
			put(histogram, "publish_clear", false); // accumulate over the collection
		}
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
		// X-T and Y-T frames are about 512 kB; let a fetch hold several.
		config.put("max.partition.fetch.bytes", 4 * 1024 * 1024);
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
