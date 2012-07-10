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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.SicsInterruptView;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.forms.FormControlWidget;
import org.gumtree.ui.util.resource.UIResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsInterruptGadgetSummary extends FormControlWidget {

	private static Logger logger = LoggerFactory.getLogger(SicsInterruptView.class);
	
	private ISicsProxyListener proxyListener;
	
	private Button button;
	
	public SicsInterruptGadgetSummary(Composite parent, int style) {
		super(parent, style);
	}
	
	public void afterParametersSet() {
		GridLayoutFactory.swtDefaults().applyTo(this);
		UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID, this);
		button = new Button(this, SWT.NONE);
		button.setImage(resourceManager.createImage("icons/Stop-Normal-Red-16x16.png"));
		button.setText("Interrupt");
		button.setBackground(getBackground());
		button.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
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
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(button);
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

	public void widgetDispose() {
		if (proxyListener != null) {
			SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		button = null;
	}
	
	private void updateUI() {
		if (button != null && !button.isDisposed()) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					button.setEnabled(SicsCore.getDefaultProxy().isConnected());
				}
			});
		}
	}

}
