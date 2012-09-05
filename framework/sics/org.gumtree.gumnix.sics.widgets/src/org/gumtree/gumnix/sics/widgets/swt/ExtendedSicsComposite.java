package org.gumtree.gumnix.sics.widgets.swt;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.core.SicsEvents;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.widgets.swt.ExtendedComposite;
import org.osgi.service.event.Event;

public abstract class ExtendedSicsComposite extends ExtendedComposite {

	private ISicsManager sicsManager;

	private EventHandler sicsProxyEventHandler;

	private boolean initialised;
	
	public ExtendedSicsComposite(Composite parent, int style) {
		super(parent, style);
		initialised = false;
	}

	@PostConstruct
	public void render() {
		// Render
		handleRender();
		initialised = true;
		bindSicsManager();
	}
	
	@Override
	protected void disposeWidget() {
		if (sicsProxyEventHandler != null) {
			sicsProxyEventHandler.deactivate();
			sicsProxyEventHandler = null;
		}
		sicsManager = null;
	}

	protected abstract void handleSicsConnect();

	protected abstract void handleSicsDisconnect();

	protected abstract void handleRender();

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public ISicsManager getSicsManager() {
		return sicsManager;
	}

	@Inject
	public void setSicsManager(ISicsManager sicsManager) {
		if (sicsProxyEventHandler != null) {
			sicsProxyEventHandler.deactivate();
			sicsProxyEventHandler = null;
		}
		this.sicsManager = sicsManager;
		if (initialised) {
			bindSicsManager();
		}
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	protected void bindSicsManager() {
		if (getSicsManager() == null) {
			return;
		}
		if (sicsManager.proxy().isConnected()) {
			internalHandleSicsConnect();
		}
		sicsProxyEventHandler = new EventHandler(SicsEvents.Proxy.TOPIC_ALL,
				SicsEvents.Proxy.PROXY_ID, sicsManager.proxy().getId()) {
			public void handleEvent(Event event) {
				if (event.getTopic().equals(SicsEvents.Proxy.TOPIC_CONNECTED)) {
					internalHandleSicsConnect();
				} else if (event.getTopic().equals(
						SicsEvents.Proxy.TOPIC_DISCONNECTED)) {
					handleSicsDisconnect();
				}
			}
		}.activate();
	}
	
	protected void internalHandleSicsConnect() {
		JobRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return getSicsManager().control().getSicsController() != null;
			}
		}, new Runnable() {
			public void run() {
				handleSicsConnect();
			}
		}, 500);
	}

}
