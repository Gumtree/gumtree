package org.gumtree.cs.sics.io;

import static org.junit.Assert.*;

import org.gumtree.sics.io.SicsRole;
import org.junit.Test;

public class SicsRoleTest {

	@Test
	public void testGetRole() {
		assertEquals(SicsRole.MANAGER, SicsRole.getRole(SicsRole.MANAGER.getConfigKey()));
		assertEquals(SicsRole.USER, SicsRole.getRole(SicsRole.USER.getConfigKey()));
		assertEquals(SicsRole.SPY, SicsRole.getRole(SicsRole.SPY.getConfigKey()));
		assertEquals(SicsRole.UNDEF, SicsRole.getRole(SicsRole.UNDEF.getConfigKey()));
	}

	@Test
	public void testLoginId() {
		assertNotNull(SicsRole.MANAGER.getLoginId());
		assertNotNull(SicsRole.USER.getLoginId());
		assertNotNull(SicsRole.SPY.getLoginId());
		// Undefined role should not have login id
		assertNull(SicsRole.UNDEF.getLoginId());
	}
	
}
