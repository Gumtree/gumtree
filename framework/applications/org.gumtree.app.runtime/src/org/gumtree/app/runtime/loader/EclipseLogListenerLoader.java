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

package org.gumtree.app.runtime.loader;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime loader for enabling Eclipse log to GumTree log adaption.
 * 
 * @author Tony Lam
 * 
 */
public class EclipseLogListenerLoader implements IRuntimeLoader {

	private static final Logger logger = LoggerFactory.getLogger(EclipseLogListenerLoader.class);
			
	private EclipseLogListener eclipseLogListener;

	public void load(BundleContext context) {
		// [GT-23] Route eclipse logs
		eclipseLogListener = new EclipseLogListener();
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (!Platform.isRunning()) {
					logger.info("We are waiting for the Eclipse Platform to be ready...");
				}
				while (!Platform.isRunning()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
				Platform.addLogListener(eclipseLogListener);
				logger.info("Successfully loaded eclipse log listener to the platform.");
			}
		}, "Eclipse Log Listener Loader");
		thread.start();
	}

	public void unload(BundleContext context) {
		if (eclipseLogListener != null && Platform.isRunning()) {
			Platform.removeLogListener(eclipseLogListener);
			eclipseLogListener = null;
		}
	}

}
