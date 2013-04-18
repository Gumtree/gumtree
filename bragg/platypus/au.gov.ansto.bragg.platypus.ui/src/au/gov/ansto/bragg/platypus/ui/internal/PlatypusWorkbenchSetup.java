package au.gov.ansto.bragg.platypus.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.ui.IStartup;
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
public class PlatypusWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "gumtree.startExperimentLayout";
	
	private static Logger logger = LoggerFactory.getLogger(PlatypusWorkbenchSetup.class);
	
	public void earlyStartup() {
		String launchKowariLayout = System.getProperty(PROP_START_EXP_LAYOUT, "false");
		// [GT-73] Launch Kowari 3 monitor layout if required
		if (Boolean.parseBoolean(launchKowariLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Platypus workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					PlatypusWorkbenchLauncher launcher = new PlatypusWorkbenchLauncher();
					launcher.launch();
				}			
			});
		}
	}

}
