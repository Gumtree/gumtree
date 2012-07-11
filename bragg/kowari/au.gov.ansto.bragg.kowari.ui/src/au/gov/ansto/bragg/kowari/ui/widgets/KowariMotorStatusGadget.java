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

package au.gov.ansto.bragg.kowari.ui.widgets;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.ui.widgets.DeviceStatusGadget;

public class KowariMotorStatusGadget extends DeviceStatusGadget {

	public KowariMotorStatusGadget(Composite parent, int style) {
		super(parent, SHOW_UNIT);
		setDeviceURIs(
				"sics://hdb/sample/sx," +
				"sics://hdb/sample/sy," +
				"sics://hdb/sample/sz," +
				"sics://hdb/sample/som," +
				"sics://hdb/sample/stth," +
				"," +
//				"sics://hdb/instrument/crystal/mphi," +
//				"sics://hdb/instrument/crystal/mchi," +
//				"sics://hdb/instrument/crystal/mx," +
//				"sics://hdb/instrument/crystal/my," +
				"sics://hdb/instrument/crystal/mom," +
				"sics://hdb/instrument/crystal/mtth," +
				"sics://hdb/instrument/crystal/mf1," +
				"sics://hdb/instrument/crystal/mf2," +
				"," +
				"sics://hdb/instrument/slits/primary_psho," +
				"sics://hdb/instrument/slits/primary_psp," +
				"sics://hdb/instrument/slits/primary_psw," +
				"sics://hdb/instrument/slits/secondary_ssho," +
				"sics://hdb/instrument/slits/secondary_ssp," +
				"sics://hdb/instrument/slits/secondary_ssw,");
	}

}
