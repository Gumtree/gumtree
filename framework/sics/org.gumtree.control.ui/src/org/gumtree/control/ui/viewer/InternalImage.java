package org.gumtree.control.ui.viewer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.ui.internal.Activator;

public enum InternalImage {
	ADD("icons/add_obj.gif"),
	UP("icons/upward_nav.gif"),
	DOWN("icons/downward_nav.gif"),
	DARK_GREEN("icons/DarkGreen.png"),
	DARK_ORANGE("icons/DarkOrange.png"),
	MAGENTA("icons/Magenta.png"),
	BLACK("icons/Black.png"),
	BLUE("icons/Blue.png"),
	RED("icons/Red.png"),
	ORANGE("icons/Orange.png"),
	CYAN("icons/Cyan.png"),
	REMOVE("icons/remove_correction.gif"),
	INTERRUPT("/icons/stop_16x16.png"),
	DELETE("icons/delete_edit.gif"),
	REFRESH("icons/full/elcl16/refresh_nav.gif"),
	BUFFER("icons/public_co.gif"),
	PLAY("icons/Must-Have/Play_16x16.png"),
	PAUSE("icons/Must-Have/Pause_16x16.png"),
	RUN("icons/full/obj16/exec_obj.gif"),
	SPY("icons/search-16x16.png"),
	COLUMN("icons/full/obj16/layout_co.gif"),
	TRANSACTION("icons/full/elcl16/synced.gif"),
	START("icons/full/etool16/run_exc.gif"),
	STOP("icons/full/elcl16/terminate_co.gif"),
	TELNET("icons/telnet.gif"),
	FILE("icons/file_obj.gif"),
	QUEUE("icons/add_to_queue.gif"),
	SAVE("icons/save_edit.gif"),
	APPEND("icons/insert_16x16.png"),
	PRINT("icons/printer_16x16.png"),
	LIBRARY("icons/showcategory_ps.gif"),
	LOAD("icons/fldr_obj.gif"),
	LOCK("icons/lock.png"),
	UNLOCK("icons/lock_open.png"),
	TEXT_EDIT("icons/wordassist_co.gif"),
	STOP_128("icons/stop_128x128.png"),
	STOP_64("icons/stop_64x64.png");
	
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
