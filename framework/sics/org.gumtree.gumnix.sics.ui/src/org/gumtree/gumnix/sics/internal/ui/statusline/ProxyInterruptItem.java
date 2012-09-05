package org.gumtree.gumnix.sics.internal.ui.statusline;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyInterruptItem extends ControlContribution {
	
	private static Logger logger = LoggerFactory.getLogger(ProxyInterruptItem.class);
			
	private static String ITEM_NAME = "Proxy Interrupt";
	
	private Label button;
	
	private Image stopEnabledImage;
	
	private Image stopDisabledImage;
	
	private ISicsProxyListener proxyListener;
	
	protected ProxyInterruptItem() {
		super(ITEM_NAME);
	}

	@Override
	protected Control createControl(Composite parent) {
		button = new Label(parent, SWT.PUSH);
		button.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
//		button.setActiveColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
//		button.setShowActiveWhenFocused(false);
		UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID, parent);
		stopEnabledImage = resourceManager.createImage("icons/Stop-Normal-Red-16x16.png");
		stopDisabledImage = resourceManager.createImage("icons/Stop-Disabled-16x16.png");
		button.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
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
//		button.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				try {
//					// Action: interrupt SICS
//					SicsCore.getSicsController().interrupt();
//				} catch (SicsIOException e1) {
//					logger.error("Failed to send interrupt.", e);
//				}
//			}
//		});
		button.setText("Interrupt");
		button.setToolTipText("Send interrupt signal to SICS");
		updateUI();
		proxyListener = new SicsProxyListenerAdapter() {
			public void proxyConnected() {
				updateUI();
			}
			public void proxyDisconnected() {
				updateUI();
			}
		};
		SicsCore.getDefaultProxy().addProxyListener(proxyListener);
		return parent;
	}
	
	private void updateUI() {
		if (button != null && !button.isDisposed()) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					if (button == null) {
						return;
					}
					boolean isConnected = SicsCore.getDefaultProxy().isConnected();
					Image image = isConnected ? stopEnabledImage : stopDisabledImage;
					button.setImage(image);
					button.setEnabled(isConnected);
					button.redraw();
					button.getParent().layout();
				}
			});
		}
	}
	
	public void dispose() {
		if (proxyListener != null) {
			SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		button = null;
		stopEnabledImage = null;
		stopDisabledImage = null;
	}
	
}
