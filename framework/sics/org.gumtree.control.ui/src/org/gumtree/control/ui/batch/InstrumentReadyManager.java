/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.control.ui.batch;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsModelException;

public class InstrumentReadyManager {

	private static final String PROP_PATH_SISREADY = "gumtree.sics.path.sis_ready";
	
	private static String pathSisReady = System.getProperty(PROP_PATH_SISREADY, "plc_ready");
	
	public static InstrumentReadyStatus isInstrumentReady() {
		ISicsController controller = SicsManager.getSicsModel().findControllerById(pathSisReady);
		if (controller != null) {
			if (controller instanceof IDynamicController) {
				try {
					if (String.valueOf(((IDynamicController) controller).getValue()).equalsIgnoreCase("True")) {
						return new InstrumentReadyStatus(true, null);
					} else {
						return new InstrumentReadyStatus(false, "PLC not ready");
					}
				} catch (SicsModelException e) {
					return new InstrumentReadyStatus(false, "illegal PLC value");
				}
			}
			return new InstrumentReadyStatus(true, null);
		} else {
			return new InstrumentReadyStatus(false, "PLC/SIS not available");
		}
	}

}
