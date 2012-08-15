package org.gumtree.service.dataaccess;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Map;

import org.gumtree.core.service.ServiceManager;
import org.gumtree.core.service.ServiceRegistrationManager;
import org.gumtree.service.dataaccess.providers.AbstractDataProvider;
import org.gumtree.service.dataaccess.support.DataAccessManagerMonitor;
import org.gumtree.service.dataaccess.support.DataAccessProviderInfo;
import org.gumtree.util.collection.CollectionUtils;
import org.junit.Test;

public class DataAccessManagerMonitorTest {

	@Test
	public void testGetProviderInfos() {
		ServiceRegistrationManager serviceRegistrationManager = new ServiceRegistrationManager();
		serviceRegistrationManager.registerService(IDataProvider.class,
				new DummyDataProvider(),
				CollectionUtils.createMap("scheme", "dummy"));

		DataAccessManagerMonitor monitor = new DataAccessManagerMonitor();
		monitor.setServiceManager(new ServiceManager());
		DataAccessProviderInfo dummyInfo = null;
		for (DataAccessProviderInfo info : monitor.getAvailableProviders()) {
			if (info.getScheme().equals("dummy")) {
				dummyInfo = info;
				break;
			}
		}
		assertEquals("dummy", dummyInfo.getScheme());
		assertEquals(DummyDataProvider.class.getName(),
				dummyInfo.getProviderName());

		serviceRegistrationManager.disposeObject();
	}

	class DummyDataProvider extends AbstractDataProvider<Object> {

		@Override
		public <T> T get(URI uri, Class<T> representation,
				Map<String, Object> properties) throws DataAccessException {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
