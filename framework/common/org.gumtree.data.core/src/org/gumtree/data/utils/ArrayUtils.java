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

package org.gumtree.data.utils;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.ISliceIterator;

public abstract class ArrayUtils implements IArrayUtils {

	private IArray m_array;

	public ArrayUtils(IArray array) {
		m_array = array;
	}

	@Override
	public IArray getArray() {
		return m_array;
	}

	/**
	 * Copy the contents of this array to another array. The two arrays must
	 * have the same size.
	 * 
     * @param newArray an existing array
	 * @throws ShapeNotMatchException
	 *             wrong shape
	 */
	@Override
	public void copyTo(IArray newArray) throws ShapeNotMatchException {
		if (getArray().getSize() != newArray.getSize()) {
			throw new ShapeNotMatchException(
					"the two Arrays do not have the same size");
		}
		IArrayIterator iterator = getArray().getIterator();
		IArrayIterator newIterator = newArray.getIterator();
		while (iterator.hasNext()) {
			newIterator.next().setObjectCurrent(iterator.getObjectNext());
		}
	}

	@Override
	public Object copyTo1DJavaArray() {
		IArrayIterator iter = getArray().getIterator();
		Object result = java.lang.reflect.Array.newInstance( 
							getArray().getElementType(), 
							((Long) getArray().getSize()).intValue() );
		int index = 0;
		while( iter.hasNext() ) {
            java.lang.reflect.Array.set(result, index++, iter.getObjectNext());
		}
		
		return result;
	}

	@Override
	public Object copyToNDJavaArray() {
		Object result;
		ISliceIterator slice;
		try {
			slice  = getArray().getSliceIterator(1);
			result = java.lang.reflect.Array.newInstance( 
								getArray().getElementType(), 
								getArray().getShape() );
			Object subPart = null;
			IArray slab;
			IArrayIterator iter;
			int rank = getArray().getRank();
			int[] curPos = new int[rank];
			int[] shape = getArray().getShape();
			int dim = rank - 2;
			
			while( slice.hasNext() ) {
                slab    = slice.getArrayNext();
				subPart = result;
				
				// Get the right array slab
				for(int i = 0; i < rank - 1; i++ ) {
					subPart = java.lang.reflect.Array.get( subPart, curPos[i] );
				}

				// Copy values into the right offset
				iter = slab.getIterator();
				for( int index = 0; index < shape[rank - 1]; index++ ) {
                    java.lang.reflect.Array.set(subPart, index, iter.getObjectNext());
				}
				
				while( dim > -1 ) {
					if( curPos[dim] + 1 < shape[dim] ) {
						curPos[dim]++;
						break;
					}
					else {
						curPos[dim] = 0;
						dim--;
					}
				}
				dim = rank - 2;
			}
		} catch (ShapeNotMatchException e) {
			result = null;
			e.printStackTrace();
		} catch (InvalidRangeException e) {
			result = null;
			e.printStackTrace();
		}
		return result;
	}

	
    // / IArray shape manipulation
	/**
     * Check if the shape matches with another IArray object.
	 * 
     * @param newArray another IArray object
	 * @throws ShapeNotMatchException
	 *             shape not match
	 */
	@Override
	public void checkShape(final IArray newArray) throws ShapeNotMatchException {
		int[] currentShape = getArray().getShape();
		int[] newShape = newArray.getShape();
		if (currentShape.length < newShape.length) {
			throw new ShapeNotMatchException(
					"Current shape can't be smaller than target shape");
		}
		// Checking if each dimension of arrays are equal
		for (int i = 0; i < newShape.length; i++) {
			if (newShape[newShape.length - 1 - i] != currentShape[currentShape.length
					- 1 - i]) {
				throw new ShapeNotMatchException(
						"the target shape does not match");
			}
		}
	}

