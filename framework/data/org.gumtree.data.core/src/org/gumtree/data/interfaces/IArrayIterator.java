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
 * @brief The IArrayIterator interface permits to run through all values of the associated IArray.
 * 
 * This interface allows the user to iterate over a IArray's values. The way the
 * IArray is traveled depends on how it has been defined.
 * <br>
 * When initialized, the iterator should be invalid: starting at index -1.
 * It means that hasNext() returns true and the first element is accessed
 * using get*Next().
 * The set methods replace the last element returned by <i>next</i> with the  
 * specified operation.<br>
 * To rewrite all values of a IArray, using an iterator, should be done as follow:<br>
 * <code>
 *  short value = 0;<br>
 *  IArrayIterator iter = my_array.getIterator();<br>
 *  while( iter.hasNext() ) {<br>
 *    iter.getShortNext();<br>
 *    iter.setShort(value);<br>
 *  }<br>
 * </code>
 * @author rodriguez
 */
public interface IArrayIterator extends IModelObject {

	/**
	 * Return true if there are more elements in the iteration.
	 * 
	 * @return true or false
	 */
	boolean hasNext();

	/**
	 * Get next value as a double.
	 * 
	 * @return double value
	 */
	double getDoubleNext();

	/**
   * Set current value with a given double.
	 * 
   * @param val double value
	 */
	void setDoubleCurrent(double val);

	/**
	 * Get next value as a float.
	 * 
	 * @return float value
	 */
	float getFloatNext();

	/**
     * Set current value with a float.
	 * 
     * @param val float value
	 */
	void setFloatCurrent(float val);

	/**
	 * Get next value as a long.
	 * 
	 * @return long value
	 */
	long getLongNext();

	/**
     * Set current value with a long.
	 * 
     * @param val long value
	 */
	void setLongCurrent(long val);

	/**
     * Get next value as a integer.
	 * 
	 * @return integer value
	 */
	int getIntNext();

	/**
     * Set current value with a integer.
	 * 
     * @param val integer value
	 */
	void setIntCurrent(int val);

	/**
	 * Get next value as a short.
	 * 
	 * @return short value
	 */
	short getShortNext();

	/**
     * Set current value with a short.
	 * 
     * @param val short value
	 */
	void setShortCurrent(short val);

	/**
	 * Get next value as a byte.
	 * 
	 * @return byte value
	 */
	byte getByteNext();

	/**
     * Set current value with a byte.
	 * 
     * @param val byte value
	 */
	void setByteCurrent(byte val);

	/**
	 * Get next value as a char.
	 * 
	 * @return char value
	 */
	char getCharNext();

	/**
     * Set current value with a char.
	 * 
     * @param val char value
	 */
	void setCharCurrent(char val);

	/**
	 * Get next value as a boolean.
	 * 
	 * @return true or false
	 */
	boolean getBooleanNext();

	/**
     * Set current value with a boolean.
	 * 
     * @param val true or false
	 */
	void setBooleanCurrent(boolean val);

	/**
	 * Get next value as an Object.
	 * 
	 * @return Object
	 */
	Object getObjectNext();

	/**
     * Set current value with a Object.
	 * 
     * @param val any Object
	 */
	void setObjectCurrent(Object val);

	/**
	 * Jump to the next element
	 * 
	 * @return iterator
	 */
	IArrayIterator next();
	
	/**
	 * Get the current counter, use for debugging.
	 * 
	 * @return array of integer
	 */
	int[] getCounter();
}
