package org.gumtree.sics.control.support;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.ISafeRunnable;
import org.gumtree.sics.control.ControllerCallbackAdapter;
import org.gumtree.sics.control.IControllerCallback;
import org.gumtree.sics.control.IDynamicController;
import org.gumtree.sics.core.ISicsMonitor;
import org.gumtree.sics.io.ISicsData;
import org.gumtree.sics.io.ISicsProxy;
import org.gumtree.sics.io.SicsCallbackAdapter;
import org.gumtree.sics.io.SicsData;
import org.gumtree.sics.io.SicsEventHandler;
import org.gumtree.sics.util.SicsCoreUtils;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicController extends SicsController implements IDynamicController {

	private static final Logger logger = LoggerFactory.getLogger(DynamicController.class);
	
	private ISicsMonitor monitor;
	
	private ISicsData currentValue;
	
	private ISicsData targetValue;
	
	private SicsEventHandler handler;
	
	private Queue<IControllerCallback> initialCallbackQueue;

	private boolean isInitialising;
	
	public DynamicController() {
		super();
		initialCallbackQueue = new ConcurrentLinkedQueue<IControllerCallback>();
		isInitialising = false;
	}
	
	// TODO: what if multiple call to trigger multiple hget????
	@Override
	public void getCurrentValue(final IControllerCallback callback) {
		if (currentValue == null) {
			if (getProxy() != null && getProxy().isConnected()) {
				if (isInitialising) {
					// Deal with it later
					initialCallbackQueue.offer(callback);
				} else {
					isInitialising = true;
					getProxy().send("hget " + getPath(), new SicsCallbackAdapter() {
						public void receiveReply(final ISicsData data) throws Exception {
							String value = data.getJSONObject().getString(getPath());
							currentValue = SicsData.wrapData(value);
							isInitialising = false;
							dispatchCurrentValue(callback, currentValue);
							// Process pending callback
							IControllerCallback pendingCallback = null;
							while ((pendingCallback = initialCallbackQueue.poll()) != null) {
								dispatchCurrentValue(pendingCallback, currentValue);
							}
						}
					});
				}
			}
		} else {
			dispatchCurrentValue(callback, currentValue);
		}
	}

	public void getTargetValue(final IControllerCallback callback) {
		if (targetValue == null) {
			getCurrentValue(new ControllerCallbackAdapter() {
				public void getCurrentValue(ISicsData data) {
					targetValue = data;
					dispatchTargetValue(callback, targetValue);
				}
			});
		} else {
			dispatchTargetValue(callback, targetValue);
		}
	}
	
	@Override
	public void setTargetValue(ISicsData data) {
		this.targetValue = data;
	}

	@Override
	public void commitTargetValue() {
		if (getProxy() != null && getProxy().isConnected()) {
			getProxy().send("hset " + getPath() + " " + targetValue.getString(), null);
		}
	}

	@Override
	@Inject
	public void setProxy(ISicsProxy proxy) {
		super.setProxy(proxy);
		if (proxy != null && handler == null) {
			handler = new SicsEventHandler(ISicsMonitor.EVENT_TOPIC_HNOTIFY
					+ getPath()) {
				@Override
				public void handleSicsEvent(Event event) {
					String newValue = getString(event,
							ISicsMonitor.EVENT_PROP_VALUE);
					currentValue = SicsData.wrapData(newValue);
				}
			};
			handler.setProxyId(getProxy().getId()).activate();
		}
	}
	
	protected void dispatchCurrentValue(final IControllerCallback callback, final ISicsData data) {
		if (callback != null) {
			SicsCoreUtils.execute(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					callback.getCurrentValue(data);
				}

				@Override
				public void handleException(Throwable exception) {
					logger.error("Failed to callback", exception);
				}
			});
		}
	}
	
	protected void dispatchTargetValue(final IControllerCallback callback, final ISicsData data) {
		if (callback != null) {
			SicsCoreUtils.execute(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					callback.getTargetValue(data);
				}

				@Override
				public void handleException(Throwable exception) {
					logger.error("Failed to callback", exception);
				}
			});
		}
	}
	
	/*************************************************************************
	 * Object life cycle
	 *************************************************************************/
	@Override
	@PreDestroy
	public void disposeObject() {
		if (initialCallbackQueue != null) {
			initialCallbackQueue.clear();
			initialCallbackQueue = null;
		}
		if (handler != null) {
			handler.deactivate();
			handler = null;
		}
		monitor = null;
		currentValue = null;
		targetValue = null;
	}
	
}
