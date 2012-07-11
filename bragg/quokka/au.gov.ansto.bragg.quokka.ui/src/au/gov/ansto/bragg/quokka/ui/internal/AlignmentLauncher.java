package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.internal.ui.actions.SicsControlLaunchAction;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignmentLauncher extends AbstractLauncher {

	private static Logger logger = LoggerFactory.getLogger(AlignmentLauncher.class);
			
	private static final String ID_PERSPECTIVE_STATUS_MONITOR = "org.gumtree.dashboard.ui.rcp.statusMonitorPerspective";
	
	private static final String ID_PERSPECTIVE_ALIGNMENT = "au.gov.ansto.bragg.quokka.ui.alignmentPerspective";
	
	private static final String ID_PERSPECTIVE_SICS = "org.gumtree.gumnix.sics.ui.sicsPerspective";
	
	private static final String PROP_STATUS_DASHBOARD_CONFIG_FILE = "status.dashboardConfigFile";
	
	public AlignmentLauncher() {
	}

	public void launch() throws LauncherException {	
		// TODO: move this logic to experiment UI manager service
			
		IMultiMonitorManager mmManager = ServiceUtils.getService(IMultiMonitorManager.class);
		// Prepare status in screen 1 (maximised)
		mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_STATUS_MONITOR, 0, 0, true);
//		String configFile = System.getProperty(PROP_STATUS_DASHBOARD_CONFIG_FILE);
//		if (configFile != null) {
//			try {
//				IFileStore fileStore = EFS.getStore(new URI(configFile));
//				IDashboardConfig config = DashboardConfigUtils.loadDashboardConfig(fileStore.openInputStream(EFS.NONE, new NullProgressMonitor()));
//				DashboardUI.setStatusViewConfig(config);
//			} catch (Exception e) {
//				// Oops ... nothing I can do
//				logger.error("Cannot load dashboard config while setting to status monitor.", e);
//			}
//		}
		
		// Attempt to close intro
		IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
		
		// Open workflow in screen 2 (maximise only for multi system)
		if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
			mmManager.openWorkbenchWindow(ID_PERSPECTIVE_ALIGNMENT, 1, true);
		}
		mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_ALIGNMENT, 1, 1, mmManager.isMultiMonitorSystem());
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().hideLaunchBar();
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().hideLaunchBar();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
		
		// Open kakadu in screen 3 (maximise only for multi system)
		if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 3) {
			mmManager.openWorkbenchWindow(ID_PERSPECTIVE_SICS, 1, true);
		}
		mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SICS, 2, 2, mmManager.isMultiMonitorSystem());
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().hideInforBar();
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().hideLaunchBar();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
		
		// hack
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Open table tree
		Action action = new SicsControlLaunchAction();
		action.run();
	}

}
