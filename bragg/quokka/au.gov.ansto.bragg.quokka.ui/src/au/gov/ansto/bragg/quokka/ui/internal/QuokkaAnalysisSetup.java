package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.ui.IStartup;
import org.gumtree.ui.util.SafeUIRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for launching the special Kowari workbench layout during
 * start up.
 * 
 * @author Tony Lam
 *
 */
public class QuokkaAnalysisSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "quokka.startAnalysisLayout";
	
	private static Logger logger = LoggerFactory.getLogger(QuokkaAnalysisSetup.class);
	
	public void earlyStartup() {
		String launchQuokkaAnalysisLayout = System.getProperty(PROP_START_EXP_LAYOUT, "false");
		// [GT-73] Launch Kowari 3 monitor layout if required
		if (Boolean.parseBoolean(launchQuokkaAnalysisLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Quokka analysis layout during early startup.", exception);
				}
				public void run() throws Exception {
					AnalysisApplicationLauncher launcher = new AnalysisApplicationLauncher();
					launcher.launch();
				}			
			});
		}
	}

}
