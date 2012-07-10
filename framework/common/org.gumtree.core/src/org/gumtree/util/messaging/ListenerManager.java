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

package org.gumtree.util.messaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The only implementation of IListenerManager.
 * 
 * This class should not be extended by clients (extend EventManager if
 * necessary).
 * 
 * @since 1.0
 * 
 */
public final class ListenerManager<E> implements IListenerManager<E> {

	private static final int NUMBER_OF_THREADS = 1;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(ListenerManager.class);

	// Static instance (shared at runtime) of thread pool
	private static ExecutorService executor = Executors
			.newFixedThreadPool(NUMBER_OF_THREADS);

	/**
	 * A collection of objects listening to changes to this manager. This
	 * collection is <code>null</code> if there are no listeners.
	 */
	private transient ListenerList listenerList = null;

	// public ListenerManager() {
	// executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gumtree.core.util.IListenerManager#addListenerObject(java.lang.Object
	 * )
	 */
	public synchronized void addListenerObject(final E listener) {
		if (listenerList == null) {
			listenerList = new ListenerList(ListenerList.IDENTITY);
		}

		listenerList.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IListenerManager#clearListeners()
	 */
	public synchronized void clearListeners() {
		if (listenerList != null) {
			listenerList.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.core.util.IListenerManager#isListenerAttached()
	 */
	public boolean isListenerAttached() {
		return (listenerList != null && listenerList.size() > 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.gumtree.core.util.IListenerManager#removeListenerObject(java.lang
	 * .Object)
	 */
	public synchronized void removeListenerObject(final E listener) {
		if (listenerList != null) {
			listenerList.remove(listener);

			if (listenerList.isEmpty()) {
				listenerList = null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<E> getListeners() {
		final ListenerList list = listenerList;
		if (list == null || list.getListeners().length == 0) {
			return new ArrayList<E>();
		}
		return (List<E>) Arrays.asList(list.getListeners());
	}

	public void invokeListeners(final ISafeListenerRunnable<E> runnable) {
		asyncInvokeListeners(runnable);
	}

	public synchronized void syncInvokeListeners(
			final ISafeListenerRunnable<E> runnable) {
		for (final E listener : getListeners()) {
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					runnable.handleException(listener, exception);
				}

				public void run() throws Exception {
					runnable.run(listener);
				}
			});
		}
	}

	public void asyncInvokeListeners(final ISafeListenerRunnable<E> runnable) {
		// No need to invoke for illegal argument
		if (runnable == null) {
			return;
		}
		executor.submit(new Runnable() {
			public void run() {
				for (final E listener : getListeners()) {
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							runnable.handleException(listener, exception);
						}

						public void run() throws Exception {
							runnable.run(listener);
						}
					});
				}
			}
		});
	}

}
