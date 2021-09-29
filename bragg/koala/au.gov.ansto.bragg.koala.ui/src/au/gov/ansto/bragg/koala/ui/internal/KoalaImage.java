package au.gov.ansto.bragg.koala.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import au.gov.ansto.bragg.koala.ui.Activator;

public enum KoalaImage {
	
	WEATHER16("/icons/weather_16.png"),
	WEATHER64("/icons/weather_64.png"),
	WEATHER96("/icons/weather_96.png"),
	WEATHER128("/icons/weather_128.png"),
	JOEY64("/icons/joey_64.png"),
	COBRA("/icons/cobra_360.png"),
	KOALA_V720("/icons/koala_v720.jpg"),
	BACK64("/icons/back_64.png"),
	NEXT64("/icons/next_64.png"),
	CHEMISTRY64("/icons/chemistry_64.png"),
	PHYSICS64("/icons/physics_64.png"),
	TEMPERATURE64("/icons/temperature_64.png"),
	PHI64("/icons/phi_64.png"),
	ALIGNED64("/icons/aligned_64.png"),
	ORIENTATION64("/icons/orientation_64.png"),
	PLAY48("/icons/play_48.png"),
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
