package org.gumtree.data.soleil.array;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.engine.nexus.array.NexusIndex;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.interfaces.IRange;
import org.gumtree.data.soleil.NxsFactory;

public final class NxsIndex implements IIndex, Cloneable {
    private NexusIndex mIndexArrayData;
    private NexusIndex mIndexStorage;


    /// Constructors
    /**
     * Construct a matrix of index. The matrix is defined by shape, start and length
     * that also carries underlying data storage dimensions. The matrixRank defines
     * which values of those arrays concern the matrix of index and which concern
     * the index.
     * The first matrixRank values concern the matrix of index.
     */
    public NxsIndex(int matrixRank, int[] shape, int[] start, int[] length) {
        mIndexArrayData = new NexusIndex(
                NxsFactory.NAME, 
                java.util.Arrays.copyOfRange(shape, 0, matrixRank),
                java.util.Arrays.copyOfRange(start, 0, matrixRank),
                java.util.Arrays.copyOfRange(length, 0, matrixRank)
                );

        mIndexStorage = new NexusIndex(
                NxsFactory.NAME, 
                java.util.Arrays.copyOfRange(shape, matrixRank, shape.length),
                java.util.Arrays.copyOfRange(start, matrixRank, start.length),
                java.util.Arrays.copyOfRange(length, matrixRank, length.length)
                );
    }

    public NxsIndex(int[] shape, int[] start, int[] length) {
        this(0, shape, start, length);
    }

    public NxsIndex(NxsIndex index) {
        mIndexArrayData = (NexusIndex) index.mIndexArrayData.clone();
        mIndexStorage = (NexusIndex) index.mIndexStorage.clone();
    }

    public NxsIndex(int[] storage) {
        mIndexArrayData = new NexusIndex( NxsFactory.NAME, new int[] {} );
        mIndexStorage   = new NexusIndex( NxsFactory.NAME, storage.clone() );
    }

    public NxsIndex(int[] matrix, int[] storage) {
        mIndexArrayData = new NexusIndex( NxsFactory.NAME, matrix.clone() );
        mIndexStorage   = new NexusIndex( NxsFactory.NAME, storage.clone() );
    }

    public NxsIndex(int matrixRank, IIndex index) {
        this(matrixRank, index.getShape(), index.getOrigin(), index.getShape());
        this.setStride(index.getStride());
        this.set(index.getCurrentCounter());
    }

    public long currentElementMatrix() {
        return mIndexArrayData.currentElement();
    }

    public long currentElementStorage() {
        return mIndexStorage.currentElement();
    }

    public int[] getCurrentCounterMatrix() {
        return mIndexArrayData.getCurrentCounter();
    }

    public int[] getCurrentCounterStorage() {
        return mIndexStorage.getCurrentCounter();
    }

    @Override
    public void setOrigin(int[] origin) {
        if( origin.length != getRank() ) {
            throw new IllegalArgumentException();
        }
        mIndexArrayData.setOrigin(
                java.util.Arrays.copyOfRange(origin, 0, mIndexArrayData.getRank())
                );

        mIndexStorage.setOrigin(
                java.util.Arrays.copyOfRange(origin, mIndexArrayData.getRank(), origin.length)
                );
    }


    @Override
    public void setShape(int[] shape) {
        if( shape.length != getRank() ) {
            throw new IllegalArgumentException();
        }
        mIndexArrayData.setShape(
                java.util.Arrays.copyOfRange(shape, 0, mIndexArrayData.getRank())
                );

        mIndexStorage.setShape(
                java.util.Arrays.copyOfRange(shape, mIndexArrayData.getRank(), shape.length)
                );
    }


