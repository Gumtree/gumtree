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

public class KowariSlitsGadget extends DeviceStatusGadget {

	public KowariSlitsGadget(Composite parent, int style) {
		super(parent, SHOW_UNIT);
		setDeviceURIs(
				"sics://hdb/instrument/slits/primary_psho," +
				"sics://hdb/instrument/slits/primary_psp," +
				"sics://hdb/instrument/slits/primary_psw," +
				"sics://hdb/instrument/slits/secondary_ssho," +
				"sics://hdb/instrument/slits/secondary_ssp," +
				"sics://hdb/instrument/slits/secondary_ssw,");
	}

}
