package au.gov.ansto.bragg.quokka.workbench;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.ui.IStartup;
import org.gumtree.ui.util.SafeUIRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.quokka.ui.QuokkaWorkbenchLauncher;

/**
 * This class is responsible for launching the special Kowari workbench layout during
 * start up.
 * 
 * @author Tony Lam
 *
 */
public class QuokkaWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "gumtree.startExperimentLayout";
	
	private static Logger logger = LoggerFactory.getLogger(QuokkaWorkbenchSetup.class);
	
	public void earlyStartup() {
		String launchTaipanLayout = System.getProperty(PROP_START_EXP_LAYOUT, "false");
		// [GT-73] Launch Taipan 2 monitor layout if required
		if (Boolean.parseBoolean(launchTaipanLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Quokka workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					QuokkaWorkbenchLauncher launcher = new QuokkaWorkbenchLauncher();
					launcher.launch();
				}			
			});
		}
	}

}
