/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Norman Xiong (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package au.gov.ansto.bragg.echidna.ui.widget;

import java.net.URI;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.ui.widgets.DeviceStatusGadget;

public class EchidnaFurnaceTemperatureGadget extends DeviceStatusGadget {

	private final static URI sensorURI = URI.create("sics://sample/tempone/sensorA");
	private final static URI setpointURI = URI.create("sics://hdb/sample/tempone/setpoint");
	
	public EchidnaFurnaceTemperatureGadget(Composite parent, int style) {
		super(parent, SHOW_UNIT);
		setDeviceURIs(sensorURI + ","
				 + setpointURI);
		setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				URI uri = (URI) element;
				if (uri.equals(sensorURI)) {
					return "Temperature";
				} else if (uri.equals(setpointURI)) {
					return "setpoint";
				}
				return "";
			}
		});
	}

}
