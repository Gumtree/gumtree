/**
 * 
 */
package org.gumtree.control.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.gumtree.control.core.ICommandController;
import org.gumtree.control.core.IDriveableController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ISicsReplyData;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.SicsCallbackAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsExecutionException;
import org.gumtree.control.exception.SicsInterruptException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.control.model.SicsModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import junit.framework.TestCase;

/**
 * @author nxi
 *
 */
public class SicsChannelTest extends TestCase {
 
	public final ExpectedException exception = ExpectedException.none();
	
//	private static ISicsChannel channel;
	private static ISicsProxy proxy;
	private static ISicsModel model;
	private static IDriveableController dm;
	private static ControllerListener listener;
	
	@Before
	protected void setUp() throws Exception {
		if (proxy == null) {
			System.out.println("create new proxy");
//			channel = new SicsChannel();
//			if (ConstantSetup.USE_LOCAL_SERVER) {
//				channel.connect(ConstantSetup.LOCAL_SERVER_ADDRESS, ConstantSetup.LOCAL_PUBLISHER_ADDRESS);
//			} else {
//				channel.connect(ConstantSetup.REMOTE_SERVER_ADDRESS, ConstantSetup.REMOTE_PUBLISHER_ADDRESS);
//			}
			if (ConstantSetup.USE_LOCAL_SERVER) {
				proxy = SicsManager.getSicsProxy(ConstantSetup.LOCAL_SERVER_ADDRESS, ConstantSetup.LOCAL_PUBLISHER_ADDRESS);
			} else {
				proxy = SicsManager.getSicsProxy(ConstantSetup.REMOTE_SERVER_ADDRESS, ConstantSetup.REMOTE_PUBLISHER_ADDRESS);
			}

//			proxy.syncRun("manager ansto");
//			proxy = SicsManager.getSicsProxy();
//			if (ConstantSetup.USE_LOCAL_SERVER) {
//				proxy.connect(ConstantSetup.LOCAL_SERVER_ADDRESS, ConstantSetup.LOCAL_PUBLISHER_ADDRESS);
//			} else {
//				proxy.connect(ConstantSetup.REMOTE_SERVER_ADDRESS, ConstantSetup.REMOTE_PUBLISHER_ADDRESS);
//			}
			model = proxy.getSicsModel();
			
			dm = (IDriveableController) model.findController("dummy_motor");
			listener = new ControllerListener();
			dm.addControllerListener(listener);

		}
	}
	
	private void testCommand(String command, String expected) {
		String ret = null;
		try {
			ret = proxy.syncRun(command);
		} catch (SicsException e) {
			e.printStackTrace();
		}
//		System.out.println(expected + ":" + ret);
		assertTrue(expected.equals(ret));
	}
	
//	@Test
	public void testLogin() {
//		String ret = null;
//		try {
//			ret = channel.send("manager ansto");
//		} catch (SicsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		assertTrue("OK".equals(ret));
//		System.out.println(ret);
		testCommand("manager ansto", "OK");
		assertEquals(ServerStatus.EAGER_TO_EXECUTE, proxy.getServerStatus());
	}
	
	@Test
	public void testRun() throws SicsException {
		float newValue = ThreadLocalRandom.current().nextFloat() * 30;
//		testCommand("drive dummy_motor " + String.valueOf(newValue), String.valueOf(newValue));
		IDriveableController a2 = (IDriveableController) model.findController("a2");
		a2.setTarget(newValue);
		a2.run();
		assertEquals(ServerStatus.DRIVING, proxy.getServerStatus());
	}
	
	@Test
	public void testDrive() throws SicsException {
		float newValue = ThreadLocalRandom.current().nextFloat() * 100;
//		testCommand("drive dummy_motor " + String.valueOf(newValue), String.valueOf(newValue));
		proxy.syncRun("drive dummy_motor " + newValue);
		IDriveableController dummy_motor = (IDriveableController) model.findController("dummy_motor");
//		dummy_motor.setTargetValue(newValue);
//		dummy_motor.commitTargetValue();
		double precision = dummy_motor.getPrecision();
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		float curValue = (float) dummy_motor.getValue();
//		System.out.println(newValue + ":" + curValue);
		assertTrue(Math.abs(newValue - curValue) < precision);
	}

	@Test
	public void testDriveInterrupt() {
		float newValue = ThreadLocalRandom.current().nextFloat() * 100;
		try {
			proxy.syncRun("drive s2 " + String.valueOf(newValue) + " interrupt");
		} catch (Exception e) {
			assertSame("catching interrupt message", SicsInterruptException.class, e.getClass());
		}
	}

//	@Test
//	public void testStress() throws SicsModelException {
//		long t = System.currentTimeMillis();
//		for (int i = 0; i < 10000; i++) {
//			float newValue = ThreadLocalRandom.current().nextFloat()*100;
//			testCommand("drive dummy_motor " + String.valueOf(newValue), String.valueOf(newValue));
//		}
//		t = System.currentTimeMillis() - t;
//		System.out.println(String.format("time used for processing 10,000 commands is " + t 
//				+ " ms, averagely %.1f transactions per second", 10000000./t));
//	}
	
