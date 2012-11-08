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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.SicsInterruptView;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsInterruptGadget extends FormControlWidget {

	private static Logger logger = LoggerFactory.getLogger(SicsInterruptView.class);
	
	private ISicsProxyListener proxyListener;
	
	private Label button;
	
	private Cursor handCursor;
	
	public SicsInterruptGadget(Composite parent, int style) {
		super(parent, style);
	}

	public void afterParametersSet() {
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(this);
		UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID, this);
		button = new Label(this, SWT.NONE);
//		button.setBackground(getBackground());
		button.setImage(resourceManager.createImage("icons/StopNormalRed_64x64.png"));
		handCursor = new Cursor(Display.getDefault(), SWT.CURSOR_HAND);
		button.setCursor(handCursor);
		button.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				try {
					// Action: interrupt SICS
					SicsCore.getSicsController().interrupt();
				} catch (SicsIOException e1) {
					logger.error("Failed to send interrupt.", e);
				}
			}
		});
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(button);
		this.layout(true, true);
		// Set UI status
		updateUI();
		// Register proxy listener
		proxyListener = new SicsProxyListenerAdapter() {
			public void proxyConnected() {
				updateUI();
			}
			public void proxyDisconnected() {
				updateUI();
			}
		};
		SicsCore.getDefaultProxy().addProxyListener(proxyListener);
	}

	private void disposeWidget() {
		if (proxyListener != null) {
			SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		if (handCursor != null) {
			handCursor.dispose();
			handCursor = null;
		}
		button = null;
	}
	
	private void updateUI() {
		if (button != null && !button.isDisposed()) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					if (button != null  && !button.isDisposed()) {
						button.setEnabled(SicsCore.getDefaultProxy().isConnected());
					}
				}
			});
		}
	}

	@Override
	protected void widgetDispose() {
		disposeWidget();
	}
}
