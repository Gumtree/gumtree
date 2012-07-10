package org.gumtree.cs.sics.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gumtree.cs.sics.core.tests.SicsTestUtils;
import org.gumtree.sics.core.ISicsModelProvider;
import org.gumtree.sics.core.support.SicsModelProvider;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.io.support.SicsProxy;
import org.junit.Ignore;
import org.junit.Test;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.SICS;

public class SicsModelProviderTest {

	@Test(expected=SicsIOException.class)
	public void testLoadModelAtAbnormalCondition() throws SicsIOException {
		ISicsModelProvider modelProvider = new SicsModelProvider();
		// No model available as proxy is missing
		assertNull(modelProvider.getModel());
		
		ISicsProxy proxy = new SicsProxy();
		modelProvider.setProxy(proxy);
		assertFalse(proxy.isConnected());
		// This will throw SicsIOException as proxy is disconnected
		modelProvider.getModel();
		
		proxy.disposeObject();
		modelProvider.disposeObject();
	}
	
	// This test crashes with SicsControllerProviderTest.testCreateControllerFromServer() ????
	@Test
	@Ignore
	public void testLoadModel() throws SicsIOException, SicsExecutionException {
		ISicsModelProvider modelProvider = new SicsModelProvider();
		ISicsProxy proxy = new SicsProxy();
		modelProvider.setProxy(proxy);
		proxy.login(SicsTestUtils.createConnectionContext());
		assertTrue(proxy.isConnected());
		
		SICS model = modelProvider.getModel();
		assertNotNull(model);
		for (Component component :model.getComponent()) {
			if (component.getId().equals("user")) {
				return;
			}
		}
		fail("Component user not found");
		
		proxy.disposeObject();
		modelProvider.disposeObject();
	}
	
}
