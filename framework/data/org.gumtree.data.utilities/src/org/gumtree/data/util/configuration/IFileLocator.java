package org.gumtree.data.util.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public interface IFileLocator {

	public URL findFileURL(String bundleId, String relativePath);
	
	public File findFile(String bundleId, String relativePath)
			throws URISyntaxException, IOException;

}
