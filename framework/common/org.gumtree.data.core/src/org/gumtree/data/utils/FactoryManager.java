/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data.utils;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.gumtree.data.IFactory;
import org.gumtree.data.internal.BasicFactoryResolver;

public class FactoryManager implements IFactoryManager {

	// TODO: Make it configurable in system properties
	private static final String CLASS_OSGI_FACTORY_RESOLVER = "org.gumtree.data.internal.OsgiFactoryResolver";
	
	private static final String CLASS_OSGI_BUNDLE_CONTEXT = "org.osgi.framework.BundleContext";
	
	// System property for default factory 
	private static final String PROP_DEFAULT_FACTORY = "gumtree.data.defaultFactory";
	
	private Map<String, IFactory> factoryRegistry;
	
	public FactoryManager() {
		factoryRegistry = new TreeMap<String, IFactory>();
		discoverFactories();
	}
	
	private void discoverFactories() {
		// Use basic factory resolver
		IFactoryResolver basicResolver = new BasicFactoryResolver();
		basicResolver.discoverFactories(this);

		// Use osgi factory resolver if available
		try {
			// [ANSTO][Tony][2011-05-25] Check to see if OSGi classes are
			// available before loading the factory
			Class<?> osgiClass = Class.forName(CLASS_OSGI_BUNDLE_CONTEXT);
			if (osgiClass != null) {
				// Use reflection in case OSGi is not available at runtime
				IFactoryResolver osgiResolver = (IFactoryResolver) Class
						.forName(CLASS_OSGI_FACTORY_RESOLVER).newInstance();
				osgiResolver.discoverFactories(this);
			}
		} catch (Exception e) {
			// Don't worry if we can't find the osgi resolver
		}
	}

	public void registerFactory(String name, IFactory factory) {
		factoryRegistry.put(name, factory);
	}

	public IFactory getFactory() {
		IFactory factory = null;
		String defaultFactoryName = System.getProperty(PROP_DEFAULT_FACTORY);
		if (defaultFactoryName != null) {
			// If default factory is specified
			factory = factoryRegistry.get(defaultFactoryName);
		}
		if (factory == null && !factoryRegistry.isEmpty()) {
			// If default factory is not specified or doesn't exist
			factory = factoryRegistry.values().iterator().next();
		}
		return factory;
	}

	public IFactory getFactory(String name) {
		return factoryRegistry.get(name);
	}

	public Map<String, IFactory> getFactoryRegistry() {
		return Collections.unmodifiableMap(factoryRegistry);
	}

}
