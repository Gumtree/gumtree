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
import org.gumtree.data.impl.netcdf.NcArray;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IIndex;

import ucar.ma2.MAMath;

/**
 * Operations on two-dimensional arrays with associated error propagation.
 * <p>
 * This class manipulates two-dimensional arrays, matching the data and 
 * calibration artifacts representing detector data. One-dimensional array
 * manipulations used for manipulating spectrums are found in the
 * <CODE>VectorOperations</CODE> class. The arguments to each method are 2-element 
 * java arrays, the first element of which is the data, and the second element of 
 * which are the corresponding variances.  Note that all calculations are performed
 * in terms of variances
 * 
 * @author Lindsay Winkler, James Hester
 */
public class ArrayOperations {

	/**
	 * Add two 2-dimensional arrays element-wise, and propagate the associated measurement
	 * errors.  All input arrays must have the same shape.
	 * 
	 * @param array1WithError A 2-element array structured as [2D data, 2D variances]
	 * @param array2WithError Array of identical structure to array1WithError
	 * @return A 2-element array [resultant data, resultant variance]
	 */
	public static IArray[] add(IArray[] array1WithError, IArray[] array2WithError) {
		
		compareShapes(array1WithError,array2WithError);
		// Use MA math libraries rather than our scalar libraries
		IArray array1Data = array1WithError[0];
		IArray array1Error = array1WithError[1];
		IArray array2Data = array2WithError[0];
		IArray array2Error = array2WithError[1];

		// Use element type and shape of first array

		Class<?> elementType = array1Data.getElementType();
		int [] shape = array1Data.getShape();
		ucar.ma2.Array resultantData = ((NcArray) Factory.createArray(elementType, shape)).getArray();
		ucar.ma2.Array resultantError = ((NcArray) Factory.createArray(elementType, shape)).getArray();

		// Call MAMath add for both arrays

		try{
			resultantData = MAMath.add(((NcArray) array1Data).getArray(), ((NcArray) array2Data).getArray());
			resultantError = MAMath.add(((NcArray) array1Error).getArray(), ((NcArray) array2Error).getArray());
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		IArray [] result = {new NcArray(resultantData), new NcArray(resultantError)};
		return result;
	}

	/**
	 * Subtract two 2-dimensional arrays element-wise, and propagate the associated measurement
	 * errors.  All input arrays must have the same shape.
	 * 
	 * @param array1WithError A 2-element array structured as [2D data, 2D variances]
	 * @param array2WithError Array of identical structure to array1WithError
	 * @return A 2-element array [resultant data, resultant variance]
	 */
	public static IArray[] subtract(IArray[] array1WithError, IArray[] array2WithError) {

		Class<?> elementType = array1WithError[0].getElementType();
		int [] shape = array1WithError[0].getShape();
		IArray[] result = { Factory.createArray(elementType, shape), Factory.createArray(elementType, shape) };
        subtract(array1WithError,array2WithError,result);
		return result;
	}

	/**
	 *  Subtract two any-dimensional arrays element-wise, and propagate the associated
	 *  measurement errors.  All input arrays must have the same shape
	 *  
	 *  @param array1WithError A 2-element array [data, variance]
	 *  @param array2WithError Array to be subtracted, structure as for first parameter
	 *  @param resultWithError Result array, same structure as first two parameters
	 * 
	 * 
	 */
	
	public static void subtract(IArray[] array1WithError, IArray[] array2WithError, IArray[] resultWithError) {
		// Check that all arrays and associated error matrices have
		// the same shape.
		compareShapes(array1WithError, array2WithError);
		compareShapes(array1WithError, resultWithError);

	    IArrayIterator iterR = resultWithError[0].getIterator();
	    IArrayIterator iterA = array1WithError[0].getIterator();
	    IArrayIterator iterB = array2WithError[0].getIterator();
	    IArrayIterator iterRE = resultWithError[1].getIterator();
	    IArrayIterator iterAE = array1WithError[1].getIterator();
	    IArrayIterator iterBE = array2WithError[1].getIterator();

	    while (iterA.hasNext()) {
	    
	      iterR.next().setDoubleCurrent(iterA.getDoubleNext() - iterB.getDoubleNext());
	      iterRE.next().setDoubleCurrent(iterAE.getDoubleNext() + iterBE.getDoubleNext());    
	    }

	}
	
	/**
	 *  Subtract two any-dimensional arrays element-wise, setting all negative values to zero. propagate the associated
	 *  measurement errors.  All input arrays must have the same shape.  Note that no attempt is made to adjust the errors
	 *  for values which are calculated to be negative, although this would be most correct.
	 *  
	 *  @param array1WithError A 2-element array [data, variance]
	 *  @param array2WithError Array to be subtracted, structure as for first parameter
	 *  @param resultWithError Result array, same structure as first two parameters
	 * 
	 * 
	 */
	
	public static void subtractandzero(IArray[] array1WithError, IArray[] array2WithError, IArray[] resultWithError) {
		// Check that all arrays and associated error matrices have
		// the same shape.
		compareShapes(array1WithError, array2WithError);
		compareShapes(array1WithError, resultWithError);

	    IArrayIterator iterR = resultWithError[0].getIterator();
	    IArrayIterator iterA = array1WithError[0].getIterator();
	    IArrayIterator iterB = array2WithError[0].getIterator();
	    IArrayIterator iterRE = resultWithError[1].getIterator();
	    IArrayIterator iterAE = array1WithError[1].getIterator();
	    IArrayIterator iterBE = array2WithError[1].getIterator();

	    while (iterA.hasNext()) {
	    
	      double result = iterA.getDoubleNext() - iterB.getDoubleNext();
	      if(result<0) result=0d;
	      iterR.next().setDoubleCurrent(result);
	      iterRE.next().setDoubleCurrent(iterAE.getDoubleNext() + iterBE.getDoubleNext());    
	    }

	}
	
	/**
	 * Multiply two 2-dimensional arrays element-wise, and propagate the associated measurement
	 * errors.  All input arrays must have the same shape.
	 * 
	 * @param array1WithError A 2-element array structured as [2D data, 2D variances]
	 * @param array2WithError Array of identical structure to array1WithError
	 * @return A 2-element array [resultant data, resultant variance]
	 */

	public static IArray[] multiply(IArray[] array1WithError, IArray[] array2WithError) {

		Class<?> elementType = array1WithError[0].getElementType();
		int [] shape = array1WithError[0].getShape();
		IArray[] result = { Factory.createArray(elementType, shape), Factory.createArray(elementType, shape) };
        multiply(array1WithError,array2WithError,result);
		return result;
	}



	/**
	 *  Multiply two any-dimensional arrays element-wise, and propagate the associated
	 *  measurement errors.  All input arrays must have the same shape
	 *  
	 *  @param array1WithError A 2-element array [data, variance]
	 *  @param array2WithError Array to be multiplied, structure as for first parameter
	 *  @param resultWithError Result array, same structure as first two parameters
	 * 
	 * 
	 */
	
	public static void multiply(IArray[] array1WithError, IArray[] array2WithError, IArray[] resultWithError) {
		// Check that all arrays and associated error matrices have
		// the same shape.
		compareShapes(array1WithError, array2WithError);
		compareShapes(array1WithError, resultWithError);

	    IArrayIterator iterR = resultWithError[0].getIterator();
	    IArrayIterator iterA = array1WithError[0].getIterator();
	    IArrayIterator iterB = array2WithError[0].getIterator();
	    IArrayIterator iterRE = resultWithError[1].getIterator();
	    IArrayIterator iterAE = array1WithError[1].getIterator();
	    IArrayIterator iterBE = array2WithError[1].getIterator();

	    while (iterA.hasNext()) {
	      double firstval = iterA.getDoubleNext();
	      double secondval = iterB.getDoubleNext();
	      iterR.next().setDoubleCurrent(firstval*secondval);
	      iterRE.next().setDoubleCurrent(iterAE.getDoubleNext()*secondval*secondval + iterBE.getDoubleNext()*firstval*firstval);    
	    }

	}
	
	/**
	 * Divide array1 by array2 element-wise, and propagate the associated measurement
	 * errors.  All input arrays must have the same shape.  The second argument is the
	 * denominator in the division.
	 * 
	 * @param array1WithError A 2-element array structured as [2D data, 2D variances]
	 * @param array2WithError Array of identical structure to array1WithError
	 * @return A 2-element array [resultant data, resultant variance]
	 */

	public static IArray[] divide(IArray[] array1WithError, IArray[] array2WithError) {
		// Check that all arrays and associated error matrices have
		// the same shape.
		compareShapes(array1WithError, array2WithError);

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
			for (int j = 0; j < shape[1]; j++) {

				data1Index.set(i, j);
				error1Index.set(i, j);
				data2Index.set(i, j);
				error2Index.set(i, j);
				dataResultIndex.set(i, j);
				errorResultIndex.set(i, j);

				ScalarWithVariance value1 = new ScalarWithVariance(array1Data.getDouble(data1Index), array1Error.getDouble(error1Index));
				ScalarWithVariance value2 = new ScalarWithVariance(array2Data.getDouble(data2Index), array2Error.getDouble(error2Index));
				ScalarWithVariance result = ScalarOperations.divide(value1, value2);

				resultantData.setDouble(dataResultIndex, result.getData());
				resultantError.setDouble(errorResultIndex, result.getVariance());
			}
		}

		IArray[] result = {resultantData, resultantError};
		return result;
	}

