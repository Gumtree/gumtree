package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.ui.util.resource.UIResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// [GT-56] Interrupt view
public class SicsInterruptView extends ViewPart {

	private static Logger logger = LoggerFactory.getLogger(SicsInterruptView.class);
	
	private ISicsProxyListener proxyListener;
	
	private Button button;
	
	public SicsInterruptView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID, parent);
		button = new Button(parent, SWT.PUSH);
		button.setImage(resourceManager.createImage("icons/Stop-Normal-Red-128x128.png"));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try {
					// Action: interrupt SICS
					SicsCore.getSicsController().interrupt();
				} catch (SicsIOException e1) {
					logger.error("Failed to send interrupt.", e);
				}
			}
		});
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

	public void dispose() {
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
	
	@Override
	public void setFocus() {
	}

}
