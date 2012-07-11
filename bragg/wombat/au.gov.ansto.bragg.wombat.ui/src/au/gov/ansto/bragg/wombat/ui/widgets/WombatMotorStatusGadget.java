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

package au.gov.ansto.bragg.wombat.ui.widgets;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.ui.widgets.DeviceStatusGadget;

public class WombatMotorStatusGadget extends DeviceStatusGadget {

	public WombatMotorStatusGadget(Composite parent, int style) {
		super(parent, SHOW_UNIT);
		setDeviceURIs(
				"sics://hdb/instrument/crystal/takeoff_angle," +
				"sics://hdb/instrument/crystal/rotate," +
				"sics://hdb/instrument/monochromator/mf2," + 
				"," +
				"sics://hdb/sample/azimuthal_angle," + 
				"sics://hdb/sample/rotate," +
				"," +
				"sics://hdb/sample/translate_x," +
				"sics://hdb/sample/translate_y," +
				"sics://hdb/sample/chi," +
				"sics://hdb/sample/phi," +
				"sics://hdb/sample/eom," +
				"sics://hdb/sample/echi," +
				"sics://hdb/sample/ephi");
	}

}
