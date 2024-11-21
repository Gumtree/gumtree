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

package org.gumtree.control.ui.widgets;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.control.core.ISicsProxy;
import org.gumtree.control.core.ServerStatus;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.ui.internal.Activator;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PauseStatusWidget extends ExtendedWidgetComposite {

	private static Logger logger = LoggerFactory.getLogger(PauseStatusWidget.class);
	
	private Label statusLabel;
	
	private Label buttonLabel;
	
	private Cursor handCursor;
	
	private Image pauseImage;
	
	private Image continueImage;
	
	private ISicsProxyListener proxyListener;
	
	private boolean isRequested = false;
	
	public PauseStatusWidget(Composite parent, int style) {
		super(parent, style);
	}

	public void handleRender() {
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(this);
		UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID, this);
		statusLabel = getWidgetFactory().createLabel(this, "", SWT.CENTER);
		statusLabel.setText("Click to Pause Counting");
		statusLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridDataFactory.swtDefaults().align(SWT.CENTER, GridData.VERTICAL_ALIGN_CENTER)
					.indent(SWT.DEFAULT, 8).grab(true, false).applyTo(statusLabel);
		buttonLabel = getWidgetFactory().createLabel(this, "", SWT.CENTER);
		pauseImage = resourceManager.createImage("icons/button_blue_pause.png");
		continueImage = resourceManager.createImage("icons/StepForwardNormalBlue16.png");
		buttonLabel.setImage(pauseImage);
		buttonLabel.setToolTipText("Click to pause counting.");
		GridDataFactory.swtDefaults().align(SWT.LEFT, GridData.VERTICAL_ALIGN_CENTER)
					.hint(50, 32).grab(false, false).applyTo(buttonLabel);
//		button.setText("PAUSED");
		buttonLabel.setEnabled(false);
		FontData[] fontData = buttonLabel.getFont().getFontData();
		fontData[0].setHeight(16);
		buttonLabel.setFont(new Font(buttonLabel.getDisplay(), fontData));
		handCursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
		buttonLabel.setCursor(handCursor);
		buttonLabel.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				togglePause();
			}
		});
		this.layout(true, true);
		// Set UI status
		proxyListener = new SicsProxyListenerAdapter() {
			
			@Override
			public void disconnect() {
				handleSicsDisconnect();
			}
			
			@Override
			public void connect() {
				handleSicsConnect();
			}

			@Override
			public void setStatus(ServerStatus newStatus) {
				handleStatusChange(newStatus);
			}

		};
		ISicsProxy proxy = SicsManager.getSicsProxy();
		proxy.addProxyListener(proxyListener);
		
		if (proxy.isConnected()) {
			handleSicsConnect();
		}
		// Register proxy listener
	}

	protected void handleSicsConnect() {
		updateUI(null);
	}
	
	protected void handleSicsDisconnect() {
//		if (proxyListener != null) {
//			SicsManager.getSicsProxy().removeProxyListener(proxyListener);
//			proxyListener = null;
//		}
		updateUI(null);
	}
	
	private void handleStatusChange(ServerStatus newStatus) {
		updateUI(newStatus);
	}

	private void togglePause() {
		try {
			// Action: interrupt SICS
			isRequested = !isRequested;
			ServerStatus serverStatus = SicsManager.getSicsProxy().getServerStatus();
			if (serverStatus.equals(ServerStatus.COUNTING)){
				runVeto(true);
				statusLabel.setText("Pause requested");
				buttonLabel.setToolTipText("Wait for system to respond.");
			} else if (serverStatus.equals(ServerStatus.PAUSED)) {
				runVeto(false);
				statusLabel.setText("Request sent");
				buttonLabel.setToolTipText("Wait for system to respond.");
			} else {
				statusLabel.setText("Pause not available");
				buttonLabel.setToolTipText("Please wait for counting status");
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void widgetDispose() {
		if (proxyListener != null) {
			SicsManager.getSicsProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		if (handCursor != null) {
			handCursor.dispose();
			handCursor = null;
		}
		buttonLabel = null;
		statusLabel = null;
	}
	
	private void updateUI(ServerStatus status) {
		if (buttonLabel != null && !buttonLabel.isDisposed()) {
			ISicsProxy proxy = SicsManager.getSicsProxy();
			final ServerStatus curStatus = status != null ? status : proxy.getServerStatus();
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					if (curStatus.equals(ServerStatus.COUNTING)) {
						buttonLabel.setEnabled(true);
						statusLabel.setText("Click to Pause Counting");
						buttonLabel.setImage(pauseImage);
						buttonLabel.setToolTipText("Click to pause counting.");
					} else if (curStatus.equals(ServerStatus.PAUSED)) {
						buttonLabel.setEnabled(true);
						buttonLabel.setImage(continueImage);
						buttonLabel.setToolTipText("Click to continue counting.");
						statusLabel.setText("Counting Paused");
					} else {
						buttonLabel.setEnabled(false);
						buttonLabel.setImage(pauseImage);
						buttonLabel.setToolTipText("Click to pause counting.");
						statusLabel.setText("Pause on counting only");
					}
					layout(true, true);
				}
			});
		}
	}

	private void runVeto(boolean vetoFlag) throws Exception {
		ISicsProxy proxy = SicsManager.getSicsProxy();
		if (vetoFlag) {
			proxy.asyncRun("histmem veto on", null);
		} else {
			proxy.asyncRun("histmem veto off", null);
		}
	}
	
}
