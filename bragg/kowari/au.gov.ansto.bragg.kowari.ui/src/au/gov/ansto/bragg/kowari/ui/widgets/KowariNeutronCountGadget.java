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

import java.net.URI;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.ui.widgets.DeviceStatusGadget;

public class KowariNeutronCountGadget extends DeviceStatusGadget {

	private static URI URI_BM1_COUNTS = URI.create("sics://hdb/monitor/bm1_counts");
	
	private static URI URI_BM2_COUNTS = URI.create("sics://hdb/monitor/bm2_counts");
	
	private static URI URI_BM3_COUNTS = URI.create("sics://hdb/monitor/bm3_counts");
	
	private static URI URI_TOTAL_DETECTOR_COUNT = URI.create("sics://hdb/instrument/detector/total_counts");
	
	public KowariNeutronCountGadget(Composite parent, int style) {
		super(parent, style);
		getDeviceURIList().add(URI_BM1_COUNTS);
//		getDeviceURIList().add(URI_BM2_COUNTS);
//		getDeviceURIList().add(URI_BM3_COUNTS);
		getDeviceURIList().add(URI_TOTAL_DETECTOR_COUNT);
		setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				URI uri = (URI) element;
				if (uri.equals(URI_BM1_COUNTS)) {
					return "BM1 Counts";
				} 
//				else if (uri.equals(URI_BM2_COUNTS)) {
//					return "BM2 Counts";
//				} else if (uri.equals(URI_BM3_COUNTS)) {
//					return "BM3 Counts";
//				} 
				else if (uri.equals(URI_TOTAL_DETECTOR_COUNT)) {
					return "Det Counts";
				}
				return "";
			}
		});
	}

}
