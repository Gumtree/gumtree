package org.gumtree.control.ui.statusline;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.gumtree.control.core.SicsCoreProperties;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.ui.internal.Activator;
import org.gumtree.ui.util.workbench.WorkbenchUtils;

public class StatusLineStartup implements IStartup {


	public void earlyStartup() {
		boolean newProxy = false;
		try {
			newProxy = Boolean.valueOf(SicsCoreProperties.USE_NEW_PROXY.getValue());
		} catch (Exception e) {
		}
		if (newProxy) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {

					// Hack: get SICS model to activate SICS
					try {
						SicsManager.getSicsModel();
					} catch (Exception e) {
						// this is very likely to throw exception as sics is not yet ready
					}

					startUI();
					String loginMode = SicsCoreProperties.LOGIN_MODE.getValue();
					if ("auto".equals(loginMode)) {
						if(!SicsManager.getSicsProxy().isConnected()) {
							Activator.getDefault().getLoginHandler().login(true);
						} 
					}
				}
			});
		}
	}

	private void startUI() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			addToWindow(windows[i]);
		}
		workbench.addWindowListener(new IWindowListener() {
			public void windowActivated(IWorkbenchWindow window) {
			}
			public void windowDeactivated(IWorkbenchWindow window) {
			}
			public void windowClosed(IWorkbenchWindow window) {
			}
			public void windowOpened(IWorkbenchWindow window) {
				addToWindow(window);
			}
		});

	}

	    private void addToWindow(IWorkbenchWindow window) {
	        IStatusLineManager statusLine = WorkbenchUtils.getStatusLineManager(window);
	        statusLine.add(new ProxyStatusLine());
	        statusLine.add(new ProxyInterruptLine());
	        statusLine.update(true);
	    }

}
