package org.gumtree.data.soleil;

import org.gumtree.data.interfaces.ArrayTest;
import org.junit.Ignore;
import org.junit.Test;

public class NxsArrayTest extends ArrayTest {

	public NxsArrayTest() {
		setFactory(new NxsFactory());
	}
	
	@Test
	@Ignore("SOLEIL implementation cannot handle int for toLn()")
	@Override
	public void testTest1DIntArrayMath() {
	}
	
	@Test
	@Ignore("Not implemented")
	@Override
	public void testTest1DIntArrayMathAddArray() {
	}
	
}
