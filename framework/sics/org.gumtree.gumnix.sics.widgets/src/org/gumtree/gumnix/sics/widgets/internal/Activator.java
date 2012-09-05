package org.gumtree.gumnix.sics.widgets.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.gumtree.gumnix.sics.widgets";

	private static Activator instance;

	private BundleContext context;

	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		instance = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		this.context = null;
		instance = null;
	}

	public BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}

}
