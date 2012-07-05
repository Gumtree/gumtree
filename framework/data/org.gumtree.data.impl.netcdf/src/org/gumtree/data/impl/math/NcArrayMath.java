package org.gumtree.data.impl.math;

import org.gumtree.data.IFactory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.impl.netcdf.NcArray;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.math.ArrayMath;
import org.gumtree.data.math.IArrayMath;

import ucar.ma2.MAMath;
import ucar.ma2.MAMatrix;
import ucar.ma2.MAVector;

public class NcArrayMath extends ArrayMath {
    
    public NcArrayMath(NcArray array, IFactory factory)  {
    	super(array, factory);
    }

    protected NcArray getNcArray() {
        return (NcArray) getArray();
    }
    
    /// Math methods

    /**
     * @param newArray
     *            another Array object
     * @return this
     * @throws ShapeNotMatchException
     *             shape not match
     * @see org.gumtree.data.interfaces.IArray#add(org.gumtree.data.interfaces.IArray)
     */
    public IArrayMath add(final IArray newArray) throws ShapeNotMatchException {
    	getArray().getArrayUtils().checkShape(newArray);
        if (getArray().getRank() == newArray.getRank()) {
            MAMath.addDouble(getNcArray().getArray(), getNcArray().getArray(), ((NcArray) newArray).getArray());
        } else {
            ISliceIterator sourceSliceIterator = null;
            try {
                sourceSliceIterator = getArray().getSliceIterator(newArray.getRank());
                while (sourceSliceIterator.hasNext()) {
                    IArray sourceSlice = sourceSliceIterator.getArrayNext();
                    MAMath.addDouble(((NcArray) sourceSlice).getArray(),
                            ((NcArray) sourceSlice).getArray(),
                            ((NcArray) newArray).getArray());
                }
            } catch (InvalidRangeException e) {
                throw new ShapeNotMatchException("shape is invalid");
            }
        }
        getArray().setDirty(true);
        return this;
    }
    
    /**
     * @param dimension
     *            integer value
     * @param isVariance
     *            true or false
     * @return new Array object
     * @throws ShapeNotMatchException
     *             shape not match
     * @see org.gumtree.data.interfaces.IArray#enclosedSumForDimension(int, boolean)
     */
    public IArrayMath enclosedSumForDimension(final int dimension,
            final boolean isVariance) throws ShapeNotMatchException {
        if (dimension >= getArray().getRank()) {
            throw new ShapeNotMatchException(dimension
                    + " dimension is not available");
        }
        if (getArray().getRank() == 1) {
            return new NcArrayMath(getNcArray().copy(), getFactory());
        }
        int[] shape = getArray().getShape();
        double[] result = new double[shape[dimension]];
        for (int i = 0; i < shape[dimension]; i++) {
            if (!isVariance) {
                result[i] = getNcArray().slice(dimension, i).getArrayMath().sum();
            } else {
                result[i] = getNcArray().slice(dimension, i).getArrayMath().sum();
            }
        }
        return getFactory().createArray(Double.TYPE, new int[] { shape[dimension] },
                result).getArrayMath();
    }

    /**
     * @return double value
     * @throws ShapeNotMatchException
     *             shape not match
     */
    public double getDeterminant() throws ShapeNotMatchException {
        MAMatrix matrix = null;
        try {
            matrix = new MAMatrix(getNcArray().getArray());
        } catch (Exception e) {
            throw new ShapeNotMatchException("not a matrix array");
        }

        return matrix.getDeterminant();
    }

//    /**
//     * @return double value
//     * @see org.gumtree.data.interfaces.IArray#getMaximum()
//     */
//    public double getMaximum() {
//        return MAMath.getMaximum(getNcArray().getArray());
//    }
//
//    /**
//     * @return double value
//     * @see org.gumtree.data.interfaces.IArray#getMinimum()
//     */
//    public double getMinimum() {
//        return MAMath.getMinimum(getNcArray().getArray());
//    }

    /**
     * @return double value
     * @see org.gumtree.data.interfaces.IArray#getNorm()
     */
    public double getNorm() {
        MAVector vector = new MAVector(getNcArray().getArray());
        return vector.norm();
    }

