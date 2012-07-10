package org.gumtree.service.dataaccess;

import static org.junit.Assert.assertEquals;

import org.gumtree.service.dataaccess.support.DataAccessManager;
import org.junit.Test;

public class AccessManagerTest {

	@Test
	public void testGet() {
		IDataAccessManager dam = new DataAccessManager();
		assertEquals(1, dam.getManageableBeans().length);
	}
	
}
