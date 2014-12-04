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
import org.eclipse.ui.intro.IIntroPart;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAQWorkbenchLayoutLauncher extends AbstractLauncher {

	private static final String ID_PERSPECTIVE_STATUS_MONITOR = "org.gumtree.dashboard.ui.rcp.statusMonitorPerspective";
	
	private static final String ID_PERSPECTIVE_SICS = "org.gumtree.gumnix.sics.ui.sicsPerspective";
	
	private static final String ID_PERSPECTIVE_EXPERIMENT = "au.gov.ansto.bragg.echidna.ui.internal.TCLRunnerPerspective";

	private static final String ID_PERSPECTIVE_DEFAULT = "org.gumtree.ui.isee.workbenchPerspective";

//	private static final String ID_PERSPECTIVE_KAKADU = "au.gov.ansto.bragg.kakadu.ui.KakaduPerspective";
	private static final String ID_PERSPECTIVE_KAKADU = "au.gov.ansto.bragg.echidna.ui.internal.EchidnaAnalysisPerspective";
	
	private static final String ID_WORKFLOW_MULTI_SAMPLE = "au.gov.ansto.bragg.quokka.multiSampleExperimentWorkflow";
	
	private static final String PROP_STATUS_DASHBOARD_CONFIG_FILE = "status.dashboardConfigFile";
	
	private static Logger logger = LoggerFactory.getLogger(DAQWorkbenchLayoutLauncher.class);
	
	public DAQWorkbenchLayoutLauncher() {
	}

	private void hideMenus(WorkbenchWindow window){
		WorkbenchWindow workbenchWin = (WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MenuManager menuManager = ((WorkbenchWindow) window).getMenuManager();
		IContributionItem[] items = menuManager.getItems();

		for(IContributionItem item : items) {
		  item.setVisible(false);
		}
		menuManager.setVisible(false);
	    
	    IContributionItem[] menubarItems = ((WorkbenchWindow) window).getMenuBarManager().getItems();
	    for (IContributionItem item : menubarItems) {
	    	item.setVisible(false);
	    }
	    ((WorkbenchWindow) window).getMenuBarManager().setVisible(false);
	    
	}
	
	public void launch() throws LauncherException {
		{			
			// TODO: move this logic to experiment UI manager service
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
						
			IMultiMonitorManager mmManager = ServiceUtils.getService(IMultiMonitorManager.class);
			// Prepare status in screen 1 (maximised)
//			String configFile = System.getProperty(PROP_STATUS_DASHBOARD_CONFIG_FILE);
//			if (configFile != null) {
//				try {
//					IFileStore fileStore = EFS.getStore(new URI(configFile));
//					IDashboardConfig config = DashboardConfigUtils.loadDashboardConfig(fileStore.openInputStream(EFS.NONE, new NullProgressMonitor()));
//					DashboardUI.setStatusViewConfig(config);
//				} catch (Exception e) {
//					// Oops ... nothing I can do
//					logger.error("Cannot load dashboard config while setting to status monitor.", e);
//				}
//			}
			
			// Attempt to close intro
			IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
			PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
			
//			InstrumentDashboardLauncher launcher = new InstrumentDashboardLauncher();
//			launcher.launch(0);
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_EXPERIMENT, 0, 0, true);
			IPerspectiveDescriptor perspective = PlatformUI.getWorkbench(
			).getPerspectiveRegistry().findPerspectiveWithId(ID_PERSPECTIVE_DEFAULT);
			if (perspective != null) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages(
				)[0].closePerspective(perspective, false, true);
			}
			
			// Open workflow in screen 2 (maximise only for multi system)
			if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
				// open new window
				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_DEFAULT, 1, true);
			}
			// position it
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_KAKADU, 1, 1, mmManager.isMultiMonitorSystem());
			
			// Open kakadu in screen 3 (maximise only for multi system)
//			if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 3) {
//				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_DEFAULT, 2, true);
//			}
//			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_KAKADU, 2, 2, mmManager.isMultiMonitorSystem());
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().hideInforBar();
			
			// hack
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Open table tree
//			Action action = new SicsControlLaunchAction();
//			action.run();
		}
	}

}
