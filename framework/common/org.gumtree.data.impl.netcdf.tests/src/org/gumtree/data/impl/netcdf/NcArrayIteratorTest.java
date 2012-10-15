package org.gumtree.data.impl.netcdf;

import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.ArrayIteratorTest;

public class NcArrayIteratorTest extends ArrayIteratorTest {

	public NcArrayIteratorTest() {
		setFactory(new NcFactory());
	}

}
