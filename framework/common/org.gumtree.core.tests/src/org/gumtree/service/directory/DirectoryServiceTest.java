package org.gumtree.service.directory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.gumtree.service.directory.support.DirectoryService;
import org.junit.Test;

public class DirectoryServiceTest {

	@Test
	public void testLookup() {
		IDirectoryService directoryService = new DirectoryService();
		
		assertNull(directoryService.lookup("dummy"));
		
		Dummy dummy = new Dummy();
		directoryService.bind("dummy", dummy);
		assertEquals(dummy, directoryService.lookup("dummy"));
		assertEquals(dummy, directoryService.lookup("dummy", Dummy.class));
	}

	class Dummy {
	}

}
