/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.experiment.util;

import org.gumtree.gumnix.sics.control.controllers.ComponentData;
import org.gumtree.gumnix.sics.control.controllers.ComponentDataFormatException;
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
			float softupperlim = ((IDynamicController) samx.getChildController("/softupperlim")).getValue().getFloatData();
			float tolerance = ((IDynamicController) samx.getChildController("/precision")).getValue().getFloatData();
			float hardupperlim = ((IDynamicController) samx.getChildController("/hardupperlim")).getValue().getFloatData();
			float softzero = ((IDynamicController) samx.getChildController("/softzero")).getValue().getFloatData();
			if (softupperlim > hardupperlim - softzero) {
				softupperlim = hardupperlim - softzero;
			}
			samx.setTargetValue(ComponentData.createData(softupperlim - tolerance));
//			samx.setTargetValue(ComponentData.createData(System.getProperty(PROP_LOAD_POSITION)));
			samx.commitTargetValue(null);
		} catch (SicsIOException e) {
			logger.error("Failed to drive samx to load position.", e);
		} catch (ComponentDataFormatException e) {
			logger.error("Failed to drive samx to load position.", e);
		}
	}
	
	private InstrumentOperationHelper() {
		super();
	}

}
