package org.gumtree.widgets.swt.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gumtree.widgets.internal.Activator;
import org.osgi.framework.Bundle;

public final class UIResourceUtils {

	public static ImageDescriptor imageDescriptorFromPlugin(String pluginId,
			String relativePath) {
		URL fullPathString = findFile(pluginId, relativePath);
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

	public static URL findFile(String bundleId, String relativePath) {
		Bundle resultBundle = null;
		for (Bundle bundle : Activator.getDefault().getContext().getBundles()) {
			// TODO: The system may have multiple bundles
			// with the same name. We need to either pick the latest bundle,
			// or support searching on multiple versions
			if (bundle.getSymbolicName().equals(bundleId)) {
				resultBundle = bundle;
			}
		}
		if (resultBundle != null) {
			return FileLocator.find(resultBundle, new Path(relativePath), null);
		}
		return null;
	}

	public static boolean isOsgiRunning() {
		return Activator.getDefault() != null;
	}
	
	private UIResourceUtils() {
		super();
	}

}
