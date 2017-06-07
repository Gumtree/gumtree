package au.gov.ansto.bragg.quokka.msw.util;

import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;

public class TertiaryShutter {
	// finals
	private static final String ID_DEVICE_TERTIARY_SHUTTER = "plc_tertiary";
	
	// construction
	private TertiaryShutter() {
		
	}
	
	// methods
	public static TertiaryShutterState acquireState() {
		try {
			IComponentController controller = SicsCore
					.getSicsController()
					.findDeviceController(ID_DEVICE_TERTIARY_SHUTTER);
			
			if (controller instanceof IDynamicController) {
				IDynamicController shutterStatus = (IDynamicController)controller;
				IComponentData data = shutterStatus.getValue();
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
