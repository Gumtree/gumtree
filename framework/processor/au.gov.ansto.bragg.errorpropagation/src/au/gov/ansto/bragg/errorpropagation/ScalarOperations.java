/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lindsay Winkler (Bragg Institute) - initial implementation
 *******************************************************************************/

package au.gov.ansto.bragg.errorpropagation;

/**
 * Collection of scalar operations along with implementation of error propagation.
 * 
 * These operations can be combined into more complex functions, simplifying the
 * management of error propagation in the functions themselves.  Note that all
 * errors are assumed to be expressed as variances (ie standard uncertainty squared)
 * 
 * @author Lindsay Winkler, James Hester
 */
public class ScalarOperations {
	
	/**
	 * Add two values with associated variances, and give the result, along
	 * with propagated error. The user of the library is responsible for
	 * decomposing a function into its component parts before calling methods
	 * to calculate values and propagate errors.
	 * <p>
	 * Parameters and result are packed into arrays of doubles to emphasise the
	 * fact that a measured number should always be associated with the
	 * measurement error.
	 * 
	 */
	public static ScalarWithVariance add(ScalarWithVariance value1, ScalarWithVariance value2) {
		double resultantData = value1.getData() + value2.getData();
		double resultantVariance = value1.getVariance() + value2.getVariance();		
		return new ScalarWithVariance(resultantData, resultantVariance);
	}
		
	/**
	 * Add multiple values and propagate measurement error. 
	 * 
	 * Note that the operation of addition is not associative because of the rounding
	 * that occurs. That is (a + b) + c != a + (b + c). The left and right hand sides
	 * may be equal, but will not necessarily be so.
	 * 
	 * In particular add(a, b, c) != add(a, add(b, c)).
	 */
	public static ScalarWithVariance add(ScalarWithVariance ...values) {
		// Add data terms, combine error terms.
		
		double resultantData = 0d;
		double resultantVariance = 0d;
		for (ScalarWithVariance value : values) {
			resultantData += value.getData();
			resultantVariance += value.getVariance();
		}
				
		return new ScalarWithVariance(resultantData, resultantVariance);
	}
		
	/**
	 * Subtract value2 from value1 and return result with propagated error. 
	 */
	public static ScalarWithVariance subtract(ScalarWithVariance value1, ScalarWithVariance value2) {
		double resultantData = value1.getData() - value2.getData();		
		double resultantVariance = value1.getVariance() + value2.getVariance();		
		return new ScalarWithVariance(resultantData,resultantVariance);
	}
		
	/**
	 * Multiply two values together and return result with propagated error. 
	 */
	public static ScalarWithVariance multiply(ScalarWithVariance value1, ScalarWithVariance value2) {
		double data1 = value1.getData();
		double data2 = value2.getData();
		
		double variance1 = value1.getVariance();
		double variance2 = value2.getVariance();
		
		double resultantData = data1 * data2;
		
		double resultantVariance = data2 * data2 * variance1 + data1 * data1 * variance2;
				
		return new ScalarWithVariance(resultantData,resultantVariance);
	}
	
	
	/**
	 * Overloaded version to allow multiplication of a value with a measurement
	 * error by an exact value.
	 */
	public static ScalarWithVariance multiply(double exactValue, ScalarWithVariance valueWithVariance) {
		ScalarWithVariance exactValueWithVariance = new ScalarWithVariance(exactValue, 0);
		return multiply(exactValueWithVariance, valueWithVariance);
	}
		
	/**
	 * Divide value1 by value2 and return result with propagated variance. 
	 */
	public static ScalarWithVariance divide(ScalarWithVariance value1, ScalarWithVariance value2) {
		double data1 = value1.getData();
		double data2 = value2.getData();
		
		double variance1 = value1.getVariance();
		double variance2 = value2.getVariance();
		
		double resultantData = data1 / data2;
		
		double data2sqr = 1.0/(data2*data2);  /* for convenience */
		/* The following equation is given by
		 * var(x/y) = 1/y^2 ( var(x) + (x/y^2)^2 var(y))
		 */
		double resultantVariance = data2sqr*(variance1 + (data1*data1*data2sqr*variance2));
		
		return new ScalarWithVariance(resultantData,resultantVariance);
	} 
	/** Calculate the reciprocal of a number and propagate error.  The calculation
	 * should return 1/x^4 * variance(x)
	 */
	public static ScalarWithVariance reciprocal(ScalarWithVariance value) {
		double resultantData = 1.0d/value.getData();
		double resultantError = Math.pow(resultantData, 4.0d)*value.getVariance();
		return new ScalarWithVariance(resultantData,resultantError);
	}
	/**
	 * Divide an exact value (a value with zero error) by a value with measurement error.
	 */
	public static ScalarWithVariance divide(double exactValue, ScalarWithVariance valueWithError) {
		ScalarWithVariance exactValueWithError = new ScalarWithVariance(exactValue, 0);
		return divide(exactValueWithError, valueWithError);
	}
		
	/**
	 * Divide a value with measurement error by an exact value
	 * 
	 * @param valueWithError A scalar with measurement error
	 * @param exactValue A value with zero associated error
	 */
	public static ScalarWithVariance divide(ScalarWithVariance valueWithError, double exactValue) {
		ScalarWithVariance exactValueWithError = new ScalarWithVariance(exactValue, 0);
		return divide (valueWithError, exactValueWithError);
	}
		
	/**
	 * Convenience method for squaring a value, and propagating the
	 * associated measurement error. Note that we cannot just simply
	 * multiply, as the value is perfectly correlated with itself and
	 * the standard approximations assuming non-correlated errors do 
	 * not apply.  But we can prove that the variance is exactly twice
	 * that of the uncorrelated case.
	 */
	public static ScalarWithVariance square(ScalarWithVariance value) {
		double resultantData = value.getData()*value.getData();
		double resultantVariance = value.getVariance()*4.0*resultantData;
		return new ScalarWithVariance(resultantData,resultantVariance);
	}
	
	/**
	 * Compute the square root of a value and propagate the associated
	 * variance 
	 */
	public static ScalarWithVariance squareRoot(ScalarWithVariance value) {
		double data = value.getData();
		double variance = value.getVariance();
		
		double resultantData = Math.sqrt(data);
		double resultantVariance = (1 / (4 * data) ) * variance;
				
		return new ScalarWithVariance(resultantData,resultantVariance);
	}
		
}
