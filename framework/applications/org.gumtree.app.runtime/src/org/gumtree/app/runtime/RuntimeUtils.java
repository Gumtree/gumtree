/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.app.runtime;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.gumtree.app.runtime.internal.Activator;
import org.osgi.framework.Bundle;

public final class RuntimeUtils {

	public static Bundle findLatestBundle(String bundleName) {
		Bundle lastestBundle = null;
		for (Bundle bundle : Activator.getContext().getBundles()) {
			if (bundle.getSymbolicName().equals(bundleName)) {
				if (lastestBundle == null) {
					// Put in the cache straight away
					lastestBundle = bundle;
				} else {
					// Find the latest
					if (bundle.getVersion().compareTo(lastestBundle.getVersion()) > 0) {
						lastestBundle = bundle;
					}
				}
			}
		}
		return lastestBundle;
	}
	
	public static String getFilename(Bundle bundle, String filePath) throws IOException {
		URL rawUrl = FileLocator.find(bundle, Path.fromPortableString(filePath), null);
		URL fileUrl = FileLocator.toFileURL(rawUrl); 
		return fileUrl.toString();
	}
	
	private RuntimeUtils() {
		super();
	}
	
}
