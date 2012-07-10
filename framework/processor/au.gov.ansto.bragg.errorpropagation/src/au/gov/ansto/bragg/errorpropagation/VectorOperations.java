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

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IIndex;

/**
 * Static functions for performing arithmetic operations on one-dimensional Arrays
 * and propagating variances.  Not all simple arithmetic operations have been implemented
 * 
 * @author jrh
 *
 */
public class VectorOperations {
	
	private static VectorOperations INSTANCE = null;
	
	public static VectorOperations getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new VectorOperations();
		}
		return INSTANCE;
	}

	/**
	 * Multiply two vectors element-wise, and propagate the associated measurement
	 * errors. Both arguments must have identical shapes.
	 * 
	 * @param array1WithError A 2-element array structured as [1D data, 1D error]
	 * @param array2WithError As for array1WithError
	 * @return A 2-element array [resultant data, resultant variance]
	 */
	public IArray[] multiply(IArray[] array1WithError, IArray[] array2WithError) {
		// Check that all arrays and associated error matrices have
		// the same shape.
		// compareShapes(array1WithError, array2WithError);
		
		IArray array1Data = array1WithError[0];
		IArray array1Error = array1WithError[1];
		IArray array2Data = array2WithError[0];
		IArray array2Error = array2WithError[1];
		
		Class<?> elementType = array1Data.getElementType();
		int [] shape = array1Data.getShape();
		IArray resultantData = Factory.createArray(elementType, shape);
		IArray resultantError = Factory.createArray(elementType, shape);
		
		IIndex data1Index = array1Data.getIndex();
		IIndex error1Index = array1Error.getIndex();
		IIndex data2Index = array2Data.getIndex();
		IIndex error2Index = array2Error.getIndex();
		IIndex dataResultIndex = resultantData.getIndex();
		IIndex errorResultIndex = resultantError.getIndex();
		for (int i = 0; i < shape[0]; i++) {
				
				data1Index.set(i);
				error1Index.set(i);
				data2Index.set(i);
				error2Index.set(i);
				dataResultIndex.set(i);
				errorResultIndex.set(i);
				
				ScalarWithVariance value1 = new ScalarWithVariance(array1Data.getDouble(data1Index), array1Error.getDouble(error1Index));
				ScalarWithVariance value2 = new ScalarWithVariance(array2Data.getDouble(data2Index), array2Error.getDouble(error2Index));
				ScalarWithVariance result = ScalarOperations.multiply(value1, value2);
				
				resultantData.setDouble(dataResultIndex, result.getData());
				resultantError.setDouble(errorResultIndex, result.getVariance());
			}
		
		IArray[] result = {resultantData, resultantError};
		return result;
	}

	/**
	 * Calculate a vector of upper bounds for data values given a vector of values with errors.
	 * 
	 * @param values An array of n x [data value, sigma value]
	 * @return A vector obtained by adding the sigma value to the data value
	 */
	public double [] getUpperEnvelopeBound(double [][] values) {
		// Shape of array should be length x 2.
		// Verify this.
		if (values[0] == null || values[0].length != 2) {
			throw new IllegalArgumentException("Expected n x 2 data, n > 0");
		}
		
		int length = values.length; 
		double [] result = new double[length];
		for (int i = 0; i < length; i++) {
			result[i] = values[i][0] + values[i][1];
		}
		return result;
	}
	
	/**
	 * Calculate a vector of lower bounds for data values given a vector of values with errors.
	 * 
	 * @param values An array of n x [data value, sigma value]
	 * @return A vector obtained by subtracting the sigma value from the data value
	 */

	public double [] getLowerEnvelopeBound(double [][] values) {
		// Shape of array should be length x 2.
		// Verify this.
		if (values[0] == null || values[0].length != 2) {
			throw new IllegalArgumentException("Expected n x 2 data, n > 0");
		}
		int length = values.length; 
		double [] result = new double[length];
		for (int i = 0; i < length; i++) {
			result[i] = values[i][0] - values[i][1];
		}
		return result;
	}
}
