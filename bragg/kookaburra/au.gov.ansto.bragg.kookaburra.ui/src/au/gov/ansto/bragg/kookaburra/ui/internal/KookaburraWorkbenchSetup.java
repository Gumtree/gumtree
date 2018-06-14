package au.gov.ansto.bragg.kookaburra.ui.internal;

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

import au.gov.ansto.bragg.kookaburra.ui.KookaburraWorkbenchLauncher;

/**
 * This class is responsible for launching the special Kowari workbench layout during
 * start up.
 * 
 * @author Tony Lam
 *
 */
public class KookaburraWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "gumtree.startExperimentLayout";
	
	private static Logger logger = LoggerFactory.getLogger(KookaburraWorkbenchSetup.class);
	
	public void earlyStartup() {
		String launchExperimentLayout = System.getProperty(PROP_START_EXP_LAYOUT, "true");
		// [GT-73] Launch Taipan 2 monitor layout if required
		if (Boolean.parseBoolean(launchExperimentLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Taipan workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					KookaburraWorkbenchLauncher launcher = new KookaburraWorkbenchLauncher();
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
					mmManager.showPerspectiveOnOpenedWindow(KookaburraWorkbenchLauncher.ID_PERSPECTIVE_SCRIPTING, 0, 0, true);
				}
			});
		}
	}
}
