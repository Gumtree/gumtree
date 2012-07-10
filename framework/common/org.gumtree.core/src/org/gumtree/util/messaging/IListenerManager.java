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

import java.util.List;

/**
 * Copy of the Event Manager from org.eclipse.core.commands.common.EventManager.
 * Methods have be modified to allow public access so Bridge pattern can be used
 * with this manager.
 * 
 * This interface is not intented to be implmeneted by clients.
 * 
 * @since 1.0
 */
public interface IListenerManager<E> {

	/**
	 * A collection of objects listening to changes to this manager. This
	 * collection is <code>null</code> if there are no listeners.
	 */
	public void addListenerObject(E listener);

	/**
	 * Clears all of the listeners from the listener list.
	 */
	public void clearListeners();

	/**
	 * Returns the listeners attached to this event manager.
	 * 
	 * @return The listeners currently attached; may be empty, but never
	 *         <code>null</code>
	 */
	public List<E> getListeners();

	/**
	 * Whether one or more listeners are attached to the manager.
	 * 
	 * @return <code>true</code> if listeners are attached to the manager;
	 *         <code>false</code> otherwise.
	 */
	public boolean isListenerAttached();

	/**
	 * Removes a listener from this manager.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public void removeListenerObject(E listener);

	/**
	 * Invoke listeners for callback.
	 * 
	 * Same as calling asyncInvokeListeners(runnable)
	 * 
	 * @param runnable
	 */
	public void invokeListeners(final ISafeListenerRunnable<E> runnable);

	/**
	 * Invoke listeners synchronously for callback.
	 * 
	 * @param runnable
	 */
	public void syncInvokeListeners(final ISafeListenerRunnable<E> runnable);

	/**
	 * Invoke listeners asynchronously for callback.
	 * 
	 * @param runnable
	 */
	public void asyncInvokeListeners(final ISafeListenerRunnable<E> runnable);

}
