/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

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
import org.gumtree.util.messaging.EventHandler;
import org.gumtree.widgets.swt.util.UIResources;
import org.osgi.service.event.Event;


public class SicsStatusGadget extends AbstractSicsWidget {

	public static final String EVENT_TOPIC_HNOTIFY = "org/gumtree/gumnix/sics/monitor/hnotify";
	
	public static final String EVENT_PROP_VALUE = "value";
	
	private EventHandler eventHandler;

	private Label statusLabel;

	public SicsStatusGadget(Composite parent, int style) {
		super(parent, style);
		eventHandler = new EventHandler(
				SicsEvents.Server.TOPIC_SERVER_STATUS) {
			public void handleEvent(Event event) {
				updateUI((ServerStatus) event
						.getProperty(SicsEvents.Server.STATUS));
			}
		};
	}

	@Override
	protected void handleRender() {
		setLayout(new FillLayout());
		statusLabel = getToolkit().createLabel(this, "--", SWT.CENTER);
		statusLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
		statusLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		updateUI(null);
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

	private void disposeWidget() {
		if (eventHandler != null) {
			eventHandler.deactivate();
			eventHandler = null;
		}
		statusLabel = null;
	}

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
					} else if (serverStatus.equals(ServerStatus.PAUSED)) {
						statusLabel.setBackground(getDisplay().getSystemColor(
								SWT.COLOR_BLUE));
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

	@Override
	protected void widgetDispose() {
		disposeWidget();
	}

	@Override
	public void afterParametersSet() {
		// TODO Auto-generated method stub
		
	}
}
