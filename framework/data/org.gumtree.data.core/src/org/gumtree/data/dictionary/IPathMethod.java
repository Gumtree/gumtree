/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/


package org.gumtree.data.dictionary;

/**
 * @brief The IPathMethod interface refers to a method that should be executed to obtain the requested data.
 * 
 * This interface is only used by the extended dictionary mechanism. Its aim
 * is to allow mapping dictionary to specify how to get a IDataItem that don't
 * only rely on a specific path. The IPathMethod permits to specify a method that
 * must be executed associated to a IPath. The more often it's called because we have
 * to pre/post process data to fit a particular need. 
 * <p>
 * The method will be called using an already implemented mechanism. The call will
 * be done only when the IPath, carrying it, will be resolved by the ILogicalGroup.
 * <p>
 * @example In case of a stack of spectrums that are split onto various IDataItem
 * if we want to see them as only one IDataItem the only solution will be to use a
 * specific method for the request.
 * 
 * @author rodriguez
 */
public interface IPathMethod {

	/**
	 * Returns name of the method that will be called (using it's package name)
	 * return String
	 */
	public String getName();

	/**
	 * Sets the name of the method that will be called (using it's package name)
     * 
     * @param method in its namespace
	 */
	public void setName(String method);

	/**
	 * Return parameters Object that are used by this method
     * 
	 * @return Object array
	 */
	public Object[] getParam();

	/**
	 * Set a parameter value that will be used by this method
     * 
     * @param param Object that will be used by this method
	 * @note works as a FIFO
	 */
	public void pushParam(Object param);

	/**
	 * Set a parameter value that will be used by this method
     * 
     * @return Object that will be used by this method
	 * @note works as a FIFO
	 */
	public Object popParam();

	/**
	 * Tells whether or not the method is already contained by the plug-in or if it 
	 * will be dynamically loaded from the external folder specific to the plug-in.
     * 
	 * @return boolean
	 */
	public boolean isExternalCall();

	/**
	 * Set whether or not the method is already contained by the plug-in or if it 
	 * will be dynamically loaded from the external folder specific to the plug-in.
     * 
	 * @return boolean
	 * @see LogicalGroup.resolveMethod
	 * @see org.gumtree.data.dictionary.IClassLoader
	 */
	public void isExternal(boolean external);

}