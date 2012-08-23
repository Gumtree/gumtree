package org.gumtree.ui.internal;

import java.net.URI;
import java.net.URL;

import javax.swing.ImageIcon;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.gumtree.util.eclipse.OsgiUtils;

public enum InternalImage {

	// Launcher
	ICON_16("/icons/launcher/play16.png"),
	ICON_32("/icons/launcher/play32.png"),
	ICON_64("/icons/launcher/Settings64.png"),
	CATEGORY("/icons/launcher/Aqua-Smooth-Folder-Applications-32x32m.png"),
	
	// Sidebar
	RIGHT_ARROW_22("/icons/sidebar/next22x22.png"),
	LEFT_ARROW_22("/icons/sidebar/previous22x22.png"),
	ADD_16("/icons/sidebar/add_item16x16.png"),
	ADD_22("/icons/sidebar/add22x22.png"),
	JUMP_16("/icons/sidebar/go-jump16x16.png"),
	RELOAD_22("/icons/sidebar/undo22x22.png"),
	ERROR("/icons/sidebar/button_cancel22x22.png"),
	DELETE_16("/icons/sidebar/delete_item16x16.png"),
	INFO_16("/icons/sidebar/info16x16.png"),
	TOOL_16("/icons/sidebar/tool16x16.png"),
	
	// New sidebar
	PREFERENCES("/icons/sidebar/control2.png"),
	EXPAND_ALL("/icons/sidebar/expandall.gif"),
	COLLAPSE_ALL("/icons/sidebar/collapseall.gif"),
	DELETE_12("/icons/sidebar/delete_12x12.png"),
	
	// Scripting
	CONTENT_ASSIST("icons/scripting/help_search.gif"),
	SCROLL_LOCK("icons/scripting/lock_co.gif"),
	CLEAR("icons/scripting/clear_co.gif"),
	INTERRUPT("icons/scripting/terminate_co.gif"),
	EXPORT_OUTPUT("icons/scripting/exportshelloutput.gif"),
	EXPORT_HISTORY("icons/scripting/exportshellhistory.gif"),
	
	// Cruise
	LEFT_ARROW_16("icons/cruise/backward_nav.gif"),
	RIGHT_ARROW_16("icons/cruise/forward_nav.gif"),

	// Tasklet
	FOLDER_16("icons/tasklet/fldr_obj.gif"),
	TASKLET_16("icons/tasklet/default_persp.gif"),
	HIERARCHY_16("icons/tasklet/hierarchicalLayout.gif"),
	ADD_TASKLET_16("icons/tasklet/add_exc.gif"),
	DELETE_EDIT_16("icons/tasklet/delete_edit.gif"),
	SETTING_16("icons/tasklet/prefs_misc.gif"),
	EDIT_16("icons/tasklet/editor_area.gif"),
	RUN_16("icons/tasklet/run_exec.gif");
	
	private InternalImage(String path) {
		this.path = path;
	}

	public Image getImage() {
		return getRegistry().get(name());
	}

	public ImageDescriptor getDescriptor() {
		return getRegistry().getDescriptor(name());
	}

	public URI getURI() {
		return URI.create("bundle://" + Activator.PLUGIN_ID + "/" + path());
	}
	
	public ImageIcon createImageIcon() {
        java.net.URL imgURL = Activator.class.getResource(path());
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            return null;
        }
    }
	
	public static boolean isInstalled() {
		return registry != null;
	}

	public static synchronized void dispose() {
		if (registry != null) {
			registry.dispose();
			registry = null;
		}
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
						if (OsgiUtils.isOsgiRunning()) {
							// Under OSGi runtime
							URL fullPathString = OsgiUtils.findFile(Activator.PLUGIN_ID, key.path());
							registry.put(key.name(),
									ImageDescriptor.createFromURL(fullPathString));
						} else {
							// Under normal Java runtime
							registry.put(key.name(),
									ImageDescriptor.createFromFile(
											Activator.class, key.path()));
						}
					}
				}
			}
		}
		return registry;
	}

	private String path;

	private static volatile ImageRegistry registry;

}
