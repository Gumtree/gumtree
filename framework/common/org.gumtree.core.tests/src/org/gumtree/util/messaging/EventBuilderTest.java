package org.gumtree.util.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.gumtree.core.tests.internal.Activator;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
//import org.gumtree.util.eclipse.OsgiUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.event.Event;

public class EventBuilderTest {

	private static final String EVENT_TOPIC = "test/topic";
	
	@BeforeClass
	public static void setup() throws Exception {
//		OsgiUtils.startBundle("org.eclipse.equinox.ds");
//		OsgiUtils.startBundle("org.eclipse.equinox.event");
//		OsgiUtils.startBundle("org.eclipse.e4.ui.services");
	}
	
	@Test
	public void testPostEvent() {
		IEventBroker eventBroker = Activator.getDefault().getEclipseContext()
				.get(IEventBroker.class);
		assertNotNull(eventBroker);
		
		final String[] data = new String[1];
		EventHandler eventHandler = new EventHandler(EVENT_TOPIC) {
			@Override
			public void handleEvent(Event event) {
				data[0] = (String) event.getProperty("key");
			}
		}.activate();
		new EventBuilder(EVENT_TOPIC).append("key", "123").post();
		
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return data[0] != null;
			}
		});
		eventHandler.deactivate();
		assertEquals(LoopRunnerStatus.OK, status);
		assertEquals("123", data[0]);
	}
	
}
