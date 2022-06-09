package org.gumtree.control.ui.statusline;

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
import org.gumtree.control.core.ISicsConnectionContext;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.ui.internal.Activator;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResourceManager;
import org.gumtree.widgets.swt.util.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyStatusLine extends ControlContribution {

	private static final String ITEM_NAME = "Proxy Status";

	private static Logger logger = LoggerFactory.getLogger(ProxyStatusLine.class);
	
	private Label launchImage;
	
	private Label lauunchText;

	private Image connectedImage;
	
	private Image disconnectedImage;

	private ISicsProxyListener proxyListener;

	protected ProxyStatusLine() {
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
    	
    	connectedImage = resourceManager.createImage("icons/connected.png");
    	disconnectedImage = resourceManager.createImage("icons/disconnected.png");
    	
    	launchImage.addMouseListener(new MouseAdapter() {
    		public void mouseDown(MouseEvent e) {
				if(!SicsManager.getSicsProxy().isConnected()) {
					Activator.getDefault().getLoginHandler().login(true);
				} else {
					boolean confirmed = MessageDialog.openConfirm(launchImage.getShell(), "Confirm Disconnect", "Disconnecting from SICS?");
					if (confirmed) {
						try {
							SicsManager.getSicsProxy().disconnect();
						} catch (Exception exception) {
							logger.error("Cannot disconnect from SICS", exception);
						}
					}
				}
			}
		});
    	
    	updateUI();
    	
    	proxyListener = new SicsProxyListenerAdapter() {
    		
			public void connect() {
				updateUI();
			}
			public void disconnect() {
				updateUI();
			}
		};
		SicsManager.getSicsProxy().addProxyListener(proxyListener);
        return parent;
    }

    private void updateUI() {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				if (launchImage != null && !launchImage.isDisposed()) {
					if (SicsManager.getSicsProxy().isConnected()) {
						launchImage.setImage(connectedImage);
						launchImage
								.setToolTipText("Sics is connected. Click to disconnect from SICS");
						ISicsConnectionContext context = SicsManager.getSicsProxy().getConnectionContext();
						lauunchText.setText(context.getServerAddress());
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
			SicsManager.getSicsProxy().removeProxyListener(proxyListener);
			proxyListener = null;
		}
		launchImage = null;
		connectedImage = null;
		disconnectedImage = null;
	}

}
