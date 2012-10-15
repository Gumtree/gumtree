package org.gumtree.data.utils;

import static org.junit.Assert.assertArrayEquals;

import org.gumtree.data.core.tests.DataTestObject;
import org.gumtree.data.interfaces.IArray;
import org.junit.Test;

public class ArrayUtilsTest extends DataTestObject {

	@Test
	public void testCopyTo1DJavaArray() {
		int[] rawArray = new int[] { 2, 4, 6, 8, 10 };
		IArray array = getFactory().createArray(rawArray);
		IArrayUtils arrayUtils = array.getArrayUtils();
		int[] javaArray = (int[]) arrayUtils.copyTo1DJavaArray();
		assertArrayEquals(rawArray, javaArray);
	}
	
}
