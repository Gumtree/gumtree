package org.gumtree.jython.ui.internal;

import javax.swing.ImageIcon;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public enum InternalImage {
	
	MODULE("icons/jmeth_obj.gif"),
	CLASS("icons/class_obj.gif"),
	FUNCTION("icons/sample.gif"),
	TYPE("icons/typedef_obj.gif"),
	VARIABLE("icons/var_simple.gif"),
	STRING("icons/var_string.gif"),
	LIST("icons/list.gif"),
	TUPLE("icons/tuple.gif");
	
	private InternalImage(String path) {
		this.path = path;
	}
	
	public Image getImage() {
		return getRegistry().get(name());
	}
	
	public ImageDescriptor getDescriptor() {
		return getRegistry().getDescriptor(name());
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
						if (Activator.getDefault() != null) {
							// Under OSGi runtime
							registry.put(key.name(), Activator
									.imageDescriptorFromPlugin(
											Activator.ID_PLUGIN, key.path()));
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
