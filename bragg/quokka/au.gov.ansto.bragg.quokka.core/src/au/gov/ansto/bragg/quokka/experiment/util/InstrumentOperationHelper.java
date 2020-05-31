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

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.model.ControllerData;
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
			IDynamicController samx = (IDynamicController) SicsManager.getSicsModel(
					).findControllerById("samx");
			float softupperlim = ((IDynamicController) samx.getChild("/softupperlim")
					).getControllerDataValue().getFloatData();
			float tolerance = ((IDynamicController) samx.getChild("/precision")
					).getControllerDataValue().getFloatData();
			float hardupperlim = ((IDynamicController) samx.getChild("/hardupperlim")
					).getControllerDataValue().getFloatData();
			float softzero = ((IDynamicController) samx.getChild("/softzero")
					).getControllerDataValue().getFloatData();
			if (softupperlim > hardupperlim - softzero) {
				softupperlim = hardupperlim - softzero;
			}
			samx.setTargetValue(ControllerData.createData(softupperlim - tolerance));
//			samx.setTargetValue(ComponentData.createData(System.getProperty(PROP_LOAD_POSITION)));
			samx.commitTargetValue();
		} catch (SicsException e) {
			logger.error("Failed to drive samx to load position.", e);
		} 
	}
	
	private InstrumentOperationHelper() {
		super();
	}

}
