package org.gumtree.data.engine.nexus.array;

import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.ISliceIterator;

public final class NexusSliceIterator implements ISliceIterator {

    /// Members
    private NexusArrayIterator  mIterator;      // iterator of the whole_array
    private IArray              mArray;         // array of the original shape containing all slices
    private int[]               mDimension;     // shape of the slice

    /// Constructor
    /**
     * Create a new NxsSliceIterator that permits to iterate over
     * slices that are the last dim dimensions of this array.
     * 
     * @param array source of the slice
     * @param dim returned dimensions
     */
    public NexusSliceIterator(final IArray array, final int dim) throws InvalidRangeException {
	// If ranks are equal, make sure at least one iteration is performed.
	// We cannot use 'reshape' (which would be ideal) as that creates a
	// new copy of the array storage and so is unusable
	mArray = array;
	int[] shape     = mArray.getShape();
	int[] rangeList = shape.clone();
	int[] origin    = mArray.getIndex().getOrigin().clone();
	long[] stride   = mArray.getIndex().getStride().clone();
	int   rank      = mArray.getRank();

	// shape to make a 2D array from multiple-dim array
	mDimension = new int[dim];
	System.arraycopy(shape, shape.length - dim, mDimension, 0, dim);
	for( int i = 0; i < dim; i++ ) {
	    rangeList[rank - i - 1] = 1;
	}
	// Create an iterator over the higher dimensions. We leave in the
	// final dimensions so that we can use the getCurrentCounter method
	// to create an origin.

	IIndex index = mArray.getIndex();

	// As we get a reference on array's IIndex we directly modify it
	index.setOrigin(origin);
	index.setStride(stride);
	index.setShape(rangeList);

	mIterator = new NexusArrayIterator(mArray, index, false);

    }

    /// Public methods
    @Override
    public IArray getArrayNext() throws InvalidRangeException {
	next();
	return createSlice();
    }

    @Override
    public int[] getSliceShape() throws InvalidRangeException {
	return mDimension.clone();
    }

    public int[] getSlicePosition() {
	return mIterator.getCounter();
    }

    @Override
    public boolean hasNext() {
	return mIterator.hasNext();
    }

    @Override
    public void next() {
	mIterator.incrementIndex();
    }

    @Override
    public String getFactoryName() {
	return mArray.getFactoryName();
    }

    private IArray createSlice() throws InvalidRangeException {
	int i = 0;
	int[] iShape  = mArray.getShape();
	int[] iOrigin = mArray.getIndex().getOrigin();
	int[] iCurPos = mIterator.getCounter();
	for( int pos : iCurPos ) {
	    if( pos >= iShape[i] ) {
		return null;
	    }
	    iOrigin[i] += pos;
	    i++;
	}

	java.util.Arrays.fill(iShape, 1);
	System.arraycopy(mDimension, 0, iShape, iShape.length - mDimension.length, mDimension.length); 

	IArray slice = mArray.copy(false);

	IIndex index = slice.getIndex();
	index.setShape(iShape);
	index.setOrigin(iOrigin);
	index.reduce();
	slice.setIndex(index);
	return slice;
    }

	@Override
	public IArray getArrayCurrent() throws InvalidRangeException {
		return createSlice();
	}

}
