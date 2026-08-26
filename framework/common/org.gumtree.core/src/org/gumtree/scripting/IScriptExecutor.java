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

	public static final String EVENT_TOPIC_SCRIPT_EXECUTOR = "org/gumtree/scripting/executor";
	
	public static final String EVENT_TOPIC_SCRIPT_EXECUTOR_BUSY = EVENT_TOPIC_SCRIPT_EXECUTOR + "/busy";
	
	public static final String EVENT_TOPIC_SCRIPT_EXECUTOR_COMPLETED = EVENT_TOPIC_SCRIPT_EXECUTOR + "/completed";
	
	public static final String EVENT_PROP_EXECUTOR_ID = "executorId";
	
	// Used "__" prefix (Python convention) for this local variable 
	public static final String VAR_EXECUTOR = "__executor__";
	
	public static final String VAR_SILENCE_MODE = "slienceMode";

	// Upper bound for waiting on engine initialisation.  This is only a backstop
	// against waiting forever: a failed initialisation is reported immediately
	// through getInitialisationError() and does not wait this out.  Generous
	// because OsgiPackageLoader indexes every bundle's classpath into Jython,
	// which measured ~3.5 minutes of CPU on a warm cache and more on a cold one.
	public static final long DEFAULT_INITIALISATION_TIMEOUT = 600000;

	public String getId();
	
	public void runScript(String script);
	
	public void runScript(String script, boolean silenceMode);
	
	public void runScript(Reader reader);
	
	public void runScript(IScriptBlock scriptBlock);
	
	public void shutDown();
	
	// Can be null before the system is initialised
	public ScriptEngine getEngine();
	
	public boolean isInitialised();

	// Returns the failure that stopped the engine from initialising, or null when
	// initialisation has not failed.  A non-null value means this executor will
	// never become initialised.
	public Throwable getInitialisationError();

	// Waits until the engine is ready, initialisation has failed, or the timeout
	// expires.  Returns true only when the engine is ready.
	public boolean awaitInitialisation(long timeoutMillis);

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
