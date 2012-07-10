package org.gumtree.cs.sics.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gumtree.cs.sics.core.tests.SicsTestUtils;
import org.gumtree.sics.io.ISicsConnectionContext;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.ISicsProxy.ProxyState;
import org.gumtree.sics.io.SicsEventHandler;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.io.SicsRole;
import org.gumtree.sics.io.support.SicsProxy;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.gumtree.util.messaging.EventHandler;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;

public class SicsProxyConnectionTest {

	private ISicsProxy proxy;

	private boolean connectionRequestCalled;

	private boolean connectListenerCalled;

	private boolean disconnectListenerCalled;

	private ISicsConnectionContext context;

	@Before
	public void setUp() {
		// Manual proxy test.....use new proxy instead of the default from Sics
		// Manager
		proxy = new SicsProxy();
		connectListenerCalled = false;
		disconnectListenerCalled = false;
		context = SicsTestUtils.createConnectionContext();
	}

	// Simply login and logoff from sics
	@Test
	public void testConnection() throws SicsExecutionException, SicsIOException {
		assertNotNull(context);

		// Make sure this is not connected
		checkDisconnectedState();

		// connect
		EventHandler eventHandler = new SicsEventHandler(
				ISicsProxy.EVENT_TOPIC_PROXY_STATE_ALL, proxy.getId()) {
			@Override
			public void handleSicsEvent(Event event) {
				if (event.getTopic().equals(
						ISicsProxy.EVENT_TOPIC_PROXY_STATE_CONNECTED)) {
					connectListenerCalled = true;
				} else if (event.getTopic().equals(
						ISicsProxy.EVENT_TOPIC_PROXY_STATE_DISCONNECTED)) {
					disconnectListenerCalled = true;
				}
			}
		}.activate();
		
		proxy.login(context);
		
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return connectListenerCalled;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
		assertTrue(connectListenerCalled);
		checkConnectedState();

		// disconnect
		proxy.disconnect();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return disconnectListenerCalled;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
		assertTrue(disconnectListenerCalled);
		checkDisconnectedState();
		eventHandler.deactivate();
	}

	@Test
	public void testProxyActivation() {
		assertEquals(ProxyState.DISCONNECTED, proxy.getProxyState());
		EventHandler eventHandler = new SicsEventHandler(
				ISicsProxy.EVENT_TOPIC_PROXY_STATE_ACTIVATION_REQUESTED,
				proxy.getId()) {
			@Override
			public void handleSicsEvent(Event event) {
				connectionRequestCalled = true;
			}
		}.activate();
		try {
			proxy.send("", null);
			fail("Should throw exception");
		} catch (SicsIOException e) {
		}
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			@Override
			public boolean getExitCondition() {
				return connectionRequestCalled;
			}
		});
		assertEquals(LoopRunnerStatus.OK, status);
		assertTrue(connectionRequestCalled);
		eventHandler.deactivate();
	}

	private void checkConnectedState() throws SicsIOException {
		// Connected state
		assertEquals(ProxyState.CONNECTED, proxy.getProxyState());
		// Context is set
		assertEquals(context, proxy.getConnectionContext());
		// Role should be same as the connection context
		assertEquals(proxy.getConnectionContext().getRole(),
				proxy.getCurrentRole());
		// Should have more than zero connection
		assertTrue(proxy.getConnectedChannelIds().length > 0);
		// Send an emtpy command
		proxy.send("", null);
		// Five connections are available
		// General, status, scan, batch, raw batch
//		assertEquals(5, proxy.getConnectedChannelIds().length);
		// General, status, scan, batch
		assertEquals(4, proxy.getConnectedChannelIds().length);
	}

	private void checkDisconnectedState() {
		// Disconnected state
		assertEquals(ProxyState.DISCONNECTED, proxy.getProxyState());
		// No connection context
		assertNull(proxy.getConnectionContext());
		// Undef role
		assertEquals(SicsRole.UNDEF, proxy.getCurrentRole());
		// Empty channel ids
		assertEquals(0, proxy.getConnectedChannelIds().length);
		// Exception in disconnecting
		try {
			proxy.disconnect();
			fail("Should throw exception");
		} catch (SicsIOException e) {
		}
		// Exception in sending commands
		try {
			proxy.send("", null);
			fail("Should throw exception");
		} catch (SicsIOException e) {
		}
		try {
			proxy.send("", null, "");
			fail("Should throw exception");
		} catch (SicsIOException e) {
		}
	}

}
