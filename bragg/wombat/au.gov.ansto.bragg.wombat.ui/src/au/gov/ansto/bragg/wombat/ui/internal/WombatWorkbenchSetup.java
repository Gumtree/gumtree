package au.gov.ansto.bragg.wombat.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.ui.IStartup;
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
		}
	}

}
