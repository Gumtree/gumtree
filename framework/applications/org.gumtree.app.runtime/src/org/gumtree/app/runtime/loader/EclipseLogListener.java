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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EclipseLogListener listens to the Eclipse ILog facility and route logs into
 * SLF4J.
 * 
 * @author Tony Lam
 * 
 */
public class EclipseLogListener implements ILogListener {

	private Map<String, Logger> loggerMap;

	public void logging(IStatus status, String plugin) {
		Logger logger = getLogger(plugin);
		// Convert logging type based on status' severity
		switch (status.getSeverity()) {
		case IStatus.OK:
			logger.debug(status.getMessage(), status.getException());
			break;
		case IStatus.INFO:
			logger.info(status.getMessage(), status.getException());
			break;
		case IStatus.WARNING:
			logger.warn(status.getMessage(), status.getException());
			break;
		case IStatus.ERROR:
			logger.error(status.getMessage(), status.getException());
			break;
		case IStatus.CANCEL:
			logger.debug(status.getMessage(), status.getException());
			break;
		default:
			logger.debug(status.getMessage(), status.getException());
			break;
		}
	}

	private Logger getLogger(String plugin) {
		if (loggerMap == null) {
			loggerMap = new HashMap<String, Logger>();
		}
		Logger logger = loggerMap.get(plugin);
		if (logger == null) {
			logger = LoggerFactory.getLogger("(plugin)" + plugin);
			loggerMap.put(plugin, logger);
		}
		return logger;
	}

}
