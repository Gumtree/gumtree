package au.gov.ansto.bragg.kowari.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;
import org.gumtree.ui.util.SafeUIRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.kowari.ui.KowariWorkbenchLauncher;

/**
 * This class is responsible for launching the special Kowari workbench layout during
 * start up.
 * 
 * @author NXI
 *
 */
public class KowariWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "gumtree.startExperimentLayout";
	
	private static Logger logger = LoggerFactory.getLogger(KowariWorkbenchSetup.class);
	
	public void earlyStartup() {
		String launchKowariLayout = System.getProperty(PROP_START_EXP_LAYOUT, "false");
		// [GT-73] Launch Kowari layout for experiment mode or analysis mode
		if (Boolean.parseBoolean(launchKowariLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Kowari workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					KowariWorkbenchLauncher launcher = new KowariWorkbenchLauncher();
					launcher.launch();
				}			
			});
		} else {
			final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow window : windows) {
				hideMenus(window);
				if (window instanceof WorkbenchWindow) {
					IWorkbenchPage[] pages = window.getPages();
					for (IWorkbenchPage page : pages) {
						try {
							IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
							for (IPerspectiveDescriptor perspective : perspectives) {
								window.getActivePage().closePerspective(perspective, false, true);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					IMultiMonitorManager mmManager = new MultiMonitorManager();
					mmManager.showPerspectiveOnOpenedWindow(KowariWorkbenchLauncher.ID_PERSPECTIVE_ANALYSIS, 0, 1, true);
					hideMenus(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				}
			});
		}
	}
	
	private void hideMenus(IWorkbenchWindow window){
		WorkbenchWindow workbenchWin = (WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MenuManager menuManager = ((WorkbenchWindow) window).getMenuManager();
		IContributionItem[] items = menuManager.getItems();

		for(IContributionItem item : items) {
		  item.setVisible(false);
		}
		menuManager.setVisible(false);
		menuManager.setRemoveAllWhenShown(true);
	    
	    IContributionItem[] menubarItems = ((WorkbenchWindow) window).getMenuBarManager().getItems();
	    for (IContributionItem item : menubarItems) {
	    	item.setVisible(false);
	    }
	    ((WorkbenchWindow) window).getMenuBarManager().setVisible(false);
	    ((WorkbenchWindow) window).getMenuBarManager().setRemoveAllWhenShown(true);
	}
	
}
