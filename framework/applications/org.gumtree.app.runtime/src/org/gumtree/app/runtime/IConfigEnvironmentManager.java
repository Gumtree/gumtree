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

import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * A config environment manager loads properties based current config
 * environment.
 * <p>
 * A config environment is a setting which is used to configure the system, and
 * affects how GumTree, via this manager, selects a particular configuration
 * property. For example, GumTree may be specified a config environment mode
 * 'server', so that all properties associated to the server mode will be
 * selected and loaded into the System.
 * 
 * @author Tony Lam
 * @since 1.3
 */
public interface IConfigEnvironmentManager {

	/**
	 * Gets the current config environment settings from the system. The config
	 * environment is specified in the JVM via the following format:
	 * <p>
	 * -DconfigEnv.[type]=[value]
	 * <p>
	 * For example, to configure GumTree to the server mode, try:
	 * <p>
	 * -DconfigEnv.mode=server
	 * <p>
	 * Once this is set, properties with the [mode@server] appended to the end
	 * will be loaded.
	 * 
	 * @return a map of config environment with key as type, and value as config
	 *         environment value
	 */
	public Map<String, String> getConfigEnvironments();

	/**
	 * @param fileURL
	 *            properties file to be loaded
	 * @return a properties object will selected properties loaded
	 * @throws Exception
	 *             if file loading error occuried
	 */
	public Properties loadProperties(URL fileURL) throws Exception;

	/**
	 * Resolves a given properties according to the config environment and
	 * returns a new properties.
	 * 
	 * @param props
	 *            a properties object to resolve
	 * @return new properties object
	 */
	public Properties resolveProperties(Properties props);

}
