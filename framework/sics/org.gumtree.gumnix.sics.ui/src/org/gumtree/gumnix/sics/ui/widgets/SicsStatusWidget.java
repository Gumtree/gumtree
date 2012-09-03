package org.gumtree.gumnix.sics.ui.widgets;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.control.ServerStatus;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsEvents;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.resource.UIResources;
import org.gumtree.util.messaging.EventHandler;
import org.osgi.service.event.Event;

public class SicsStatusWidget extends AbstractSicsComposite {

	private EventHandler eventHandler;

	private Label statusLabel;

	public SicsStatusWidget(Composite parent, int style) {
		super(parent, style);
		eventHandler = new EventHandler(SicsEvents.Server.TOPIC_SERVER_STATUS) {
			public void handleEvent(Event event) {
				updateUI((ServerStatus) event
						.getProperty(SicsEvents.Server.STATUS));
			}
		};
	}

	@Override
	protected void handleSicsConnect() {
		updateUI(null);
		if (eventHandler != null) {
			eventHandler.activate();
		}
	}

	@Override
	protected void handleSicsDisconnect() {
		updateUI(null);
		if (eventHandler != null) {
			eventHandler.deactivate();
		}
	}

	@Override
	protected void handleRender() {
		setLayout(new FillLayout());
		statusLabel = getWidgetFactory().createLabel(this, "--", SWT.CENTER);
		statusLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
		statusLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		updateUI(null);
	}

	@Override
	protected void disposeWidget() {
		if (eventHandler != null) {
			eventHandler.deactivate();
			eventHandler = null;
		}
		statusLabel = null;
		super.disposeWidget();
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	public void updateUI(final ServerStatus status) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (statusLabel == null || statusLabel.isDisposed()) {
					return;
				}
				if (SicsCore.getDefaultProxy().isConnected()) {
					ServerStatus serverStatus = status;
					if (serverStatus == null) {
						serverStatus = SicsCore.getSicsController()
								.getServerStatus();
					}
					statusLabel.setText(serverStatus.getText());
					if (serverStatus.equals(ServerStatus.EAGER_TO_EXECUTE)) {
						statusLabel.setBackground(getDisplay().getSystemColor(
								SWT.COLOR_GREEN));
					} else if (serverStatus.equals(ServerStatus.DRIVING)) {
						statusLabel.setBackground(getDisplay().getSystemColor(
								SWT.COLOR_YELLOW));
					} else if (serverStatus.equals(ServerStatus.COUNTING)) {
						statusLabel.setBackground(getDisplay().getSystemColor(
								SWT.COLOR_YELLOW));
					} else if (serverStatus.equals(ServerStatus.WAIT)) {
						statusLabel.setBackground(getDisplay().getSystemColor(
								SWT.COLOR_YELLOW));
					} else {
						statusLabel.setBackground(getDisplay().getSystemColor(
								SWT.COLOR_GRAY));
					}
				} else {
					statusLabel.setText("DISCONNECTED");
					statusLabel.setBackground(getDisplay().getSystemColor(
							SWT.COLOR_GRAY));
				}
			}
		});
	}

}
