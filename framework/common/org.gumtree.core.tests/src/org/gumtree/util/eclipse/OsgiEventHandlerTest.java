package org.gumtree.util.eclipse;

import static org.junit.Assert.assertEquals;

import org.gumtree.core.service.IServiceManager;
import org.gumtree.core.service.ServiceManager;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.gumtree.util.messaging.OsgiEventBuilder;
import org.gumtree.util.messaging.OsgiEventHandler;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class OsgiEventHandlerTest {

	private static final int TIME_OUT = 250;
	
	private static final String EVENT_TOPIC_SIMPLE = "test/simple";

	private static final String EVENT_TOPIC_1 = "test/1";

	private static final String EVENT_TOPIC_2 = "test/2";

	private static final String EVENT_TOPIC_WILDCARD = "test/*";

	private static final String EVENT_TOPIC_UNRELATED = "unknown/1";
	
	private static final String EVENT_PROP_KEY = "key";
	
	private static final String FILTER_SIMPLE = "(key=123)";

	private static IServiceManager serviceManager;
	
	@BeforeClass
	public static void setup() throws Exception {
//		OsgiUtils.startBundle("org.eclipse.equinox.event");
		serviceManager = new ServiceManager();
	}

	@Test
	public void testSimpleTopic() throws Exception {
		// Prepare listener
		final Event[] events = new Event[1];
		OsgiEventHandler handler = new OsgiEventHandler(EVENT_TOPIC_SIMPLE) {
			@Override
			public void handleEvent(Event event) {
				events[0] = event;
			}
		};
		handler.activate();

		// Prepare sender
		EventAdmin eventAdmin = serviceManager.getService(EventAdmin.class);
		eventAdmin.postEvent(new OsgiEventBuilder(EVENT_TOPIC_SIMPLE).get());

		// Wait
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return events[0] != null;
			}
		});
		handler.deactivate();
		assertEquals(LoopRunnerStatus.OK, status);
		assertEquals(EVENT_TOPIC_SIMPLE, handler.getTopic(events[0]));

		// Since handler is deactivated, send again will have no effect
		events[0] = null;
		eventAdmin.postEvent(new OsgiEventBuilder(EVENT_TOPIC_SIMPLE).get());
		status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return events[0] != null;
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.TIMEOUT, status);
	}

	@Test
	public void testMultipleTopics() throws Exception {
		// Prepare listener
		final Event[] events = new Event[1];
		OsgiEventHandler handler = new OsgiEventHandler(EVENT_TOPIC_WILDCARD) {
			@Override
			public void handleEvent(Event event) {
				events[0] = event;
			}
		};
		handler.activate();

		// Send topic 1
		EventAdmin eventAdmin = serviceManager.getService(EventAdmin.class);
		eventAdmin.postEvent(new OsgiEventBuilder(EVENT_TOPIC_1).get());

		// Wait
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return events[0] != null;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);

		// Send topic 2
		events[0] = null;
		eventAdmin.postEvent(new OsgiEventBuilder(EVENT_TOPIC_2).get());

		// Wait
		status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return events[0] != null;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);

		// Send unrelated topic
		events[0] = null;
		eventAdmin.postEvent(new OsgiEventBuilder(EVENT_TOPIC_UNRELATED).get());
		status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return events[0] != null;
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.TIMEOUT, status);

		handler.deactivate();
	}

	@Test
	public void testWithFilter() throws Exception {
		// Prepare listener
		final Event[] events = new Event[1];
		OsgiEventHandler handler = new OsgiEventHandler(EVENT_TOPIC_SIMPLE, FILTER_SIMPLE) {
			@Override
			public void handleEvent(Event event) {
				events[0] = event;
			}
		};
		handler.activate();
		
		// Send with no properties
		EventAdmin eventAdmin = serviceManager.getService(EventAdmin.class);
		eventAdmin.postEvent(new OsgiEventBuilder(EVENT_TOPIC_SIMPLE).get());
		
		// Wait (should timeout)
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return events[0] != null;
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.TIMEOUT, status);
		
		// Send with correct properties
		events[0] = null;
		eventAdmin.postEvent(new OsgiEventBuilder(EVENT_TOPIC_SIMPLE).append(EVENT_PROP_KEY, "123").get());
		status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return events[0] != null;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
	
		// Send with incorrect properties
		events[0] = null;
		eventAdmin.postEvent(new OsgiEventBuilder(EVENT_TOPIC_SIMPLE).append(EVENT_PROP_KEY, "456").get());
		status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return events[0] != null;
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.TIMEOUT, status);
	}
}
