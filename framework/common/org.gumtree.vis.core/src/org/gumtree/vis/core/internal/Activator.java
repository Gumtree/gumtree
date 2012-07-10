package org.gumtree.vis.core.internal;

import javax.swing.UIManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private static Activator instance;
	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		instance = this;
		Activator.context = bundleContext;
		try {
			if ("linux".equals(System.getProperty("osgi.os"))
					&& "gtk".equals(System.getProperty("osgi.ws"))) {
				// [GUMTREE-606] Avoid using system LAF on GTK
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());
			} else if ("win32".equals(System.getProperty("osgi.os"))
					&& "win32".equals(System.getProperty("osgi.ws"))) {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			}
			// [GUMTREE-706] Don't set LAF on the Mac
		} catch (Exception e) {
			// handle exception
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		instance = null;
	}

	public static Activator getDefault() {
		return instance;
	}

}
