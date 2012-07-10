package org.gumtree.service.eventbus;

import junit.framework.TestCase;

import org.gumtree.service.eventbus.support.EventBus;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;

public class EventBusTest extends TestCase {

	private EventX eventX;

	private EventY eventY;

	private Event genericEvent;

	private IEventBus eventBus;

	protected void setUp() {
		eventBus = new EventBus();
		eventX = null;
		eventY = null;
		genericEvent = null;
	}

	// Register for event X and receive event X on this publisher only
	public void testSubscribeX() {
		eventBus.subscribe(this, new EventXHandler());
		EventX event = new EventX(this);
		eventBus.postEvent(event);

		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return eventX != null && eventY == null && genericEvent == null;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
	}

	// Register for event X and receive event X on this publisher only
	public void testSubscribeXOnDifferentPublisher() {
		eventBus.subscribe(this, new EventXHandler());
		EventX event = new EventX(new Object());
		eventBus.postEvent(event);

		// Should fail because wrong publisher
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return eventX == null && eventY == null && genericEvent != null;
			}
		}, 100, 10);
		assertEquals(LoopRunnerStatus.TIMEOUT, status);
	}

	// Register for event X and receive event X on all publisher
	public void testGlobalSubscribeX() {
		eventBus.subscribe(new EventXHandler());
		EventX event = new EventX(new Object());
		eventBus.postEvent(event);

		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return eventX != null && eventY == null && genericEvent == null;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
	}

	// Register for event X and receive event X on this publisher only
	public void testSubscribeY() {
		eventBus.subscribe(this, new EventYHandler());
		EventY event = new EventY(this);
		eventBus.postEvent(event);

		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return eventX == null && eventY != null && genericEvent == null;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
	}

	// Register for all event and receive event X on this publisher only
	public void testSubscribeGeneric() {
		eventBus.subscribe(this, new GenericEventHandler());
		EventX event = new EventX(this);
		eventBus.postEvent(event);

		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return eventX == null && eventY == null && genericEvent != null;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
	}

	// Register for all event and receive event X on different publisher only
	public void testSubscribeGenericOnDifferentPusblisher() {
		eventBus.subscribe(this, new GenericEventHandler());
		EventX event = new EventX(new Object());
		eventBus.postEvent(event);

		// Should fail because wrong publisher
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return eventX == null && eventY == null && genericEvent != null;
			}
		}, 100, 10);
		assertEquals(LoopRunnerStatus.TIMEOUT, status);
	}

	// Register for all event and receive event X on all publisher
	public void testSubscribeGlobalGeneric() {
		eventBus.subscribe(new GenericEventHandler());
		EventX event = new EventX(new Object());
		eventBus.postEvent(event);

		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return eventX == null && eventY == null && genericEvent != null;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
	}

	// Register for event X and receive event Y on this publisher only
	public void testSubscribeExpectTimeout() {
		eventBus.subscribe(this, new EventXHandler());
		EventY event = new EventY(this);
		eventBus.postEvent(event);

		// Expect time out because we only interest in event X
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return eventX != null && eventY == null && genericEvent == null;
			}
		}, 100, 10);
		assertEquals(LoopRunnerStatus.TIMEOUT, status);
	}

	private class EventXHandler implements IEventHandler<EventX> {
		public void handleEvent(EventX event) {
			eventX = event;
		}
	}

	private class EventYHandler implements IEventHandler<EventY> {
		public void handleEvent(EventY event) {
			eventY = event;
		}
	}

	private class GenericEventHandler implements IEventHandler<Event> {
		public void handleEvent(Event event) {
			genericEvent = event;
		}
	}

}
