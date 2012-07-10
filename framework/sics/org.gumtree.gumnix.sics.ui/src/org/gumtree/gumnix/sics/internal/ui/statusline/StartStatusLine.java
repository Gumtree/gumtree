package org.gumtree.gumnix.sics.internal.ui.statusline;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.ui.util.workbench.WorkbenchUtils;

public class StartStatusLine implements IStartup {

	public void earlyStartup() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				
				// Hack: get SICS model to activate SICS
				try {
					SicsCore.getSicsController().getSICSModel();
				} catch (Exception e) {
					// this is very likely to throw exception as sics is not yet ready
				}
				
				startUI();
			}
		});
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
	        statusLine.add(new ProxyStatusItem());
	        statusLine.add(new ProxyInterruptItem());
	        statusLine.update(true);
	    }

}
