package org.gumtree.cs.sics.core;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.gumtree.cs.sics.core.tests.SicsTestUtils;
import org.gumtree.sics.control.IServerController;
import org.gumtree.sics.control.ISicsController;
import org.gumtree.sics.core.ISicsControllerProvider;
import org.gumtree.sics.core.ISicsModelProvider;
import org.gumtree.sics.core.support.SicsControllerProvider;
import org.gumtree.sics.core.support.SicsModelProvider;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.io.support.SicsProxy;
import org.gumtree.sics.util.SicsModelUtils;
import org.gumtree.util.eclipse.EclipseUtils;
import org.junit.Test;

import ch.psi.sics.hipadaba.SICS;

public class SicsControllerProviderTest {

	@Test
	public void testCreateControllerFromServer() throws SicsIOException, SicsExecutionException {
		// Prepare controller provider
		ISicsProxy proxy = new SicsProxy();
		ISicsModelProvider modelProvider = new SicsModelProvider();
		modelProvider.setProxy(proxy);
		ISicsControllerProvider controllerProvider = new SicsControllerProvider();
		controllerProvider.setProxy(proxy);
		controllerProvider.setModelProvider(modelProvider);
		
		// Login to get online model
		proxy.login(SicsTestUtils.createConnectionContext());
		
		// Find if experiment controller available
		IServerController controller = controllerProvider.createServerController();
		List<ISicsController> selected = select(
				Arrays.asList(controller.getChildren()),
				having(on(ISicsController.class).getId(), equalTo("experiment")));
		assertEquals(1, selected.size());
		
		ISicsController experimentController = selected.get(0);
		assertEquals("experiment", experimentController.getId());
		assertEquals(proxy, experimentController.getProxy());
		
	}
	
	@Test
	public void testCreateControllerFromFile() throws CoreException, IOException {
		// Load model from file
		IFileStore file = EclipseUtils.find(SicsTestUtils.PLUGIN_ID, "data/echidna-2011-19-14-hipadaba.xml");
		SICS sicsModel = SicsModelUtils.loadSICSModel(file.toURI());
		assertNotNull(sicsModel);
		
		// Mock model provider
		ISicsModelProvider modelProvider = mock(ISicsModelProvider.class);
		when(modelProvider.getModel()).thenReturn(sicsModel);
		ISicsControllerProvider controllerProvider = new SicsControllerProvider();
		controllerProvider.setModelProvider(modelProvider);
		
		// Create controller
		IServerController controller = controllerProvider.createServerController();
		
		// Select /user/name
		List<ISicsController> selected = select(
				Arrays.asList(controller.getChildren()),
				having(on(ISicsController.class).getId(), equalTo("experiment")));
		ISicsController experimentController = selected.get(0); 
		selected = select(Arrays.asList(experimentController.getChildren()),
				having(on(ISicsController.class).getId(), equalTo("title")));
		ISicsController titleController = selected.get(0);
		
		assertEquals(1, selected.size());
		assertEquals("title", titleController.getId());
		assertEquals("title", titleController.getDeviceId());
		assertEquals(experimentController, titleController.getParent());
	}

	
}
