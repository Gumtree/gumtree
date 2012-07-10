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

import org.gumtree.data.exception.ShapeNotMatchException;

/**
 * A Dimension is used to define the array shape of a DataItem. It may be shared
 * among DataItems, which provides a simple yet powerful way of associating
 * DataItems.
 * 
 * @author nxi
 * 
 */
public interface IDimension extends IModelObject {

	/**
	 * Returns the name of this Dimension; may be null. A Dimension with a null
	 * name is called "anonymous" and must be private. Dimension names are
	 * unique within a Group.
	 * 
	 * @return String object
	 */
	String getName();

	/**
	 * Get the length of the Dimension.
	 * 
	 * @return integer value
	 */
	int getLength();

	/**
	 * If unlimited, then the length can increase; otherwise it is immutable.
	 * 
	 * @return true or false
	 */
	boolean isUnlimited();

	/**
	 * If variable length, then the length is unknown until the data is read.
	 * 
	 * @return true or false
	 */
	boolean isVariableLength();

	/**
	 * If this Dimension is shared, or is private to a Variable. All Dimensions
	 * in NetcdfFile.getDimensions() or Group.getDimensions() are shared.
	 * Dimensions in the Variable.getDimensions() may be shared or private.
	 * 
	 * @return true or false
	 */
	boolean isShared();

	/**
	 * Get the coordinate variables or coordinate variable aliases if the
	 * dimension has any, else return an empty list. A coordinate variable has
	 * this as its single dimension, and names this Dimensions's the
	 * coordinates. A coordinate variable alias is the same as a coordinate
	 * variable, but its name must match the dimension name. If numeric,
	 * coordinate axis must be strictly monotonically increasing or decreasing.
	 * 
	 * @return IArray containing coordinates
	 */
	IArray getCoordinateVariable();

	/**
	 * Instances which have same contents are equal.
	 * 
     * @param oo Object
	 * @return true or false
	 */
	boolean equals(Object oo);

	/**
	 * Override Object.hashCode() to implement equals.
	 * 
	 * @return integer value
	 */
	int hashCode();

	/**
	 * String representation.
	 * 
	 * @return String object
	 */
	String toString();

	/**
	 * Dimensions with the same name are equal.
	 * 
     * @param o compare to this Dimension
	 * @return 0, 1, or -1
	 */
	int compareTo(Object o);

	/**
	 * String representation.
	 * 
     * @param strict boolean type
	 * @return String object
	 */
	String writeCDL(boolean strict);

	/**
	 * Set whether this is unlimited, meaning length can increase.
	 * 
     * @param b boolean type
	 */
	void setUnlimited(boolean b);

	/**
	 * Set whether the length is variable.
	 * 
     * @param b boolean type
	 */
	void setVariableLength(boolean b);

	/**
	 * Set whether this is shared.
	 * 
     * @param b boolean type
	 */
	void setShared(boolean b);

	/**
	 * Set the Dimension length.
	 * 
     * @param n integer value
	 */
	void setLength(int n);

	/**
	 * Rename the dimension.
	 * 
     * @param name String object
	 */
	void setName(String name);

	/**
	 * Set coordinates values for this dimension.
	 * 
     * @param array with new coordinates
	 */
	void setCoordinateVariable(IArray array) throws ShapeNotMatchException;

}
