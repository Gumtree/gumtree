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

import org.gumtree.data.exception.DivideByZeroException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;

/**
 * CDMA math library. Most of the calculation will be delegated to object
 * level calculation.
 * 
 * @author nxi 
 */
public final class GMath {

	/**
	 * Hide the default constructor.
	 */
	private GMath() {
	}

	/**
	 * Transpose the two given dimension of the array. The array has to have
	 * more than one dimension. A'=A.
	 * 
     * @param array in IArray type
     * @param dim1 an integer value
     * @param dim2 an integer value
     * @return IArray with new storage 
	 */
	public static IArray transpose(final IArray array, final int dim1,
			final int dim2) {
		return array.getArrayUtils().transpose(dim1, dim2).getArray();
	}

	/**
     * Add two IArray together, element-wisely. The two arrays must have the same
	 * shape.
	 * 
     * @param array1 in IArray type
     * @param array2 in IArray type
     * @return IArray with new storage
	 * @throws ShapeNotMatchException
	 */
	public static IArray add(final IArray array1, final IArray array2)
			throws ShapeNotMatchException {
		return array1.getArrayMath().toAdd(array2).getArray();
	}

	/**
     * Add a value to the IArray element-wisely.
	 * 
     * @param array in IArray type
     * @param value double value
     * @return IArray with new storage 
	 */
	public static IArray add(final IArray array, final double value) {
		return array.getArrayMath().toAdd(value).getArray();
	}

	/**
	 * Multiply the two arrays element-wisely. Xij = Aij * Bij. The two arrays
	 * must have the same shape.
	 * 
     * @param array1 in IArray type
     * @param array2 in IArray type
     * @return IArray with new storage
	 * @throws ShapeNotMatchException
	 */
	public static IArray eltMultiply(final IArray array1, final IArray array2)
			throws ShapeNotMatchException {
		return array1.getArrayMath().toEltMultiply(array2).getArray();
	}

	/**
	 * Scale the array with a double value.
	 * 
     * @param array in IArray type
     * @param value double value
     * @return IArray with new storage 
	 */
	public static IArray scale(final IArray array, final double value) {
		return array.getArrayMath().toScale(value).getArray();
	}

	/**
	 * Inverse every element of the array into a new storage.
	 * 
     * @param array in IArray type
     * @return IArray with new storage
	 * @throws DivideByZeroException
	 */
	public static IArray eltInverse(final IArray array)
			throws DivideByZeroException {
		return array.getArrayMath().toEltInverse().getArray();
	}

	/**
	 * Multiple two arrays in matrix multiplication rule. The two arrays must
	 * comply matrix multiply requirement.
	 * 
     * @param array1 in IArray type
     * @param array2 in IArray type
     * @return IArray with new storage
	 * @throws ShapeNotMatchException
	 */
	public static IArray matMultiply(final IArray array1, final IArray array2)
			throws ShapeNotMatchException {
		return array1.getArrayMath().matMultiply(array2).getArray();
	}

	/**
	 * Calculate the vector dot production of two arrays. Both array must have
	 * the same size.
	 * 
     * @param array1 in IArray type
     * @param array2 in IArray type
	 * @return a double value
	 * @throws ShapeNotMatchException
	 */
	public static double vecDot(final IArray array1, final IArray array2)
			throws ShapeNotMatchException {
		return array1.getArrayMath().vecDot(array2);
	}

	/**
	 * Calculate the square root value of every element of the array.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray sqrt(final IArray array) {
		return array.getArrayMath().toSqrt().getArray();
	}

	/**
	 * Calculate the e raised to the power of double values in the IArray
	 * element-wisely.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray exp(final IArray array) {
		return array.getArrayMath().toExp().getArray();
	}

	/**
	 * Calculate an element-wise natural logarithm (base e) of values of an
	 * IArray.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray ln(final IArray array) {
		return array.getArrayMath().toLn().getArray();
	}

	/**
	 * Calculate an element-wise logarithm (base 10) of values of an IArray.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray log10(final IArray array) {
		return array.getArrayMath().toLog10().getArray();
	}

	/**
	 * Calculate the sine value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray sin(final IArray array) {
		return array.getArrayMath().toSin().getArray();
	}

	/**
	 * Calculate the arc sine value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray asin(final IArray array) {
		return array.getArrayMath().toAsin().getArray();
	}

	/**
	 * Calculate the cosine value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray cos(final IArray array) {
		return array.getArrayMath().toCos().getArray();
	}

	/**
	 * Calculate the arc cosine value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray acos(final IArray array) {
		return array.getArrayMath().toAcos().getArray();
	}

	/**
	 * Calculate the trigonometric value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray tan(final IArray array) {
		return array.getArrayMath().toTan().getArray();
	}

	/**
	 * Calculate the arc trigonometric value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray atan(final IArray array) {
		return array.getArrayMath().toAtan().getArray();
	}

	/**
	 * Do an element-wise power calculation of the array. Yij = Xij ^ power.
	 * 
     * @param array in IArray type
     * @param power integer value
     * @return IArray with new storage 
	 */
	public static IArray power(final IArray array, final int power) {
		return array.getArrayMath().toPower(power).getArray();
	}

