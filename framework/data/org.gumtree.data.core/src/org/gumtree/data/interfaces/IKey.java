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

package org.gumtree.data.interfaces;

import java.util.List;

import org.gumtree.data.dictionary.IPathParameter;

/**
 * @brief The IKey is used by group to interrogate the dictionary.
 * 
 * The key's name corresponds to an entry in the dictionary. This entry 
 * targets a path in the currently explored document. The group will open it.
 * <p>
 * The IKey can carry some filters to help group to decide which node is relevant.
 * The filters can specify an order index to open a particular type of node, an 
 * attribute, a part of the name... 
 * <p>
 * @author rodriguez
 */

public interface IKey extends IModelObject, Cloneable, Comparable<Object> {
    /**
     * Get the entry name in the dictionary that will be 
     * searched when using this key. 
     * 
     * @return the name of this key
     */
    String getName();

    /**
     * Set the entry name in the dictionary that will be 
     * searched when using this key. 
     * 
     * @param name of this key
     */
    void setName(String name);

    /**
     * Return true if both key have similar names. Filters are not compared. 
     * 
     * @param key to compare
     * @return true if both keys have same name
     */
    boolean equals(Object key);

    /**
     * Get the list of parameters that will be applied when using this key.
     * 
     * @return list of IPathParameter 
     */
    List<IPathParameter> getParameterList();

    /**
     * Add a IPathParameter to this IKey that will be used when 
     * searching an object with this key. .
     * 
     * @param parameter to be applied
     * @note work as a FILO
     */
    void pushParameter(IPathParameter filter);

    /**
     * Remove a IPathParameter to this IKey that will be used when 
     * searching an object with this key.
     * 
     * @return parameter that won't be applied anymore
     * @note work as a FILO
     */
    IPathParameter popParameter();

    String toString();

    /**
     * Copy entirely the key : name and filters are cloned
     * @return a copy of this key
     */
    IKey clone();
}
