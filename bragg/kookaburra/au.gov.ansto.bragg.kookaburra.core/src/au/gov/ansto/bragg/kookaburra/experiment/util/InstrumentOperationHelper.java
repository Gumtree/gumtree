package au.gov.ansto.bragg.kookaburra.experiment.util;

import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instrument operation helpers provides a set of static methods for helping to
 * drive the instrument.
 * 
 */
public final class InstrumentOperationHelper {

	// System property option for the sample holder loading position (samx position)
//	private static final String PROP_LOAD_POSITION = "quokka.loadPosition";

	private static final Logger logger = LoggerFactory.getLogger(InstrumentOperationHelper.class);
	
	// Asynchronous
	public static void setToSampleLoadPosition() {
		try {
			IDynamicController samx = (IDynamicController) SicsCore.getSicsController().findDeviceController("samx");
			IComponentData softlowerlim = ((IDynamicController) samx.getChildController("/softlowerlim")).getValue();
			samx.setTargetValue(softlowerlim);
//			samx.setTargetValue(ComponentData.createData(System.getProperty(PROP_LOAD_POSITION)));
			samx.commitTargetValue(null);
		} catch (SicsIOException e) {
			logger.error("Failed to drive samx to load position.", e);
		}
	}
	
	private InstrumentOperationHelper() {
		super();
	}
	
}
