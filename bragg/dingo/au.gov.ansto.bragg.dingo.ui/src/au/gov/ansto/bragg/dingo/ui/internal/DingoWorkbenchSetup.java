package au.gov.ansto.bragg.dingo.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
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
 * This class is responsible for launching the special Platypus workbench layout during
 * start up.
 * 
 * @author nxi
 *
 */
public class DingoWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "gumtree.startExperimentLayout";
	private static final String ID_PERSPECTIVE_SICS = "au.gov.ansto.bragg.nbi.ui.SICSExperimentPerspective";
	private static final String ID_PERSPECTIVE_SCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	
	private static Logger logger = LoggerFactory.getLogger(DingoWorkbenchSetup.class);
	
	public void earlyStartup() {
		String launchDingoLayout = System.getProperty(PROP_START_EXP_LAYOUT, "false");
		// [GT-73] Launch Dingo layout if required
		if (Boolean.parseBoolean(launchDingoLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Dingo workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (activeWorkbenchWindow instanceof WorkbenchWindow) {
//						activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
						IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
						for (IWorkbenchPage page : pages) {
							try {
//								if (!ID_PERSPECTIVE_SICS.equals(page.getPerspective().getId())){
									activeWorkbenchWindow.getActivePage().closePerspective(page.getPerspective(), false, true);
//								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					IMultiMonitorManager mmManager = new MultiMonitorManager();

					mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 0, 0, mmManager.isMultiMonitorSystem());
				}			
			});
		}
	}

}
