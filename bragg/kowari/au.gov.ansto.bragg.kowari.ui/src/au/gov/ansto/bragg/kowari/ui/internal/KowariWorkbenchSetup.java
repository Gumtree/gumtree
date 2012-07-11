package au.gov.ansto.bragg.kowari.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.ui.IStartup;
import org.gumtree.ui.util.SafeUIRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.kowari.ui.KowariWorkbenchLauncher;

/**
 * This class is responsible for launching the special Kowari workbench layout during
 * start up.
 * 
 * @author Tony Lam
 *
 */
public class KowariWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "kowari.startExperimentLayout";
	
	private static Logger logger = LoggerFactory.getLogger(KowariWorkbenchSetup.class);
	
	public void earlyStartup() {
		String launchKowariLayout = System.getProperty(PROP_START_EXP_LAYOUT, "false");
		// [GT-73] Launch Kowari 3 monitor layout if required
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
		}
	}

}
