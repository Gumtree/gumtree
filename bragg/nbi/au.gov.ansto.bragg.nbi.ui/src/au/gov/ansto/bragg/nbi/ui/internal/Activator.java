package au.gov.ansto.bragg.nbi.ui.internal;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.gumtree.control.core.SicsManager;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.ui.util.resource.SharedImage;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "au.gov.ansto.bragg.nbi.ui";

	// The shared instance
	private static Activator plugin;
	
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
		SicsCore.getDefaultProxy().addProxyListener(new ISicsProxyListener() {
			
			@Override
			public void proxyDisconnected() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void proxyConnectionReqested() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void proxyConnected() {
				SafeRunner.run(new SafeRunnable() {
					
					@Override
					public void run() throws Exception {
						SicsManager.autoStartProxy();
					}
				});
			}
			
			@Override
			public void messageSent(String message, String channelId) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void messageReceived(String message, String channelId) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (InternalImage.isInstalled()) {
			InternalImage.dispose();
		}
		if (SharedImage.isInstalled()) {
			SharedImage.dispose();
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

}
