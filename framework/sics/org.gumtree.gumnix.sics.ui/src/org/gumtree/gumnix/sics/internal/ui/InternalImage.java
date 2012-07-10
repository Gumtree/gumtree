package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public enum InternalImage {
	INTERRUPT("/icons/Hand.png"),
	REFRESH("icons/full/elcl16/refresh_nav.gif"),
	RUN("icons/full/obj16/exec_obj.gif"),
	SPY("icons/search-16x16.png"),
	COLUMN("icons/full/obj16/layout_co.gif"),
	TRANSACTION("icons/full/elcl16/synced.gif"),
	START("icons/full/etool16/run_exc.gif"),
	STOP("icons/full/elcl16/terminate_co.gif"),
	TELNET("icons/telnet.gif"),
	LOCK("icons/lock.png"),
	UNLOCK("icons/lock_open.png");
	
	private InternalImage(String path) {
		this.path = path;
	}
	
	public Image getImage() {
		return getRegistry().get(name());
	}
	
	public ImageDescriptor getDescriptor() {
		return getRegistry().getDescriptor(name());
	}
	
	public static boolean isInstalled() {
		return registry != null;
	}
	
	public static synchronized void dispose() {
		registry.dispose();
		registry = null;
	}
	
	private String path() {
		return path;
	}
	
	private static ImageRegistry getRegistry() {
		if (registry == null) {
			synchronized (InternalImage.class) {
				if (registry == null) {
					registry = new ImageRegistry(Display.getDefault()); 
					for (InternalImage key : values()) {
						registry.put(key.name(), Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, key.path()));
					}
				}
			}
		}
		return registry;
	}
	
	private String path;
	
	private static volatile ImageRegistry registry;
	
}
