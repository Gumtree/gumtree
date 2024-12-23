package au.gov.ansto.bragg.echidna.ui.internal;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobotWorkbenchLayoutLauncher extends AbstractLauncher {

	private static final String ID_CUSTOMISED_TEMPERATURE_EXPERIMENT = "au.gov.ansto.bragg.echidna.ui.TCLRunnerPerspective";
	private static final String ID_HIGH_TEMPERATURE_EXPERIMENT = "au.gov.ansto.bragg.echidna.ui.HighTemperaturePerspective";
	private static final String ID_LOW_TEMPERATURE_EXPERIMENT = "au.gov.ansto.bragg.echidna.ui.LowTemperaturePerspective";
	private static final String ID_MAGNETICFIELD_EXPERIMENT = "au.gov.ansto.bragg.echidna.ui.MagneticFieldPerspective";
	private static final String ID_ROOM_TEMPERATURE_EXPERIMENT = "au.gov.ansto.bragg.echidna.ui.RobotExperimentPerspective";

	private static final String ID_PERSPECTIVE_DEFAULT = "org.gumtree.ui.isee.workbenchPerspective";

	private static final String ID_PERSPECTIVE_ANALYSIS = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	
	private static Logger logger = LoggerFactory.getLogger(RobotWorkbenchLayoutLauncher.class);
	
	public RobotWorkbenchLayoutLauncher() {
	}

	private void hideMenus(WorkbenchWindow window){
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
	
	public void launch() throws LauncherException {
		{			
			
			IMultiMonitorManager mmManager = new MultiMonitorManager();
			
			// Attempt to close intro
			final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			hideMenus((WorkbenchWindow) activeWorkbenchWindow);
			
			if (activeWorkbenchWindow instanceof WorkbenchWindow) {
//				((WorkbenchWindow) activeWorkbenchWindow).setCoolBarVisible(false);
				activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
				activeWorkbenchWindow.addPerspectiveListener(new IPerspectiveListener() {
					
					@Override
					public void perspectiveChanged(IWorkbenchPage page,
							IPerspectiveDescriptor perspective, String changeId) {
						hideMenus((WorkbenchWindow) activeWorkbenchWindow);
					}
					
					@Override
					public void perspectiveActivated(IWorkbenchPage page,
							IPerspectiveDescriptor perspective) {
						hideMenus((WorkbenchWindow) activeWorkbenchWindow);
					}
				});
			}
			
			PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
				
				@Override
				public void windowOpened(IWorkbenchWindow window) {
					hideMenus((WorkbenchWindow) window);
				}
				
				@Override
				public void windowDeactivated(IWorkbenchWindow window) {
				}
				
				@Override
				public void windowClosed(IWorkbenchWindow window) {
				}
				
				@Override
				public void windowActivated(IWorkbenchWindow window) {
					hideMenus((WorkbenchWindow) window);
				}
			});
			
			final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow window : windows){
				if (window != null && window != activeWorkbenchWindow) {
					window.close();
				}
			}
			if (activeWorkbenchWindow instanceof WorkbenchWindow) {
//				activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
				IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
				for (IWorkbenchPage page : pages) {
					try {
						IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
						for (IPerspectiveDescriptor perspective : perspectives) {
							String pid = perspective.getId();
							if (!ID_CUSTOMISED_TEMPERATURE_EXPERIMENT.equals(pid) 
									&& !ID_HIGH_TEMPERATURE_EXPERIMENT.equals(pid)
									&& !ID_LOW_TEMPERATURE_EXPERIMENT.equals(pid)
									&& !ID_MAGNETICFIELD_EXPERIMENT.equals(pid)
									&& !ID_ROOM_TEMPERATURE_EXPERIMENT.equals(pid)
									){
								activeWorkbenchWindow.getActivePage().closePerspective(perspective, false, true);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			mmManager.showPerspectiveOnOpenedWindow(ID_CUSTOMISED_TEMPERATURE_EXPERIMENT, 0, 0, true);
			mmManager.showPerspectiveOnOpenedWindow(ID_HIGH_TEMPERATURE_EXPERIMENT, 0, 0, true);
			mmManager.showPerspectiveOnOpenedWindow(ID_LOW_TEMPERATURE_EXPERIMENT, 0, 0, true);
			mmManager.showPerspectiveOnOpenedWindow(ID_MAGNETICFIELD_EXPERIMENT, 0, 0, true);
			mmManager.showPerspectiveOnOpenedWindow(ID_ROOM_TEMPERATURE_EXPERIMENT, 0, 0, true);
//			IPerspectiveDescriptor perspective = PlatformUI.getWorkbench(
//					).getPerspectiveRegistry().findPerspectiveWithId(ID_PERSPECTIVE_DEFAULT);
//			if (perspective != null) {
//				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages(
//						)[0].closePerspective(perspective, false, true);
//			}
			
			// Open workflow in screen 2 (maximise only for multi system)
			if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
				// open new window
				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_ANALYSIS, 1, true);
			} else {
			// position it
				mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_ANALYSIS, 1, 1, mmManager.isMultiMonitorSystem());
			}
			
		}
	}

}
