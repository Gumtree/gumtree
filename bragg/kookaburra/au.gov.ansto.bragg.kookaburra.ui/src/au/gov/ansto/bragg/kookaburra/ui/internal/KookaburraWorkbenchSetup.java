package au.gov.ansto.bragg.kookaburra.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.ui.IStartup;
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
		String launchTaipanLayout = System.getProperty(PROP_START_EXP_LAYOUT, "true");
		// [GT-73] Launch Taipan 2 monitor layout if required
		if (Boolean.parseBoolean(launchTaipanLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Taipan workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					KookaburraWorkbenchLauncher launcher = new KookaburraWorkbenchLauncher();
					launcher.launch();
				}			
			});
		}
	}

}
