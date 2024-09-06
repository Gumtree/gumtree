package au.gov.ansto.bragg.wombat.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
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

/**
 * This class is responsible for launching the special Echidna workbench layout during
 * start up.
 * 
 * @author Tony Lam
 *
 */
public class WombatWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "gumtree.startExperimentLayout";
	
	private static Logger logger = LoggerFactory.getLogger(WombatWorkbenchSetup.class);
	
	public void earlyStartup() {
		String launchExperimentLayout = System.getProperty(PROP_START_EXP_LAYOUT, "false");
		// [GT-73] Launch Kowari 3 monitor layout if required
		if (Boolean.parseBoolean(launchExperimentLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Wombat workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					ExperimentLauncher launcher = new ExperimentLauncher();
					launcher.launch();
				}			
			});
		} else {
			final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow window : windows) {
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
					mmManager.showPerspectiveOnOpenedWindow(WombatAnalysisLauncher.ID_PERSPECTIVE_SCRIPTING, 0, 0, true);
				}
			});
		}
	}

}
