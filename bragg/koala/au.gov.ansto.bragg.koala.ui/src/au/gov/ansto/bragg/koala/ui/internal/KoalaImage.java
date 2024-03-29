package au.gov.ansto.bragg.koala.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import au.gov.ansto.bragg.koala.ui.Activator;

public enum KoalaImage {
	
	WEATHER16("/icons/weather_16.png"),
	WEATHER32("/icons/weather_32.png"),
	WEATHER64("/icons/weather_64.png"),
	HOME64("/icons/home_64.png"),
	HOME_GRAY64("/icons/home_gray_64.png"),
	JOEY64("/icons/joey_64.png"),
	JOEY32("/icons/joey_32.png"),
	DRUM32("/icons/drum_blue_32.png"),
	COBRA("/icons/cobra_360.png"),
	KOALA_V720("/icons/koala_v720.jpg"),
	BACK64("/icons/back_64.png"),
	NEXT64("/icons/next_64.png"),
	CHEMISTRY64("/icons/chemistry_64.png"),
	CHEMISTRY_GRAY64("/icons/chemistry_gray_64.png"),
	PHYSICS64("/icons/physics_64.png"),
	PHYSICS_GRAY64("/icons/physics_gray_64.png"),
	LOCK64("/icons/lock_64.png"),
	TEMPERATURE64("/icons/temperature_64.png"),
	PHI64("/icons/phi_64.png"),
	ALIGNED64("/icons/aligned_64.png"),
	ORIENTATION64("/icons/orientation_64.png"),
	ORIENTATION32("/icons/orientation_32.png"),
	PLAY48("/icons/play_48.png"),
	COPY_EMPTY32("/icons/copy_emp_32.png"),
	COPY_FILLED32("/icons/copy_fill_32.png"),
	COPY32("/icons/copy_32.png"),
	DELETE32("/icons/delete_32.png"),
	COPY48("/icons/copy_48.png"),
	DELETE48("/icons/delete_48.png"),
	DELETE_INV32("/icons/delete_inv_32.png"),
	PAUSE64("/icons/pause_64.png"),
	SKIP48("/icons/skip_48.png"),
	STOP48("/icons/stop_48.png"),
	STOP64("/icons/stop_64.png"),
	IMAGE32("/icons/image_32.png"),
	IMAGE64("/icons/image_64.png"),
	MULTI_APPLY48("/icons/multitick_48.png"),
	TARGET48("/icons/target_48.gif"),
	TOOLS48("/icons/tools_48.png"),
	RELOAD48("/icons/reload_48.png"),
	RELOAD32("/icons/reload_32.png"),
	CONNECT32("/icons/connect_32.png"),
	SAVE32("/icons/save_32.png"),
	LOAD32("/icons/load_32.png"),
	OPEN32("/icons/open_32.png"),
	OPEN64("/icons/open_64.png"),
	UP64("/icons/up_64.png"),
	DOWN64("/icons/down_64.png"),
	UP32("/icons/up_32.png"),
	DOWN32("/icons/down_32.png"),
	PLUS32("/icons/plus_32.png"),
	PLUS_INV32("/icons/plus_inv_32.png"),
	MOVE_UP32("/icons/move_up_32.png"),
	MOVE_DOWN32("/icons/move_down_32.png"),
	MOVE_UP_INV32("/icons/move_up_inv_32.png"),
	MOVE_DOWN_INV32("/icons/move_down_inv_32.png"),
	PASS32("/icons/pass_32.png"),
	;
	
	private KoalaImage(String path) {
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
			synchronized (KoalaImage.class) {
				if (registry == null) {
					registry = new ImageRegistry(Display.getDefault()); 
					for (KoalaImage key : values()) {
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