	/**
	 * Concatenate with another array. The "array" need to be equal or less in
	 * rank.
	 * 
     * @param array IArray object
	 * @return new IArray
	 * @throws ShapeNotMatchException
	 *             mismatching shape
	 * @deprecated under construction TODO finish this method
	 */
	@Deprecated
	@Override
	public IArrayUtils concatenate(final IArray array)
			throws ShapeNotMatchException {
		IArray newArray = null;
		int rank1 = getArray().getRank();
		int rank2 = array.getRank();
		// Check rank are equals
		if (rank1 != rank2) {
			throw new ShapeNotMatchException("different rank");
		} else {
			// Check element type are equal
			Class<?> class1 = getArray().getElementType();
			Class<?> class2 = array.getElementType();
			if (!class1.equals(class2)) {
				throw new ShapeNotMatchException("different type");
			}
			// Starting concatenation
			int[] shape1 = getArray().getShape();
			int[] shape2 = array.getShape();
			int concatDimension = 0;
			int numberOfDifferentLength = 0;
			for (int i = 0; i < rank1; i++) {
				if (shape1[i] != shape2[i]) {
					concatDimension = i;
					numberOfDifferentLength++;
				}
			}
			if (numberOfDifferentLength > 1) {
				throw new ShapeNotMatchException("2 or more dimensions are "
						+ "different in length, at most 1 allowed");
			}
			int[] newShape = new int[rank1];
			for (int i = 0; i < newShape.length; i++) {
				newShape[i] = shape1[i];
			}
			newShape[concatDimension] = shape1[concatDimension]
					+ shape2[concatDimension];
			newArray = Factory.getFactory(getArray().getFactoryName())
					.createArray(class1, newShape);
			ISliceIterator newSliceIterator = null;
			ISliceIterator sliceIterator1 = null;
			ISliceIterator sliceIterator2 = null;
			int sliceDimension = concatDimension;
			try {
				newSliceIterator = newArray.getSliceIterator(sliceDimension);
				sliceIterator1 = getArray().getSliceIterator(sliceDimension);
				sliceIterator2 = array.getSliceIterator(sliceDimension);
				while (newSliceIterator.hasNext()) {
					IArrayIterator newSlice = newSliceIterator.getArrayNext()
							.getIterator();
					IArrayIterator slice1 = sliceIterator1.getArrayNext()
							.getIterator();
					IArrayIterator slice2 = sliceIterator2.getArrayNext()
							.getIterator();
					while (slice1.hasNext()) {
						newSlice.next()
								.setObjectCurrent(slice1.getObjectNext());
					}
					while (slice2.hasNext()) {
						newSlice.next()
								.setObjectCurrent(slice2.getObjectNext());
					}
				}
			} catch (Exception e) {
				throw new ShapeNotMatchException("can not get the slice "
						+ "iterator of rank " + sliceDimension);
			}
			return newArray.getArrayUtils();
		}
	}

	/**
     * Create a new IArray using same backing store as this Array, by eliminating
	 * any dimensions with length one.
	 * 
     * @return the new IArray
	 */
	@Override
	public IArrayUtils reduce() {
		//[SOLEIL][clement] TODO temporary bug fix
		/*
		int[] shape = getArray().getShape();
		List<Integer> shapeList = new ArrayList<Integer>();
		for (int length : shape) {
			if (length > 1) {
				shapeList.add(length);
			}
		}
		if (shapeList.size() == 0) {
			shapeList.add((int) getArray().getSize());
		}
		int[] newShape = new int[shapeList.size()];
		for (int i = 0; i < newShape.length; i++) {
			newShape[i] = shapeList.get(i);
		}
		try {
			return reshape(newShape);
		} catch (ShapeNotMatchException e) {
			return this;
		}
		*/
		IArray array = getArray().copy(false);
		IIndex index = array.getIndex().reduce();
		array.setIndex(index);
		return array.getArrayUtils();
	}
	
