package org.gumtree.control.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.widgets.swt.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResources;

public class ServerStatusWidget extends ExtendedWidgetComposite {


	private Label statusLabel;
	private Button reconnectButton;
//	private ISicsProxyListener proxyListener;

	public ServerStatusWidget(Composite parent, int style) {
		super(parent, style);
		
//		eventHandler = new EventHandler(SicsEvents.Server.TOPIC_SERVER_STATUS) {
//			public void handleEvent(Event event) {
//				updateUI((ServerStatus) event
//						.getProperty(SicsEvents.Server.STATUS));
//			}
//		};
		

	}

	@Override
	protected void handleSicsConnect() {
		updateUI(null);
	}

	@Override
	protected void handleModelUpdate() {
	}
	
	@Override
	protected void handleStatusUpdate(ServerStatus status) {
		updateUI(status);
	}
	
	@Override
	protected void handleSicsDisconnect() {
		updateUI(null);
	}

	@Override
	protected void handleRender() {
//		setLayout(new FillLayout());
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(this);
		statusLabel = getWidgetFactory().createLabel(this, "--", SWT.CENTER);
		statusLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
		statusLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 22).align(SWT.FILL, SWT.CENTER).applyTo(statusLabel);
		reconnectButton = getWidgetFactory().createButton(this, "Connect", SWT.PUSH);
		GridDataFactory.fillDefaults().indent(0, 0).hint(60, 18).applyTo(reconnectButton);
		reconnectButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					SicsManager.getSicsProxy().connect(null, null);
				} catch (Exception ex) {
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	protected void disposeWidget() {
//		if (proxyListener != null) {
//			SicsManager.getSicsProxy().removeProxyListener(proxyListener);
//			proxyListener = null;
//		}
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
				ISicsProxy proxy = SicsManager.getSicsProxy();
				if (proxy.isConnected()) {
					ServerStatus serverStatus = status;
					if (serverStatus == null) {
						serverStatus = proxy.getServerStatus();
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
					} else if (serverStatus.equals(ServerStatus.RUNNING_A_SCAN)) {
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
