package org.gumtree.core.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ServiceManagerTest {

	@Test
	public void testGetServiceNow() {
		// Initialise
		IServiceManager serviceManager = new ServiceManager();
		IServiceRegistrationManager registrationManager = new ServiceRegistrationManager();
		DummyService dummyService = new DummyService();

		// Register service
		registrationManager.registerService(DummyService.class, dummyService);

		// Get service
		DummyService retrievedService = serviceManager
				.getServiceNow(DummyService.class);
		assertEquals(dummyService, retrievedService);
	}

	// Dummy class for testing
	class DummyService {
	}

}