	@Test
	public void testGetGumtreeModel() {
		if (model == null) {
			String ret = null;
			try {
				ret = proxy.syncRun("getgumtreexml /");
			} catch (SicsException e) {
				e.printStackTrace();
			}
			assertNotNull(ret);
			try {
				int idx = ret.indexOf("<");
				ret = ret.substring(idx);
				model = new SicsModel(proxy);
				model.loadFromString(ret);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		assertNotNull(model);
		assertNotNull(model.findController("dummy_motor"));
	}
	
	@Test
	public void testModel() {
		ISicsModel model = SicsManager.getSicsModel();
		assertNotNull(model);
	}

	@Test
	public void testModelDrive() throws SicsException {
		IDriveableController dm = (IDriveableController) proxy.getSicsModel().findControllerById("dummy_motor");
		float newValue = ThreadLocalRandom.current().nextFloat() * 100;
//		dm.setTarget(newValue);
		dm.drive(newValue);
		double precision = dm.getPrecision();
		System.out.println(dm.getValue() + ":" + newValue);
		assertTrue(Math.abs((float) dm.getValue() - newValue) < precision);
//		assertTrue(dm.getValue().equals(newValue));
	}
	
	@Test
	public void testSetValue() throws SicsException {
		IDynamicController gte = (IDynamicController) proxy.getSicsModel().findControllerById("gumtree_time_estimate");
		float newValue = ThreadLocalRandom.current().nextFloat() * 100;
		gte.setTargetValue(newValue);
		gte.commitTargetValue();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(gte.getValue() + ":" + newValue);
		assertEquals(gte.getValue(), String.valueOf(newValue));
	}
	
	@Test
	public void testCommandController() throws SicsException {
		ICommandController gte = (ICommandController) proxy.getSicsModel().findControllerByPath("/commands/scan/runscan");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("scan_variable", "s2");
		params.put("scan_start", "0");
		params.put("scan_stop", "3");
		params.put("numpoints", "4");
		params.put("mode", "time");
		params.put("preset", "10");
		gte.run(params, null);
		assertNull(gte.getErrorMessage());
	}
	
//	class Callback implements ISicsCallback {
//
//		boolean isFinished;
//		boolean hasError;
//		boolean replyReceived;
//		
//		@Override
//		public void receiveReply(ISicsReplyData data) {
//			replyReceived = true;
//		}
//
//		@Override
//		public void receiveError(ISicsReplyData data) {
//			hasError = true;
//		}
//
//		@Override
//		public void receiveFinish(ISicsReplyData data) {
//			isFinished = true;
//		}
//
//		@Override
//		public boolean hasError() {
//			// TODO Auto-generated method stub
//			return hasError;
//		}
//
//		@Override
//		public void setError(boolean error) {
//			hasError = error;
//		}
//
//		@Override
//		public void receiveWarning(ISicsReplyData data) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void receiveRawData(Object data) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public boolean isCallbackCompleted() {
//			// TODO Auto-generated method stub
//			return false;
//		}
//
//		@Override
//		public void setCallbackCompleted(boolean completed) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//	}

	@Test
	public void testCallback() throws SicsException {
		SicsCallbackAdapter callback = new SicsCallbackAdapter() {

			@Override
			public void receiveFinish(ISicsReplyData data) {
				setCallbackCompleted(true);
				assertTrue(isCallbackCompleted());
				assertTrue(!hasError());
			}
			
		};
		callback.setError(false);
		proxy.syncRun("drive m2 20", callback);
	}
	
	@Test
	public void testCountAndPause() throws SicsException {
		proxy.syncRun("histmem start");
		assertTrue(proxy.getServerStatus().equals(ServerStatus.COUNTING));
//		proxy.syncRun("pause on");
//		assertTrue(proxy.getServerStatus().equals(ServerStatus.PAUSED));
//		proxy.syncRun("pause off");
//		assertTrue(proxy.getServerStatus().equals(ServerStatus.COUNTING));
//		proxy.syncRun("histmem stop");
//		assertTrue(proxy.getServerStatus().equals(ServerStatus.EAGER_TO_EXECUTE));
	}
	
	class ControllerListener implements ISicsControllerListener {

		private float value;
		private ControllerState state;
		private boolean isEnabled;
		
		@Override
		public void updateValue(Object oldValue, Object newValue) {
			value = Float.valueOf(newValue.toString());
		}
		
		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
			state = newState;
//			System.out.println("new state is " + newState);
		}
		
		@Override
		public void updateEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}
		
		public float getValue() {
			return value;
		}
		
		public ControllerState getState() {
			return state;
		}
		
		public boolean isEnabled() {
			return isEnabled;
		}

		@Override
		public void updateTarget(Object oldValue, Object newValue) {
			
		}
		
	}
	
	@Test
	public void testControllerListener() throws SicsException {
		float newValue = ThreadLocalRandom.current().nextInt(20, 30);
//		dm.setTarget(newValue);
		dm.drive(newValue);
		assertEquals(newValue, listener.getValue());
	}
	
	@Test
	public void testCommandError() throws SicsException {
		try {
			proxy.syncRun("none existing command");
		} catch (Exception e) {
			assertTrue(e instanceof SicsExecutionException);
		}
	}
	
	@Test
	public void testAsyncRun() throws SicsException {
		proxy.syncRun("drive m2 20");
		proxy.asyncRun("drive m2 21", new SicsCallbackAdapter() {
			
			@Override
			public void receiveFinish(ISicsReplyData data) {
				try {
					assertEquals(0.f, (float) dm.getValue());
				} catch (SicsModelException e) {
				}
			}
			
		});
		
		assertTrue(Math.abs(20 - (float) dm.getValue()) < dm.getPrecision());
//		assertEquals(20.f, dm.getValue());
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
	}
	
	@After
	protected void tearDown() throws Exception {
//		channel.disconnect();
	}
}
