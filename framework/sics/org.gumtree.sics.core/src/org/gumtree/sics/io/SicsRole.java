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

package org.gumtree.sics.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.gumtree.sics.util.Activator;
import org.gumtree.util.eclipse.EclipseUtils;

public enum SicsRole {
	MANAGER("manager"), USER("user"), SPY("spy"), UNDEF("def");

	private SicsRole(String configKey) {
		this.configKey = configKey;
	}

	public String getConfigKey() {
		return configKey;
	}

	public String getLoginId() {
		return getConfig().getProperty(configKey);
	}

	public static SicsRole getRole(String configKey) {
		for (SicsRole role : values()) {
			if (role.getConfigKey().equals(configKey)) {
				return role;
			}
		}
		return null;
	}

	private static Properties getConfig() {
		if (properties == null) {
			properties = new Properties();
			try {
				URI fileURI = null;
				if (Activator.getDefault() != null) {
					IFileStore store = EclipseUtils.find(Activator.PLUGIN_ID,
							FILE_CONFIG);
					properties.loadFromXML(store
							.openInputStream(EFS.NONE, null));
				} else {
					fileURI = new File(FILE_CONFIG).toURI();
					FileInputStream input = new FileInputStream(new File(
							fileURI));
					properties.loadFromXML(input);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (InvalidPropertiesFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return properties;
	}

	private String configKey;

	private static Properties properties;

	private static final String FILE_CONFIG = "config.xml";
}
