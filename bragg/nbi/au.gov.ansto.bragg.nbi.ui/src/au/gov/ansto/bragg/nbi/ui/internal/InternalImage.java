package au.gov.ansto.bragg.nbi.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public enum InternalImage {
	
	POWER_16("/icons/power16x16.png"),
	INLET_16("/icons/in16x16.png"),
	OUTLET_16("/icons/out16x16.png"),
	A("/icons/fugue/document-attribute.png"),
	B("/icons/fugue/document-attribute-b.png"),
	C("/icons/fugue/document-attribute-c.png"),
	D("/icons/fugue/document-attribute-d.png"),
	START_32("/icons/player_play32x32.png"),
	STOP_32("/icons/player_stop32x32.png"),
	PAUSE_32("/icons/player_pause32x32.png"),
	ADD_ITEM("/icons/script/add_item.gif"),
	ADD_DIR("/icons/script/add_dir.gif"),
	BUSY_STATUS_16("/icons/script/busy_16.png"),
	BUSY_STATUS_12("/icons/script/busy_12.png"),
	ERROR_STATUS_16("/icons/script/error_16.gif"),
	ERROR_STATUS_12("/icons/script/error_12.gif"),
	DONE_STATUS_16("/icons/script/done_16.png"),
	DONE_STATUS_12("/icons/script/done_12.png"),
	INTERRUPT_STATUS_16("/icons/script/warning_16.png"),
	INTERRUPT_STATUS_12("/icons/script/warning_12.png"),
	REMOVE_ITEM("/icons/script/rem_item.gif"),
	REMOVE_ITEM_12("/icons/script/rem_item_12.gif"),
	PLAY_16("/icons/script/Play-Normal-16x16.png"),
	PAUSE_16("icons/player_pause16x16.png"),
	DOWN_16("/icons/script/down_16.png"),
	OPEN_16("/icons/script/toc_open.gif"),
	NEW_16("/icons/script/new_con.gif"),
	EDIT_16("/icons/script/edit_16.png"),
	RELOAD_16("/icons/script/reload_page16x16.png"),
	LOADING_16("/icons/script/loading16.gif"),
	REFRESH_16("/icons/script/refresh.gif"),
	DASHBOARD_16("/icons/everaldo/Dashboard16x16.png"), 
	RECENT_16("/icons/script/recent_16.png"),
	CATEGORY_BTN_IMAGE_BAR("/icons/script/Button_bar.png"),
	CATEGORY_BTN_IMAGE_BEGIN("/icons/script/Button_begin.png"),
	CATEGORY_BTN_IMAGE_END("/icons/script/Button_end.png"),
	CATEGORY_BTN_IMAGE_BLUE_UP("/icons/script/Button_blue_up.png"),
	CATEGORY_BTN_IMAGE_BLUE_DOWN("/icons/script/Button_blue_down.png"),
	CATEGORY_BTN_IMAGE_BLUE_OVER("/icons/script/Button_blue_over.png"),
	CATEGORY_BTN_IMAGE_GRAY_UP("/icons/script/Button_gray_up.png"),
	CATEGORY_BTN_IMAGE_GRAY_DOWN("/icons/script/Button_gray_down.png"),
	CATEGORY_BTN_IMAGE_GRAY_OVER("/icons/script/Button_gray_over.png"),
	PLUS_64("/icons/plus64.png"),
	;
	
	private InternalImage(String path) {
		this.path = path;
	}

	public Image getImage() {
		return getRegistry().get(name());
	}
		
	public Image getImage(int width, int height) {
		Image img = getRegistry().get(name());
		boolean scale = false;

		if (width != -1)
			scale = true;
		else
			width = img.getImageData().width;

		if (height != -1)
			scale = true;
		else
			height = img.getImageData().height;

		if (!scale)
			return img;
		else
			return new Image(
					img.getDevice(),
					img.getImageData().scaledTo(width, height));
	}

	public Image getImage(int width) {
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
