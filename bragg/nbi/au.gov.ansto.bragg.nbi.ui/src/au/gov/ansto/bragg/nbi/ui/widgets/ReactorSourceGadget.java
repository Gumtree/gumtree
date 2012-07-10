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

package au.gov.ansto.bragg.nbi.ui.widgets;

import java.net.URI;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.ui.widgets.DeviceStatusGadget;

import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;

public class ReactorSourceGadget extends DeviceStatusGadget {

	private static URI URI_POWER = URI.create("sics://hdb/instrument/source/power");
	
	private static URI URI_CNS_IN = URI.create("sics://hdb/instrument/source/cns_inlet_temp");
	
	private static URI URI_CNS_OUT = URI.create("sics://hdb/instrument/source/cns_outlet_temp");
	
	public ReactorSourceGadget(Composite parent, int style) {
		super(parent, SHOW_ICON | SHOW_UNIT);
		getDeviceURIList().add(URI_POWER);
		getDeviceURIList().add(URI_CNS_IN);
		getDeviceURIList().add(URI_CNS_OUT);
		setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				URI uri = (URI) element;
				if (uri.equals(URI_POWER)) {
					return InternalImage.POWER_16.getImage();
				} else if (uri.equals(URI_CNS_IN)) {
					return InternalImage.INLET_16.getImage();
				} else if (uri.equals(URI_CNS_OUT)) {
					return InternalImage.OUTLET_16.getImage();
				}
				return null;
			}
			public String getText(Object element) {
				URI uri = (URI) element;
				if (uri.equals(URI_POWER)) {
					return "Power";
				} else if (uri.equals(URI_CNS_IN)) {
					return "CNS In";
				} else if (uri.equals(URI_CNS_OUT)) {
					return "CNS Out";
				}
				return "";
			}
		});
	}

}