    @Override
    public void setStride(long[] stride) {
        if( stride.length != getRank() ) {
            throw new IllegalArgumentException();
        }
        int iRank = mIndexArrayData.getRank();

        // Set the stride for the storage arrays
        mIndexStorage.setStride(
                java.util.Arrays.copyOfRange(stride, iRank, stride.length)
                );

        // Get the number of cells in storage arrays
        long[] iStride = mIndexStorage.getStride();
        long current = iStride[ 0 ] * mIndexStorage.getShape()[0];


        // Divide the stride by number of cells contained in storage arrays
        iStride = new long[iRank];
        for( int i = iRank; i > 0; i-- ) {
            iStride[i - 1] = stride[i - 1] / current;
            if( iStride[i - 1] == 0 ) {
                iStride[i - 1] = 1;
            }
        }

        mIndexArrayData.setStride(iStride);
    }

    @Override
    public IIndex set(int[] index) {
        if( index.length != getRank() ) {
            throw new IllegalArgumentException();
        }
        mIndexArrayData.set(
                java.util.Arrays.copyOfRange(index, 0, mIndexArrayData.getRank())
                );

        mIndexStorage.set(
                java.util.Arrays.copyOfRange(index, mIndexArrayData.getRank(), index.length)
                );

        return this;
    }

    @Override
    public void setDim(int dim, int value) {
        int[] curPos = this.getCurrentCounter();
        curPos[dim] = value;
        this.set(curPos);
    }


    @Override
    public IIndex set0(int v) {
        setDim(DIM0, v);
        return this;
    }


    @Override
    public IIndex set1(int v) {
        setDim(DIM1, v);
        return this;
    }


    @Override
    public IIndex set2(int v) {
        setDim(DIM2, v);
        return this;
    }


    @Override
    public IIndex set3(int v) {
        setDim(DIM3, v);
        return this;
    }


    @Override
    public IIndex set4(int v) {
        setDim(DIM4, v);
        return this;
    }

    @Override
    public IIndex set5(int v) {
        setDim(DIM5, v);
        return this;
    }

    @Override
    public IIndex set6(int v) {
        setDim(DIM6, v);
        return this;
    }

    @Override
    public IIndex set(int v0) {
        int[] curPos = this.getCurrentCounter();
        curPos[DIM0] = v0;
        this.set(curPos);
        return this;
    }

    @Override
    public IIndex set(int v0, int v1) {
        int[] curPos = this.getCurrentCounter();
        curPos[DIM0] = v0;
        curPos[DIM1] = v1;
        this.set(curPos);
        return this;
    }

    @Override
    public IIndex set(int v0, int v1, int v2) {
        int[] curPos = this.getCurrentCounter();
        curPos[DIM0] = v0;
        curPos[DIM1] = v1;
        curPos[DIM2] = v2;
        this.set(curPos);
        return this;
    }

    @Override
    public IIndex set(int v0, int v1, int v2, int v3) {
        int[] curPos = this.getCurrentCounter();
        curPos[DIM0] = v0;
        curPos[DIM1] = v1;
        curPos[DIM2] = v2;
        curPos[DIM3] = v3;
        this.set(curPos);
        return this;
    }

    @Override
    public IIndex set(int v0, int v1, int v2, int v3, int v4) {
        int[] curPos = this.getCurrentCounter();
        curPos[DIM0] = v0;
        curPos[DIM1] = v1;
        curPos[DIM2] = v2;
        curPos[DIM3] = v3;
        curPos[DIM4] = v4;
        this.set(curPos);
        return this;
    }

    @Override
    public IIndex set(int v0, int v1, int v2, int v3, int v4, int v5) {
        int[] curPos = this.getCurrentCounter();
        curPos[DIM0] = v0;
        curPos[DIM1] = v1;
        curPos[DIM2] = v2;
        curPos[DIM3] = v3;
        curPos[DIM4] = v4;
        curPos[DIM5] = v5;
        this.set(curPos);
        return this;
    }

    @Override
    public IIndex set(int v0, int v1, int v2, int v3, int v4, int v5, int v6) {
        int[] curPos = this.getCurrentCounter();
        curPos[DIM0] = v0;
        curPos[DIM1] = v1;
        curPos[DIM2] = v2;
        curPos[DIM3] = v3;
        curPos[DIM4] = v4;
        curPos[DIM5] = v5;
        curPos[DIM6] = v6;
        this.set(curPos);
        return this;
    }

