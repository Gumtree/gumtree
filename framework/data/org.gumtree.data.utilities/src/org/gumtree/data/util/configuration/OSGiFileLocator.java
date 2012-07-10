package org.gumtree.data.util.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.gumtree.data.util.internal.Activator;
import org.osgi.framework.Bundle;


public class OSGiFileLocator implements IFileLocator {

	public URL findFileURL(String bundleId, String relativePath) {
		Bundle bundle = null;
		for (Bundle potentialBundle : Activator.getContext().getBundles()) {
			if (potentialBundle.getSymbolicName().equals(bundleId)) {
				bundle = potentialBundle;
				break;
			}
		}
		if (bundle != null) {
			return FileLocator.find(bundle, new Path(relativePath), null);
		}
		return null;
	}
	
	public File findFile(String bundleId, String relativePath)
			throws URISyntaxException, IOException {
		URL url = findFileURL(bundleId, relativePath);
		URI uri = FileLocator.toFileURL(url).toURI();
		return new File(uri);
	}
	
}
