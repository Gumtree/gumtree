package au.gov.ansto.bragg.quokka.msw.util;

import org.gumtree.control.core.IControllerData;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.core.SicsCore;

import au.gov.ansto.bragg.nbi.core.NBISystemProperties;

public class TertiaryShutter {
	// finals
	private static final String ID_DEVICE_TERTIARY_SHUTTER = "plc_tertiary";
	
	// construction
	private TertiaryShutter() {
		
	}
	
	// methods
	public static TertiaryShutterState acquireState() {
		if (NBISystemProperties.USE_NEW_PROXY) {
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
		} else {
			try {
				IComponentController controller = SicsCore
						.getSicsController()
						.findDeviceController(ID_DEVICE_TERTIARY_SHUTTER);
				
				if (controller instanceof org.gumtree.gumnix.sics.control.controllers.IDynamicController) {
					org.gumtree.gumnix.sics.control.controllers.IDynamicController shutterStatus = (org.gumtree.gumnix.sics.control.controllers.IDynamicController)controller;
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
		}		
		return TertiaryShutterState.UNKNOWN;
	}
}
