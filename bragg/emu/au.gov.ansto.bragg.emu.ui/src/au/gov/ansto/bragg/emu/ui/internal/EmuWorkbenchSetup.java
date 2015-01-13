package au.gov.ansto.bragg.emu.ui.internal;

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
public class EmuWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "gumtree.startExperimentLayout";
	
	private static Logger logger = LoggerFactory.getLogger(EmuWorkbenchSetup.class);
	
	public void earlyStartup() {
		String launchExperimentLayout = System.getProperty(PROP_START_EXP_LAYOUT, "true");
		// [GT-73] Launch Taipan 2 monitor layout if required
		if (Boolean.parseBoolean(launchExperimentLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Emu workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					EmuWorkbenchLauncher launcher = new EmuWorkbenchLauncher();
					launcher.launch();
				}			
			});
		}
	}

}
