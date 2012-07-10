package org.gumtree.util.messaging;

import static org.junit.Assert.assertEquals;

import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.junit.Test;

public class ListenerManagerTest {

	@Test
	public void testListenerManager() {
		// Initialise
		IListenerManager<DummyListener> listenerManager = new ListenerManager<DummyListener>();
		final DummyListener dummyListener = new DummyListener();

		// Register and call listener
		listenerManager.addListenerObject(dummyListener);
		listenerManager
				.asyncInvokeListeners(new SafeListenerRunnable<DummyListener>() {
					@Override
					public void run(DummyListener listener) throws Exception {
						listener.listen("hello");
					}
				});

		// Verify result
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return dummyListener.getText() != null;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
		assertEquals("hello", dummyListener.getText());
	}

	class DummyListener {
		private String text;

		public void listen(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

}
