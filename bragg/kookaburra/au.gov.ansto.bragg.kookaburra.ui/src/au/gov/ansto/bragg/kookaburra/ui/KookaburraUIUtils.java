package au.gov.ansto.bragg.kookaburra.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Shell;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KookaburraUIUtils {

	private static final Logger logger = LoggerFactory.getLogger(KookaburraUIUtils.class);
	
	/** [GT-99] Property for checking tertiary shutter */
	private static final String PROP_CHECK_TERTIARY_SHUTTER = "kookaburra.scan.checkTertiaryShutter";
	
	/** Device id for tertiary shutter */
	private static final String ID_DEVICE_TERTIARY_SHUTTER = "plc_tertiary";
	
	private static Boolean confirmed;
	
	// Note: not thread safe
	public static boolean checkTertiaryShutter(final Shell shell) {
		boolean checkShutter = Boolean.valueOf(System.getProperty(PROP_CHECK_TERTIARY_SHUTTER));
		boolean shutterOpened = true;
		if (checkShutter) {
			IComponentController controller = SicsCore.getSicsController().findDeviceController(ID_DEVICE_TERTIARY_SHUTTER);
			if (controller != null && controller instanceof IDynamicController) {
				IDynamicController shutterStatus = (IDynamicController) controller;
				try {
					String shutterStatusValue = shutterStatus.getValue().getStringData();
					shutterOpened = shutterStatusValue.equalsIgnoreCase("Opened") | shutterStatusValue.equalsIgnoreCase("OPEN");
				} catch (SicsIOException e) {
					logger.error("Failed to read from device " + ID_DEVICE_TERTIARY_SHUTTER, e);
					// Don't go any further
					return false;
				}
			}
			// else assume this is opened
		}
		if (!shutterOpened) {
			confirmed = null;
			// Prompt error diaglog if possible
			if (shell != null) {
				SafeUIRunner.asyncExec(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						confirmed = MessageDialog
								.openConfirm(shell, "Tertiary shutter closed",
										"Tertiary shutter is closed. Please open the shutter and press OK to conitune");
					}
				});
			}
			// Wait for confirmation
			LoopRunner.run(new ILoopExitCondition() {
				@Override
				public boolean getExitCondition() {
					return confirmed != null;
				}
			}, LoopRunner.NO_TIME_OUT);
			return confirmed;
		}
		// Nothing wrong
		return true;
	}
	
}