    /**
     * @return new Array object
     * @throws ShapeNotMatchException
     *             shape not match
     * @see org.gumtree.data.interfaces.IArray#matInverse()
     */
    public IArrayMath matInverse() throws ShapeNotMatchException {
        MAMatrix matrix = new MAMatrix(getNcArray().getArray());
        matrix = matrix.inverse();
        return new NcArray(matrix.getArrayStorage()).getArrayMath();
    }

    /**
     * @param newArray
     *            another Array object
     * @return this
     * @throws ShapeNotMatchException
     *             shape not match
     * @see org.gumtree.data.interfaces.IArray#matMultiply(org.gumtree.data.interfaces.IArray)
     */
    public IArrayMath matMultiply(final IArray newArray)
            throws ShapeNotMatchException {
        IArrayMath result = null;
        try {
            MAMatrix matrix1 = new MAMatrix(getNcArray().getArray());
            MAMatrix matrix2 = new MAMatrix(((NcArray) newArray).getArray());
            MAMatrix resultMatrix = MAMatrix.multiply(matrix1, matrix2);
            result = new NcArrayMath(new NcArray(resultMatrix.getArrayStorage()), getFactory());
        } catch (Exception e) {
            throw new ShapeNotMatchException("the shapes of two array is not "
                    + "compatable for matrix multiplication");
        }
        return result;
    }

    /**
     * @return new Array object
     * @see org.gumtree.data.interfaces.IArray#normalise()
     */
    public IArrayMath normalise() {
        MAVector vector = new MAVector(getNcArray().getArray());
        vector.normalize();
        return new NcArray(vector.getArrayStorage()).getArrayMath();
    }

    /**
     * @param dimension
     *            integer value
     * @param isVariance
     *            true or false
     * @return new Array object
     * @throws ShapeNotMatchException
     *             shape not match
     * @see org.gumtree.data.interfaces.IArray#sumForDimension(int, boolean)
     */
    public IArrayMath sumForDimension(final int dimension, final boolean isVariance)
            throws ShapeNotMatchException {
        if (dimension >= getArray().getRank()) {
            throw new ShapeNotMatchException(dimension
                    + " dimension is not available");
        }
        if (getArray().getRank() == 1) {
            return getArray().copy().getArrayMath();
        }
        int[] shape = getArray().getShape();
        double[] result = new double[shape[dimension]];
        for (int i = 0; i < shape[dimension]; i++) {
            if (!isVariance) {
                result[i] = getNcArray().slice(dimension, i).getArrayMath().sumNormalise();
            } else {
                result[i] = getNcArray().slice(dimension, i).getArrayMath().varianceSumNormalise();
                // result[i] = slice(dimension, i).sum();
            }
        }
        return getFactory().createArray(Double.TYPE, new int[] { shape[dimension] },
                result).getArrayMath();
    }

    /**
     * @param newArray
     *            another IArray object
     * @return new Array object
     * @throws ShapeNotMatchException
     *             shape not match
     * @see org.gumtree.data.interfaces.IArray#toAdd(org.gumtree.data.interfaces.IArray)
     */
    public IArrayMath toAdd(final IArray newArray) throws ShapeNotMatchException {
    	getArray().getArrayUtils().checkShape(newArray);
        IArray result = getFactory().createArray(getArray().getElementType(), getArray().getShape());
        if (getArray().getRank() == newArray.getRank()) {
            MAMath.addDouble(((NcArray) result).getArray(), getNcArray().getArray(),
                    ((NcArray) newArray).getArray());
        } else {
            ISliceIterator sourceSliceIterator = null;
            ISliceIterator resultSliceIterator = null;
            try {
                sourceSliceIterator = getArray().getSliceIterator(newArray.getRank());
                resultSliceIterator = result.getSliceIterator(newArray.getRank());
                while (sourceSliceIterator.hasNext()
                        && resultSliceIterator.hasNext()) {
                    IArray sourceSlice = sourceSliceIterator.getArrayNext();
                    IArray resultSlice = resultSliceIterator.getArrayNext();
                    MAMath.addDouble(((NcArray) resultSlice).getArray(),
                            ((NcArray) sourceSlice).getArray(),
                            ((NcArray) newArray).getArray());
                }
            } catch (InvalidRangeException e) {
                throw new ShapeNotMatchException("shape is invalid");
            }
        }
        return result.getArrayMath();
    }


}
