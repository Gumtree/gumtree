package org.gumtree.ui.util.resource;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.gumtree.util.eclipse.OsgiUtils;

public final class UIResourceUtils {

	public static ImageDescriptor imageDescriptorFromPlugin(String pluginId, String relativePath) {
		URL fullPathString = OsgiUtils.findFile(pluginId, relativePath);
        if (fullPathString == null) {
            try {
                fullPathString = new URL(relativePath);
            } catch (MalformedURLException e) {
            }
        }
        if (fullPathString != null) {
        	return ImageDescriptor.createFromURL(fullPathString);
        }
        return null;
	}
	
	private UIResourceUtils() {
		super();
	}
	
}
