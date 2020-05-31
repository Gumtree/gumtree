package au.gov.ansto.bragg.quokka.msw.util;

import org.gumtree.control.core.IControllerData;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;

public class TertiaryShutter {
	// finals
	private static final String ID_DEVICE_TERTIARY_SHUTTER = "plc_tertiary";
	
	// construction
	private TertiaryShutter() {
		
	}
	
	// methods
	public static TertiaryShutterState acquireState() {
		try {
			ISicsController controller = SicsManager.getSicsModel().findControllerById(
					ID_DEVICE_TERTIARY_SHUTTER);
			
			if (controller instanceof IDynamicController) {
				IDynamicController shutterStatus = (IDynamicController)controller;
				IControllerData data = shutterStatus.getControllerDataValue();
				if (data != null) {
					String shutterStatusValue = data.getStringData();
					if (shutterStatusValue != null)
						if (shutterStatusValue.toLowerCase().startsWith("open")) // open or opened
							return TertiaryShutterState.OPEN;
						else
							return TertiaryShutterState.CLOSED;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return TertiaryShutterState.UNKNOWN;
	}
}
