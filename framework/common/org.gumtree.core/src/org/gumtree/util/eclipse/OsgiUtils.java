/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.util.eclipse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.gumtree.core.internal.Activator;
import org.gumtree.core.service.ServiceUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.service.packageadmin.PackageAdmin;

@SuppressWarnings({ "restriction", "deprecation" })
public final class OsgiUtils {

	private static final int BUNDLE_ACTIVAION_TIME_OUT = 1 * 60 * 1000;

	private static final File[] EMPTY_FILE_ARRAY = new File[0];

	public static boolean isOsgiRunning() {
		return Activator.getDefault() != null;
	}

	public static Bundle getBundle(String bundleId) {
		PackageAdmin packageAdmin = ServiceUtils.getService(PackageAdmin.class);
		if (packageAdmin == null)
			return null;
		Bundle[] bundles = packageAdmin.getBundles(bundleId, null);
		if (bundles == null)
			return null;
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	public static URL findFile(String bundleId, String relativePath) {
		Bundle bundle = OsgiUtils.getBundle(bundleId);
		if (bundle != null) {
			return FileLocator.find(bundle, new Path(relativePath), null);
		}
		return null;
	}

	public static String findFilePath(String bundleId, String relativePath)
			throws URISyntaxException, IOException {
		URL url = FileLocator.toFileURL(findFile(bundleId, relativePath));
		// [GUMTREE-836] Fix space error
		URI uri = new URI(url.toString().replace(" ", "%20"));
		return new File(uri).getAbsolutePath();
	}

	/**
	 * Returns the class path roots of a bundle: the bundle jar itself for a
	 * jarred bundle, or the folders named by Bundle-ClassPath for a bundle
	 * installed as a directory.
	 * <p>
	 * This must stay at the granularity of roots. Enumerating individual
	 * *.class entries here (as this method used to do) forces
	 * {@link FileLocator#toFileURL(URL)} to extract every class of every
	 * installed bundle to the configuration area - close to 100,000 files for
	 * a workbench product - which costs minutes on startup and defeats the
	 * Jython package cache, since a jar is only cached when it is indexed as
	 * a whole.
	 */
	public static File[] getBundleClasspaths(Bundle bundle) {
		File root = FileLocator.getBundleFileLocation(bundle).orElse(null);
		if (root == null || !root.exists()) {
			return EMPTY_FILE_ARRAY;
		}
		// A jarred bundle is one single root. Nested class path jars are not
		// readable from outside the bundle jar and were never visible here.
		if (root.isFile()) {
			return new File[] { root };
		}
		List<File> files = new ArrayList<File>();
		for (String entry : getBundleClasspathEntries(bundle)) {
			if (".".equals(entry)) {
				files.add(root);
			} else {
				File file = new File(root, entry);
				if (file.exists()) {
					files.add(file);
				}
			}
		}
		if (files.isEmpty()) {
			files.add(root);
		}
		return files.toArray(new File[files.size()]);
	}

	private static List<String> getBundleClasspathEntries(Bundle bundle) {
		List<String> entries = new ArrayList<String>();
		String header = bundle.getHeaders().get(Constants.BUNDLE_CLASSPATH);
		if (header == null) {
			entries.add(".");
			return entries;
		}
		for (String entry : header.split(",")) {
			// Drop any parameter, eg "lib/foo.jar;attribute=value"
			entry = entry.split(";")[0].trim();
			if (entry.length() > 0) {
				entries.add(entry);
			}
		}
		if (entries.isEmpty()) {
			entries.add(".");
		}
		return entries;
	}

	public static URI[] findBundleResources(String manifestHeader) {
		// Find and select the newest bundles that declare GumTree-Properties
		Map<String, Bundle> latestBundle = new HashMap<String, Bundle>();
		for (Bundle bundle : Activator.getContext().getBundles()) {
			String bundleId = bundle.getSymbolicName();
			Dictionary<?, ?> allHeaders = bundle.getHeaders();
			String header = (String) allHeaders.get(manifestHeader);
			if (header == null) {
				continue;
			}
			if (!latestBundle.containsKey(bundleId)) {
				// Put in the cache straight away
				latestBundle.put(bundleId, bundle);
			} else {
				// Find the latest
				Bundle proposedBundle = latestBundle.get(bundleId);
				if (bundle.getVersion().compareTo(proposedBundle.getVersion()) > 0) {
					latestBundle.put(bundleId, bundle);
				}
			}
		}

		// Find folders
		List<URI> uriList = new ArrayList<URI>();
		for (Bundle bundle : latestBundle.values()) {
			String header = (String) bundle.getHeaders().get(manifestHeader);
			// Split into file entries
			for (String entry : header.split(",")) {
				String resourcePath = entry.trim();
				int ind = resourcePath.lastIndexOf('/');
				String path = ind != -1 ? resourcePath.substring(0, ind) : "/";
				// Resolve into urls
				Enumeration<?> urls = bundle.findEntries(path,
						ind != -1 ? resourcePath.substring(ind + 1)
								: resourcePath, false);
				if (urls == null || !urls.hasMoreElements()) {
					// logger.warn("Resource " + resourcePath + " is missing.");
					continue;
				}
				while (urls.hasMoreElements()) {
					URL url = (URL) urls.nextElement();
					try {
						// Replace space with valid URI character
						// See
						// http://stackoverflow.com/questions/2593214/android-howto-parse-url-string-with-spaces-to-uri-object
						String urlString = FileLocator.toFileURL(url)
								.toString().replace(" ", "%20");
						uriList.add(URI.create(urlString));
					} catch (IOException e) {
						// logger.error("Failed to convert bundle resource " +
						// url.toString(), e);
					}
				}
			}
		}

		return uriList.toArray(new URI[uriList.size()]);
	}

//	public static void sendOSGiFrameworkEvent(int type, Bundle bundle,
//			Exception e) {
//		Bundle systemBundle = Activator.getContext().getBundle(0);
//		if (systemBundle != null) {
//			systemBundle.getFramework().publishFrameworkEvent(type, bundle, e);
//		} else {
//			// logger.error("Cannot obtain system bundle for deliever framework event.");
//		}
//	}

	public static void startBundle(String bundleId) throws Exception {
		startBundle(bundleId, BUNDLE_ACTIVAION_TIME_OUT);
	}

	public static void startBundle(String bundleId, long timeout)
			throws Exception {
		Bundle bundle = Platform.getBundle(bundleId);
		if (bundle == null) {
			throw new Exception("Bundle " + bundleId + " not found");
		}
		if (bundle.getState() != Bundle.ACTIVE) {
			bundle.start();
			int counter = 0;
			while (bundle.getState() != Bundle.ACTIVE) {
				Thread.sleep(10);
				counter += 10;
				if (counter > timeout) {
					throw new Exception("Failed to activate bundle " + bundleId);
				}
			}
		}
	}

	private OsgiUtils() {
		super();
	}

}
