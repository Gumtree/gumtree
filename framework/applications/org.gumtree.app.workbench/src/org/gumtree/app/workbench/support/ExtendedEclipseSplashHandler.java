package org.gumtree.app.workbench.support;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.splash.EclipseSplashHandler;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class ExtendedEclipseSplashHandler extends EclipseSplashHandler {

	// See: org.gumtree.core.util.PlatformUtils
	public static final String EVENT_TOPIC_RUNTIME_STARTUP_MESSAGE = "org/gumtree/runtime/startupMessage";

	public static final String EARLY_ACTIVATED_BUNDLES = "gumtree.workbench.earlyActivatedBundles";

	public static final String EVENT_PROP_RUNTIME_STARTUP_MESSAGE = "message";

	private ServiceReference ref;

	private IProgressMonitor monitor;

	public void init(Shell splash) {
		super.init(splash);
		ref = WorkbenchPlugin
				.getDefault()
				.getBundleContext()
				.registerService(
						EventHandler.class.getName(),
						new MessageEventHandler(),
						createEventHandlerProperties("org/gumtree/runtime/startupMessage"))
				.getReference();
	}

	public IProgressMonitor getBundleProgressMonitor() {
		if (monitor == null) {
			monitor = new ExtendedBundleProgressMonitor(
					super.getBundleProgressMonitor());
		}
		return monitor;
	}

	public void dispose() {
		if (ref != null) {
			WorkbenchPlugin.getDefault().getBundleContext().ungetService(ref);
			ref = null;
		}
		monitor = null;
		super.dispose();
	}

	protected Composite getContent() {
		return (Composite) ((ExtendedBundleProgressMonitor) getBundleProgressMonitor())
				.getWrappedProgressMonitor();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Dictionary createEventHandlerProperties(String... topics) {
		Dictionary properties = new Hashtable();
		properties.put(EventConstants.EVENT_TOPIC, topics);
		return properties;
	}

	private class MessageEventHandler implements EventHandler {
		@Override
		public void handleEvent(Event event) {
			if (getBundleProgressMonitor() != null) {
				getBundleProgressMonitor().subTask(
						event.getProperty("message").toString());
			}
		}
	}

	private class ExtendedBundleProgressMonitor extends ProgressMonitorWrapper {

		protected ExtendedBundleProgressMonitor(IProgressMonitor monitor) {
			super(monitor);
		}

		public void beginTask(String name, int totalWork) {
			// We make sure the user specified bundles are ready before we go
			// on. This will make the splash screen stay until all necessary
			// bundles are loaded.
			String bundleList = System.getProperty(EARLY_ACTIVATED_BUNDLES, "");
			for (String bundleId : bundleList.split(",")) {
				Bundle bundle = Platform.getBundle(bundleId.trim());
				if (bundleId.length() == 0) {
					continue;
				}
				if (bundle != null) {
					while (bundle.getState() == Bundle.STARTING
							|| bundle.getState() == Bundle.RESOLVED) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			super.beginTask(name, totalWork);
		}

	}

}
