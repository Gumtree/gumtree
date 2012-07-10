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

import org.osgi.framework.BundleContext;

/**
 * A loader which executes codes during system startup. The loading service is
 * provided by the GumTree runtime. The GumTree runtime is usually started after
 * the Eclipse runtime. Loader may for example perform the following tasks to
 * configure the system:
 * <ul>
 * <li>Routes Eclipse log to the standard GumTree log
 * <li>Starts additional bundles programmatically without specifying them in
 * config.ini
 * <li>Loads and process additional properties into JVM system properties
 * <li>...
 * </ul>
 * <p>
 * 
 * @author Tony Lam
 * @since 1.3
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IRuntimeLoader {

	/**
	 * @param context
	 *            the bundle context
	 * @throws Exception
	 *             if a error occurred during loading
	 */
	public void load(BundleContext context) throws Exception;

	/**
	 * @param context
	 *            the bundle context
	 * @throws Exception
	 *             if a error occurred during unloading
	 */
	public void unload(BundleContext context) throws Exception;

}