	/**
	 * Add a scalar to all elements of an array, propagating variances
	 * 
	 * @param array1WithError A 2-element array structured as [2D data, 2D variances]
	 * @param scalar A 2-element array [scalar value, variance of scalar value]
	 * @return A 2-element array [resultant data, resultant variance]
	 */

	public static IArray[] addScalar(IArray [] arrayWithError, double [] scalar) {

		IArray dataArray = arrayWithError[0];
		IArray errorArray = arrayWithError[1];
		IIndex dataIndex = dataArray.getIndex();
		IIndex errorIndex = errorArray.getIndex();

		Class<?> elementType = dataArray.getElementType();
		int [] shape = dataArray.getShape();
		IArray resultantData = Factory.createArray(elementType, shape);
		IArray resultantError = Factory.createArray(elementType, shape);

		IIndex dataResultIndex = resultantData.getIndex();
		IIndex errorResultIndex = resultantError.getIndex();
		ScalarWithVariance swv = new ScalarWithVariance(scalar[0],scalar[1]);

		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {

				dataIndex.set(i, j);
				errorIndex.set(i, j);
				dataResultIndex.set(i, j);
				errorResultIndex.set(i, j);

				ScalarWithVariance arrayValue = new ScalarWithVariance(dataArray.getDouble(dataIndex), errorArray.getDouble(errorIndex));
				ScalarWithVariance result = ScalarOperations.add(arrayValue, swv);

				resultantData.setDouble(dataResultIndex, result.getData());
				resultantError.setDouble(errorResultIndex, result.getVariance());
			}
		}

