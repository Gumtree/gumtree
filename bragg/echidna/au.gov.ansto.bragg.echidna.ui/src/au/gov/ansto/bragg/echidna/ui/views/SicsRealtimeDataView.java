/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package au.gov.ansto.bragg.echidna.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import au.gov.ansto.bragg.nbi.ui.realtime.RealtimeDataViewer;
import au.gov.ansto.bragg.nbi.ui.realtime.SicsRealtimeRourceProvider;

/**
 * @author nxi
 *
 */
public class SicsRealtimeDataView extends ViewPart {

	private static final String SICS_REALTIME_MONITOR_PROPERTY_NAME = "sics.realtime.monitor.devices";
	/**
	 * 
	 */
	public SicsRealtimeDataView() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		RealtimeDataViewer viewer = new RealtimeDataViewer(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(parent);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer);
		final SicsRealtimeRourceProvider provider = new SicsRealtimeRourceProvider();
//		provider.setFilter(new String[]{
//				"/sample/tc1/sensor/sensorValueA",
//				"/sample/tc1/sensor/sensorValueB",
//				"/sample/tc1/sensor/sensorValueC", 
//				"/sample/tc1/sensor/sensorValueD",
//				"/sample/tempone/sensorA",
//				"stth"});
		provider.setFilter(getDeviceFilter());
		viewer.setResourceProvider(provider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public static List<String> getDeviceFilter(){
		String propertyString = System.getProperty("sics.realtime.monitor.devices");
		String[] items = propertyString.split(",");
		List<String> filter = new ArrayList<String>();
		for (String item : items) {
			if (item.trim().length() > 0) {
				filter.add(item.trim());
			}
		}
		return filter;
	}
}
