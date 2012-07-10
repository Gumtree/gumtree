package org.gumtree.scripting;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.script.ScriptEngineFactory;

import org.gumtree.core.service.ServiceUtils;
import org.junit.Test;

public class ScriptFactoryTest {

	@Test
	public void testFindFactory() {
		List<ScriptEngineFactory> factories = ServiceUtils.getServiceManager().getServices(ScriptEngineFactory.class);
		assertNotNull(factories);
	}
	
}