		IArray[] result = { resultantData, resultantError };
		return result;
	}

	/**
	 * Subtract a scalar from all elements of an array, propagating variances
	 * 
	 * @param array1WithError A 2-element array structured as [2D data, 2D variances]
	 * @param scalar A 2-element array [scalar value, variance of scalar value]
	 * @return A 2-element array [resultant data, resultant variance]
	 */

	public static IArray[] subtractScalar(IArray[] arrayWithError, double [] scalar) {
		IArray dataArray = arrayWithError[0];
		IArray errorArray = arrayWithError[1];
		IIndex dataIndex = dataArray.getIndex();
		IIndex errorIndex = errorArray.getIndex();

		Class<?> elementType = dataArray.getElementType();
		int [] shape = dataArray.getShape();
		IArray resultantData = Factory.createArray(elementType, shape);
		IArray resultantError = Factory.createArray(elementType, shape);

		IIndex dataResultIndex = resultantData.getIndex();
		IIndex errorResultIndex = resultantError.getIndex();
		ScalarWithVariance swv = new ScalarWithVariance(scalar[0],scalar[1]);

		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {

				dataIndex.set(i, j);
				errorIndex.set(i, j);
				dataResultIndex.set(i, j);
				errorResultIndex.set(i, j);

				ScalarWithVariance arrayValue = new ScalarWithVariance(dataArray.getDouble(dataIndex), errorArray.getDouble(errorIndex));
				ScalarWithVariance result = ScalarOperations.subtract(arrayValue, swv);

				resultantData.setDouble(dataResultIndex, result.getData());
				resultantError.setDouble(errorResultIndex, result.getVariance());
			}
		}

		IArray[] result = {resultantData, resultantError};
		return result;
	}

	/**
	 * Multiply all elements of an array by a scalar, propagating variances
	 * 
	 * @param array1WithError A 2-element array structured as [2D data, 2D variances]
	 * @param scalar A 2-element array [scalar value, variance of scalar value]
	 * @return A 2-element array [resultant data, resultant variance]
	 */

	public static IArray[] multiplyByScalar(IArray[] arrayWithError, double [] scalar) {

		Class<?> elementType = arrayWithError[0].getElementType();
		int [] shape = arrayWithError[0].getShape();
		IArray[] result = { Factory.createArray(elementType, shape), Factory.createArray(elementType, shape) };
        multiplyByScalar(arrayWithError,scalar,result);
		return result;
	}
	
	/**
	 *  Multiply an any-dimensional arrays by a scalar, and propagate the associated
	 *  measurement errors.  
	 *  
	 *  @param array1WithError A 2-element array [data, variance]
	 *  @param array2WithError A 2-element array [scalar, scalar variance]
	 *  @param resultWithError Result array, same structure as first parameter
	 * 
	 * 
	 */
	
	public static void multiplyByScalar(IArray[] array1WithError, double [] scalarwithError, IArray[] resultWithError) {
		// Check that all arrays and associated error matrices have
		// the same shape.
		compareShapes(array1WithError, resultWithError);

	    IArrayIterator iterR = resultWithError[0].getIterator();
	    IArrayIterator iterA = array1WithError[0].getIterator();
	    double scalar_val = scalarwithError[0];
	    IArrayIterator iterRE = resultWithError[1].getIterator();
	    IArrayIterator iterAE = array1WithError[1].getIterator();
	    double scalar_variance = scalarwithError[1];

	    while (iterA.hasNext()) {
	      double this_val = iterA.getDoubleNext();
	      iterR.next().setDoubleCurrent(this_val * scalar_val);
	      iterRE.next().setDoubleCurrent(this_val*this_val*scalar_variance + iterAE.getDoubleNext()*scalar_val*scalar_val);    
	    }

	}
	
	public static void multiplyByScalar(IArray[] array1, ScalarWithVariance sv, IArray[] resultWithError) {
		double[] sv_as_double = {sv.getData(),sv.getVariance()};
		multiplyByScalar(array1,sv_as_double,resultWithError);
	}
	/**
	 * Divide all elements of an array by a scalar, propagating variances
	 * 
	 * @param array1WithError A 2-element array structured as [2D data, 2D variances]
	 * @param scalar A 2-element array [scalar value, variance of scalar value]
	 * @return A 2-element array [resultant data, resultant variance]
	 */

	public static IArray[] divideByScalar(IArray[] arrayWithError, double [] scalar) {
		IArray dataArray = arrayWithError[0];
		IArray errorArray = arrayWithError[1];
		IIndex dataIndex = dataArray.getIndex();
		IIndex errorIndex = errorArray.getIndex();

		Class<?> elementType = dataArray.getElementType();
		int [] shape = dataArray.getShape();
		IArray resultantData = Factory.createArray(elementType, shape);
		IArray resultantError = Factory.createArray(elementType, shape);

		IIndex dataResultIndex = resultantData.getIndex();
		IIndex errorResultIndex = resultantError.getIndex();
		ScalarWithVariance swv = new ScalarWithVariance(scalar[0],scalar[1]);

		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {

				dataIndex.set(i, j);
				errorIndex.set(i, j);
				dataResultIndex.set(i, j);
				errorResultIndex.set(i, j);

				ScalarWithVariance arrayValue = new ScalarWithVariance(dataArray.getDouble(dataIndex), errorArray.getDouble(errorIndex));
				ScalarWithVariance result = ScalarOperations.divide(arrayValue, swv);

				resultantData.setDouble(dataResultIndex, result.getData());
				resultantError.setDouble(errorResultIndex, result.getVariance());
			}
		}

		IArray[] result = {resultantData, resultantError};
		return result;
	}

	/**
	 * Check whether four <code>Array</code>s all have the same shape (that is, their sizes
	 * match in every dimension) and throw an 
	 * <code>IllegalArgumentException</code> if they do not.
	 * 
	 * The <code>Array</code>s are arranged into two sets of two.
	 */
	private static void compareShapes(IArray[] arrays1, IArray[] arrays2) {
		if (arrays1.length != 2 || arrays2.length != 2) {
			throw new IllegalArgumentException("Expected two sets of two arrays");
		}

		IArray array1 = arrays1[0]; 
		IArray array2 = arrays1[1];
		IArray array3 = arrays2[0];
		IArray array4 = arrays2[1];

		// Shapes of all arrays have to match the shape of the first array
		// (Then all the shapes will be the same)
		int [] baseShape = array1.getShape();

		int [][] shapesToCompare = new int [][]{array2.getShape(), 
				array3.getShape(), array4.getShape()};

		String exceptionMessage = "Array shapes don't match";

		for (int i = 0; i < shapesToCompare.length; i++) {
			if (shapesToCompare[i].length != baseShape.length) {
				throw new IllegalArgumentException(exceptionMessage);
			}
			for (int j = 0; j < baseShape.length; j++) {
				if (shapesToCompare[i][j] != baseShape[j]) {
					throw new IllegalArgumentException(exceptionMessage);
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		DataManager dataManager = DataManagerFactory.getDataManager("C:/lwi/workspace/au.gov.ansto.bragg.quokka.dra/xml/path_table.txt");
//		String filename = "platypusSample.6";
//		URI uri = new URI("file://C:/lwi/data/scripts/" + filename + ".hdf");
//		Group group = dataManager.getGroup(uri);
//		System.out.println("Group name = " + group.getShortName());
//		List groups = group.getGroups();
//		Iterator groupsIterator = groups.iterator();
//		while (groupsIterator.hasNext()) {
//		Group currentGroup = (Group) groupsIterator.next();
//		if (currentGroup.getShortName().equals("MasterDataset")) {
//		List nextGroups = currentGroup.getGroups();
//		Iterator nextIterator = nextGroups.iterator();
//		while (nextIterator.hasNext()) {
//		Group myGroup = (Group) nextIterator.next();
//		System.out.println("Name of myGroup = " + myGroup.getShortName());
//		// Now this is the group that has the variable in which we
//		// are interested.
////		myGroup.getDataItem();
//		}
//		}
//		}

//		// NPE is thrown here.
////		Group dataGroup = group.getGroup("MasterDataset").getGroup("data");
////		dataGroup.
//		// So now we have loaded our data group and can play around with it.
//		System.out.println(group);
////		List<Group> groupEntries = group.getEntries();
////		Iterator iterator = groupEntries.iterator();
////		while (iterator.hasNext()) {
////		Object currentElement = iterator.next();
////		Object l = null;
////		}
	}

	public static void mains(String[] args) {
		IArray array = Factory.createArray(double.class, new int[]{5, 5}); // New 5x5 array.
		IIndex index = array.getIndex();

		index.set(1, 1);
		array.setDouble(index, 17);

		index.set(2, 2);
		array.setDouble(index, 6);

		double result = array.getDouble(index);
		System.out.println(result);

		index.set(1, 1);
		result = array.getDouble(index);
		System.out.println(result);
	}
}
