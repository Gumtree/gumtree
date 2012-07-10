package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.login.ILoginHandler;
import org.gumtree.gumnix.sics.internal.ui.login.LoginHandler;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.gumtree.gumnix.sics.ui";

	// The shared instance
	private static Activator plugin;

	private ILoginHandler loginHandler;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		SicsCore.getDefaultProxy().addProxyListener((LoginHandler)getLoginHandler());
		
//		SicsIOConsoleManager.getDefault().activate();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		SicsIOConsoleManager.getDefault().stop();
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public ILoginHandler getLoginHandler() {
		if(loginHandler == null) {
			loginHandler = new LoginHandler();
		}
		return loginHandler;
	}

}
