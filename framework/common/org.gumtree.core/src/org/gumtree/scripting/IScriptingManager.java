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

package org.gumtree.scripting;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.gumtree.core.service.IService;

public interface IScriptingManager extends IService {
	
	// Gets the associated JSR 223 scripting manager
	public ScriptEngineManager getScriptEngineManager();

	public String getDefaultEngineName();
	
	// Create default engine
	public ScriptEngine createEngine();
	
	// Return default if engine not found
	public ScriptEngine createEngine(String shortName);
		
	public ScriptEngineFactory getDefaultFactory();
	
	// Use this instead of getScriptEngineManager().getEngineFactories()
	public ScriptEngineFactory[] getAllEngineFactories();
	
	public ScriptEngineFactory getFactoryByName(String shortName);
	
}