	/**
	 * Do a power-sum on a certain dimension. A power-sum will raise all element
	 * of the array to a certain power, then do a sum on a certain dimension,
	 * and put weight on the result.
	 * 
     * @param array in IArray type
     * @param axis in IArray type
     * @param dimension integer value
     * @param power integer value
	 * @return a double value
	 * @throws ShapeNotMatchException
	 */
	public static double powerSum(final IArray array, final IArray axis,
			final int dimension, final int power)
	throws ShapeNotMatchException {
		return array.getArrayMath().powerSum(axis, dimension, power);
	}

	/**
	 * Find the maximum value of the array.
	 * 
     * @param array in IArray type
     * @return a double value 
	 */
	public static double getMaximum(final IArray array) {
		return array.getArrayMath().getMaximum();
	}

	/**
	 * Find the maximum value of the array.
	 * 
     * @param array in IArray type
     * @return a double value 
	 */
	public static double getMinimum(final IArray array) {
		return array.getArrayMath().getMinimum();
	}

	/**
	 * Calculate the sum value of the array.
	 * 
     * @param array in IArray type
     * @return a double value 
	 */
	public static double sum(final IArray array) {
		return array.getArrayMath().sum();
	}

	/**
	 * Do sum calculation for every slice of the array on a dimension. The
	 * result will be a one dimensional IArray.
	 * 
     * @param array in IArray type
     * @param dimension integer value
     * @param isVariance true if the array serves as variance
	 * @return IArray with new storage
	 * @throws ShapeNotMatchException
	 */
	public static IArray sumForDimension(final IArray array,
			final int dimension, final boolean isVariance)
			throws ShapeNotMatchException {
		return array.getArrayMath().sumForDimension(dimension, isVariance).getArray();
	}

	/**
	 * Get the L2 norm of the IArray. The array must have only one dimension.
	 * 
     * @param array in IArray type
     * @return a double value 
	 */
	public static double getNorm(final IArray array) {
		return array.getArrayMath().getNorm();
	}

	/**
	 * Normalise the vector to norm = 1.
	 * 
     * @param array in IArray type
     * @return IArray with new storage 
	 */
	public static IArray normalise(final IArray array) {
		return array.getArrayMath().normalise().getArray();
	}

	/**
	 * Integrate on given dimension. The result array will be one dimensional
	 * reduced from the given array.
	 * 
     * @param dimension integer value
     * @param array IArray object
     * @param isVariance true if the array serves as variance
	 * @return new IArray object
	 * @throws ShapeNotMatchException
	 */
	public static IArray integrateDimension(final IArray array,
			final int dimension, final boolean isVariance)
			throws ShapeNotMatchException {
		return array.getArrayUtils().integrateDimension(dimension, isVariance).getArray();
	}

	/**
	 * Element-wise divide the values of one array by another. The result will
     * be saved in a new IArray object.
	 * 
     * @param array1 IArray object
     * @param array2 IArray object
	 * @return new IArray object
	 * @throws ShapeNotMatchException
	 *             shape not match
	 */
	public static IArray toEltDivide(final IArray array1, final IArray array2)
			throws ShapeNotMatchException {
		return array1.getArrayMath().toEltDivide(array2).getArray();
	}

	/**
	 * Element-wise divide the values of one array by another. The result will
     * be saved in a new IArray object.
	 * 
     * @param array1 IArray object
     * @param array2 IArray object
	 * @return new IArray object
	 * @throws ShapeNotMatchException
	 *             shape not match
	 */
	public static IArray eltDivide(final IArray array1, final IArray array2)
			throws ShapeNotMatchException {
		return array1.getArrayMath().toEltDivide(array2).getArray();
	}

}
