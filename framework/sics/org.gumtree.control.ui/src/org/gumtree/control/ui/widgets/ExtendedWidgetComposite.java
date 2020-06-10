package org.gumtree.control.ui.widgets;

import javax.annotation.PostConstruct;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.widgets.swt.ExtendedComposite;

public abstract class ExtendedWidgetComposite extends ExtendedComposite {

	private static final int SICS_CONNECTION_TIMEOUT = 5000;
	
	private EventHandler sicsProxyEventHandler;

	public ExtendedWidgetComposite(Composite parent, int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		// Render
		handleRender();
		bindSicsProxy();
	}
	
	@Override
	protected void disposeWidget() {
		if (sicsProxyEventHandler != null) {
			sicsProxyEventHandler.deactivate();
			sicsProxyEventHandler = null;
		}
	}

	protected abstract void handleSicsConnect();

	protected abstract void handleSicsDisconnect();

	protected abstract void handleRender();

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	protected void bindSicsProxy() {
		ISicsProxy proxy = SicsManager.getSicsProxy();
//		if (!proxy.isConnected()) {
//			return;
//		}
		internalHandleSicsConnect();
		proxy.addProxyListener(new SicsProxyListenerAdapter() {
			
			@Override
			public void disconnect() {
				handleSicsDisconnect();
			}
			
			@Override
			public void connect() {
				internalHandleSicsConnect();
			}

		});
	}
	
	protected void internalHandleSicsConnect() {
		JobRunner.run(new ILoopExitCondition() {
			public boolean getExitCondition() {
				return SicsManager.getSicsProxy().isConnected();
			}
		}, new Runnable() {
			public void run() {
				handleSicsConnect();
			}
		}, 500);
	}

	protected void checkSicsConnection() {
		int counter = 0;
		ISicsController[] controllers = SicsManager.getSicsModel().getSicsControllers();
		if (counter <= SICS_CONNECTION_TIMEOUT && (controllers == null || controllers.length == 0)) {
			try {
				Thread.sleep(500);
				counter += 500;
			} catch (InterruptedException e) {
			}
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	}
}
