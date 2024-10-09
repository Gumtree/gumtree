package org.gumtree.gumnix.sics.widgets.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.control.ServerStatus;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsEvents;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.widgets.swt.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResources;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsStatusWidget extends ExtendedSicsComposite {

	private static final Logger logger = LoggerFactory.getLogger(SicsStatusWidget.class);
	
	private EventHandler eventHandler;

	private Label statusLabel;
	private Button reconnectButton;

	public SicsStatusWidget(Composite parent, int style) {
		super(parent, style);
		eventHandler = new EventHandler(SicsEvents.Server.TOPIC_SERVER_STATUS) {
			public void handleEvent(final Event event) {
				updateUI((ServerStatus) event
						.getProperty(SicsEvents.Server.STATUS));
				logger.warn(String.valueOf((ServerStatus) event
						.getProperty(SicsEvents.Server.STATUS)));
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
//		setLayout(new FillLayout());
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(this);
		statusLabel = getWidgetFactory().createLabel(this, "--", SWT.CENTER);
		statusLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
		statusLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 18).align(SWT.FILL, SWT.CENTER).applyTo(statusLabel);
		reconnectButton = getWidgetFactory().createButton(this, "Connect", SWT.PUSH);
		GridDataFactory.fillDefaults().indent(0, 0).hint(60, 18).applyTo(reconnectButton);
		reconnectButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					SicsCore.getDefaultProxy().send("", null);
				} catch (SicsIOException e1) {
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
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
					reconnectButton.setVisible(false);
					((GridData) reconnectButton.getLayoutData()).exclude = true;
				} else {
					statusLabel.setText("DISCONNECTED");
					statusLabel.setBackground(getDisplay().getSystemColor(
							SWT.COLOR_GRAY));
					reconnectButton.setVisible(true);
					((GridData) reconnectButton.getLayoutData()).exclude = false;
				}
				layout(new Control[]{reconnectButton});
			}
		});
	}

}
