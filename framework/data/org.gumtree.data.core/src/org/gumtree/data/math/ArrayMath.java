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

package org.gumtree.data.math;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.exception.DivideByZeroException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.ISliceIterator;

public abstract class ArrayMath implements IArrayMath {

	private IArray m_array;

	private IFactory factory;

	public ArrayMath(IArray array, IFactory factory) {
		m_array = array;
		this.factory = factory;
		if (this.factory == null) {
			this.factory = Factory.getFactory();
		}
	}

	public IArray getArray() {
		return m_array;
	}

	/**
     * Add two IArray together, element-wisely. The two arrays must have the same
	 * shape.
	 * 
     * @param array in IArray type
     * @return IArray with new storage
	 * @throws ShapeNotMatchException
     *             mismatching shape 
	 */
	public IArrayMath toAdd(IArrayMath array) throws ShapeNotMatchException {
		return toAdd(array.getArray());
	}

	public IArrayMath add(IArrayMath array) throws ShapeNotMatchException {
		return add(array.getArray());
	}

    /**
     * Add a value to the IArray element-wisely.
     * 
     * @param value double type
     * @return IArray with new storage 
     */
	public IArrayMath toAdd(double value) {
		// [ANSTO][Tony][2011-08-30] Resulting array should be typed as double
		IArray result = getFactory().createArray(double.class,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					oldIterator.getDoubleNext() + value);
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with adding a constant to its values.
	 * 
     * @param value double type
     * @return IArray itself 
	 */
	public IArrayMath add(double val) {
		IArrayIterator iter = getArray().getIterator();
		while (iter.hasNext()) {
			iter.setDoubleCurrent(iter.getDoubleNext() + val);
		}
		getArray().setDirty(true);
		return this;
	}

	public IArrayMath eltRemainder(IArray newArray)
			throws ShapeNotMatchException {
		getArray().getArrayUtils().checkShape(newArray);
		if (getArray().getRank() == newArray.getRank()) {
			eltRemainderEqualSize(newArray, getArray());
		} else {
			ISliceIterator sourceSliceIterator = null;
			try {
				sourceSliceIterator = getArray().getSliceIterator(
						newArray.getRank());
				while (sourceSliceIterator.hasNext()) {
					IArray sourceSlice = sourceSliceIterator.getArrayNext();
					sourceSlice.getArrayMath().eltRemainderEqualSize(newArray,
							sourceSlice);
				}
			} catch (InvalidRangeException e) {
				throw new ShapeNotMatchException("shape is invalid");
			}
		}
		getArray().setDirty(true);
		return this;
	}

	public IArrayMath eltRemainder(IArrayMath array)
			throws ShapeNotMatchException {
		return eltRemainder(array.getArray());
	}

	/**
	 * Multiply the two arrays element-wisely. Xij = Aij * Bij. The two arrays
	 * must have the same shape.
	 * 
     * @param array in IArray type
     * @return IArray with new storage
	 * @throws ShapeNotMatchException
     *             mismatching shape 
	 */
	public IArrayMath toEltRemainder(IArray newArray)
			throws ShapeNotMatchException {
		getArray().getArrayUtils().checkShape(newArray);
		IArrayMath arrMath = newArray.getArrayMath();
		IArray result = getFactory().createArray(getArray().getElementType(),
				getArray().getShape());
		if (getArray().getRank() == newArray.getRank()) {
			eltRemainderEqualSize(newArray, result);
		} else {
			ISliceIterator sourceSliceIterator = null;
			ISliceIterator resultSliceIterator = null;
			try {
				sourceSliceIterator = getArray().getSliceIterator(
						newArray.getRank());
				resultSliceIterator = result.getSliceIterator(newArray
						.getRank());
				while (sourceSliceIterator.hasNext()
						&& resultSliceIterator.hasNext()) {
					IArray sourceSlice = sourceSliceIterator.getArrayNext();
					IArray resultSlice = resultSliceIterator.getArrayNext();
					arrMath.eltRemainderEqualSize(sourceSlice, resultSlice);
				}
			} catch (InvalidRangeException e) {
				throw new ShapeNotMatchException("shape is invalid");
			}
		}
		return result.getArrayMath();
	}

	public IArrayMath toEltRemainder(IArrayMath array)
			throws ShapeNotMatchException {
		return toEltRemainder(array.getArray());
	}

	public IArrayMath toEltMultiply(IArray newArray)
			throws ShapeNotMatchException {
		getArray().getArrayUtils().checkShape(newArray);
		IArrayMath arrMath = newArray.getArrayMath();
		IArray result = getFactory().createArray(getArray().getElementType(),
				getArray().getShape());
		if (getArray().getRank() == newArray.getRank()) {
			eltMultiplyWithEqualSize(newArray, result);
		} else {
			ISliceIterator sourceSliceIterator = null;
			ISliceIterator resultSliceIterator = null;
			try {
				sourceSliceIterator = getArray().getSliceIterator(
						newArray.getRank());
				resultSliceIterator = result.getSliceIterator(newArray
						.getRank());
				while (sourceSliceIterator.hasNext()
						&& resultSliceIterator.hasNext()) {
					IArray sourceSlice = sourceSliceIterator.getArrayNext();
					IArray resultSlice = resultSliceIterator.getArrayNext();
					arrMath.eltMultiplyWithEqualSize(sourceSlice, resultSlice);
				}
			} catch (InvalidRangeException e) {
				throw new ShapeNotMatchException("shape is invalid");
			}
		}
		return result.getArrayMath();
	}

	public IArrayMath toEltMultiply(IArrayMath array)
			throws ShapeNotMatchException {
		return toEltMultiply(array.getArray());
	}

	/**
	 * Update the array with the element wise multiply of its values.
	 * 
     * @param array IArray object
     * @return IArray itself
	 * @throws ShapeNotMatchException
     *             mismatching shape 
	 */
	public IArrayMath eltMultiply(IArray newArray)
			throws ShapeNotMatchException {
		getArray().getArrayUtils().checkShape(newArray);
		if (getArray().getRank() == newArray.getRank()) {
			eltMultiplyWithEqualSize(newArray, getArray());
		} else {
			ISliceIterator sourceSliceIterator = null;
			try {
				sourceSliceIterator = getArray().getSliceIterator(
						newArray.getRank());
				while (sourceSliceIterator.hasNext()) {
					IArray sourceSlice = sourceSliceIterator.getArrayNext();
					sourceSlice.getArrayMath().eltMultiplyWithEqualSize(
							newArray, sourceSlice);
				}
			} catch (InvalidRangeException e) {
				throw new ShapeNotMatchException("shape is invalid");
			}
		}
		getArray().setDirty(true);
		return this;
	}

	public IArrayMath eltMultiply(IArrayMath array)
			throws ShapeNotMatchException {
		return eltMultiply(array.getArray());
	}

    /**
     * Scale the array with a double value.
     * 
     * @param value double type
     * @return IArray with new storage 
     */
	public IArrayMath toScale(double value) {
		IArray result = getFactory().createArray(getArray().getElementType(),
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					oldIterator.getDoubleNext() * value);
		}
		return result.getArrayMath();
	}

