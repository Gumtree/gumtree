package org.gumtree.ui.util.resource;

import java.net.URI;
import java.net.URL;

import javax.swing.ImageIcon;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.internal.InternalImage;

public enum SharedImage {

	// Cruise
	CRUISE_BG("images/cruise/NSTexturedFullScreenBackgroundColor.png");

	private SharedImage(String path) {
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
					for (SharedImage key : values()) {
//						if (OsgiUtils.isOsgiRunning()) {
//							// Under OSGi runtime
//							URL fullPathString = OsgiUtils.findFile(
//									Activator.PLUGIN_ID, key.path());
//							registry.put(key.name(), ImageDescriptor
//									.createFromURL(fullPathString));
//						} else {
//							// Under normal Java runtime
//							registry.put(key.name(),
//									ImageDescriptor.createFromFile(
//											Activator.class, key.path()));
//						}
						registry.put(key.name(), 
								Activator.imageDescriptorFromPlugin(
										Activator.PLUGIN_ID, key.path()));
					}
				}
			}
		}
		return registry;
	}

	private String path;

	private static volatile ImageRegistry registry;

}
