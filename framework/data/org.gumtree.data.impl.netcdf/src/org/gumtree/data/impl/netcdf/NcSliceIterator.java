/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    James Hester (jxh@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.impl.netcdf;

import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.ISliceIterator;

/**
 * This is a way to iterate over slices of arrays, and should eventually be
 * incorporated into the Gumtree Data Model rather than lying around here. Each
 * iteration returns an array of dimension dim, representing the last dim
 * dimensions of the input array. So for 3D data consisting of a set of 2D
 * arrays, each of the 2D arrays will be returned.
 */
public class NcSliceIterator implements ISliceIterator {

	/**
	 * Reference of the array.
	 */
	private IArray wholeArray;
	/**
	 * The iterator of the whole_array.
	 */
	private IArrayIterator higherDimIter;
	/**
	 * The shape of the slice.
	 */
	private int[] targetShape;
	/**
	 * If there is just one slice. Workaround for not being able to reshape
	 */
	private Boolean oneSlice = false;
	/**
	 * The number of slices. workaround for not being able to reshape.
	 */
	private int oneSliceCount = -1;

	/**
	 * Create a slice iterator from an Array and dimension. If the dimension of
	 * the requested slice is the same as the array, at least one iteration will
	 * be returned
	 * 
	 * @param inarray
	 *            The array from which the slices come
	 * @param dim
	 *            The dimension of the slice returned
	 * @throws InvalidRangeException
	 *             omva;od ramge
	 */
	public NcSliceIterator(final IArray inarray, final int dim)
			throws InvalidRangeException {
		wholeArray = inarray;
		// If ranks are equal, make sure at least one iteration is performed.
		// We cannot use 'reshape' (which would be ideal) as that creates a
		// new copy of the array storage and so is unusable
		if (inarray.getRank() == dim) {
			oneSlice = true;
		} else {
			int[] waShape = wholeArray.getShape();
			int dlength = wholeArray.getRank();
			int[] rangeList = waShape.clone();
			// shape to make a 2D array from multiple-dim array
			targetShape = waShape.clone();
			for (int i = 0; i < dlength - dim; i++) {
				targetShape[i] = 1;
			}
			for (int i = 0; i < dim; i++) {
				rangeList[dlength - i - 1] = 1;
			}
			// Create an iterator over the higher dimensions. We leave in the
			// final dimensions so that we can use the getCurrentCounter method
			// to create an origin.
			IArray loopArray = wholeArray.getArrayUtils().sectionNoReduce(new int[dlength],
					rangeList, null).getArray();
			higherDimIter = loopArray.getIterator();
		}
	}

	/**
	 * Check if there is next slice.
	 * 
	 * @return Boolean type Created on 10/11/2008
	 */
	@Override
	public boolean hasNext() {
		if (oneSlice) {
			return (oneSliceCount == -1);
		}
		return higherDimIter.hasNext();
	}

	/**
	 * Jump to the next slice.
	 * 
	 * Created on 10/11/2008
	 */
	@Override
	public void next() {
		if (oneSlice) {
			if (oneSliceCount >= 0) {
				throw new java.util.NoSuchElementException();
			}
			oneSliceCount++;
		} else {
			higherDimIter.next();
		}
	}

	/**
	 * Get the next slice of Array.
	 * 
	 * @return GDM Array
	 * @throws InvalidRangeException
	 *             Created on 10/11/2008
	 */
	@Override
	public IArray getArrayNext() throws InvalidRangeException {
		next();
		return createslice();
	}

	/**
	 * Get the current slice of Array.
	 * 
	 * @return GDM Array
	 * @throws InvalidRangeException
	 *             Created on 10/11/2008
	 */
	@Override
	public IArray getArrayCurrent() throws InvalidRangeException {
		return createslice();
	}

	/**
	 * Create a slice at the current coordinates.
	 * 
	 * @return a slice Array
	 * @throws InvalidRangeException
	 *             invalid range
	 */
	private IArray createslice() throws InvalidRangeException {
		if (oneSlice && oneSliceCount != 0) {
			throw new java.util.NoSuchElementException();
		}
		if (oneSlice) {
			return wholeArray;
		}
		// A more normal iteration
		// Will have right shape as we did sectionNoReduce, aren't we clever
		int[] currentLocation = higherDimIter.getCounter();
		return wholeArray.getArrayUtils().section(currentLocation, targetShape).getArray();
	}

	/**
	 * Get the shape of any slice that is returned. This could be used when a
	 * temporary array of the right shape needs to be created, for example.
	 * 
	 * @return dimensions of a single slice from the iterator
	 * @throws InvalidRangeException
	 *             invalid range
	 */

	@Override
	public int[] getSliceShape() throws InvalidRangeException {
		if (oneSlice) {
			return wholeArray.getShape();
		}
		return wholeArray.getArrayUtils().section(new int[wholeArray.getRank()], targetShape).getArray()
				.getShape();
	}

	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

	@Override
	public int[] getSlicePosition() {
		throw new UnsupportedOperationException();
	}

}
