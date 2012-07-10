package org.gumtree.cs.sics.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.gumtree.cs.sics.core.tests.SicsTestUtils;
import org.gumtree.sics.control.ControllerCallbackAdapter;
import org.gumtree.sics.control.IDynamicController;
import org.gumtree.sics.control.support.DynamicController;
import org.gumtree.sics.core.ISicsMonitor;
import org.gumtree.sics.core.support.SicsMonitor;
import org.gumtree.sics.io.ISicsData;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsData;
import org.gumtree.sics.io.SicsExecutionException;
import org.gumtree.sics.io.SicsIOException;
import org.gumtree.sics.io.support.SicsProxy;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.gumtree.util.LoopRunnerStatus;
import org.junit.Test;

public class DynamicControllerTest {

	private static final int TIME_OUT = 3 * 60 * 1000;

	@Test
	public void testGetCurrentValue() throws SicsIOException,
			SicsExecutionException {
		// Preparation connection
		ISicsProxy proxy = new SicsProxy();
		proxy.login(SicsTestUtils.createConnectionContext());
		ISicsMonitor monitor = new SicsMonitor();
		monitor.setProxy(proxy);

		// Create controller
		IDynamicController controller = new DynamicController();
		controller.setPath("/user/name");
		controller.setProxy(proxy);

		// Reset value in SICS
		proxy.send("hset /user/name me", null);

		final ISicsData[] sicsData = new ISicsData[1];
		controller.getCurrentValue(new ControllerCallbackAdapter() {
			public void getCurrentValue(ISicsData data) {
				sicsData[0] = data;
			}
		});
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return sicsData[0] != null;
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.OK, status);
		assertEquals("me", sicsData[0].getString());
		
		monitor.disposeObject();
		proxy.disposeObject();
	}
	
	@Test
	public void testGetCurrentAndTargetValue() throws SicsIOException, SicsExecutionException {
		// Preparation connection
		ISicsProxy proxy = new SicsProxy();
		proxy.login(SicsTestUtils.createConnectionContext());
		ISicsMonitor monitor = new SicsMonitor();
		monitor.setProxy(proxy);

		// Create controller
		IDynamicController controller = new DynamicController();
		controller.setPath("/user/name");
		controller.setProxy(proxy);

		final ISicsData[] sicsData = new ISicsData[2];
		controller.getCurrentValue(new ControllerCallbackAdapter() {
			public void getCurrentValue(ISicsData data) {
				sicsData[0] = data;
			}
		});
		controller.getTargetValue(new ControllerCallbackAdapter() {
			public void getTargetValue(ISicsData data) {
				sicsData[1] = data;
			}
		});
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return sicsData[0] != null && sicsData[1] != null;
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.OK, status);
		assertTrue(sicsData[0].getString().equals(sicsData[1].getString()));
		
		controller.disposeObject();
		monitor.disposeObject();
		proxy.disposeObject();
	}

	@Test
	public void testCommitTargetValue() throws SicsIOException,
			SicsExecutionException {
		// Preparation connection
		ISicsProxy proxy = new SicsProxy();
		proxy.login(SicsTestUtils.createConnectionContext());
		ISicsMonitor monitor = new SicsMonitor();
		monitor.setProxy(proxy);

		// Create controller
		IDynamicController controller = new DynamicController();
		controller.setPath("/user/name");
		controller.setProxy(proxy);
		
		// Reset value in SICS
		proxy.send("hset /user/name none", null);
		
		controller.setTargetValue(SicsData.wrapData("someone"));
		controller.commitTargetValue();
		
		final ISicsData[] sicsData = new ISicsData[1];
		controller.getCurrentValue(new ControllerCallbackAdapter() {
			public void getCurrentValue(ISicsData data) {
				sicsData[0] = data;
			}
		});
		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return sicsData[0] != null;
			}
		}, TIME_OUT);
		assertEquals(LoopRunnerStatus.OK, status);
		assertEquals("someone", sicsData[0].getString());
		
		controller.disposeObject();
		monitor.disposeObject();
		proxy.disposeObject();
	}

}
