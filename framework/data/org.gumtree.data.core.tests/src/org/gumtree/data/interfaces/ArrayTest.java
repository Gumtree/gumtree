package org.gumtree.data.interfaces;

import static org.gumtree.data.core.tests.DataTestConstants.PLUGIN_ID;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.filesystem.IFileStore;
import org.gumtree.data.core.tests.DataTestObject;
import org.gumtree.data.core.tests.DataTestUtils;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.utils.IArrayUtils;
import org.junit.Ignore;
import org.junit.Test;

public class ArrayTest extends DataTestObject {
	
	private static final String DATA_PATH = "/data/ansto/QKKData.nx.hdf";
	
	@Test
	public void testTest1DIntArray() {
		int[] rawArray = new int[] { 2, 4, 6, 8, 10 };
		IArray array = getFactory().createArray(rawArray);
		assertEquals(1, array.getRank());
		assertEquals(rawArray.length, array.getSize());
		assertEquals(getFactory().getName(), array.getFactoryName());
		assertEquals(rawArray[0], array.getInt(array.getIndex().set(0)));
		assertEquals(rawArray[4], array.getInt(array.getIndex().set(4)));
		
		IArrayIterator iterator = array.getIterator();
		assertEquals(rawArray[0], iterator.getIntNext());
		assertEquals(rawArray[1], iterator.getIntNext());
		assertEquals(rawArray[2], iterator.getIntNext());
		
		int[] shape = array.getShape();
		assertEquals(1, shape.length);
		assertEquals(5, shape[0]);
	}
	
	@Test
	public void testTest1DIntArrayMath() throws ShapeNotMatchException {
		int[] rawArray = new int[] { 2, 4, 6, 8, 10 };
		IArray array = getFactory().createArray(rawArray);
		
		IArray newArray = array.getArrayMath().toLn().getArray();
		assertEquals(Math.log(rawArray[1]), newArray.getDouble(array.getIndex().set(1)), 0.1);
		assertEquals(Math.log(rawArray[4]), newArray.getDouble(array.getIndex().set(4)), 0.1);
		
		newArray = array.getArrayMath().toSin().getArray();
		assertEquals(Math.sin(rawArray[1]), newArray.getDouble(array.getIndex().set(1)), 0.1);
		assertEquals(Math.sin(rawArray[4]), newArray.getDouble(array.getIndex().set(4)), 0.1);
		
		newArray = array.getArrayMath().toAsin().getArray();
		assertEquals(Math.asin(rawArray[1]), newArray.getDouble(array.getIndex().set(1)), 0.1);
		assertEquals(Math.asin(rawArray[4]), newArray.getDouble(array.getIndex().set(4)), 0.1);
		
		newArray = array.getArrayMath().toPower(2.0).getArray();
		assertEquals(Math.pow(rawArray[1], 2.0), newArray.getDouble(array.getIndex().set(1)), 0.1);
		assertEquals(Math.pow(rawArray[4], 2.0), newArray.getDouble(array.getIndex().set(4)), 0.1);
		
		newArray = array.getArrayMath().toAdd(10.0).getArray();
		assertEquals(rawArray[1] + 10.0, newArray.getDouble(array.getIndex().set(1)), 0.1);
		assertEquals(rawArray[4] + 10.0, newArray.getDouble(array.getIndex().set(4)), 0.1);
	}
	
	@Test
	public void testTest1DIntArrayMathAddArray() throws ShapeNotMatchException {
		int[] rawArray = new int[] { 2, 4, 6, 8, 10 };
		IArray array = getFactory().createArray(rawArray);
		
		IArray newArray = array.getArrayMath().toAdd(array).getArray();
		assertEquals(rawArray[1] + rawArray[1], newArray.getDouble(array.getIndex().set(1)), 0.1);
		assertEquals(rawArray[4] + rawArray[4], newArray.getDouble(array.getIndex().set(4)), 0.1);
	}
	
	// Created by SOLEIL
	@Test
	@Ignore("Test is not fully implemented")
	public void testManuallyCreatedArrays() throws ShapeNotMatchException, InvalidRangeException {
		// Create 3 dimensional primitive array
		int[] shape = { 2, 3, 30 };
		int size = 1;
    	for(int length : shape ) size *= length;
		Object javaArray = java.lang.reflect.Array.newInstance(int.class, size);
    	
    	// Create CDM array
    	IArray array = getFactory().createArray(int.class, shape, javaArray);
    	IArrayIterator iter = array.getIterator();
    	IArrayUtils util = array.getArrayUtils().reduce();
    	ISliceIterator slice = array.getSliceIterator(2);
    	IArray arrayslice = slice.getArrayNext();
    	IIndex idx = arrayslice.getIndex();
		Object o = arrayslice.getArrayUtils().copyToNDJavaArray();
		idx.reduce();
		arrayslice.setIndex(idx);
	}
	
	// Created by SOLEIL
	@Test
	public void testCopyMethods() throws Exception {
		IFileStore file = DataTestUtils.find(PLUGIN_ID, DATA_PATH);
		IDataset dataset = getFactory().createDatasetInstance(file.toURI());
		if (!dataset.isOpen()) {
			dataset.open();
		}

		IDataItem spectrums = (IDataItem) dataset.getRootGroup()
				.findContainerByPath("/QKK0030729/data/hmm_xy");
		IArray array = spectrums.getData();

		// IArray.copy(false) <=> no storage copy
		IArray copyStructure = array.copy(false);
		DataTestUtils.compareArrays(array, copyStructure);

		// IArray.copy(true) <=> storage copy
		IArray copyData1 = array.copy(true);
		DataTestUtils.compareArrays(array, copyData1);

		// IArray.copy() <=> storage copy
		IArray copyData2 = array.copy();
		DataTestUtils.compareArrays(array, copyData2);

		dataset.close();
	}
	
}
