package au.gov.ansto.bragg.echidna.ui.internal;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
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

	public void launch() throws LauncherException {
		{			
			
			IMultiMonitorManager mmManager = new MultiMonitorManager();
			
			// Attempt to close intro
			IWorkbench workbench = PlatformUI.getWorkbench();
			try {
				workbench.getIntroManager().closeIntro(workbench.getIntroManager().getIntro());
				workbench.getActiveWorkbenchWindow().getActivePage().closeAllPerspectives(true, false);
			} catch (Exception e) {
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
				mmManager.openWorkbenchWindow(ID_PERSPECTIVE_DEFAULT, 1, true);
			}
			// position it
			mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_ANALYSIS, 1, 1, mmManager.isMultiMonitorSystem());
			
		}
	}

}
