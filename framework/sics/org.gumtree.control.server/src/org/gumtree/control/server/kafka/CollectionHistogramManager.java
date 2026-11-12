package org.gumtree.control.server.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The control server's one {@link CollectionHistogramService} and its
 * {@link HistogramBroadcaster}, which the histogram REST service reads.
 * <p>
 * Started by the bundle's Activator when the system property
 * {@value #PROPERTY_ENABLED} is true. The service is started in the background,
 * retrying every {@value #RETRY_SECONDS} s, as the Kafka services may not be
 * registered yet when this bundle starts, and the broker may be unreachable.
 */
public final class CollectionHistogramManager {

	private static final Logger logger = LoggerFactory.getLogger(CollectionHistogramManager.class);

	public static final String PROPERTY_ENABLED = CollectionHistogramService.PROPERTY_PREFIX + "enabled";

	private static final int RETRY_SECONDS = 30;

	private static CollectionHistogramService service;

	private static HistogramBroadcaster broadcaster;

	private static Thread starter;

	private CollectionHistogramManager() {
	}

	public static boolean isEnabled() {
		return Boolean.getBoolean(PROPERTY_ENABLED);
	}

	/**
	 * Starts the service in the background, if enabled and not started.
	 */
	public static synchronized void start() {
		if (isEnabled()) {
			start(new CollectionHistogramService());
		}
	}

	/**
	 * Starts the given service in the background, whatever
	 * {@value #PROPERTY_ENABLED} says, for example one with its own settings.
	 * Does nothing if a service is already started; {@link #stop()} it first.
	 */
	public static synchronized void start(final CollectionHistogramService newService) {
		if (service != null) {
			return;
		}
		final HistogramBroadcaster newBroadcaster = new HistogramBroadcaster();
		newService.addListener(newBroadcaster);
		service = newService;
		broadcaster = newBroadcaster;
		starter = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						synchronized (CollectionHistogramManager.class) {
							if (service != newService) {
								return; // stopped meanwhile
							}
						}
						newService.start();
						return;
					} catch (RuntimeException e) {
						logger.error("Could not start the collection histogram service; retrying in "
								+ RETRY_SECONDS + " s", e);
					}
					try {
						Thread.sleep(RETRY_SECONDS * 1000L);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}, "Collection histogram starter");
		starter.setDaemon(true);
		starter.start();
	}

	/**
	 * Stops the service and ends client streams. Safe to call when not started.
	 */
	public static void stop() {
		CollectionHistogramService oldService;
		HistogramBroadcaster oldBroadcaster;
		Thread oldStarter;
		synchronized (CollectionHistogramManager.class) {
			oldService = service;
			oldBroadcaster = broadcaster;
			oldStarter = starter;
			service = null;
			broadcaster = null;
			starter = null;
		}
		if (oldStarter != null) {
			oldStarter.interrupt();
			try {
				oldStarter.join(5000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		if (oldBroadcaster != null) {
			oldBroadcaster.close();
		}
		if (oldService != null) {
			oldService.stop();
		}
	}

	/**
	 * @return the service, or null if not enabled; it may still be starting
	 */
	public static synchronized CollectionHistogramService getService() {
		return service;
	}

	/**
	 * @return the broadcaster, or null if not enabled
	 */
	public static synchronized HistogramBroadcaster getBroadcaster() {
		return broadcaster;
	}

}
