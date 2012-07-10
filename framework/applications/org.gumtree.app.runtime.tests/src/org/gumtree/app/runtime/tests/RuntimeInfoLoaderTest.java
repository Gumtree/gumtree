package org.gumtree.app.runtime.tests;

import static org.junit.Assert.*;

import org.gumtree.app.runtime.loader.IRuntimeLoader;
import org.gumtree.app.runtime.loader.RuntimeInfoLoader;
import org.junit.Test;

public class RuntimeInfoLoaderTest {

	@Test
	public void testInfoLoader() throws Exception {
		IRuntimeLoader loader = new RuntimeInfoLoader();
		loader.load(null);
		// Make sure the system property is set (although it may be
		// written by the GumTree runtime initially).
		assertNotNull(System.getProperty(RuntimeInfoLoader.PROP_GUMTREE_RUNTIME));
	}
	
}
