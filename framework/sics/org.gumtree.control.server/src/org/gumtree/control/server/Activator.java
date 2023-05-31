package org.gumtree.control.server;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.model.ModelUtils;
import org.gumtree.util.eclipse.EclipseUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	private static Activator instance;
	
	private static final String PROP_SICS_LOGINMODE = "gumtree.sics.loginMode";

	private IEclipseContext eclipseContext;
	
	private ISicsConnectionContext connectionContext;
	
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
	}

	private void connect() {
		Thread connectionMonitor = new Thread(new Runnable() {
			public void run() {
				while (!SicsManager.getSicsProxy().isConnected()) {
					try {
						SicsManager.getSicsProxy().connect(
								connectionContext.getServerAddress(), 
								connectionContext.getPublisherAddress());
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
