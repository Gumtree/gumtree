package org.gumtree.data.impl.utils;

import java.util.List;

import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.impl.netcdf.NcArray;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IRange;
import org.gumtree.data.utils.ArrayUtils;
import org.gumtree.data.utils.IArrayUtils;

import ucar.ma2.MAMath;

public class NcArrayUtils extends ArrayUtils {

    public NcArrayUtils(NcArray array) {
        super(array);
    }

    protected NcArray getNcArray() {
    	return (NcArray) getArray();
    }

    /**
     * @param array1
     *            another Array object
     * @return true or false
     * @see org.gumtree.data.interfaces.IArray#isConformable(org.gumtree.data.interfaces.IArray)
     */
    @Override
    public boolean isConformable(final IArray array1) {
        return MAMath.conformable(getNcArray().getArray(), ((NcArray) array1).getArray());
    }

    /**
     * @return IArray object
     * @see org.gumtree.data.interfaces.IArray#reduce()
     */
    @Override
    public IArrayUtils reduce() {
        return new NcArrayUtils(new NcArray(getNcArray().getArray().reduce()));
    }

    /**
     * @param dim
     *            integer value
     * @return IArray object
     * @see org.gumtree.data.interfaces.IArray#reduce(int)
     */
    @Override
    public IArrayUtils reduce(final int dim) {
        return new NcArrayUtils(new NcArray(getNcArray().getArray().reduce(dim)));
    }


    /**
     * @param rank
     *            integer value
     * @return new Array object
     * @see org.gumtree.data.interfaces.IArray#reduceTo(int)
     */
    @Override
    public IArrayUtils reduceTo(final int rank) {
        NcArray result = getNcArray();
        int oldRank = getArray().getRank();
        if (oldRank <= rank) {
            return this;
        } else {
            int[] shape = getArray().getShape();
            for (int i = 0; i < shape.length; i++) {
                if (shape[i] == 1) {
                    NcArray reduced = (NcArray) reduce(i);
                    result = (NcArray) reduced.getArrayUtils().reduceTo(rank).getArray();
                }
            }
        }
        return new NcArrayUtils(result);
    }

    /**
     * @param shape
     *            array of integers
     * @return IArray object
     * @see org.gumtree.data.interfaces.IArray#reshape(int[])
     */
    @Override
    public IArrayUtils reshape(final int[] shape) {
        return new NcArrayUtils(new NcArray(getNcArray().getArray().reshape(shape)));
    }
    
    /**
     * @param origin
     *            array of integers
     * @param shape
     *            array of integers
     * @return IArray object
     * @throws InvalidRangeException
     *             invalid range
     * @see org.gumtree.data.interfaces.IArray#section(int[], int[])
     */
    @Override
    public IArrayUtils section(final int[] origin, final int[] shape)
            throws InvalidRangeException {
        try {
            return new NcArrayUtils(new NcArray(getNcArray().getArray().section(origin, shape)));
        } catch (ucar.ma2.InvalidRangeException e) {
            throw new InvalidRangeException(e);
        }
    }

    /**
     * @param origin
     *            array of integers
     * @param shape
     *            array of integers
     * @param stride
     *            array of integers
     * @return IArray object
     * @throws InvalidRangeException
     *             invalid range
     * @see org.gumtree.data.interfaces.IArray#section(int[], int[], int[])
     */
    @Override
    public IArrayUtils section(final int[] origin, final int[] shape,
            final long[] stride) throws InvalidRangeException {
        try {
        	int[] intStride = null;
        	if (stride != null) {
        		intStride = new int[stride.length];
            	for (int i = 0; i < stride.length; i++) {
            		intStride[i] = (int) stride[i];
            	}
        	}
            return new NcArrayUtils(new NcArray(getNcArray().getArray().section(origin, shape, intStride)));
        } catch (ucar.ma2.InvalidRangeException e) {
            throw new InvalidRangeException(e);
        }
    }

    /**
     * @param origin
     *            array of integers
     * @param shape
     *            array of integers
     * @param stride
     *            array of integers
     * @return IArray object
     * @throws InvalidRangeException
     *             invalid range
     * @see org.gumtree.data.interfaces.IArray#sectionNoReduce(int[], int[], int[])
     */
    @Override
    public IArrayUtils sectionNoReduce(final int[] origin, final int[] shape,
            final long[] stride) throws InvalidRangeException {
        try {
        	int[] intStride = null;
        	if (stride != null) {
        		intStride = new int[stride.length];
            	for (int i = 0; i < stride.length; i++) {
            		intStride[i] = (int) stride[i];
            	}
        	}
            return (new NcArray(getNcArray().getArray()
                    .sectionNoReduce(origin, shape, intStride))).getArrayUtils();
        } catch (ucar.ma2.InvalidRangeException e) {
        	e.printStackTrace();
            throw new InvalidRangeException(e);
        }
    }

    @Override
    public IArrayUtils sectionNoReduce(List<IRange> ranges) throws InvalidRangeException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    /**
     * @param dim1
     *            integer value
     * @param dim2
     *            integer value
     * @return IArray object
     * @see org.gumtree.data.interfaces.IArray#transpose(int, int)
     */
    @Override
    public IArrayUtils transpose(final int dim1, final int dim2) {
        return new NcArrayUtils(new NcArray(getNcArray().getArray().transpose(dim1, dim2)));
    }
    
    @Override
    public Object copyTo1DJavaArray() {
        return getNcArray().getArray().copyTo1DJavaArray();
    }

    @Override
	public Object get1DJavaArray(final Class<?> wantType) {
		return getNcArray().getArray().get1DJavaArray(wantType);
	}
	
    @Override
    public Object copyToNDJavaArray() {
        return getNcArray().getArray().copyToNDJavaArray();
    }

    @Override
    public IArrayUtils slice(int dim, int value) {
    	return new NcArrayUtils(new NcArray(getNcArray().getArray().slice(dim, value)));
    }

	@Override
	public IArrayUtils createArrayUtils(IArray array) {
		return new NcArrayUtils((NcArray) array);
	}
	
	@Override
	public IArrayUtils flip(final int dim) {
		return new NcArrayUtils(new NcArray(getNcArray().getArray().flip(dim)));
	}
    
	@Override
	public IArrayUtils permute(final int[] dims) {
		return new NcArrayUtils(new NcArray(getNcArray().getArray().permute(dims)));
	}

}
