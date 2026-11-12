package org.gumtree.control.server;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.model.ModelUtils;
import org.gumtree.control.server.kafka.CollectionHistogramManager;
import org.gumtree.control.server.log.ControlLogManager;
import org.gumtree.util.eclipse.EclipseUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static BundleContext context;

	private static Activator instance;
	
	private static final String PROP_SICS_LOGINMODE = "gumtree.sics.loginMode";

	// Same as CollectionHistogramManager.PROPERTY_ENABLED; repeated so that the
	// manager, which needs the optional Kafka bundle, is loaded only when enabled.
	private static final String PROP_COLLECTION_HISTOGRAM = "gumtree.kafka.histogram.enabled";

	private boolean collectionHistogramStarted;

	private IEclipseContext eclipseContext;
	
	private ISicsConnectionContext connectionContext;
	
	private static final Logger logger = LoggerFactory.getLogger(Activator.class);
	
	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		instance = this;
		logger.error("SICS proxy module started");
		boolean newProxy = false;
		try {
			newProxy = Boolean.valueOf(SicsCoreProperties.USE_NEW_PROXY.getValue());
		} catch (Exception e) {
		}
		if (newProxy) {
			String sicsLoginMode = System.getProperty(PROP_SICS_LOGINMODE);
			if (!"skip".equalsIgnoreCase(sicsLoginMode)) {
				try {
					connectionContext = ModelUtils.createConnectionContext();
					connect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		startCollectionHistogram();
	}

	private void startCollectionHistogram() {
		if (!Boolean.getBoolean(PROP_COLLECTION_HISTOGRAM)) {
			return;
		}
		try {
			// Starts in the background; see CollectionHistogramManager.
			CollectionHistogramManager.start();
			collectionHistogramStarted = true;
		} catch (LinkageError e) {
			logger.error("Collection histogram service needs the org.gumtree.kafka.clients bundle", e);
		} catch (RuntimeException e) {
			logger.error("Failed to start the collection histogram service", e);
		}
	}

	private void connect() {
		Thread connectionMonitor = new Thread(new Runnable() {
			public void run() {
				while (!SicsManager.getSicsProxy().isConnected()) {
					try {ISicsProxy sicsProxy = SicsManager.getSicsProxy();
						sicsProxy.connect(connectionContext.getServerAddress(), 
										  connectionContext.getPublisherAddress());
						ControlLogManager.getInstance().initiate(sicsProxy);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		});
		connectionMonitor.start();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (collectionHistogramStarted) {
			try {
				CollectionHistogramManager.stop();
			} catch (RuntimeException e) {
				logger.error("Failed to stop the collection histogram service", e);
			}
			collectionHistogramStarted = false;
		}
		if (eclipseContext != null) {
			eclipseContext.dispose();
			eclipseContext = null;
		}
		instance = null;
		Activator.context = null;
	}

	public IEclipseContext getEclipseContext() {
		if (eclipseContext == null) {
			eclipseContext = EclipseUtils.createEclipseContext(context.getBundle());
		}
		return eclipseContext;
	}
	
	public static Activator getDefault() {
		return instance;
	}

}
