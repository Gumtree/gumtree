package org.gumtree.data.ui.viewers.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.gumtree.data.ui.viewers";
	
	private static Activator instance;
	private BundleContext context;
	
	@Override
	public void start(BundleContext context) throws Exception {
		instance = this;
		this.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
		instance = null;
		this.context = null;
	}
	
	public static Activator getDefault() {
		return instance;
	}

	public BundleContext getContext() {
		return context;
	}
}
