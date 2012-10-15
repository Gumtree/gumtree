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

/***
 * @brief The ISliceIterator interface permits to access easily a to a sub-part of an array.
 * 
 * This is a way to iterate over slices of arrays. Each iteration returns 
 * an array of dimension dim, representing the last dim dimensions of the 
 * input array. So for 3D data consisting of a set of 2D arrays, each of 
 * the 2D arrays will be returned.
 *
 * @author nxi
 * 
 */
public interface ISliceIterator extends IModelObject {

	/**
     * Check if there is a next slice.
	 * 
     * @return Boolean type 
	 */
	boolean hasNext();

	/**
	 * Jump to the next slice.
	 * 
	 */
	void next();

	/**
     * Get the next slice of IArray.
	 * 
     * @return CDMA IArray
	 * @throws InvalidRangeException
	 */
	IArray getArrayNext() throws InvalidRangeException;

	/**
	 * Get the current slice of Array.
	 * 
	 * @return GDM Array
	 * @throws InvalidRangeException
	 *             Created on 10/11/2008
	 */
	// [ANSTO][Tony][2012-05-02] This method is required by ANSTO code.
	IArray getArrayCurrent() throws InvalidRangeException;

	/**
	 * Get the shape of any slice that is returned. This could be used when a
	 * temporary array of the right shape needs to be created.
	 * 
	 * @return dimensions of a single slice from the iterator
	 * @throws InvalidRangeException
	 *             invalid range
	 */
	int[] getSliceShape() throws InvalidRangeException;
	
	/**
	 * Get the slice position in the whole array from which this slice iterator
	 * was created.
	 * @return <code>int</code> array of the current position of the slice
	 * @note rank of the returned position is the same as the IArray shape we are slicing 
	 */
    int[] getSlicePosition();
}
