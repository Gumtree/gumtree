package org.gumtree.data.soleil.utils;

import java.util.List;

import org.gumtree.data.engine.nexus.array.NexusIndex;
import org.gumtree.data.engine.nexus.utils.NexusArrayUtils;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IRange;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.soleil.array.NxsArray;
import org.gumtree.data.soleil.array.NxsIndex;
import org.gumtree.data.utils.IArrayUtils;

public final class NxsArrayUtils implements IArrayUtils {
    private IArrayUtils mUtils;


    public NxsArrayUtils( NxsArray array) {
        mUtils = new NexusArrayUtils(array);
    }

    @Override
    public Object copyTo1DJavaArray() {
        // Instantiate a new convenient array for storage
        int length   = ((Long) getArray().getSize()).intValue();
        Class<?> type = getArray().getElementType();
        Object array = java.lang.reflect.Array.newInstance(type, length);

        // If the storing array is a stack of DataItem
        Long size = ((NxsIndex) getArray().getIndex()).getIndexMatrix().getSize();
        Long nbMatrixCells  = size == 0 ? 1 : size;
        Long nbStorageCells = ((NxsIndex) getArray().getIndex()).getIndexStorage().getSize();

        Object fullArray = getArray().getStorage();
        Object partArray = null;
        for( int i = 0; i < nbMatrixCells; i++ ) {
            partArray = java.lang.reflect.Array.get(fullArray, i);
            System.arraycopy(partArray, 0, array, i * nbStorageCells.intValue(), nbStorageCells.intValue());
        }

        return array;
    }


    @Override
    public Object copyToNDJavaArray() {
        return copyMatrixItemsToMultiDim();
    }

    // --------------------------------------------------
    // tools methods
    // --------------------------------------------------
    public static Object copyJavaArray(Object array) {
        Object result = array;
        if( result == null ) {
            return null;
        }

        // Determine rank of array (by parsing data array class name)
        String sClassName = array.getClass().getName();
        int iRank  = 0;
        int iIndex = 0;
        char cChar;
        while (iIndex < sClassName.length()) {
            cChar = sClassName.charAt(iIndex);
            iIndex++;
            if (cChar == '[') {
                iRank++;
            }
        }

        // Set dimension rank
        int[] shape    = new int[iRank];

        // Fill dimension size array
        for ( int i = 0; i < iRank; i++) {
            shape[i] = java.lang.reflect.Array.getLength(result);
            result = java.lang.reflect.Array.get(result,0);
        }

        // Define a convenient array (shape and type)
        result = java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), shape);

        return copyJavaArray(array, result);
    }

    public static Object copyJavaArray(Object source, Object target) {
        Object item = java.lang.reflect.Array.get(source, 0);
        int length = java.lang.reflect.Array.getLength(source);

        if( item.getClass().isArray() ) {
            Object tmpSrc;
            Object tmpTar;
            for( int i = 0; i < length; i++ ) {
                tmpSrc = java.lang.reflect.Array.get(source, i);
                tmpTar = java.lang.reflect.Array.get(target, i);
                copyJavaArray( tmpSrc, tmpTar);
            }
        }
        else {
            System.arraycopy(source, 0, target, 0, length);
        }

        return target;
    }


    @Override
    public IArray getArray() {
        return mUtils.getArray();
    }

    @Override
    public void copyTo(IArray newArray) throws ShapeNotMatchException {
        mUtils.copyTo(newArray);
    }

    @Override
    public Object get1DJavaArray(Class<?> wantType) {
        return mUtils.get1DJavaArray(wantType);
    }

    @Override
    public void checkShape(IArray newArray) throws ShapeNotMatchException {
        mUtils.checkShape(newArray);
    }

    @Override
    public IArrayUtils concatenate(IArray array) throws ShapeNotMatchException {
        return mUtils.concatenate(array);
    }

    @Override
    public IArrayUtils reduce() {
        return mUtils.reduce();
    }

    @Override
    public IArrayUtils reduce(int dim) {
        return mUtils.reduce(dim);
    }

    @Override
    public IArrayUtils reduceTo(int rank) {
        return mUtils.reduceTo(rank);
    }

    @Override
    public IArrayUtils reshape(int[] shape) throws ShapeNotMatchException {
        return mUtils.reshape(shape);
    }

    @Override
    public IArrayUtils section(int[] origin, int[] shape)
            throws InvalidRangeException {
        return mUtils.section(origin, shape);
    }

    @Override
    public IArrayUtils section(int[] origin, int[] shape, long[] stride)
            throws InvalidRangeException {
        return mUtils.section(origin, shape, stride);
    }

    @Override
    public IArrayUtils sectionNoReduce(int[] origin, int[] shape, long[] stride)
            throws InvalidRangeException {
        return mUtils.sectionNoReduce(origin, shape, stride);
    }

    @Override
    public IArrayUtils sectionNoReduce(List<IRange> ranges)
            throws InvalidRangeException {
        return mUtils.sectionNoReduce(ranges);
    }

    @Override
    public IArrayUtils slice(int dim, int value) {
        return mUtils.slice(dim, value);
    }

    @Override
    public IArrayUtils transpose(int dim1, int dim2) {
        return mUtils.transpose(dim1, dim2);
    }

    @Override
    public boolean isConformable(IArray array) {
        return mUtils.isConformable(array);
    }

    @Override
    public IArrayUtils eltAnd(IArray booleanMap) throws ShapeNotMatchException {
        return mUtils.eltAnd(booleanMap);
    }

    @Override
    public IArrayUtils integrateDimension(int dimension, boolean isVariance)
            throws ShapeNotMatchException {
        return mUtils.integrateDimension(dimension, isVariance);
    }

    @Override
    public IArrayUtils enclosedIntegrateDimension(int dimension,
            boolean isVariance) throws ShapeNotMatchException {
        return mUtils.enclosedIntegrateDimension(dimension, isVariance);
    }

    @Override
    public IArrayUtils flip(int dim) {
        return mUtils.flip(dim);
    }

    @Override
    public IArrayUtils permute(int[] dims) {
        return mUtils.permute(dims);
    }


    // --------------------------------------------------
    // private methods
    // --------------------------------------------------
    /**
     * Copy the backing storage of this NxsArray into multidimensional 
     * corresponding Java primitive array
     */
    private Object copyMatrixItemsToMultiDim() {
        NxsArray array = (NxsArray) getArray();
        int[] shape  = array.getShape();
        int[] current;
        int   length;
        int   startCell;
        Object result = java.lang.reflect.Array.newInstance(array.getElementType(), shape);
        Object slab;
        Object dataset;

        ISliceIterator iter;
        try {
            iter = array.getSliceIterator(1);
            NxsIndex startIdx = (NxsIndex) array.getIndex();
            NexusIndex storage = startIdx.getIndexStorage();
            NexusIndex items   = startIdx.getIndexMatrix();
            startIdx.setOrigin(new int[startIdx.getRank()]);
            while( iter.hasNext() ) {
                length = ((Long) iter.getArrayNext().getSize()).intValue();
                slab = result;

                // Getting the right slab in the multidim result array
                current = iter.getSlicePosition();
                startIdx.set(current);
                for( int pos = 0;  pos < current.length - 1; pos++ ) {
                    slab = java.lang.reflect.Array.get(slab, current[pos]);
                }
                dataset = java.lang.reflect.Array.get(array.getStorage(), items.currentProjectionElement());

                startCell = storage.currentProjectionElement();
                System.arraycopy(dataset, startCell, slab, 0, length);
            }
        } catch (ShapeNotMatchException e) {
        } catch (InvalidRangeException e) {
        }
        return result;
    }
}
