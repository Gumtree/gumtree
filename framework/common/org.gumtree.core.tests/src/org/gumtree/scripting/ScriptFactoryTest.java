package org.gumtree.scripting;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.script.ScriptEngineFactory;

import org.gumtree.core.service.IServiceManager;
import org.gumtree.core.service.ServiceManager;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.scripting.support.ScriptingManager;
import org.junit.Test;

public class ScriptFactoryTest {

	@Test
	public void testFindFactory() {
		IScriptingManager scriptingManager = new ScriptingManager();
		IServiceManager serviceManager = new ServiceManager();
		scriptingManager.setServiceManager(serviceManager);
		List<ScriptEngineFactory> factories = ServiceUtils.getServiceManager()
				.getServices(ScriptEngineFactory.class);
		assertNotNull(factories);
	}

}