	@Override
	public IArrayUtils slice(int dim, int value) {
		IIndex index  = getArray().getIndex();
		// [ANSTO][Tony][2012-05-03] I'm not sure if clone() will have any
		// performance issue here, not it is the safest because sectionNoReduce()
		// will use those array to create when an array.
		int[] shape = index.getShape().clone();
		int[] origin = index.getOrigin().clone();
		long[] stride = index.getStride().clone();

		if (dim >= getArray().getRank()) {
			throw new IllegalArgumentException(
					"Targeted dimension doesn't exist!");
		}
		origin[dim] = value;
		/*
		 //[SOLEIL][clement] faulty algorithm, I think this mistake was mine
		if (dim > 0) {
			shape[dim - 1] = (shape[dim - 1] - 1) * shape[dim];
		}
		*/
		shape[dim] = 1;

		try {
			IArray array = sectionNoReduce(origin, shape, stride).getArray();
			if (array.getRank() > 0) {
				array.setIndex(array.getIndex().reduce(dim));
			}
			return createArrayUtils(array); // preserve other dim 1

		} catch (InvalidRangeException e) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Create a new ArrayUtils using same backing store as this Array, by
	 * eliminating the specified dimension.
	 * 
     * @param dim dimension to eliminate: must be of length one, else
	 *            IllegalArgumentException
     * @return the new IArray
	 */
	@Override
	public IArrayUtils reduce(int dim) {
		int[] shape = getArray().getShape();
		if(! (dim < shape.length && dim >= 0) ) {
			throw new IllegalArgumentException("the dimension is out of index, " +
					"" + dim + " / " + shape.length);
		}
		if (shape[dim] != 1) {
			throw new IllegalArgumentException("the dimension size must be 1: " +
					shape[dim]);
		}
		if (shape.length == 1) {
			return getArray().copy(false).getArrayUtils();
		}
		IArray array = getArray().copy(false);
		IIndex index = array.getIndex().reduce(dim);
		array.setIndex(index);
		return array.getArrayUtils();
	}
	
	/**
	 * Reduce the array to at least certain rank. The dimension with only 1 bin
	 * will be reduced.
	 * 
     * @param rank in int type
     * @return CDMA IArrayUtils whom IArray has the same storage Created on
	 *         10/11/2008
	 */
	@Override
	public IArrayUtils reduceTo(int rank) {
		int oldRank = getArray().getRank();
		if (oldRank <= rank) {
			return this;
		} else {
			int[] shape = getArray().getShape();
			for (int i = 0; i < shape.length; i++) {
				if (shape[i] == 1) {
					reduce(i).reduceTo(rank);
				}
			}
		}
		return this;
	}

	/**
	 * Create a new Array, with the given shape, that references the same
     * backing store as this IArray.
	 * 
     * @param shape the new shape
     * @return the new IArray
	 */
	@Override
	public IArrayUtils reshape(int[] shape) throws ShapeNotMatchException {
		IArray newArray = getArray().copy(false);
		IIndex index = newArray.getIndex();
		index.setShape(shape);
		newArray.setIndex(index);
		return createArrayUtils(newArray);
	}

	/**
     * Create a new IArray as a subsection of this Array, with rank reduction. No
     * data is moved, so the new IArray references the same backing store as the
	 * original.
	 * <p>
	 * 
     * @param origin int array specifying the starting index. Must be same rank as
     *            original IArray.
     * @param shape int array specifying the extents in each dimension. This
     *            becomes the shape of the returned IArray. Must be same rank as
     *            original IArray. If shape[dim] == 1, then the rank of the
     *            resulting IArray is reduced at that dimension.
	 * @return IArray object
	 * @throws InvalidRangeException
	 *             invalid range
	 * @see org.gumtree.data.interfaces.IArray#section(int[], int[])
	 */
	@Override
	public IArrayUtils section(final int[] origin, final int[] shape)
			throws InvalidRangeException {
		/*
		 * try { IArray newArray =
		 * Factory.createArrayNoCopy(getArray().getStorage()); IIndex newIndex =
		 * newArray.getIndex();
		 * 
		 * newIndex.set(origin); newIndex.setShape(shape); newIndex.reduce();
		 * 
		 * return new ArrayUtils(newArray); } catch (Exception e) { throw new
		 * InvalidRangeException(e); }
		 */
		return sectionNoReduce(origin, shape, null).reduce();
	}

	/**
     * Create a new IArray as a subsection of this Array, with rank reduction. No
     * data is moved, so the new IArray references the same backing store as the
	 * original.
	 * <p>
	 * 
     * @param origin int array specifying the starting index. Must be same rank as
     *            original IArray.
     * @param shape int array specifying the extents in each dimension. This
     *            becomes the shape of the returned IArray. Must be same rank as
     *            original IArray. If shape[dim] == 1, then the rank of the
     *            resulting IArray is reduced at that dimension.
     * @param stride int array specifying the strides in each dimension. If null,
	 *            assume all ones.
     * @return the new IArray
	 * @throws InvalidRangeException
	 *             invalid range
	 */
	@Override
	public IArrayUtils section(int[] origin, int[] shape, long[] stride)
			throws InvalidRangeException {
		/*
		 * try { IArray newArray =
		 * Factory.createArrayNoCopy(getArray().getStorage()); IIndex newIndex =
		 * newArray.getIndex();
		 * 
		 * newIndex.set(origin); newIndex.setShape(shape);
		 * newIndex.setStride(stride); newIndex.reduce();
		 * 
		 * return new ArrayUtils(newArray); } catch (Exception e) { throw new
		 * InvalidRangeException(e); }
		 */
		return sectionNoReduce(origin, shape, stride).reduce();
	}

	/**
     * Create a new IArray as a subsection of this Array, without rank reduction.
     * No data is moved, so the new IArray references the same backing store as
	 * the original.
	 * 
     * @param origin int array specifying the starting index. Must be same rank as
     *            original IArray.
     * @param shape int array specifying the extents in each dimension. This
     *            becomes the shape of the returned IArray. Must be same rank as
     *            original IArray.
     * @param stride int array specifying the strides in each dimension. If null,
	 *            assume all ones.
     * @return the new IArray
	 * @throws InvalidRangeException
	 *             invalid range
	 */
	@Override
	public IArrayUtils sectionNoReduce(int[] origin, int[] shape, long[] stride)
			throws InvalidRangeException {
		int i = 0;
		for( int or : origin ) {
			if( getArray().getIndex().getShape()[i++] < or ) {
				throw new InvalidRangeException("Unable to get a section at that position!");
			}
		}
		try {
			IArray newArray = getArray().copy(false);
			IIndex newIndex = newArray.getIndex().clone();
			newIndex.setShape(shape);

			if (stride != null) {
				newIndex.setStride(stride);
			}
			newIndex.setOrigin(origin);
			newArray.setIndex(newIndex);
			return createArrayUtils(newArray);
		} catch (Exception e) {
			throw new InvalidRangeException(e);
		}
	}

	/**
     * Element-wise apply a boolean map to the array. The values of the IArray
	 * will get updated. The map's rank must be smaller or equal to the rank of
	 * the array. If the rank of the map is smaller, apply the map to subset of
	 * the array in the lowest dimensions iteratively. For each element, if the
	 * AND map value is true, return itself, otherwise return NaN.
	 * 
     * @param booleanMap boolean IArray
     * @return IArray itself
	 * @throws ShapeNotMatchException
	 */
	@Override
	public IArrayUtils eltAnd(IArray booleanMap) throws ShapeNotMatchException {
		int[] shape = getArray().getShape();
		int[] mapShape = booleanMap.getShape();
		for (int i = 0; i < mapShape.length; i++) {
			if (mapShape[mapShape.length - 1 - i] != shape[shape.length - 1 - i]) {
				throw new ShapeNotMatchException(
						"the shape of the map does not match with the array");
			}
		}
		IArray resultArray = Factory.createArray(Double.TYPE, shape);
		if (shape.length > mapShape.length) {
			try {
				ISliceIterator sliceIterator = getArray().getSliceIterator(
						mapShape.length);
				ISliceIterator resultSliceIterator = resultArray
						.getSliceIterator(mapShape.length);
				while (sliceIterator.hasNext() && resultSliceIterator.hasNext()) {
					IArray slice = sliceIterator.getArrayNext();
					IArray resultSlice = resultSliceIterator.getArrayNext();
					IArrayIterator arrayIterator = slice.getIterator();
					IArrayIterator resultIterator = resultSlice.getIterator();
					IArrayIterator mapIterator = booleanMap.getIterator();
					while (arrayIterator.hasNext() && resultIterator.hasNext()
							&& mapIterator.hasNext()) {
						if (mapIterator.getBooleanNext()) {
							resultIterator.next().setDoubleCurrent(
									arrayIterator.getDoubleNext());
						} else {
							resultIterator.next().setDoubleCurrent(Double.NaN);
							arrayIterator.next();
						}
					}
				}
			} catch (Exception e) {
				throw new ShapeNotMatchException(e);
			}
		} else {
			IArrayIterator arrayIterator = getArray().getIterator();
			IArrayIterator resultIterator = resultArray.getIterator();
			IArrayIterator mapIterator = booleanMap.getIterator();
			while (arrayIterator.hasNext() && resultIterator.hasNext()
					&& mapIterator.hasNext()) {
				if (mapIterator.getBooleanNext()) {
					resultIterator.next().setDoubleCurrent(
							arrayIterator.getDoubleNext());
				} else {
					resultIterator.next().setDoubleCurrent(Double.NaN);
					arrayIterator.next();
				}
			}
		}

		return resultArray.getArrayUtils();
	}

	/**
	 * Integrate on given dimension. The result array will be one dimensional
	 * reduced from the given array.
	 * 
     * @param dimension integer value
     * @param isVariance true if the array serves as variance
     * @return new IArray object
	 * @throws ShapeNotMatchException
	 */
	@Override
    public IArrayUtils enclosedIntegrateDimension(final int dimension,
            final boolean isVariance) throws ShapeNotMatchException {
        if (dimension >= getArray().getRank()) {
            throw new ShapeNotMatchException(dimension
                    + " dimension is not available");
        }
        int[] shape = getArray().getShape();
        int[] newShape = new int[shape.length - 1];
        int[] origin = new int[shape.length];
        int[] section = new int[shape.length];
        for (int i = 0; i < newShape.length; i++) {
            if (i < dimension) {
                newShape[i] = shape[i];
                section[i] = 1;
            } else {
                newShape[i] = shape[i + 1];
                section[i + 1] = 1;
            }
        }
        section[dimension] = shape[dimension];
        IArray newArray = Factory.createArray(Double.TYPE, newShape);
        IArrayIterator newIterator = newArray.getIterator();
        while (newIterator.hasNext()) {
            newIterator.next();
            int[] counter = newIterator.getCounter();
            for (int j = 0; j < newShape.length; j++) {
                if (j < dimension) {
                    origin[j] = counter[j];
                } else {
                    origin[j + 1] = counter[j];
                }
            }
            try {
                IArray vector = section(origin, section).getArray();
                newIterator.setDoubleCurrent(vector.getArrayMath().sum());
            } catch (InvalidRangeException e) {
                throw new ShapeNotMatchException(e);
            }
        }
        return newArray.getArrayUtils();
    }

	/**
	 * Integrate on given dimension. The result array will be one dimensional
	 * reduced from the given array.
	 * 
     * @param dimension integer value
     * @param isVariance true if the array serves as variance
     * @return new IArray object
	 * @throws ShapeNotMatchException
	 */
	@Override
    public IArrayUtils integrateDimension(int dimension,boolean isVariance) throws ShapeNotMatchException {
        if (dimension >= getArray().getRank()) {
            throw new ShapeNotMatchException(dimension
                    + " dimension is not available");
        }
        int[] shape = getArray().getShape();
        int[] newShape = new int[shape.length - 1];
        int[] origin = new int[shape.length];
        int[] section = new int[shape.length];
        for (int i = 0; i < newShape.length; i++) {
            if (i < dimension) {
                newShape[i] = shape[i];
                section[i] = 1;
            } else {
                newShape[i] = shape[i + 1];
                section[i + 1] = 1;
            }
        }
        section[dimension] = shape[dimension];
        IArray newArray = Factory.createArray(Double.TYPE, newShape);
        IArrayIterator newIterator = newArray.getIterator();
        while (newIterator.hasNext()) {
            newIterator.next();
            int[] counter = newIterator.getCounter();
            for (int j = 0; j < newShape.length; j++) {
                if (j < dimension) {
                    origin[j] = counter[j];
                } else {
                    origin[j + 1] = counter[j];
                }
            }
            try {
                IArray vector = section(origin, section).getArray();
                newIterator.next();
                if (!isVariance) {
                    newIterator.setDoubleCurrent(vector.getArrayMath().sumNormalise());
                } else {
                    newIterator.setDoubleCurrent(vector.getArrayMath().varianceSumNormalise());
                }
            } catch (InvalidRangeException e) {
                throw new ShapeNotMatchException(e);
            }
        }
        return newArray.getArrayUtils();
    }

	public abstract IArrayUtils createArrayUtils(IArray array);
	
}
