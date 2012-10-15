package org.gumtree.data.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gumtree.data.core.tests.DataTestObject;
import org.junit.Test;

public class ArrayIteratorTest extends DataTestObject {

	@Test
	public void test1DIntInteration() {
		int[] rawArray = new int[] { 2, 4, 6, 8, 10 };
		IArray array = getFactory().createArray(rawArray);
		IArrayIterator iterator = array.getIterator();

		assertTrue(iterator.hasNext());
		assertEquals(2, iterator.getIntNext());
		assertEquals(4, iterator.getIntNext());
		assertEquals(6, iterator.getIntNext());
		assertEquals(8, iterator.getIntNext());
		assertEquals(10, iterator.getIntNext());
		assertFalse(iterator.hasNext());

		// No more element
		try {
			iterator.getIntNext();
			fail();
		} catch (Exception e) {
		}
	}

	@Test
	public void test2DIntInteration() {
		int[] shape = { 2, 3, 4 };
		int size = 1;
		for (int length : shape) size *= length;
		int[] rawArray = new int[size];
		for (int i = 0; i < size; i++) rawArray[i] = (i + 1) * 2;
		IArray array = getFactory().createArray(int.class, shape, rawArray);
		IArrayIterator iterator = array.getIterator();
		assertEquals(2, iterator.getIntNext());
		assertEquals(4, iterator.getIntNext());
		assertEquals(6, iterator.getIntNext());
		assertEquals(8, iterator.getIntNext());
		assertEquals(10, iterator.getIntNext());
	}
	
	@Test
	public void test1DIntSetInterator() {
		int[] rawArray = new int[] { 2, 4, 6, 8, 10 };
		IArray array = getFactory().createArray(rawArray);
		IArrayIterator iterator = array.getIterator();
		
		// Set the first 2 elements
		iterator.getIntNext();
		iterator.setIntCurrent(1);
		iterator.getIntNext();
		iterator.setIntCurrent(3);
		
		iterator = array.getIterator();
		assertEquals(1, iterator.getIntNext());
		assertEquals(3, iterator.getIntNext());
		assertEquals(6, iterator.getIntNext());
		assertEquals(8, iterator.getIntNext());
		assertEquals(10, iterator.getIntNext());
	}
	
}
