package org.gumtree.data.impl.netcdf;

import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.ArrayTest;

public class NcArrayTest extends ArrayTest {

	public NcArrayTest() {
		setFactory(new NcFactory());
	}
	
}
