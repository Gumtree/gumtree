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

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.gumtree.core.service.IServiceManager;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.service.eventbus.IEventBus;
import org.gumtree.service.eventbus.IEventHandler;
import org.gumtree.util.PlatformUtils;
import org.gumtree.util.messaging.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScriptExecutor implements IScriptExecutor {
	
	private static Logger logger = LoggerFactory.getLogger(ScriptExecutor.class);
	
	private ExecutorService executorService;

	private IEventBus eventBus;
	
	private IScriptingManager scriptingManager;
	
	private ScriptEngine engine;
	
	private List<Future<?>> futures;
	
	private String id;
	
	private boolean initialised = false;
	
	// Hopefully we can keep track of the engine state in this
	// single thread environment
	private volatile boolean isBusy = false;
	
	public ScriptExecutor() {
		this("");
	}
	
	public ScriptExecutor(final String engineName) {
		super();
		id = UUID.randomUUID().toString();
		// Wait forever until the scripting manager is available
		setScriptingManager(ServiceUtils.getServiceManager().getService(
				IScriptingManager.class, IServiceManager.NO_TIMEOUT));
		setEventBus(PlatformUtils.getPlatformEventBus());
		futures = new ArrayList<Future<?>>(2);
		executorService = Executors.newSingleThreadExecutor();
		runTask(new Runnable() {
			public void run() {
				engine = getScriptingManager().createEngine(engineName);
				if (engine.getContext() == null) {
					engine.setContext(new ObservableScriptContext());
				}
				engine.put(VAR_EXECUTOR, ScriptExecutor.this);
				initialised = true;
			}
		});
	}
	
	public ScriptExecutor(final ScriptEngine engine) {
		super();
		id = UUID.randomUUID().toString();
		futures = new ArrayList<Future<?>>(2);
		executorService = Executors.newSingleThreadExecutor();
		this.engine = engine;
		runTask(new Runnable() {
			public void run() {
				if (engine.getContext() == null) {
					engine.setContext(new ObservableScriptContext());
				}
				engine.put(VAR_EXECUTOR, ScriptExecutor.this);
				initialised = true;
			}
		});
	}
	
	public String getId() {
		return id;
	}
	
	public boolean isInitialised() {
		return initialised;
	}
	
	public void runScript(final String script) {
		runScript(script, false);
	}

	public void runScript(final String script, final boolean silenceMode) {
		runTask(new Runnable() {
			public void run() {
				if (engine != null) {
					try {
						// This gives hint to the engine either echo the script in UI or not.
						// The actual behaviour of this slience mode is dependent on the implementation of the scripting engine.
						engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put(VAR_SILENCE_MODE, silenceMode);
						setBusy(true);
						engine.eval(script);
					} catch (Exception e) {
						try {
							engine.getContext().getErrorWriter().write(e.getMessage());
						} catch (IOException ioe) {
							// Nothing more that I can do about
							logger.error("Reporting error has encountered problem.", ioe);
						}
					} finally {
						setBusy(false);	
					}
					getEventBus().postEvent(new ScriptExecutorCompletionEvent(ScriptExecutor.this));
					new EventBuilder(EVENT_TOPIC_SCRIPT_EXECUTOR_COMPLETED)
							.append(EVENT_PROP_EXECUTOR_ID, getId())
							.post();
				}
			}
		});
	}
		
	public void runScript(final Reader reader) {
		runTask(new Runnable() {
			public void run() {
				if (engine != null) {
					try {
						setBusy(true);
						engine.eval(reader);
					} catch (Exception e) {
						try {
							engine.getContext().getErrorWriter().write(e.getMessage());
						} catch (IOException ioe) {
							// Nothing more that I can do about
							logger.error("Report error problem.", ioe);
						}
					} finally {
						try {
							reader.close();
						} catch (IOException e) {
						}
						setBusy(false);
					}
					getEventBus().postEvent(new ScriptExecutorCompletionEvent(ScriptExecutor.this));
					new EventBuilder(EVENT_TOPIC_SCRIPT_EXECUTOR_COMPLETED)
							.append(EVENT_PROP_EXECUTOR_ID, getId())
							.post();
				}
			}			
		});
	}
	
	public void runScript(final IScriptBlock scriptBlock) {
		runTask(new Runnable() {
			public void run() {
				if (engine != null) {
					try {
						if (!scriptBlock.isSkip()) {
							setBusy(true);
							engine.eval(scriptBlock.getReader());
							try {
								// Post process
								scriptBlock.postProcess();
							} catch (Throwable e) {
								// Catch other exception from postProcess
								logger.error("Error occured in post processing script block", e);
							}
						}
					} catch (Exception e) {
						try {
							engine.getContext().getErrorWriter().write(e.getMessage());
						} catch (IOException ioe) {
							// Nothing more that I can do about
							logger.error("Report error problem.", ioe);
						}
					} finally {
						setBusy(false);
					}
					getEventBus().postEvent(new ScriptExecutorCompletionEvent(ScriptExecutor.this));
					new EventBuilder(EVENT_TOPIC_SCRIPT_EXECUTOR_COMPLETED)
							.append(EVENT_PROP_EXECUTOR_ID, getId())
							.post();
				}
			}			
		});
	}
	
	public ScriptEngine getEngine() {
		return engine;
	}
	
	// Internal use only
	// It is not recommend to use this method since it dose not keep track
	// on the executor state
	public void runTask(Runnable task) {
		if (executorService == null || executorService.isShutdown()) {
			executorService = Executors.newSingleThreadExecutor();
		}
		synchronized (futures) {
			List<Future<?>> futureList = new ArrayList<Future<?>>(futures.size());
			Collections.copy(futures, futureList);
			for (Future<?> future : futureList) {
				if (future.isCancelled() || future.isDone()) {
					futures.remove(future);
				}
			}
			Future<?> future = executorService.submit(task);
			futures.add(future);
		}
	}
	
	public void runIndependentTask(Runnable task) {
		ExecutorService service = Executors.newCachedThreadPool();
		synchronized (futures) {
			List<Future<?>> futureList = new ArrayList<Future<?>>(futures.size());
			Collections.copy(futures, futureList);
			for (Future<?> future : futureList) {
				if (future.isCancelled() || future.isDone()) {
					futures.remove(future);
				}
			}
			Future<?> future = service.submit(task);
			futures.add(future);
		}
	}
	
	public void runIndependentScript(final String script) {
		runIndependentTask(new Runnable() {
			public void run() {
				if (engine != null) {
					try {
						// This gives hint to the engine either echo the script in UI or not.
						// The actual behaviour of this slience mode is dependent on the implementation of the scripting engine.
//						engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put(VAR_SILENCE_MODE, false);
//						setBusy(true);
						System.err.println(script);
						engine.eval(script);
					} catch (Exception e) {
						try {
							engine.getContext().getErrorWriter().write(e.getMessage());
						} catch (IOException ioe) {
							// Nothing more that I can do about
							logger.error("Reporting error has encountered problem.", ioe);
						}
					} finally {
//						setBusy(false);	
					}
//					getEventBus().postEvent(new ScriptExecutorCompletionEvent(ScriptExecutor.this));
//					new EventBuilder(EVENT_TOPIC_SCRIPT_EXECUTOR_COMPLETED)
//							.append(EVENT_PROP_EXECUTOR_ID, getId())
//							.post();
				}
			}
		});
	}
	
	public void interrupt() {
		synchronized (futures) {
			for (Future<?> future : futures) {
				future.cancel(true);
			}
			executorService.shutdown();
			executorService.shutdownNow();
		}
	}
	
	public boolean isBusy() {
		return isBusy;
	}
	
	// This should be thread safe since only one thread can call this method
	private void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
		getEventBus().postEvent(new ScriptExecutorStateEvent(this, isBusy));
		new EventBuilder(EVENT_TOPIC_SCRIPT_EXECUTOR_BUSY).append(
				EVENT_PROP_EXECUTOR_ID, getId()).post();
	}
	
	public synchronized void shutDown() {
		executorService.shutdown();
		executorService = null;
		engine = null;
    }

	public <T extends ScriptExecutorEvent> void addEventListener(IEventHandler<T> listener) {
		getEventBus().subscribe(this, listener);
	}
	
	public <T extends ScriptExecutorEvent> void removeEventListener(IEventHandler<T> listener) {
		getEventBus().unsubscribe(this, listener);
	}
	
	public IEventBus getEventBus() {
		if (eventBus == null) {
			eventBus = PlatformUtils.getPlatformEventBus();
		}
		return eventBus;
	}
	
	// Use e4 DI?
	public void setEventBus(IEventBus eventBus) {
		this.eventBus = eventBus;
	}

	public IScriptingManager getScriptingManager() {
		return scriptingManager;
	}

	// Use e4 DI
	public void setScriptingManager(IScriptingManager scriptingManager) {
		this.scriptingManager = scriptingManager;
	}
	
}
