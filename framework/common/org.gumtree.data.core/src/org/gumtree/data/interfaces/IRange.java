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

import org.gumtree.data.exception.InvalidRangeException;

/**
 * @brief The IRange interface describes a dimension of a IIndex.
 * 
 * Represents a set of integers that compound a particular dimension of a array.
 * It defines for a that specific dimension:<br>
 *  - its length (number of elements)<br>
 *  - its first element offset<br>
 *  - the offset to between two consecutive elements<br>
 * The IRange interface is a canonical element that will do index calculation
 * to access a particular cell in an array. 
 * 
 * @author nxi 
 */
public interface IRange extends IModelObject {

	/**
	 * Create a new Range by composing a Range that is relative to this Range.
	 * 
     * @param r range relative to base
	 * @return combined Range, may be EMPTY
	 * @throws InvalidRangeException
	 *             elements must be nonnegative, 0 <= first <= last
	 */
	IRange compose(IRange r) throws InvalidRangeException;

	/**
	 * Create a new Range by compacting this Range by removing the stride. first
	 * = first/stride, last=last/stride, stride=1.
	 * 
	 * @return compacted Range
	 * @throws InvalidRangeException
	 *             elements must be nonnegative, 0 <= first <= last
	 */
	IRange compact() throws InvalidRangeException;

	/**
	 * Create a new Range shifting this range by a constant factor.
	 * 
     * @param origin subtract this from first, last
	 * @return shift range
	 * @throws InvalidRangeException
	 *             elements must be nonnegative, 0 <= first <= last
	 */
	IRange shiftOrigin(int origin) throws InvalidRangeException;

	/**
	 * Create a new Range by intersecting with a Range using same interval as
	 * this Range. NOTE: intersections when both Ranges have strides are not
	 * supported.
	 * 
     * @param r range to intersect
	 * @return intersected Range, may be EMPTY
	 * @throws InvalidRangeException
	 *             elements must be nonnegative
	 */
	IRange intersect(IRange r) throws InvalidRangeException;

	/**
	 * Determine if a given Range intersects this one. NOTE: we dont yet support
	 * intersection when both Ranges have strides
	 * 
     * @param r range to intersect
	 * @return true if they intersect
	 */
	boolean intersects(IRange r);

	/**
	 * Create a new Range by making the union with a Range using same interval
	 * as this Range. NOTE: no strides.
	 * 
     * @param r range to add
	 * @return intersected Range, may be EMPTY
	 * @throws InvalidRangeException
	 *             elements must be nonnegative
	 */
	IRange union(IRange r) throws InvalidRangeException;

	/**
	 * Get the number of elements in the range.
	 * 
	 * @return the number of elements in the range.
	 */
	int length();

	/**
	 * Get i-th element.
	 * 
     * @param i index of the element
	 * @return the i-th element of a range.
	 * @throws InvalidRangeException
	 *             i must be: 0 <= i < length
	 */
	int element(long i) throws InvalidRangeException;

	/**
	 * Get the index for this element: inverse of element.
	 * 
     * @param elem the element of the range
	 * @return index
	 * @throws InvalidRangeException
	 *             if illegal element
	 */
	long index(int elem) throws InvalidRangeException;

	/**
	 * Is the ith element contained in this Range?
	 * 
     * @param i index in the original Range
	 * @return true if the ith element would be returned by the Range iterator
	 */
	boolean contains(int i);

	/**
	 * @return first element's index in range
	 */
	long first();

	/**
	 * @return last element's index in range, inclusive
	 */
	long last();

	/**
	 * @return stride, must be >= 1
	 */
	long stride();

	/**
	 * Get name.
	 * 
	 * @return name, or null if none
	 */
	String getName();

	/**
	 * Find the smallest element k in the Range, such that
	 * <ul>
	 * <li>k >= first
	 * <li>k >= start
	 * <li>k <= last
	 * <li>k = first + i * stride for some integer i.
	 * </ul>
	 * 
     * @param start starting index
	 * @return first in interval, else -1 if there is no such element.
	 */
	int getFirstInInterval(int start);

}
