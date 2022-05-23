package org.gumtree.control.test;

import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsException;
import org.junit.After;
import org.junit.Test;
import org.zeromq.ZMQ;

import junit.framework.TestCase;

public class CommunicationTest extends TestCase{
	protected int value1, value2;

	private ZMQ.Socket subscriber;
	private ZMQ.Context context;
	private boolean isConnected;
	private ISicsModel model;
	private ISicsProxy proxy;

	// assigning the values
	protected void setUp(){
//		context = ZMQ.context(1);
//		subscriber = context.socket(ZMQ.SUB);
//		if (ConstantSetup.USE_LOCAL_SERVER) {
//			isConnected = subscriber.connect(ConstantSetup.LOCAL_SERVER_ADDRESS);
//		} else {
//			isConnected = subscriber.connect(ConstantSetup.REMOTE_SERVER_ADDRESS);
//		}
		proxy = SicsManager.getSicsProxy();
		if (ConstantSetup.USE_LOCAL_SERVER) {
			isConnected = proxy.connect(ConstantSetup.LOCAL_SERVER_ADDRESS, ConstantSetup.LOCAL_PUBLISHER_ADDRESS);
		} else {
			isConnected = subscriber.connect(ConstantSetup.REMOTE_SERVER_ADDRESS);
		}
		 
	}

	@Test
	public void testLogin() {
		String ret = null;
		try {
			ret = proxy.syncRun("manager ansto");
		} catch (SicsException e) {
		}
		assertTrue("OK".equals(ret));
	}
	
	public void testModel() {
		model = proxy.getSicsModel();
		assertNotNull(model);
	}

	// test method to add two values
	@Test
	public void testConnected(){
		assertTrue(isConnected);
	}

	@After
	@Override
	protected void tearDown() throws Exception {
		subscriber.close();
		context.term();
	}
}
