package org.gumtree.cs.sics.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.gumtree.cs.sics.core.tests.SicsTestUtils;
import org.gumtree.sics.core.ISicsMonitor;
import org.gumtree.sics.core.SicsMonitorState;
import org.gumtree.sics.core.support.SicsMonitor;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsEventHandler;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.io.support.SicsProxy;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.gumtree.util.messaging.EventHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.service.event.Event;

public class SicsMonitorTest {

	private static final int TIME_OUT = 3 * 60 * 1000;

	private ISicsProxy proxy;
	
	ISicsMonitor monitor;
	
	@Before
	public void setUp() throws SicsExecutionException, SicsIOException {
		monitor = new SicsMonitor();
		proxy = new SicsProxy();
		proxy.login(SicsTestUtils.createConnectionContext());
		monitor.setProxy(proxy);
	}
	
	@Test
	public void testHipadabaMonitor() throws SicsIOException {
		// Setup listener
		final String[] results = new String[1];
		EventHandler handler = new SicsEventHandler(
				ISicsMonitor.EVENT_TOPIC_HNOTIFY + "/user/name", proxy.getId()) {
			@Override
			public void handleSicsEvent(Event event) {
				results[0] = getString(event, ISicsMonitor.EVENT_PROP_VALUE);

			}
		}.activate();

		// Send (twice to ensure hnotify is triggered)
		proxy.send("hset /user/name me", null);
		proxy.send("hset /user/name you", null);
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return results[0] != null && results[0].equals("you");
			}
		}, TIME_OUT);
		handler.deactivate();

		// Assert
		assertEquals(LoopRunnerStatus.OK, status);
		assertEquals("you", results[0]);
	}

	@Test
	@Ignore("Currently ignored for simulation server")
	public void testStateMonitor() throws SicsIOException {
		// Setup listener
		final SicsMonitorState[] states = new SicsMonitorState[1];
		EventHandler handler = new SicsEventHandler(
				ISicsMonitor.EVENT_TOPIC_STATEMON + "/dummy_motor",
				proxy.getId()) {
			@Override
			public void handleSicsEvent(Event event) {
				states[0] = (SicsMonitorState) getProperty(event,
						ISicsMonitor.EVENT_PROP_STATE);
			}
		}.activate();
		
		// Send
		proxy.send("hset /sample/dummy_motor 1", null);
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return states[0] != null;
			}
		}, TIME_OUT);
		handler.deactivate();
		
		// Assert
		assertEquals(LoopRunnerStatus.OK, status);
		assertNotNull(states[0]);
	}
	
	// Some threading problem needs to be resolve....
	// This test case only works if a breakpoint is place in SicsMonitor
	@Ignore
	@Test
	public void testSicsMonitor() throws SicsIOException, InterruptedException {
//		// Setup listener
//		final Integer[] levels = new Integer[1];
//		ISicsListener sicsListener = new ISicsListener() {
//			@Override
//			public void interrupted(int level) {
//				levels[0] = level;
//			}
//		};
//		monitor.addSicsListener(sicsListener);
//		
//		// Send
//		proxy.send("INT1712 3", null);
//		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
//			public boolean getExitCondition() {
//				return levels[0] != null;
//			}
//		}, TIME_OUT);
//		monitor.removeSicsListener(sicsListener);
//		
//		// Assert
//		assertEquals(LoopRunnerStatus.OK, status);
//		assertEquals(3, levels[0].intValue());
	}
	
	@After
	public void tearDown() throws SicsIOException {
		proxy.disconnect();
		monitor.setProxy(null);
		proxy = null;
		monitor = null;
	}

}
