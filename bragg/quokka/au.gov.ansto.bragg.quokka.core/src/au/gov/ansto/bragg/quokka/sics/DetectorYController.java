package au.gov.ansto.bragg.quokka.sics;

import org.gumtree.gumnix.sics.control.controllers.DrivableController;
import org.gumtree.gumnix.sics.core.SicsCore;

import ch.psi.sics.hipadaba.Component;

public class DetectorYController extends DrivableController {

	private static final String ID_DEVICE = "anticollider";
	
	public DetectorYController(Component component) {
		super(component);
	}

	public void activate() {
		super.activate();
		// Remove old listener (det)
		SicsCore.getSicsManager().monitor().removeStateMonitor(getDeviceName(), this);
		SicsCore.getSicsManager().monitor().removeStateMonitor(getPath(), this);
		// Add new one (anticollider)
		SicsCore.getSicsManager().monitor().addStateMonitor(ID_DEVICE, this);
	}
	
}
