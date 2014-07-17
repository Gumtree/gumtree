package au.gov.ansto.bragg.quokka.sics;

import org.gumtree.gumnix.sics.control.ComponentControllerFactory;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.ISicsObjectController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;

public class QuokkaComponentControllFactory extends ComponentControllerFactory {

	private static final String ID_COMPONENT_SAMPLENUM = "sampleNum";
	
	private static final String ID_COMPONENT_DETECTOR_Y = "detector_y";
	
	private static final String ID_COMPONENT_DETECTOR_X = "detector_x";
	
	private static final String ID_COMPONENT_TC_1 = "tc1";
	
	private static final String ID_COMPONENT_SELBSN = "selbsn";
	
	private static final String ID_COMPONENT_SELBS_XZ = "selbsxz";
	
	private static final Logger logger = LoggerFactory.getLogger(QuokkaComponentControllFactory.class);
	
	public IComponentController createComponentController(Component component) {
		if (component.getId().equals(ID_COMPONENT_SAMPLENUM)) {
			return new SampleHolderController(component);
		} else if (component.getId().equals(ID_COMPONENT_DETECTOR_Y)) {
			// hack
			return new DetectorYController(component);
		}else if (component.getId().equals(ID_COMPONENT_DETECTOR_X)) {
			// hack
			return new DetectorYController(component);
//		}else if (component.getId().equals(ID_COMPONENT_TC_1)) {
//			try {
//				return new JulaboLH45Controller(component);	
//			} catch (Exception e) {
//				logger.warn("HDB setting is incorrect, generic controller will be used instead.", e);
//			}
		} else if (component.getId().equals(ID_COMPONENT_SELBSN)) {
			return new BeamStopCommandController(component);
		} else if (component.getId().equals(ID_COMPONENT_SELBS_XZ)) {
			return new BeamStopCommandController(component);
		}
		return super.createComponentController(component);
	}
	
	public ISicsObjectController[] createSicsObjectControllers() {
		return new ISicsObjectController[] { new DetectorHighVoltageController2() };
	}
	
}
