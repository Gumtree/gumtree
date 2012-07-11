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

package au.gov.ansto.bragg.echidna.ui.widget;

import java.net.URI;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.ui.widgets.DeviceStatusGadget;

public class NeutronCountRateGadget extends DeviceStatusGadget {

	private static URI URI_BM1_RATE = URI.create("sics://hdb/monitor/bm1_event_rate");
	
	private static URI URI_BM2_RATE = URI.create("sics://hdb/monitor/bm2_event_rate");
	
	public NeutronCountRateGadget(Composite parent, int style) {
		super(parent, style);
		getDeviceURIList().add(URI_BM1_RATE);
		getDeviceURIList().add(URI_BM2_RATE);
		setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				URI uri = (URI) element;
				if (uri.equals(URI_BM1_RATE)) {
					return "BM1 Counts/s";
				} 
				else if (uri.equals(URI_BM2_RATE)) {
					return "BM2 Counts/s";
				}
				return "";
			}
		});
	}

}
