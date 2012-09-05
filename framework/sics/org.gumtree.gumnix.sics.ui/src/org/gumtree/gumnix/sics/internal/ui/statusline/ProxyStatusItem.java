package org.gumtree.gumnix.sics.internal.ui.statusline;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.gumtree.gumnix.sics.io.ISicsConnectionContext;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyStatusItem extends ControlContribution {

	private static final String ITEM_NAME = "Proxy Status";

	private static Logger logger = LoggerFactory.getLogger(ProxyStatusItem.class);
	
	private Label launchImage;
	
	private Label lauunchText;

	private Image connectedImage;
	
	private Image disconnectedImage;

	private ISicsProxyListener proxyListener;

	protected ProxyStatusItem() {
		super(ITEM_NAME);
	}

    protected Control createControl(Composite parent) {
    	Composite holderComposite = new Composite(parent, SWT.NONE);
    	GridLayoutFactory.swtDefaults().numColumns(2).applyTo(holderComposite);
    	launchImage = new Label(holderComposite, SWT.NONE);
    	launchImage.setCursor(UIResources.getSystemCursor(SWT.CURSOR_HAND));
    	GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(launchImage);
    	lauunchText = new Label(holderComposite, SWT.CENTER);
    	GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(lauunchText);
    	launchImage.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
//    	launchLink.setActiveColor(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
//    	launchLink.setShowActiveWhenFocused(false);
    	UIResourceManager resourceManager = new UIResourceManager(Activator.PLUGIN_ID, holderComposite);
    	
    	connectedImage = resourceManager.createImage("icons/start.gif");
    	disconnectedImage = resourceManager.createImage("icons/stop.gif");
    	
    	launchImage.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent e) {
				if(!SicsCore.getDefaultProxy().isConnected()) {
					Activator.getDefault().getLoginHandler().login(true);
				} else {
					boolean confirmed = MessageDialog.openConfirm(launchImage.getShell(), "Confirm Disconnect", "Disconnecting from SICS?");
					if (confirmed) {
						try {
							SicsCore.getDefaultProxy().disconnect();
						} catch (SicsIOException exception) {
							logger.error("Cannot disconnect from SICS", exception);
						}
					}
				}
			}
		});
    	
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
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (launchImage != null && !launchImage.isDisposed()) {
					if (SicsCore.getDefaultProxy().isConnected()) {
						launchImage.setImage(connectedImage);
						launchImage
								.setToolTipText("Sics is connected. Click to disconnect from SICS");
						ISicsConnectionContext context = SicsCore
								.getDefaultProxy().getConnectionContext();
						lauunchText.setText(context.getHost() + ":"
								+ context.getPort());
					} else {
						launchImage.setImage(disconnectedImage);
						launchImage
								.setToolTipText("Sics is disconnected. Click to connect SICS");
						lauunchText.setText("Disconnected");
					}
					launchImage.redraw();
					launchImage.getParent().layout();
				}
			}
		});
    }
    
    public void dispose() {
		if (proxyListener != null) {
			SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		launchImage = null;
		connectedImage = null;
		disconnectedImage = null;
	}

}
