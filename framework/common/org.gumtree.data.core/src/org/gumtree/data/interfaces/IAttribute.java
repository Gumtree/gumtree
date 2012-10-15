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

/**
 * @brief The IAttribute interface describes a metadata of a IContainer.
 * 
 * CDMA Attribute, with name and value. The metadata is carried by data items and groups.
 * Those data have a name, an element type, size and can be whether an IArray or a scalar.
 * 
 * 
 * @author nxi
 */
public interface IAttribute extends IModelObject {

	/**
     * Get the name of this IAttribute. Attribute's names are unique within a
     * IContainer.
	 * 
	 * @return String object
	 */
	String getName();

	/**
     * Get the data type of the IAttribute value.
	 * 
	 * @return Class object
	 */
	Class<?> getType();

	/**
     * True if value is a string or an array of strings.
	 * 
	 * @return true or false
	 */
	boolean isString();

	/**
     * True if the value is an array (getLength() > 1).
	 * 
	 * @return true or false
	 */
	boolean isArray();

	/**
     * Get the number of element within the array of values.
	 * 
	 * @return integer value
     * @note returns 1 if it is a scalar attribute 
	 */
	int getLength();

	/**
     * Get the value as an IArray.
	 * 
     * @return IArray of value(s).
	 */
	IArray getValue();

	/**
     * Retrieve the string value or null if the value isn't a string.
	 * 
     * @return string value
	 * @see IAttribute#isString
	 */
	String getStringValue();

	/**
     * Retrieve the string value or null if the value isn't a string.
	 * 
     * @param index integer value
     * @return string value
	 * @see IAttribute#isString
     * @see IAttribute#isArray
	 */
	String getStringValue(int index);

	/**
	 * Retrieve numeric value. Equivalent to <code>getNumericValue(0)</code>
	 * 
	 * @return the first element of the value array, or null if its a String.
	 */
	Number getNumericValue();

	/**
	 * Retrieve a numeric value by index. If its a String, it will try to parse
	 * it as a double.
	 * 
     * @param index the index into the value array.
	 * @return Number <code>value[index]</code>, or null if its a non-parsable
	 *         String or the index is out of range.
	 */
	Number getNumericValue(int index);

	/**
     * Instances which have same content's values are equal.
	 * 
     * @param o Object
	 * @return true or false
	 */
	boolean equals(Object o);

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
     * Set the value as a String, trimming trailing zeroes.
	 * 
     * @param val String object
	 */
	void setStringValue(String val);

	/**
     * Set the values from an IArray.
	 * 
     * @param value IArray object
	 */
	void setValue(IArray value);

}
