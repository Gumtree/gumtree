package au.gov.ansto.bragg.pelican.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.ui.IStartup;
import org.gumtree.ui.util.SafeUIRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for launching the special Pelican workbench layout during
 * start up.
 * 
 * @author Tony Lam
 *
 */
public class PelicanWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "pelican.startExperimentLayout";
	
	private static Logger logger = LoggerFactory.getLogger(PelicanWorkbenchSetup.class);
	
	public void earlyStartup() {
		String launchTaipanLayout = System.getProperty(PROP_START_EXP_LAYOUT, "true");
		// [GT-73] Launch Taipan 2 monitor layout if required
		if (Boolean.parseBoolean(launchTaipanLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Pelican workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					PelicanWorkbenchLauncher launcher = new PelicanWorkbenchLauncher();
					launcher.launch();
				}			
			});
		}
	}

}