    /**
     * Modulo the array with a double value.
     * 
     * @param value double type
     * @return IArray with new storage 
     */
	public IArrayMath toMod(double value) {
		IArray result = getFactory().createArray(getArray().getElementType(),
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					oldIterator.getDoubleNext() % value);
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with the scale of its values.
	 * 
     * @param value double type
     * @return IArray itself 
	 */
	public IArrayMath scale(double value) {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			oldIterator.setDoubleCurrent(oldIterator.getDoubleNext() * value);
		}
		getArray().setDirty(true);
		return this;
	}

	/**
	 * Update the array with the mod of a value.
	 * 
     * @param value double type
     * @return IArray itself 
	 */
	public IArrayMath mod(double value) {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			oldIterator.setDoubleCurrent(oldIterator.getDoubleNext() % value);
		}
		getArray().setDirty(true);
		return this;
	}

	public IArrayMath matMultiply(IArrayMath array)
			throws ShapeNotMatchException {
		return matMultiply(array.getArray());
	}

	/**
	 * Calculate the square root value of every element of the array.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toSqrt() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					Math.sqrt(oldIterator.getDoubleNext()));
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with of the square root its value.
	 * 
     * @return IArray itself 
	 */
	public IArrayMath sqrt() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			oldIterator
					.setDoubleCurrent(Math.sqrt(oldIterator.getDoubleNext()));
		}
		getArray().setDirty(true);
		return this;
	}

	/**
     * Calculate the e raised to the power of double values in the IArray
	 * element-wisely.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toExp() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					Math.exp(oldIterator.getDoubleNext()));
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with e raised to the power of its values.
	 * 
     * @return IArray itself 
	 */
	public IArrayMath exp() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			oldIterator.setDoubleCurrent(Math.exp(oldIterator.getDoubleNext()));
		}
		getArray().setDirty(true);
		return this;
	}

	/**
     * Calculate an element-wise natural logarithm of values of an IArray.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toLn() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			double value = oldIterator.getDoubleNext();
			if (value == 0) {
				newIterator.next().setDoubleCurrent(Double.NaN);
			} else {
				newIterator.next().setDoubleCurrent(Math.log(value));
			}
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with element-wise natural logarithm of its values.
	 * 
     * @return IArray itself 
	 */
	public IArrayMath ln() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			double value = oldIterator.getDoubleNext();
			if (value == 0) {
                oldIterator.setDoubleCurrent(Double.NaN);
			} else {
                oldIterator.setDoubleCurrent(Math.log(value));
			}
		}
		getArray().setDirty(true);
		return this;
	}

	/**
     * Calculate an element-wise logarithm (base 10) of values of an IArray.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toLog10() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			double value = oldIterator.getDoubleNext();
			if (value == 0) {
				newIterator.next().setDoubleCurrent(Double.NaN);
			} else {
				newIterator.next().setDoubleCurrent(Math.log10(value));
			}
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with element-wise logarithm (base 10) of its values.
	 * 
     * @return IArray itself 
	 */
	public IArrayMath log10() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			double value = oldIterator.getDoubleNext();
			if (value == 0) {
                oldIterator.setDoubleCurrent(Double.NaN);
			} else {
                oldIterator.setDoubleCurrent(Math.log10(value));
			}
		}
		getArray().setDirty(true);
		return this;
	}

	/**
     * Calculate the sine value of each elements in the IArray.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toSin() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					Math.sin(oldIterator.getDoubleNext()));
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with sine of its values.
	 * 
     * @return IArray itself 
	 */
	public IArrayMath sin() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			oldIterator.setDoubleCurrent(Math.sin(oldIterator.getDoubleNext()));
		}
		getArray().setDirty(true);
		return this;
	}

	/**
     * Calculate the arc sine value of each elements in the IArray.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toAsin() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					Math.asin(oldIterator.getDoubleNext()));
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with arc sine of its values.
	 * 
	 * @return IArray itself
	 */
	public IArrayMath asin() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			oldIterator
					.setDoubleCurrent(Math.asin(oldIterator.getDoubleNext()));
		}
		getArray().setDirty(true);
		return this;
	}

	/**
     * Calculate the cosine value of each elements in the IArray.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toCos() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					Math.cos(oldIterator.getDoubleNext()));
		}
		return result.getArrayMath();
	}

	/**
     * Calculate the arc cosine value of each elements in the IArray.
	 * 
     * @param array in array type
     * @return IArray with new storage 
	 */
	public IArrayMath toAcos() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					Math.acos(oldIterator.getDoubleNext()));
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with cosine of its values.
	 * 
     * @param array in array type
     * @return IArray itself 
	 */
	public IArrayMath cos() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			oldIterator.setDoubleCurrent(Math.cos(oldIterator.getDoubleNext()));
		}
		getArray().setDirty(true);
		return this;
	}

	/**
	 * Update the array with arc cosine of its values.
	 * 
     * @return IArray itself 
	 */
	public IArrayMath acos() {
		IArrayIterator iterator = getArray().getIterator();
		while (iterator.hasNext()) {
			iterator.setDoubleCurrent(Math.acos(iterator.getDoubleNext()));
		}
		getArray().setDirty(true);
		return this;
	}

	/**
     * Calculate the trigonometric value of each elements in the IArray.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toTan() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					Math.tan(oldIterator.getDoubleNext()));
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with trigonometric of its values.
	 * 
     * @return IArray itself 
	 */
	public IArrayMath tan() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			oldIterator.setDoubleCurrent(Math.tan(oldIterator.getDoubleNext()));
		}
		getArray().setDirty(true);
		return this;
	}

	/**
     * Calculate the arc trigonometric value of each elements in the IArray.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toAtan() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					Math.atan(oldIterator.getDoubleNext()));
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with arc trigonometric of its values.
	 * 
     * @return IArray itself 
	 */
	public IArrayMath atan() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
            oldIterator.setDoubleCurrent(Math.atan(oldIterator.getDoubleNext()));
		}
		getArray().setDirty(true);
		return this;
	}

	/**
	 * Do an element-wise power calculation of the array. Yij = Xij ^ power.
	 * 
     * @param power double value
     * @return IArray with new storage 
	 */
	public IArrayMath toPower(double value) {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			newIterator.next().setDoubleCurrent(
					Math.pow(oldIterator.getDoubleNext(), value));
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with to a constant power of its values.
	 * 
     * @param power double value
     * @return IArray itself 
	 */
	public IArrayMath power(double value) {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
            oldIterator.setDoubleCurrent(Math.pow(oldIterator.getDoubleNext(),value));
		}
		getArray().setDirty(true);
		return this;
	}

    /**
     * Do a power-sum on a certain dimension. A power-sum will raise all element
     * of the array to a certain power, then do a sum on a certain dimension,
     * and put weight on the result.
     * 
     * @param axis IArray object
     * @param dimension integer
     * @param power double value
     * @return IArray with new storage
     * @throws ShapeNotMatchException
     */
    public double powerSum(IArray axis, int dimension,
            double power) throws ShapeNotMatchException {
        if (dimension >= getArray().getRank()) {
            throw new ShapeNotMatchException(dimension
                    + " dimension is not available");
        }
        int[] shape = getArray().getShape();
        if (axis != null && axis.getSize() < shape[dimension]) {
            throw new ShapeNotMatchException("axis size not match");
        }
        IArray result = getFactory().createArray(getArray().getElementType(), getArray().getShape());
        result = power(power).getArray();
        double powerSum = 0;
        for (int i = 0; i < shape[dimension]; i++) {
            IArray sumOnDimension;
            if (axis == null) {
                sumOnDimension = result.getArrayMath().sumForDimension(i, false).scale(i).getArray();
            } else {
                sumOnDimension = result.getArrayMath().sumForDimension(i, false).eltMultiply(axis).getArray();
            }
            powerSum += sumOnDimension.getArrayMath().sum();
        }
        return powerSum;
    }

	public double powerSum(IArrayMath axis, int dimension, double power)
			throws ShapeNotMatchException {
		return powerSum(axis.getArray(), dimension, power);
	}

	/**
	 * Calculate the sum value of the array. If an element is NaN, skip it.
	 * 
     * @return a double value 
	 */
	public double sum() {
		double sum = Double.NaN;
		IArrayIterator iterator = getArray().getIterator();
		while (iterator.hasNext()) {
			Double value = iterator.getDoubleNext();
			if (!value.isNaN()) {
				sum = value;
				break;
			}
		}
		while (iterator.hasNext()) {
			Double value = iterator.getDoubleNext();
			if (!value.isNaN()) {
				sum += value;
			}
		}
		return sum;
	}

	/**
	 * Calculate the sum value of the array. If an element is NaN, skip it. Then
	 * after calculation, normalise the result to the actual size of the array.
	 * For example, result = raw sum * size of array / (size of array - number
	 * of NaNs).
	 * 
     * @return a double value 
	 */
	public double sumNormalise() {
		double sum = Double.NaN;
		int countNaN = 0;
		IArrayIterator iterator = getArray().getIterator();
		while (iterator.hasNext()) {
			Double value = iterator.getDoubleNext();
			if (value.isNaN()) {
				countNaN++;
			} else {
				sum = value;
				break;
			}
		}
		while (iterator.hasNext()) {
			Double value = iterator.getDoubleNext();
			if (value.isNaN()) {
				countNaN++;
			} else {
				sum += value;
			}
		}
		if (Double.isNaN(sum)) {
			return sum;
		}
		return Double.valueOf(sum) * getArray().getSize()
				/ Double.valueOf(getArray().getSize() - countNaN);
	}

	/**
	 * Inverse every element of the array into a new storage.
	 * 
     * @return IArray with new storage
	 * @throws DivideByZeroException
	 */
	public IArrayMath toEltInverse() throws DivideByZeroException {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			try {
				newIterator.next().setDoubleCurrent(
						1 / oldIterator.getDoubleNext());
			} catch (Exception e) {
				throw new DivideByZeroException(e);
			}
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with element-wise inverse of its values.
	 * 
     * @return IArray itself
	 * @throws DivideByZeroException
     *             divided by zero 
	 */
	public IArrayMath eltInverse() throws DivideByZeroException {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			try {
				oldIterator.setDoubleCurrent(1 / oldIterator.getDoubleNext());
			} catch (Exception e) {
				throw new DivideByZeroException(e);
			}
		}
		getArray().setDirty(true);
		return this;
	}

	/**
	 * Do a element-wise inverse calculation that skip zero values. Yij = 1 /
	 * Xij.
	 * 
     * @return IArray with new storage 
	 */
	public IArrayMath toEltInverseSkipZero() {
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		IArrayIterator oldIterator = getArray().getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (oldIterator.hasNext()) {
			double det = oldIterator.getDoubleNext();
            newIterator.next().setDoubleCurrent(det == 0 ? 0 : 1 / det);
		}
		return result.getArrayMath();
	}

	/**
	 * Update the array with element-wise inverse of its values, skip zero
	 * values.
	 * 
     * @return IArray itself 
	 */
	public IArrayMath eltInverseSkipZero() {
		IArrayIterator oldIterator = getArray().getIterator();
		while (oldIterator.hasNext()) {
			double det = oldIterator.getDoubleNext();
            oldIterator.setDoubleCurrent(det == 0 ? 0 : 1 / det);
		}
		getArray().setDirty(true);
		return this;
	}

	/**
	 * Calculate the vector dot production of two arrays. Both array must have
	 * the same size.
	 * 
     * @param array in IArray type
     * @return IArray with new storage
	 * @throws ShapeNotMatchException
	 */
	public double vecDot(IArray newArray) throws ShapeNotMatchException {
		try {
			return toEltMultiply(newArray).sum();
		} catch (Exception e) {
			throw new ShapeNotMatchException(e);
		}
	}

	public double vecDot(IArrayMath array) throws ShapeNotMatchException {
		return vecDot(array.getArray());
	}

	/**
     * Treat the array as a variance. Normalize the sum against the number of
	 * elements in the array.
	 * 
	 * @return double value
	 */
	public double varianceSumNormalise() {
		double sum = Double.NaN;
		int countNaN = 0;
		IArrayIterator iterator = getArray().getIterator();
		while (iterator.hasNext()) {
			Double value = iterator.getDoubleNext();
			if (value.isNaN()) {
				countNaN++;
			} else {
				sum = value;
				break;
			}
		}
		while (iterator.hasNext()) {
			Double value = iterator.getDoubleNext();
			if (value.isNaN()) {
				countNaN++;
			} else {
				sum += value;
			}
		}
		if (Double.isNaN(sum)) {
			return sum;
		}
		double normaliseFactor = getArray().getSize()
				/ Double.valueOf(getArray().getSize() - countNaN);
		return Double.valueOf(sum) * normaliseFactor * normaliseFactor;
	}

	/**
	 * Element-wise multiply another array, and put the result in a given array.
	 * 
     * @param array CDMA IArray object
     * @param result CDMA IArray object
	 * @throws ShapeNotMatchException
	 */
	public void eltMultiplyWithEqualSize(IArray newArray, IArray result)
			throws ShapeNotMatchException {
		if (getArray().getSize() != newArray.getSize()) {
			throw new ShapeNotMatchException("the size of the arrays not match");
		}
		IArrayIterator iterator1 = getArray().getIterator();
		IArrayIterator iterator2 = newArray.getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (iterator1.hasNext()) {
			newIterator.next().setDoubleCurrent(
					iterator1.getDoubleNext() * iterator2.getDoubleNext());
		}
		getArray().setDirty(true);
	}

	public void eltMultiplyWithEqualSize(IArrayMath array, IArrayMath result)
			throws ShapeNotMatchException {
		eltMultiplyWithEqualSize(array.getArray(), result.getArray());
	}

	public void eltRemainderEqualSize(IArrayMath array, IArrayMath result)
			throws ShapeNotMatchException {
		eltRemainderEqualSize(array.getArray(), result.getArray());
	}

	public void eltRemainderEqualSize(IArray newArray, IArray result)
			throws ShapeNotMatchException {
		if (getArray().getSize() != newArray.getSize()) {
			throw new ShapeNotMatchException("the size of the arrays not match");
		}
		IArrayIterator iterator1 = getArray().getIterator();
		IArrayIterator iterator2 = newArray.getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (iterator1.hasNext()) {
			newIterator.next().setDoubleCurrent(
					iterator1.getDoubleNext() % iterator2.getDoubleNext());
		}
		getArray().setDirty(true);
	}

	/**
	 * Element-wise divided by another array, and put the result in a given
	 * array.
	 * 
     * @param array CDMA IArray object
     * @param result CDMA IArray object
	 * @throws ShapeNotMatchException
	 */
	public void eltDivideWithEqualSize(IArray newArray, IArray result)
			throws ShapeNotMatchException {
		if (getArray().getSize() != newArray.getSize()) {
			throw new ShapeNotMatchException("the size of the arrays not match");
		}
		IArrayIterator iterator1 = getArray().getIterator();
		IArrayIterator iterator2 = newArray.getIterator();
		IArrayIterator newIterator = result.getIterator();
		while (iterator1.hasNext()) {
			double newValue = iterator2.getDoubleNext();
			if (newValue != 0) {
				newIterator.next().setDoubleCurrent(
						iterator1.getDoubleNext() / newValue);
			} else {
				newIterator.next().setDoubleCurrent(iterator1.getDoubleNext());
			}
		}
		getArray().setDirty(true);
	}

	public void eltDivideWithEqualSize(IArrayMath array, IArrayMath result)
			throws ShapeNotMatchException {
		eltDivideWithEqualSize(array.getArray(), result.getArray());
	}

	/**
	 * Element wise divide the value by value from a given array.
	 * 
     * @param array IArray object
	 * @return new array
	 * @throws ShapeNotMatchException
	 *             mismatching shape
	 */
	public IArrayMath toEltDivide(IArray newArray)
			throws ShapeNotMatchException {
		getArray().getArrayUtils().checkShape(newArray);
		IArray result = getFactory().createArray(Double.TYPE,
				getArray().getShape());
		if (getArray().getRank() == newArray.getRank()) {
			eltDivideWithEqualSize(newArray, result);
		} else {
			ISliceIterator sourceSliceIterator = null;
			ISliceIterator resultSliceIterator = null;
			try {
				sourceSliceIterator = getArray().getSliceIterator(
						newArray.getRank());
				resultSliceIterator = result.getSliceIterator(newArray
						.getRank());
				while (sourceSliceIterator.hasNext()
						&& resultSliceIterator.hasNext()) {
					IArray sourceSlice = sourceSliceIterator.getArrayNext();
					IArray resultSlice = resultSliceIterator.getArrayNext();
					sourceSlice.getArrayMath().eltDivideWithEqualSize(newArray,
							resultSlice);
				}
			} catch (InvalidRangeException e) {
				throw new ShapeNotMatchException("shape is invalid");
			}
		}
		return result.getArrayMath();
	}

	public IArrayMath toEltDivide(IArrayMath array)
			throws ShapeNotMatchException {
		return toEltDivide(array.getArray());
	}

	/**
	 * Element wise divide the array1 value by value from given array2.
	 * 
     * @param array IArray object
	 * @return this array1 after modification
	 * @throws ShapeNotMatchException
	 *             mismatching shape
	 */
	public IArrayMath eltDivide(IArray newArray) throws ShapeNotMatchException {
		getArray().getArrayUtils().checkShape(newArray);
		if (getArray().getRank() == newArray.getRank()) {
			eltDivideWithEqualSize(newArray, getArray());
		} else {
			ISliceIterator sourceSliceIterator = null;
			try {
				sourceSliceIterator = getArray().getSliceIterator(
						newArray.getRank());
				while (sourceSliceIterator.hasNext()) {
					IArray sourceSlice = sourceSliceIterator.getArrayNext();
					sourceSlice.getArrayMath().eltDivideWithEqualSize(newArray,
							sourceSlice);
				}
			} catch (InvalidRangeException e) {
				throw new ShapeNotMatchException("shape is invalid");
			}
		}
		getArray().setDirty(true);
		return this;
	}

	public IArrayMath eltDivide(IArrayMath array) throws ShapeNotMatchException {
		return eltDivide(array.getArray());
	}

	@Override
	public IFactory getFactory() {
		return factory;
	}

	public double getMaximum() {
		IArrayIterator iter = getArray().getIterator();
		double max = -Double.MAX_VALUE;
		while (iter.hasNext()) {
			double val = iter.getDoubleNext();
			if (Double.isNaN(val))
				continue;
			if (val > max)
				max = val;
		}
		return max;
	}

	public double getMinimum() {
		IArrayIterator iter = getArray().getIterator();
		double min = Double.MAX_VALUE;
		while (iter.hasNext()) {
			double val = iter.getDoubleNext();
			if (Double.isNaN(val))
				continue;
			if (val < min)
				min = val;
		}
		return min;
	}
}
