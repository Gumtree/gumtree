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

import java.io.Reader;

import javax.script.ScriptEngine;

import org.gumtree.service.eventbus.IEventHandler;


/**
 * Script executor is a wrapper to the script engine.  It provides a new to allow
 * script to be executed in a customised way, for example, running within a single
 * thread pool.
 * 
 * @Tony Lam
 */
public interface IScriptExecutor {

	// Used "__" prefix (Python convention) for this local variable 
	public static final String VAR_EXECUTOR = "__executor__";
	
	public static final String VAR_SILENCE_MODE = "slienceMode";
	
	public void runScript(String script);
	
	public void runScript(String script, boolean silenceMode);
	
	public void runScript(Reader reader);
	
	public void runScript(IScriptBlock scriptBlock);
	
	public void shutDown();
	
	// Can be null before the system is initialised
	public ScriptEngine getEngine();
	
	public boolean isInitialised();
	
	// Internal
	// Allow caller to interact with the engine with the thread pool 
	public void runTask(Runnable task);
	
	// It tries its best to interrupt all eval actions queued in this executor
	public void interrupt();
	
	public boolean isBusy();
	
	public <T extends ScriptExecutorEvent> void addEventListener(
			IEventHandler<T> listener);
	
	public <T extends ScriptExecutorEvent> void removeEventListener(
			IEventHandler<T> listener);
	
}
