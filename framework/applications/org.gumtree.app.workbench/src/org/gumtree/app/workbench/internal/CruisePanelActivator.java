package org.gumtree.app.workbench.internal;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.util.SafeUIRunner;

public class CruisePanelActivator implements IStartup {

	@Override
	public void earlyStartup() {
		// Initial check
		openCruisePanel();
		
		// Listener
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
			@Override
			public void windowOpened(IWorkbenchWindow window) {
				openCruisePanel();
			}			
			@Override
			public void windowDeactivated(IWorkbenchWindow window) {
			}
			@Override
			public void windowClosed(IWorkbenchWindow window) {
			}			
			@Override
			public void windowActivated(IWorkbenchWindow window) {
			}
		});
	}
	
	private void openCruisePanel() {
		SafeUIRunner.syncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				// Get page object immediately and then open it later
				final IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						page.showView("org.gumtree.app.workbench.cruisePanel", null,
								IWorkbenchPage.VIEW_ACTIVATE);
					}
				});
			}			
		});
		
	}

}
