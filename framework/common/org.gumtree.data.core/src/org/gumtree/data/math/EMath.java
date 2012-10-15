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
import org.gumtree.data.utils.IArrayUtils;

/**
 * EMath is the Error Propagation Math Library. It acts upon GMD IArray
 * structures for data and associated variance, and carries the uncertainty
 * through IArray calculations. This library operates under the presumption of
 * independent random variables, that is, all covariance are zero.
 * <p>
 * The return types of the math calculation are mostly in EData type with result
 * and variance.
 * 
 * @see EData  (fixed 12/2008)
 */
public final class EMath {

	/**
	 * Value of log10(e).
	 */
	private static final double LOG10E = Math.log10(Math.E);
	/**
	 * Value of (log10(e))^2.
	 */
	private static final double LOG10E_SQ = Math.pow(LOG10E, 2.0);

	/**
	 * 4.
	 */
	private static final double POWER_4 = 4.0;
	/**
	 * 0.25.
	 */
	private static final double QUARTER = 0.25;

	/**
	 * Hide default constructor.
	 */
	private EMath() {
	}

	/**
	 * IArray adding with uncertainty. The rank of array1 must be greater than
	 * or equal to that of array2.
	 * 
     * @param arrA in IArray type
     * @param arrB in IArray type
     * @param varA in IArray type
     * @param varB in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> add(final IArray arrA, final IArray arrB,
			final IArray varA, final IArray varB) 
			throws ShapeNotMatchException {
		IArray result = GMath.add(arrA, arrB);
		IArray variance = null;
		if ((null != varA) && (null != varB)) {
			variance = GMath.add(varA, varB);
		} else {
			if (null == varA) {
				variance = varB.copy();
			} else {
				variance = varA;
			}
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Add a value with variance to an IArray element-wisely.
	 * 
     * @param arrA in IArray type
     * @param valB a double value
     * @param varA in IArray type
     * @param varB a double value
     * @return EData with IArray type 
	 */
	public static EData<IArray> add(final IArray arrA, final double valB,
			final IArray varA, final double varB) {
		IArray result = GMath.add(arrA, valB);
		IArray variance = null;
		if (varA != null) {
			if (varB == 0) {
				variance = varA;
			} else {
				variance = GMath.add(varA, varB);
			}
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Do element-wise multiply on two IArray objects. The rank of array1 must
	 * be greater than or equal to that of array2. Rij = Aij * Bij.
	 * 
     * @param arrA in IArray type
     * @param arrB in IArray type
     * @param varA in IArray type
     * @param varB in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> eltMultiply(final IArray arrA,
			final IArray arrB, final IArray varA, final IArray varB)
			throws ShapeNotMatchException {
		/**
		 * TODO: Check. Currently, if one variance parameter missing, that one
		 * treated as zero. if both missing, null propagated
		 */
        IArrayMath arrMath = arrA.getArrayMath();
		IArray arrASquare = null;
		if (varB != null) {
			arrASquare = arrMath.toPower(2).getArray();
		}
		IArray result = arrMath.eltMultiply(arrB).getArray();
		IArray variance = null;
		if (varA != null && varB != null) {
            IArrayMath vMath1 = varA.getArrayMath();
            IArrayMath vMath2 = arrASquare.getArrayMath();
			variance = vMath1.eltMultiply(arrB).eltMultiply(arrB).add(
                           vMath2.eltMultiply(varB)
                       ).getArray();
		} else if (varA != null) {
			variance = varA.getArrayMath().eltMultiply(arrB).eltMultiply(arrB).getArray();
		} else if (varB != null) {
			variance = arrASquare.getArrayMath().eltMultiply(varB).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Multiply two double values with error propagation.
	 * 
     * @param valX double value
     * @param valY double value
     * @param varX double value
     * @param varY double value
	 * @return EData data wrapper
	 */
	public static EData<Double> scalarMultiply(final double valX,
			final double valY, final double varX, final double varY) {
		double result = valX * valY;
		double variance = valX * valX * varY + valY * valY * varX;
		return new EData<Double>(result, variance);
	}

	/**
	 * Add two double values with error propagation.
	 * 
     * @param valX double value
     * @param valY double value
     * @param varX double value
     * @param varY double value
	 * @return EData data wrapper
	 */
	public static EData<Double> scalarAdd(final double valX, final double valY,
			final double varX, final double varY) {
		double result = valX + valY;
		double variance = varX + varY;
		return new EData<Double>(result, variance);
	}

	/**
	 * Divide a double value with a divisor with error propagation.
	 * 
     * @param valX double value
     * @param valY double value
     * @param varX double value
     * @param varY double value
	 * @return EData data wrapper
	 */
	public static EData<Double> scalarDivide(final double valX,
			final double valY, final double varX, final double varY) {
		double result = valX / valY;
		/*
		 * If covariance = 0, then
		 * 
		 * var(X/Y) = (X/Y)^2 * [var(X)/X^2 + var(Y)/Y^2]
		 * 
		 * = 1/Y^2 * [var(X) + var(Y) * (X/Y)^2]
		 */
		double variance = (varX + (varY * result * result)) / (valY * valY);
		return new EData<Double>(result, variance);
	}

	/**
	 * Scale the array with a double value element-wisely.
	 * 
     * @param arrA in IArray type
     * @param valB double value
     * @param varA in IArray type
     * @param varB a double value
	 * @return EData type for IArray
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> scale(final IArray arrA, final double valB,
			final IArray varA, final double varB)
			throws ShapeNotMatchException {
		IArray arrASquare = null;
		if (varA != null && varB != 0) {
			arrASquare = arrA.getArrayMath().toEltMultiply(arrA).getArray();
		}
		IArray result = arrA.getArrayMath().scale(valB).getArray();
		IArray variance = null;
		/*
		 * If covariance = 0, then
		 * 
		 * var(A.B) = (A.B)^2 * [var(A)/A^2 + var(B)/B^2]
		 * 
		 * = var(A)*B^2 + var(B)*A^2
		 */
		// Deprecate:
		// double newVariance = varB / valB / valB;
		// if (varA != null)
		// variance = varA.eltMultiply(arrA.eltInverseSkipZero().power(2)).
		// add(newVariance).eltMultiply(result.power(2));
		if (null != varA) {
			if (varB == 0) {
				variance = varA.getArrayMath().scale(valB * valB).getArray();
			} else {
				variance = varA.getArrayMath().scale(valB * valB).add(
                                arrASquare.getArrayMath().scale(varB)
                           ).getArray();
			}
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Do a vector dot calculation for two IArray objects.
	 * 
     * @param arrA in IArray type
     * @param arrB in IArray type
     * @param varA in IArray type
     * @param varB in IArray type
	 * @return EData with Double type
	 * @throws ShapeNotMatchException
	 */
	public static EData<Double> vecDot(final IArray arrA, final IArray arrB,
			final IArray varA, final IArray varB)
			throws ShapeNotMatchException {
		double result = arrA.getArrayMath().vecDot(arrB);
		double rVariance = 0;
		if (varA != null && varB != null) {
            IArrayMath vAMath = varA.getArrayMath();
            IArrayMath vBMath = varB.getArrayMath();
            IArrayMath aAMath = arrA.getArrayMath();
            IArrayMath aBMath = arrB.getArrayMath();
            
            rVariance = vAMath.toEltMultiply(aAMath.toEltInverseSkipZero().power(2)).add(
                            vBMath.toEltMultiply(aBMath.toEltInverseSkipZero().power(2))
                        ).eltMultiply(
                            aAMath.toEltMultiply(aBMath)).power(2).sum();
        }
		return new EData<Double>(result, rVariance);
	}

	/**
	 * Calculate an element-wise reciprocal on each IArray element Rij.. = 1 /
	 * Aij.. A zero value in the array will throw a DivideByZeroException.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type
	 * @throws DivideByZeroException
	 */
	public static EData<IArray> eltInverse(final IArray arrA, final IArray varA)
			throws DivideByZeroException, ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray result = arrMath.eltInverse().getArray();
		IArray variance = null;
		if (null != varA) {
			variance = varA.getArrayMath().eltMultiply(result.getArrayMath().toPower(POWER_4)).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate element-wise reciprocals for each IArray element except skip
	 * zero values. If Aij.. = 0, Rij.. = 0 Otherwise Rij.. = 1 / Aij..
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> eltInverseSkipZero(final IArray arrA,
			final IArray varA) throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray result = arrMath.eltInverseSkipZero().getArray();
		// [ANSTO][Tony] Dead logic
		IArray variance = null;
//		if (null != variance) {
		if (null != varA) {
			variance = varA.getArrayMath().eltMultiply(result.getArrayMath().toPower(POWER_4)).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Multiply two matrices (2D-IArray) The two matrices must abide by matrix
	 * multiplication dimension requirements. X * @param array1 in IArray type
	 * 
     * @param array2 in IArray type
     * @param variance1 in IArray type
     * @param variance2 in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 *             incomplete
	 */
	public static EData<IArray> matMultiply(final IArray array1,
			final IArray array2, final IArray variance1, final IArray variance2)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array1.getArrayMath();
		IArray result = arrMath.matMultiply(array2).getArray();
		IArray rVariance = null;
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * @deprecated not implemented yet
     * @param array IArray object
     * @param variance IArray object
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 *             incomplete
	 */
	public static EData<IArray> matInverse(final IArray array,
			final IArray variance) throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.matInverse().getArray();
		IArray rVariance = null;
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate the square root of each element of an IArray. Create a new
	 * object with the result of the calculation.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> sqrt(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray arrAInverse = null;
		if (varA != null) {
            arrMath.eltInverseSkipZero();
		}
		IArray result = arrMath.sqrt().getArray();
		IArray variance = null;
		if (null != varA) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(arrAInverse).scale(QUARTER).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate the square root of a double value with error propagation.
	 * 
     * @param value double value
     * @param variance double value
	 * @return EData wrapper
	 */
	public static EData<Double> sqrt(final double value, 
			final double variance) {
		// double result = Math.sqrt(value);
		// double rVariance = variance * 0.25 * result * result / value / value;
		// Simplified:
		return new EData<Double>(Math.sqrt(value), QUARTER * variance / value);
	}

	/**
	 * Calculate the e raised to the power of double values in the IArray
	 * element-wisely.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> exp(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray result = arrMath.exp().getArray();
		IArray variance = null;
		if (varA != null) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(result).eltMultiply(result).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate the exponential of a double value with error propagation.
	 * 
     * @param valA double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> exp(final double valA, final double varA) {
		double result = Math.exp(valA);
		double variance = varA * result * result;
		return new EData<Double>(result, variance);
	}

	/**
	 * Calculate an element-wise natural logarithm (base e) of values of an
	 * IArray.
	 * 
     * @param arrA in IArray
     * @param varA in IArray
	 * @return EData of IArray
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> ln(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray arrASquareInverse = null;
		if (varA != null) {
			arrASquareInverse = arrMath.toPower(0 - 2).getArray();
		}
		IArray result = arrMath.ln().getArray();
		IArray variance = null;
		if (varA != null) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(arrASquareInverse).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate logarithm of a double value with a e base.
	 * 
     * @param valA double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> ln(final double valA, final double varA) {
		double result = Math.log(valA);
		double variance = varA / valA / valA;
		return new EData<Double>(result, variance);
	}

	/**
	 * Calculate an element-wise logarithm (base 10) of values of an IArray.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> log10(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
		/*
		 * Change of base method: log10(A) = ln(A) * log10(e)
		 * 
		 * Constant: LOG10E_SQ = (log10(e))^2
		 */
        IArrayMath arrMath = arrA.getArrayMath();
		IArray arrASquareInverse = null;
		if (varA != null) {
			arrASquareInverse = arrMath.toPower(0 - 2).getArray();
		}
		IArray result = arrMath.log10().getArray();
		IArray variance = null;
		if (varA != null) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(arrASquareInverse).scale(LOG10E_SQ).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate logarithm of a double value with a 10 base.
	 * 
     * @param valA double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> log10(final double valA, final double varA) {
		/*
		 * Change of base method: log10(A) = ln(A) * log10(e)
		 * 
		 * Constant: LOG10E_SQ = (log10(e))^2
		 */
		double result = Math.log10(valA);
		double variance = LOG10E_SQ * varA / valA / valA;
		return new EData<Double>(result, variance);
	}

	/**
	 * Calculate the sine value of each element in the IArray.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> sin(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray cosA = null;
		if (varA != null) {
			cosA = arrMath.toCos().getArray();
		}
		IArray result = arrMath.sin().getArray();
		IArray variance = null;
		if (varA != null) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(cosA).eltMultiply(cosA).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate sine of a double value with error propagation.
	 * 
     * @param valA double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> sin(final double valA, final double varA) {
		double result = Math.sin(valA);
		double variance = varA * Math.pow(Math.cos(valA), 2.0);
		return new EData<Double>(result, variance);
	}

	/**
	 * Calculate the arc sine value of each element in the IArray.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> asin(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath(), arrPow;
		IArray arrAPower = null;
		if (varA != null) {
			arrAPower = arrMath.toPower(2).getArray();
		}
		IArray result = arrMath.asin().getArray();
		IArray variance = null;
		if (null != varA) {
            arrPow   = arrAPower.getArrayMath();
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(arrPow.scale(-1).add(1).eltInverseSkipZero()).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate arcsine of a double value with error propagation.
	 * 
     * @param valA double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> asin(final double valA, final double varA) {
		double result = Math.asin(valA);
		double variance = varA / (1.0 - valA * valA);
		return new EData<Double>(result, variance);
	}

	/**
	 * Calculate the cosine value of each element in the IArray.
	 * 
     * @param arrA IArray object
     * @param varA IArray object
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
     *             shape not match 
	 */
	public static EData<IArray> cos(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
        IArray sinA = null;
		if (varA != null) {
			sinA = arrMath.toSin().getArray();
		}
		IArray result = arrMath.cos().getArray();
		IArray variance = null;
		if (null != varA) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(sinA).eltMultiply(sinA).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate cosine of a double value with error propagation.
	 * 
     * @param valA double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> cos(final double valA, final double varA) {
		double result = Math.cos(valA);
		double variance = varA * Math.pow(Math.sin(valA), 2.0);
		return new EData<Double>(result, variance);
	}

	/**
	 * Calculate the arc cosine value of each element in the IArray.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> acos(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath(), arrPow;
		IArray arrAPower = null;
		if (varA != null) {
			arrAPower = arrMath.toPower(2).getArray();
		}
		IArray result = arrMath.acos().getArray();
		IArray variance = null;
		if (null != varA) {
            arrPow   = arrAPower.getArrayMath();
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(arrPow.scale(-1).add(1).eltInverseSkipZero()).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate arc cosine of a double value with error propagation.
	 * 
     * @param valA double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> acos(final double valA, final double varA) {
		double result = Math.acos(valA);
		double variance = varA / (1.0 - valA * valA);
		return new EData<Double>(result, variance);
	}

	/**
	 * Calculate the trigonometric value of each elements in the IArray.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type object
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> tan(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray cosA = null;
		if (varA != null) {
			cosA = arrMath.toCos().getArray();
		}
		IArray result = arrMath.tan().getArray();
		IArray variance = null;
		if (varA != null) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(cosA.getArrayMath().power(-POWER_4)).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate tangent of a double value with error propagation.
	 * 
     * @param valA double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> tan(final double valA, final double varA) {
		double result = Math.tan(valA);
		double variance = varA / Math.pow(Math.cos(valA), POWER_4);
		return new EData<Double>(result, variance);
	}

	/**
	 * Calculate the arc trigonometric value of each elements in the IArray.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
	 * @return EData with IArray type object
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> atan(final IArray arrA, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath(), arrPow;
		IArray arrAPower = null;
		if (varA != null) {
			arrAPower = arrMath.toPower(2).getArray();
		}
		IArray result = arrMath.atan().getArray();
		IArray variance = null;
		if (varA != null) {
            arrPow   = arrAPower.getArrayMath();
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(arrPow.add(1).power(0 - 2)).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate arc trigonometric of a double value with error propagation.
	 * 
     * @param valA double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> atan(final double valA, final double varA) {
		double result = Math.atan(valA);
		double variance = varA / Math.pow((valA * valA + 1.0), 2.0);
		return new EData<Double>(result, variance);
	}

	/**
	 * Do an element-wise power calculation of the array. Yij = Xij ^ power.
	 * 
     * @param arrA in IArray type
     * @param power a double value
     * @param varA in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> power(final IArray arrA, final double power,
			final IArray varA) throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray arrAPower = null;
		if (varA != null) {
			arrAPower = arrMath.toPower(2 * power - 2).getArray();
		}
		IArray result = arrMath.power(power).getArray();
		IArray variance = null;
		if (varA != null) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.eltMultiply(arrAPower).scale(power * power).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Calculate the power of a double value with error propagation.
	 * 
     * @param valA double value
     * @param power double value
     * @param varA double value
	 * @return EData wrapper
	 */
	public static EData<Double> power(final double valA, final double power,
			final double varA) {
		double result = Math.pow(valA, power);
		double b = Math.abs(power);
		double rVariance = varA * b * b * Math.pow(valA, 2.0 * b - 2.0);
		return new EData<Double>(result, rVariance);
	}

	/**
	 * Do a power-sum on a certain dimension. A power-sum will raise all element
	 * of the array to a certain power, then do a sum on a certain dimension,
	 * and put weight on the result.
	 * 
     * @param arrA in IArray type
     * @param axis weight on the dimension
     * @param dimn the index of the dimension
     * @param power a double value
     * @param varA in IArray type
     * @param varAxis variance of the weight
	 * @return EData with Double type
	 * @throws ShapeNotMatchException
	 */
	public static EData<Double> powerSum(final IArray arrA, final IArray axis,
			final int dimn, final double power, final IArray varA,
			final IArray varAxis) throws ShapeNotMatchException {
		EData<IArray> result = power(arrA, power, varA);
		result = sumForDimension(result.getData(), dimn, result.getVariance());
		result = eltMultiply(result.getData(), axis, result.getVariance(),
				varAxis);
		return sum(result.getData(), result.getVariance());
	}

	/**
	 * Calculate the sum on the IArray.
	 * 
     * @param arrA in IArray type
     * @param varA in IArray type
     * @return EData with IArray type 
	 */
	public static EData<Double> sum(final IArray arrA, final IArray varA) {
        IArrayMath arrMath = arrA.getArrayMath();
		double result = arrMath.sum();
		double variance = 0;
		if (varA != null) {
			variance = arrMath.sum();
		}
		return new EData<Double>(result, variance);
	}

	/**
	 * Do sum calculation for every slice of the array on a dimension. The
	 * result will be a one dimensional IArray.
	 * 
     * @param arrA in IArray type
     * @param dimension Integer value
     * @param varA in IArray type
	 * @return EData with IArray
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> sumForDimension(final IArray arrA,
			final int dimension, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray result = arrMath.sumForDimension(dimension, false).getArray();
		IArray variance = null;
		if (varA != null) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.sumForDimension(dimension, true).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Do sum calculation for every slice of the array on a dimension. The
	 * result will be a one dimensional IArray.
	 * 
     * @param arrA in IArray type
     * @param dimension Integer value
     * @param varA in IArray type
	 * @return EData with IArray
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> enclosedSumForDimension(final IArray arrA,
			final int dimension, final IArray varA)
			throws ShapeNotMatchException {
        IArrayMath arrMath = arrA.getArrayMath();
		IArray result = arrMath.enclosedSumForDimension(dimension, false).getArray();
		IArray variance = null;
		if (varA != null) {
            arrMath  = varA.getArrayMath();
			variance = arrMath.enclosedSumForDimension(dimension, true).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * Integrate on given dimension. The result array will be one dimensional
	 * reduced from the given array.
	 * 
     * @param arrA the signal in CDMA IArray type
     * @param dimension a primary integer
     * @param varA the variance in CDMA IArray type
	 * @return EData in IArray
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> integrateDimension(final IArray arrA,
			final int dimension, final IArray varA)
			throws ShapeNotMatchException {
        IArrayUtils arrUtils = arrA.getArrayUtils();
		IArray result = arrUtils.integrateDimension(dimension, false).getArray();
		IArray variance = null;
		if (varA != null) {
            arrUtils  = varA.getArrayUtils();
			variance = arrUtils.integrateDimension(dimension, true).getArray();
		}
		return new EData<IArray>(result, variance);
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
        IArrayUtils arrUtils = array.getArrayUtils();
        return arrUtils.integrateDimension(dimension, isVariance).getArray();
    }
    
	/**
	 * Integrate on given dimension. The result array will be one dimensional
	 * reduced from the given array.
	 * 
     * @param arrA the signal in CDMA IArray type
     * @param dimension a primary integer
     * @param varA the variance in CDMA IArray type
	 * @return EData in IArray
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> enclosedIntegrateDimension(final IArray arrA,
			final int dimension, final IArray varA)
			throws ShapeNotMatchException {
        IArrayUtils arrUtils = arrA.getArrayUtils();
		IArray result = arrUtils.enclosedIntegrateDimension(dimension, false).getArray();
		IArray variance = null;
		if (varA != null) {
            arrUtils = varA.getArrayUtils();
			variance = arrUtils.enclosedIntegrateDimension(dimension, true).getArray();
		}
		return new EData<IArray>(result, variance);
	}

	/**
	 * IArray adding with uncertainty. The rank of array1 must be greater than
	 * or equal to that of array2.
	 * 
     * @param array1 in IArray type
     * @param array2 in IArray type
     * @param variance1 in IArray type
     * @param variance2 in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toAdd(final IArray array1, final IArray array2,
			final IArray variance1, final IArray variance2)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array1.getArrayMath();
		IArray result = arrMath.toAdd(array2).getArray();
		IArray rVariance = null;
		if (variance1 != null && variance2 != null) {
            arrMath   = variance1.getArrayMath();
			rVariance = arrMath.toAdd(variance2).getArray();
		} else if (variance1 != null) {
			rVariance = variance1.copy();
		} else if (variance2 != null) {
			rVariance = variance2.copy();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Add a value with variance to an IArray element-wisely. Note here the two
	 * arrays are not necessary to have the same size. It will finish at the
	 * short length.
	 * 
     * @param array in IArray type
     * @param value a double value
     * @param variance in IArray type
     * @param valueVariance a double value
     * @return EData with IArray type 
	 */
	public static EData<IArray> toAdd(final IArray array, final double value,
			final IArray variance, final double valueVariance) {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toAdd(value).getArray();
		IArray rVariance = null;
		if (variance != null) {
			if (valueVariance == 0) {
				rVariance = variance.copy();
			} else {
                arrMath   = variance.getArrayMath();
				rVariance = arrMath.toAdd(valueVariance).getArray();
			}
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Do element-wise multiply on two IArray objects. Xij = Aij * Bij. The rank
	 * of array1 must be greater than or equal to that of array2.
	 * 
     * @param array1 in IArray type
     * @param array2 in IArray type
     * @param variance1 in IArray type
     * @param variance2 in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toEltMultiply(final IArray array1,
			final IArray array2, final IArray variance1, final IArray variance2)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array1.getArrayMath();
		IArray result = arrMath.toEltMultiply(array2).getArray();
		IArray rVariance = null;
		if (variance1 != null && variance2 != null) {
			rVariance = arrMath.toPower(2).eltMultiply(variance2).add(
                            variance1.getArrayMath().toEltMultiply(array2.getArrayMath().toPower(2))
                        ).getArray();
		} else if (variance1 != null) {
            arrMath   = variance1.getArrayMath();
			rVariance = arrMath.toEltMultiply(array2.getArrayMath().toPower(2)).getArray();
		} else if (variance2 != null) {
			rVariance = arrMath.toPower(2).eltMultiply(variance2).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Scale the array with a double value element-wisely.
	 * 
     * @param array in IArray type
     * @param value double value
     * @param variance in IArray type
     * @param valueVariance a double value
	 * @return EData type for IArray
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toScale(final IArray array, final double value,
			final IArray variance, final double valueVariance)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toScale(value).getArray();
		IArray rVariance = null;
		if (variance != null) {
            arrMath = variance.getArrayMath();
			if (valueVariance == 0) {
				rVariance = arrMath.toScale(value * value).getArray();
			} else {
				rVariance = arrMath.toScale(value * value).add(
                                array.getArrayMath().toPower(2).scale(valueVariance)
                            ).getArray();
			}
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Do an element-wise inverse calculation on an IArray object. Yij = 1 /
	 * Xij. If there is zero values in the array, it will throw
	 * DivideByZeroException.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws DivideByZeroException
	 *             divide by zero
	 * @throws ShapeNotMatchException
     *             shape not match 
	 */
	public static EData<IArray> toEltInverse(final IArray array,
			final IArray variance) throws DivideByZeroException,
			ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toEltInverse().getArray();
		IArray rVariance = null;
		if (variance != null) {
            arrMath   = result.getArrayMath();
			rVariance = arrMath.toPower(POWER_4).eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Do a element-wise inverse calculation that skip zero values. For example,
	 * Yij = 1 / Xij.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toEltInverseSkipZero(final IArray array,
			final IArray variance) throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toEltInverseSkipZero().getArray();
		IArray rVariance = null;
		if (variance != null) {
            arrMath   = result.getArrayMath();
			rVariance = arrMath.toPower(POWER_4).eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate the square root values of each element of an IArray. Create a
	 * new plot object with the result of the calculation.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toSqrt(final IArray array, 
			final IArray variance)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toSqrt().getArray();
		IArray rVariance = null;
		if (variance != null) {
			rVariance = arrMath.toScale(POWER_4).toEltInverseSkipZero().eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate the e raised to the power of double values in the IArray
	 * element-wisely.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toExp(final IArray array, final IArray variance)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toExp().getArray();
		IArray rVariance = null;
		if (variance != null) {
            arrMath   = variance.getArrayMath();
			rVariance = arrMath.toEltMultiply(result).eltMultiply(result).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate an element-wise natural logarithm (base e) of values of an
	 * IArray.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toLn(final IArray array, final IArray variance)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toLn().getArray();
		IArray rVariance = null;
		if (variance != null) {
			rVariance = arrMath.toPower(0 - 2.0).eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate an element-wise logarithm (base 10) of values of an IArray.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toLog10(final IArray array,
			final IArray variance) throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toLog10().getArray();
		IArray rVariance = null;
		if (variance != null) {
			rVariance = arrMath.toPower(0 - 2.0).scale(LOG10E_SQ).eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate the sine value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toSin(final IArray array, final IArray variance)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toSin().getArray();
		IArray rVariance = null;
		if (variance != null) {
            IArray cosA;
			cosA      = arrMath.toCos().getArray();
            arrMath   = cosA.getArrayMath();
			rVariance = arrMath.eltMultiply(cosA).eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate the arc sine value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toAsin(final IArray array, 
			final IArray variance)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toAsin().getArray();
		IArray rVariance = null;
		if (variance != null) {
			rVariance = arrMath.toPower(2).scale(-1).add(1).eltInverseSkipZero().eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate the cosine value of each elements in the IArray.
	 * 
     * @param array IArray object
     * @param variance IArray object
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toCos(final IArray array, final IArray variance)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toCos().getArray();
		IArray rVariance = null;
		if (variance != null) {
			IArray sinA;
            sinA      = arrMath.toSin().getArray();
            arrMath   = sinA.getArrayMath();
			rVariance = arrMath.eltMultiply(sinA).eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate the arc cosine value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toAcos(final IArray array, 
			final IArray variance) throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
        IArray result = arrMath.toAcos().getArray();
		IArray rVariance = null;
		if (variance != null) {
			rVariance = arrMath.toPower(2).scale(-1).add(1).toEltInverseSkipZero().eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate the trigonometric value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type object
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toTan(final IArray array, final IArray variance)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toTan().getArray();
		IArray rVariance = null;
		if (variance != null) {
			rVariance = arrMath.toCos().power(-POWER_4).eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Calculate the arc trigonometric value of each elements in the IArray.
	 * 
     * @param array in IArray type
     * @param variance in IArray type
	 * @return EData with IArray type object
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toAtan(final IArray array,
			final IArray variance) throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
		IArray result = arrMath.toAtan().getArray();
		IArray rVariance = null;
		if (variance != null) {
			rVariance = arrMath.toPower(2).add(1).power(0 - 2.0).eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Do an element-wise power calculation of the array. Yij = Xij ^ power.
	 * 
     * @param array in IArray type
     * @param power a double value
     * @param variance in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toPower(final IArray array, final double power,
			final IArray variance) throws ShapeNotMatchException {
        IArrayMath arrMath = array.getArrayMath();
        IArray result = arrMath.toPower(power).getArray();
		IArray rVariance = null;
		if (variance != null) {
			rVariance = arrMath.toPower(2 * power - 2).scale(power * power).eltMultiply(variance).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}

	/**
	 * Do element-wise divide on two IArray objects. Xij = Aij / Bij. If the
	 * divisor is 0, skip that element. The rank of array1 must be greater than
	 * or equal to that of array2.
	 * 
     * @param array1 in IArray type
     * @param array2 in IArray type
     * @param variance1 in IArray type
     * @param variance2 in IArray type
	 * @return EData with IArray type
	 * @throws ShapeNotMatchException
	 */
	public static EData<IArray> toEltDivideSkipZero(final IArray array1,
			final IArray array2, final IArray variance1, final IArray variance2)
			throws ShapeNotMatchException {
        IArrayMath arrMath = array1.getArrayMath();
		IArray result = arrMath.toEltDivide(array2).getArray();
		IArray rVariance = null;
		if (variance1 != null && variance2 != null) {
            IArrayMath varMath, resMath;
            varMath   = variance1.getArrayMath();
            resMath   = result.getArrayMath();
            arrMath   = array2.getArrayMath();
            rVariance = varMath.toAdd(
                            resMath.toPower(2).eltMultiply(variance2)
                        ).eltDivide(
                                arrMath.toPower(2)
                        ).getArray();
		} else if (variance1 != null) {
            IArrayMath varMath;
            varMath   = variance1.getArrayMath();
            arrMath   = array2.getArrayMath();
			rVariance = varMath.toEltDivide(arrMath.toPower(2)).getArray();
		} else if (variance2 != null) {
            IArrayMath resMath;
            resMath   = result.getArrayMath();
            arrMath   = array2.getArrayMath();
			rVariance = resMath.toPower(2).eltMultiply(variance2).eltDivide(arrMath.toPower(2)).getArray();
		}
		return new EData<IArray>(result, rVariance);
	}
}
