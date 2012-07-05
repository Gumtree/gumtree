package org.gumtree.data.engine.nexus.utils;

import java.util.List;

import org.gumtree.data.engine.nexus.array.NexusArray;
import org.gumtree.data.engine.nexus.array.NexusIndex;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.IRange;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.utils.ArrayUtils;
import org.gumtree.data.utils.IArrayUtils;

public final class NexusArrayUtils extends ArrayUtils {
    public NexusArrayUtils( IArray array) {
	super(array);
    }

    @Override
    public Object copyTo1DJavaArray() {
	// Instantiate a new convenient array for storage
	int length   = ((Long) getArray().getSize()).intValue();
	Class<?> type = getArray().getElementType();
	Object array = java.lang.reflect.Array.newInstance(type, length);

	int start = 0;

	start = ((NexusIndex) getArray().getIndex()).currentProjectionElement();
	System.arraycopy(getArray().getStorage(), start, array, 0, length);

	return array;
    }

    @Override
    public Object copyToNDJavaArray() {
	IArray array = getArray();
	int[] shape  = array.getShape();
	int[] current;
	int   length;
	int   startCell;
	Object result = java.lang.reflect.Array.newInstance(array.getElementType(), shape);
	Object slab;

	ISliceIterator iter;
	try {
	    iter = array.getSliceIterator(1);
	    length = ((Long) iter.getArrayNext().getSize()).intValue();
	    NexusIndex startIdx = (NexusIndex) array.getIndex().clone();
	    startIdx.setOrigin(new int[startIdx.getRank()]);
	    while( iter.hasNext() ) {
		slab = result;

		// Getting the right slab in the multidim result array
		current = iter.getSlicePosition();
		startIdx.set(current);
		for( int pos = 0;  pos < current.length - 1; pos++ ) {
		    slab = java.lang.reflect.Array.get(slab, current[pos]);
		}
		startCell = startIdx.currentProjectionElement();

		System.arraycopy(array.getStorage(), startCell, slab, 0, length);

		iter.next();
	    }
	} catch (ShapeNotMatchException e) {
	    result = null;
	} catch (InvalidRangeException e) {
	    result = null;
	} catch (CloneNotSupportedException e) {
	    result = null;
	}
	return result;
    }

    @Override
    public IArrayUtils flip(int dim) {
	IArray array   = getArray().copy(false);
	IIndex index   = array.getIndex();
	int   rank     = array.getRank();       
	int[] shape    = index.getShape();
	int[] origin   = index.getOrigin();
	int[] position = index.getCurrentCounter();
	long[] stride  = index.getStride();

	int[] newShape    = new int[rank];
	int[] newOrigin   = new int[rank];
	int[] newPosition = new int[rank];
	long[] newStride  = new long[rank];

	for( int i = 0; i < rank; i++ ) {
	    newShape[i]    = shape[rank - 1 - i];
	    newOrigin[i]   = origin[rank - 1 - i];
	    newStride[i]   = stride[rank - 1 - i];
	    newPosition[i] = position[rank - 1 - i];
	}

	index = new NexusIndex( array.getFactoryName(), newShape, newOrigin, newShape );
	index.setStride(newStride);
	index.set(newPosition);
	array.setIndex(index);
	return array.getArrayUtils();
    }

    @Override
    public IArrayUtils permute(int[] dims) {
	IArray array      = getArray().copy(false);
	IIndex index      = array.getIndex();
	int   rank        = array.getRank();       
	int[] shape       = index.getShape();
	int[] origin      = index.getOrigin();
	int[] position    = index.getCurrentCounter();
	long[] stride     = index.getStride();
	int[] newShape    = new int[rank];
	int[] newOrigin   = new int[rank];
	int[] newPosition = new int[rank];
	long[] newStride  = new long[rank];
	for( int i = 0; i < rank; i++ ) {
	    newShape[i]    = shape[ dims[i] ];
	    newOrigin[i]   = origin[ dims[i] ];
	    newStride[i]   = stride[ dims[i] ];
	    newPosition[i] = position[ dims[i] ];
	}

	index = new NexusIndex( array.getFactoryName(), newShape, newOrigin, newShape );
	index.setStride(newStride);
	index.set(newPosition);
	array.setIndex(index);
	return array.getArrayUtils();
    }

    @Override
    public IArrayUtils transpose(int dim1, int dim2) {
	IArray array   = getArray().copy(false);
	IIndex index   = array.getIndex();
	int[] shape    = index.getShape();
	int[] origin   = index.getOrigin();
	int[] position = index.getCurrentCounter();
	long[] stride  = index.getStride();

	int sha = shape[dim1];
	int ori = origin[dim1];
	int pos = position[dim1];
	long str = stride[dim1];

	shape[dim2]    = shape[dim1];
	origin[dim2]   = origin[dim1];
	stride[dim2]   = stride[dim1];
	position[dim2] = position[dim1];

	shape[dim2]    = sha;
	origin[dim2]   = ori;
	stride[dim2]   = str;
	position[dim2] = pos;

	index = new NexusIndex( array.getFactoryName(), shape, origin, shape );
	index.setStride(stride);
	index.set(position);
	array.setIndex(index);
	return array.getArrayUtils();
    }

    @Override
    public IArrayUtils sectionNoReduce(List<IRange> ranges)
	    throws InvalidRangeException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public IArrayUtils createArrayUtils(IArray array) {
	IArrayUtils result;
	if( array instanceof NexusArray ) {
	    result = new NexusArrayUtils( (NexusArray) array);
	}
	else {
	    result = array.getArrayUtils();
	}
	return result;
    }

    @Override
    public Object get1DJavaArray(Class<?> wantType) {
	// TODO Auto-generated method stub
	return null;
    }


    // --------------------------------------------------
    // private methods
    // --------------------------------------------------
    @Override
    public boolean isConformable(IArray array) {
	boolean result = false;
	if( array.getRank() == getArray().getRank() ) {
	    IArray copy1 = this.reduce().getArray();
	    IArray copy2 = array.getArrayUtils().reduce().getArray();

	    int[] shape1 = copy1.getShape();
	    int[] shape2 = copy2.getShape();

	    for( int i = 0; i < shape1.length; i++ ) {
		if( shape1[i] != shape2[i] ) {
		    result = false;
		    break;
		}
	    }

	}
	return result;
    }
}
