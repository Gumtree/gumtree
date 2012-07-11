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

public class EchidnaRobotGadget extends DeviceStatusGadget {

	private final static URI palletNameURI = URI.create("sics://hdb/sample/robby/Control/Pallet_Nam");
	private final static URI palletIndexURI = URI.create("sics://hdb/sample/robby/Control/Pallet_Idx");
	private final static URI robotStatusURI = URI.create("sics://hdb/sample/robby/status");
	private final static URI sampleInOutURI = URI.create("sics://hdb/sample/robby/setpoint");
	
	public EchidnaRobotGadget(Composite parent, int style) {
		super(parent, SHOW_UNIT);
		setDeviceURIs(palletNameURI + ","
				 + palletIndexURI + ","
				 + sampleInOutURI + ","
				 + robotStatusURI);
		setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				URI uri = (URI) element;
				if (uri.equals(palletNameURI)) {
					return "Pallet Name";
				} else if (uri.equals(palletIndexURI)) {
					return "Sample Position";
				} else if (uri.equals(sampleInOutURI)) {
					return "Sample";
				} else if (uri.equals(robotStatusURI)) {
					return "Robot Status";
				}
				return "";
			}
		});
	}

	@Override
	protected void updateValue(URI uri, String data) {
		if (uri.equals(sampleInOutURI)) {
			String status = data;
			if (data.equals("1")) {
				status = "In";
			}
			else if (data.equals("0")) {
				status = "Out";
			}
			super.updateValue(uri, status);
		} else {
			super.updateValue(uri, data);
		}
	}
}
