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

package org.gumtree.scripting.support;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.gumtree.core.CoreProperties;
import org.gumtree.core.service.IServiceManager;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.util.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptingManager implements IScriptingManager {
	
	private Logger logger = LoggerFactory.getLogger(ScriptingManager.class);
	
	private volatile ScriptEngineManager scriptEngineManager;
	
	private Set<ScriptEngineFactory> factoryCache;
	
	private String defaultEngineName;
	
	private IServiceManager serviceManager;
	
	public ScriptingManager() {
		super();
		factoryCache = new HashSet<ScriptEngineFactory>();
	}
	
	public ScriptEngineManager getScriptEngineManager() {
		if (scriptEngineManager == null) {
			synchronized (this) {
				if (scriptEngineManager == null) {
					scriptEngineManager = new ScriptEngineManager();
					// Cache factories
					factoryCache.addAll(scriptEngineManager.getEngineFactories());
					// Find factories from OSGi service registry
					List<ScriptEngineFactory> factories = getServiceManager().getServices(ScriptEngineFactory.class);
					for (ScriptEngineFactory factory : factories) {
						// Cache
						factoryCache.add(factory);
						// Register names
						for (String name : factory.getNames()) {
							scriptEngineManager.registerEngineName(name, factory);
						}
						// Register MIME types
						for (String type : factory.getMimeTypes()) {
							scriptEngineManager.registerEngineMimeType(type, factory);
						}
						for (String extension : factory.getExtensions()) {
							scriptEngineManager.registerEngineExtension(extension, factory);
						}
					}
				}
			}
		}
		return scriptEngineManager;
	}

	public ScriptEngine createEngine() {
		return createEngine(getDefaultEngineName());
	}
	
	public ScriptEngine createEngine(String shortName) {
		if (StringUtils.isEmpty(shortName)) {
			shortName = getDefaultEngineName();
		}
		ScriptEngine engine = null;
//		try {
//			logger.info("Creating scripting engine {}.", shortName);
//			engine = getScriptEngineManager().getEngineByName(shortName);
//			logger.info("Created scripting engine {}.", shortName);
//		} catch (Throwable throwable) {
//			engine = createDefaultEngine();
//			logger.error("Cannot create engine for " + shortName
//					+ ".  GumTree will use the default engine "
//					+ engine.getFactory().getEngineName() + " instead.",
//					throwable);
//		}
		int timeout = 20;
		int wait = 0;
		logger.info("Creating scripting engine {}.", shortName);
		while (engine == null && wait < timeout) {
			try {
				engine = getScriptEngineManager().getEngineByName(shortName);
				if (engine != null) {
					logger.info("Created scripting engine {}.", shortName);
				}
			} catch (Throwable throwable) {
//				try {
//					Thread.sleep(1000);
//					wait += 1;
//				} catch (InterruptedException e) {
//				}
//				logger.info("failed to create scripting engine " + shortName + " in trial #" + wait);
			}
			if (engine == null) {
				try {
					Thread.sleep(1000);
					wait += 1;
				} catch (InterruptedException e) {
				}
				logger.info("failed to create scripting engine " + shortName + " in trial #" + wait);
			}
		}
		if (engine == null) {
			engine = createDefaultEngine();
		}
		return engine;
	}

	private ScriptEngine createDefaultEngine() {
		return findDefaultFactory().getScriptEngine();
	}

	/**
	 * Picks the factory to fall back on when no engine name has been configured.
	 * <p>
	 * ScriptEngineManager.getEngineFactories() only reports factories discovered
	 * by the JDK ServiceLoader.  Factories contributed through the OSGi service
	 * registry are bound with registerEngineName(), which does not add them to
	 * that list, so it is empty here unless a bundle such as org.beanshell
	 * [GT-44] is installed.  Since JDK 15 removed Nashorn the JDK itself no
	 * longer supplies one either, so the factory cache has to be consulted
	 * before giving up.
	 */
	private ScriptEngineFactory findDefaultFactory() {
		// Also populates the factory cache
		List<ScriptEngineFactory> discovered = getScriptEngineManager()
				.getEngineFactories();
		if (!discovered.isEmpty()) {
			return discovered.get(0);
		}
		ScriptEngineFactory[] cached = getAllEngineFactories();
		if (cached.length == 0) {
			throw new IllegalStateException(
					"Runtime does not contain any scripting engine.  Set "
							+ CoreProperties.DEFAULT_ENGINE_NAME.getKey()
							+ " to the engine to use, or install a scripting engine bundle.");
		}
		// Sorted so that the fallback is at least reproducible across restarts
		Arrays.sort(cached, new Comparator<ScriptEngineFactory>() {
			public int compare(ScriptEngineFactory f1, ScriptEngineFactory f2) {
				return String.valueOf(f1.getEngineName()).compareTo(
						String.valueOf(f2.getEngineName()));
			}
		});
		return cached[0];
	}

	public String getDefaultEngineName() {
		if (defaultEngineName == null) {
			// The default value may not be correct, but this is the best guess
			defaultEngineName = CoreProperties.DEFAULT_ENGINE_NAME.getValue();
			if (defaultEngineName == null || defaultEngineName.isEmpty()) {
				// Read the name off the factory rather than createDefaultEngine(),
				// which would build and discard a whole engine just to name it
				defaultEngineName = findDefaultFactory().getEngineName();
			}
		}
		return defaultEngineName;
	}
	
	public ScriptEngineFactory getDefaultFactory() {
		return getFactoryByName(getDefaultEngineName());
	}
	
	public ScriptEngineFactory[] getAllEngineFactories() {
		return factoryCache.toArray(new ScriptEngineFactory[factoryCache.size()]);
	}
	
	public ScriptEngineFactory getFactoryByName(String shortName) {
		for (ScriptEngineFactory factory : getAllEngineFactories()) {
			for (String name : factory.getNames()) {
				if (name.equals(shortName)) {
					return factory;
				}
			}
		}
		return null;
	}
	
	/*************************************************************************
	 * Referenced services
	 *************************************************************************/
	
	@Override
	public IServiceManager getServiceManager() {
		return serviceManager;
	}
	
	@Override
	public void setServiceManager(IServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}
	
}
