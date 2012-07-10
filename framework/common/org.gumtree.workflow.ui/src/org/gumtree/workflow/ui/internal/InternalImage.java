package org.gumtree.workflow.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public enum InternalImage {
	POINTER("/icons/forward_nav.gif"),
	CHECKED("/icons/enabled_co.gif"),
	UNCHECKED("/icons/incomplete_tsk.gif"),
	UNAVAILABLE("/icons/fatalerror_obj.gif"),
	NEXT_STEP("/icons/view_menu.gif"),
	WORKFLOW("/icons/welcome_item.gif"),
	WORKFLOW_INTRO("/icons/Settings.png"),
	PLAY("/icons/Play-Normal-32x32.png"),
	PAUSE("/icons/PAUSE-Normal-32x32.png"),
	STOP("/icons/Stop-Normal-Red-32x32.png"),
	ERROR_TASK("/icons/error_tsk.gif"),
	SAVE("/icons/filesaveas32x32.png"),
	BG_METAL("/images/metal.png"),
	TICK("/icons/tick.png"),
	ERROR("/icons/fatalerror_obj.gif"),
	LOG_SHOWED("/icons/log_shown16x16.gif"),
	LOG_HIDED("/icons/log_hided16x16.gif"),
	LIST_SHOWED("/icons/list_mode_enabled.gif"),
	LIST_HIDED("/icons/list_mode_disabled.gif"),
	CONTEXT_SHOWN("/icons/context_shown16x16.gif"),
	CONTEXT_HIDED("/icons/context_hided16x16.gif"),
	TOOL_SHOWED("/icons/sidebar-collapse.png"),
	TOOL_HIDED("/icons/sidebar-expand.png"),
	CLEAR_EDIT("/icons/clear_co_gif.gif"),
	DELETE("/icons/delete_edit.gif");

	
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
						registry.put(key.name(), 
								Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, key.path()));
					}
				}
			}
		}
		return registry;
	}
	
	// Special
	public static Image[] getIndictorImages() {
		if (indictorImages == null) {
			synchronized (InternalImage.class) {
				if (indictorImages == null) {
					indictorImages = new Image[12];
					for (int i = 1; i <= 12; i++) {
						getRegistry().put("indicator_d_" + i,
								Activator.imageDescriptorFromPlugin(
										Activator.PLUGIN_ID, "images/sequences/indicator_d_" + i + ".png"));
						indictorImages[i - 1] = getRegistry().get("indicator_d_" + i);
					}
				}
			}
		}
		return indictorImages;
	}
	
	public static Image[] getSmallIndictorImages() {
		if (smallIndictorImages == null) {
			synchronized (InternalImage.class) {
				if (smallIndictorImages == null) {
					smallIndictorImages = new Image[12];
					for (int i = 1; i <= 12; i++) {
						getRegistry().put("" + i,
								Activator.imageDescriptorFromPlugin(
										Activator.PLUGIN_ID, "images/sequences_small/" + i + ".png"));
						smallIndictorImages[i - 1] = getRegistry().get("" + i);
					}
				}
			}
		}
		return smallIndictorImages;
	}
	
	private String path;
	
	private static volatile ImageRegistry registry;
	
	private static volatile Image[] indictorImages;
	
	private static volatile Image[] smallIndictorImages;
	
}
