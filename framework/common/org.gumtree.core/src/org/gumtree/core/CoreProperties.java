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

package org.gumtree.core;

import org.gumtree.util.ISystemProperty;
import org.gumtree.util.SystemProperty;

public final class CoreProperties {

	/**
	 * Default crypto key
	 */
	public static final ISystemProperty CRYPTO_KEY = new SystemProperty(
			"gumtree.security.cryptoKey", "1A2r3uc1");

	/**
	 * Default crypto initialise vector
	 */
	public static final ISystemProperty CRYPTO_IV = new SystemProperty(
			"gumtree.security.cryptoIV", "9P01slD6");

	/**
	 * The project name for GumTree component to use as workspace
	 */
	public static final ISystemProperty WORKSPACE_PROJECT = new SystemProperty(
			"gumtree.workspace.project", "GumTree_Workspace");

	public static final ISystemProperty DEFAULT_ENGINE_NAME = new SystemProperty(
			"gumtree.scripting.defaultEngineName", "");
	
	private CoreProperties() {
		super();
	}
	
}
