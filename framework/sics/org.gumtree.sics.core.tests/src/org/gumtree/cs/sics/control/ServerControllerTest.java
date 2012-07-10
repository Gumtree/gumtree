package org.gumtree.cs.sics.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.gumtree.cs.sics.core.tests.SicsTestUtils;
import org.gumtree.sics.control.ControllerStatus;
import org.gumtree.sics.control.IServerController;
import org.gumtree.sics.control.ServerStatus;
import org.gumtree.sics.control.support.ServerController;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsEventHandler;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.io.support.SicsProxy;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.util.messaging.OsgiEventHandler;
import org.junit.Test;
import org.osgi.service.event.Event;

import com.google.common.base.Strings;

public class ServerControllerTest {

	private static final int TIME_OUT = 3 * 60 * 1000;
	
	public void testController() {
		IServerController controller = new ServerController();
		assertEquals("/", controller.getPath());
		assertTrue(Strings.isNullOrEmpty(controller.getId()));
		assertTrue(Strings.isNullOrEmpty(controller.getDeviceId()));
		controller.disposeObject();
	}
	
	@Test
	public void testControllerServerStatus() throws SicsIOException, SicsExecutionException {
		final IServerController controller = new ServerController();
		
		// Injection
		ISicsProxy proxy = new SicsProxy();
		controller.setProxy(proxy);
		
		// 1. Unknow when disconnected
		assertFalse(proxy.isConnected());
		assertEquals(ServerStatus.UNKNOWN, controller.getServerStatus());
		
		// 2. Eager to execute when connected
		proxy.login(SicsTestUtils.createConnectionContext());
		assertTrue(proxy.isConnected());
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return !controller.getServerStatus().equals(ServerStatus.UNKNOWN);
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.OK, status);
		assertEquals(ServerStatus.EAGER_TO_EXECUTE, controller.getServerStatus());
		
		// 3. Back to unknown when disconnected
		proxy.disconnect();
		assertFalse(proxy.isConnected());
		status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return controller.getServerStatus().equals(ServerStatus.UNKNOWN);
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.OK, status);
		assertEquals(ServerStatus.UNKNOWN, controller.getServerStatus());
		
		// 4. Reconnect and test for listener
		final ServerStatus[] serverStatus = new ServerStatus[1];
		EventHandler eventHandler = new SicsEventHandler(
				IServerController.EVENT_TOPIC_SERVER_STATUS_CHANGE,
				proxy.getId()) {
			@Override
			public void handleSicsEvent(Event event) {
				serverStatus[0] = (ServerStatus) event
						.getProperty(IServerController.EVENT_PROP_SERVER_STATUS);
			}
		}.activate();
		
		proxy.login(SicsTestUtils.createConnectionContext());
		assertTrue(proxy.isConnected());
		status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return serverStatus[0] != null;
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.OK, status);
		assertEquals(ServerStatus.EAGER_TO_EXECUTE, controller.getServerStatus());
		
		// Disconnect
		eventHandler.deactivate();
		proxy.disposeObject();
		controller.disposeObject();
	}
	
	@Test
	public void testControllerStatus() {
		IServerController sicsController = new ServerController();
		assertEquals(ControllerStatus.OK, sicsController.getStatus());
		
		// TODO: test driving motors
		sicsController.disposeObject();
	}
	
	@Test
	public void testInterrupt() throws SicsIOException, SicsExecutionException {
		// No proxy available, should have no effect
		IServerController sicsController = new ServerController();
		assertFalse(sicsController.isInterrupted());
		sicsController.interrupt();
		assertFalse(sicsController.isInterrupted());
		
		// Proxy is disconnected, should have no effect
		ISicsProxy proxy = new SicsProxy();
		sicsController.setProxy(proxy);
		assertFalse(sicsController.isInterrupted());
		sicsController.interrupt();
		assertFalse(sicsController.isInterrupted());
		
		// Proxy is connected, do proper testing
		proxy.login(SicsTestUtils.createConnectionContext());
		assertFalse(sicsController.isInterrupted());
		sicsController.interrupt();
		assertTrue(sicsController.isInterrupted());
		sicsController.clearInterrupt();
		assertFalse(sicsController.isInterrupted());
		
		// Test listener
		final Boolean[] interrupt = new Boolean[1];
		OsgiEventHandler eventHandler = new OsgiEventHandler(
				IServerController.EVENT_TOPIC_SERVER_INTERRUPT) {
			@Override
			public void handleEvent(Event event) {
				interrupt[0] = true;
			}
		}.activate();
		
		sicsController.interrupt();
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return interrupt[0] != null;
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.OK, status);
		assertTrue(interrupt[0]);
		
		// Clean up
		eventHandler.deactivate();
		proxy.disposeObject();
		sicsController.disposeObject();
	}
	
}
