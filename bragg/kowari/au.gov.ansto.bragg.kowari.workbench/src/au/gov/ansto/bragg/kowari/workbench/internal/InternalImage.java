package au.gov.ansto.bragg.kowari.workbench.internal;

import java.net.URI;
import java.net.URL;

import javax.swing.ImageIcon;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.gumtree.util.eclipse.OsgiUtils;

public enum InternalImage {

	// Cruise panel page
	EXPERIMENT_INFO("/icons/cruise/experiments_16x16.png"),
	EXPERIMENT_STATUS("/icons/cruise/rotate_16x16.png"),
	FURNACE("/icons/cruise/dashboard.png"),
	CRADLE("/icons/cruise/cradle_16x16.png"),
	SLITS("/icons/cruise/slits_16x16.png"),
	POSITIONER("/icons/cruise/stage_16x16.gif"),
	MONOCHROMATOR("/icons/cruise/mono_16x16.png"),
	ROBOT("/icons/cruise/robot_16x16.png"),
	SERVER("/icons/cruise/data_source_view.gif"),
	MONITOR("/icons/cruise/counter_16x16.png"),
	A("/icons/cruise/document-attribute.png"),
	B("/icons/cruise/document-attribute-b.png"),
	C("/icons/cruise/document-attribute-c.png"),
	D("/icons/cruise/document-attribute-d.png"),
	ONE("/icons/cruise/hh1_16x16.png"),
	TWO("/icons/cruise/hh2_16x16.png");
	
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
