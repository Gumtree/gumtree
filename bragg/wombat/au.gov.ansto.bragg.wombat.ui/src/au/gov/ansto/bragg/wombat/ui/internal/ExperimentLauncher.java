package au.gov.ansto.bragg.wombat.ui.internal;

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

public class ExperimentLauncher extends AbstractLauncher {

	private static Logger logger = LoggerFactory.getLogger(ExperimentLauncher.class);
			
	
	private static final String ID_PERSPECTIVE_SCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	
	private static final String ID_PERSPECTIVE_DEFAULT = "au.gov.ansto.bragg.nbi.ui.EmptyPerspective";

	private static final String ID_PERSPECTIVE_EXPERIMENT = "au.gov.ansto.bragg.wombat.ui.internal.TCLRunnerPerspective";
	
	private static final String ID_PERSPECTIVE_SICS = "au.gov.ansto.bragg.nbi.ui.SICSExperimentPerspective";
	
		
	public ExperimentLauncher() {
	}

	public void launch() throws LauncherException {	
		// TODO: move this logic to experiment UI manager service
			
		final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow instanceof WorkbenchWindow) {
//			activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
			IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
			for (IWorkbenchPage page : pages) {
				try {
					if (!ID_PERSPECTIVE_EXPERIMENT.equals(page.getPerspective().getId()) 
							&& !ID_PERSPECTIVE_SICS.equals(page.getPerspective().getId())){
						activeWorkbenchWindow.getActivePage().closePerspective(page.getPerspective(), false, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		IMultiMonitorManager mmManager = new MultiMonitorManager();
		// Attempt to close intro
		mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SICS, 0, 0, false);
		mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_EXPERIMENT, 0, 0, mmManager.isMultiMonitorSystem());
		
//		if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
//		// open new window as editor buffer
//			mmManager.openWorkbenchWindow(ID_PERSPECTIVE_DEFAULT, 1, true);
//		}
//		// position it
//		mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 1, 1, mmManager.isMultiMonitorSystem());


	}

}
