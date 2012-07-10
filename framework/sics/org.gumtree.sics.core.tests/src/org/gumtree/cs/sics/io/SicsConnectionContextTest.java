package org.gumtree.cs.sics.io;

import static org.junit.Assert.*;

import org.gumtree.sics.io.ISicsConnectionContext;
import org.gumtree.sics.io.SicsConnectionContext;
import org.gumtree.sics.io.SicsRole;
import org.junit.Test;

public class SicsConnectionContextTest {

	private static final String HOST = "localhost";

	private static final int PORT = 1234;

	private static final SicsRole ROLE = SicsRole.USER;

	private static final String PASSWORD = "password";

	@Test
	public void testDefaultSicsConnectionContext() {
		ISicsConnectionContext context = new SicsConnectionContext();
		assertNotNull(context.getHost());
		assertNotNull(context.getPort());
		assertNotNull(context.getRole());
		assertNotNull(context.getPassword());
	}

	@Test
	public void testSicsConnectionContext() {
		ISicsConnectionContext context = new SicsConnectionContext(HOST, PORT, ROLE, PASSWORD);
		assertEquals(HOST, context.getHost());
		assertEquals(PORT, context.getPort());
		assertEquals(ROLE, context.getRole());
		assertEquals(PASSWORD, context.getPassword());
	}
	
}
