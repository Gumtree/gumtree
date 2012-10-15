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

import org.gumtree.data.interfaces.IModelObject;
import org.gumtree.data.utils.Utilities.ParameterType;


/**
 * @brief The IPathParameter interface is used to make a selective choice when browsing a IDataset.
 * 
 * A IPathParameter represents conditions that permits identifying a specific node using the 
 * extended dictionary mechanism. 
 * When according to a given IPath several IContainer can be returned, the path parameter
 * will make possible how to find which one is relevant.
 * <p>
 * The parameter can consist in a regular expression on a name, an attribute or 
 * whatever that should be relevant to formally identify a specific node while 
 * several are possible according to the path.
 * 
 * @see org.gumtree.data.utils.Utilities.ParameterType
 * @see org.gumtree.data.dictionary.IPath
 * 
 * @author rodriguez
 *
 */
public interface IPathParameter extends IModelObject, Cloneable {

    // ---------------------------------------------------------
    /// Public methods
    // ---------------------------------------------------------
    /**
     * Get the filter's kind
     * 
     * @return filter's kind
     */
	public ParameterType getType();
    
	/**
     * Get the filter's value
     * 
     * @return filter's value
     */
	public Object getValue();

	/**
     * Get the filter's name
     * 
     * @return name of the filter
     */
	public String getName();
	

	/**
     * Set the filter's value
     * 
     * @param value of the filter
     */
	public void setValue(Object value);

    /**
     * Equality test
     * 
     * @return true if both KeyFilter have same kind and value
     */

	public boolean equals(IPathParameter keyfilter);

    /**
     * To String method
     * 
     * @return a string representation of the KeyFilter
     */
    public String toString();
    
    /**
     * Clone this IKeyFilter
     * @return a copy of this
     */
    public IPathParameter clone();
}