    @Override
    public void setIndexName(int dim, String indexName) {
        if( dim >= mIndexArrayData.getRank() ) {
            mIndexStorage.setIndexName(dim, indexName);
        }
        else {
            mIndexArrayData.setIndexName(dim, indexName);
        }
    }

    @Override
    public IIndex reduce() {
        mIndexArrayData.reduce();
        mIndexStorage.reduce();
        return this;
    }

    @Override
    public IIndex reduce(int dim) {
        if( dim < mIndexArrayData.getRank() ) {
            mIndexArrayData.reduce(dim);
        }
        else {
            mIndexStorage.reduce(dim - mIndexArrayData.getRank());
        }
        return this;
    }

    @Override
    public String toString() {
        return mIndexArrayData.toString() + "\n" + mIndexStorage;
    }

    @Override
    public IIndex clone() {
        return new NxsIndex(this);
    }

    @Override
    public String getFactoryName() {
        return NxsFactory.NAME;
    }

    @Override
    public int getRank() {
        return mIndexArrayData.getRank() + mIndexStorage.getRank();
    }

    @Override
    public int[] getShape() {
        return concat(mIndexArrayData.getShape(), mIndexStorage.getShape());
    }

    @Override
    public int[] getOrigin() {
        return concat(mIndexArrayData.getOrigin(), mIndexStorage.getOrigin());
    }

    @Override
    public long getSize() {
        long sizeArrayData = mIndexArrayData.getSize();
        return ( sizeArrayData == 0 ? 1 : sizeArrayData ) * mIndexStorage.getSize();
    }

    @Override
    public long[] getStride() {
        return concat(mIndexArrayData.getStride(), mIndexStorage.getStride());
    }

    @Override
    public long currentElement() {
        return mIndexArrayData.currentElement() * mIndexStorage.getSize() + mIndexStorage.currentElement();
    }

    @Override
    public long lastElement() {
        return mIndexArrayData.lastElement() * mIndexStorage.getSize() + mIndexStorage.lastElement();
    }

    @Override
    public String toStringDebug() {
        return (mIndexArrayData.toStringDebug() + mIndexStorage.toStringDebug());
    }

    @Override
    public int[] getCurrentCounter() {
        return concat(mIndexArrayData.getCurrentCounter(), mIndexStorage.getCurrentCounter());
    }

    @Override
    public String getIndexName(int dim) {
        String name;
        if( dim >= mIndexArrayData.getRank() ) {
            name = mIndexStorage.getIndexName(dim);
        }
        else {
            name = mIndexArrayData.getIndexName(dim);
        }
        return name;
    }

    public List<IRange> getRangeList() {
        ArrayList<IRange> list = new ArrayList<IRange>();
        list.addAll(mIndexArrayData.getRangeList());
        list.addAll(mIndexStorage.getRangeList());
        return list;
    }

    // ---------------------------------------------------------
    /// Public tool methods
    // ---------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[] array1, T[] array2) {
        T[] result = (T[]) java.lang.reflect.Array.newInstance( array1.getClass() , array1.length + array2.length);
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    public static int[] concat(int[] array1, int[] array2) {
        int[] result = new int[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    public static long[] concat(long[] array1, long[] array2) {
        long[] result = new long[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    // ---------------------------------------------------------
    /// Protected methods
    // ---------------------------------------------------------
    public NexusIndex getIndexMatrix() {
        return mIndexArrayData;
    }

    public NexusIndex getIndexStorage() {
        return mIndexStorage;
    }

    private static final int DIM0 = 0;
    private static final int DIM1 = 1;
    private static final int DIM2 = 2;
    private static final int DIM3 = 3;
    private static final int DIM4 = 4;
    private static final int DIM5 = 5;
    private static final int DIM6 = 6;

}
