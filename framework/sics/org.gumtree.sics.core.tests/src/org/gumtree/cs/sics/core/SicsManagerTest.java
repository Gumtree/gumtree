package org.gumtree.cs.sics.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.gumtree.cs.sics.core.tests.SicsTestUtils;
import org.gumtree.sics.control.IServerController;
import org.gumtree.sics.core.ISicsManager;
import org.gumtree.sics.core.support.SicsManager;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.junit.Test;

public class SicsManagerTest {

	@Test
	public void testDefault() throws SicsIOException, SicsExecutionException{
		ISicsManager manager = new SicsManager();
		manager.getProxy().login(SicsTestUtils.createConnectionContext());
		IServerController controller = manager.getServerController();
		
		assertNotNull(manager.getProxy());
		
		assertNotNull(manager.getMonitor());
		assertEquals(manager.getProxy(), manager.getMonitor().getProxy());
		
		assertNotNull(manager.getModelProvider());
		assertEquals(manager.getProxy(), manager.getModelProvider().getProxy());
		
		assertNotNull(manager.getSicsControllerProvider());
		assertEquals(manager.getProxy(), manager.getSicsControllerProvider().getProxy());
		assertEquals(manager.getModelProvider(), manager.getSicsControllerProvider().getModelProvider());
		
		assertNotNull(controller);
		assertEquals(manager.getProxy(), controller.getProxy());
		
		manager.disposeObject();
	}
	
	@Test
	public void testGetMonitor() {
		ISicsManager manager = new SicsManager();
		
		assertNotNull(manager.getMonitor());
		assertEquals(manager.getProxy(), manager.getMonitor().getProxy());
		
		manager.disposeObject();
	}
	
}
