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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.ui.widgets.DeviceStatusGadget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;

public class TemperatureSensorGadget extends DeviceStatusGadget {

	private static final Logger logger = LoggerFactory.getLogger(TemperatureSensorGadget.class);
	
	private static URI URI_TC1_SENSOR_A = URI.create("sics://hdb/sample/tc1/sensor/sensorValueA");
	
	private static URI URI_TC1_SENSOR_B = URI.create("sics://hdb/sample/tc1/sensor/sensorValueB");
	
	private static URI URI_TC1_SENSOR_C = URI.create("sics://hdb/sample/tc1/sensor/sensorValueC");
	
	private static URI URI_TC1_SENSOR_D = URI.create("sics://hdb/sample/tc1/sensor/sensorValueD");
	
	private static URI URI_TC1_CONTROL_1 = URI.create("sics://hdb/sample/tc1/sensor/ctrl_Loop_1");
	
	private static URI URI_TC1_CONTROL_2 = URI.create("sics://hdb/sample/tc1/sensor/ctrl_Loop_2");
	
	private static URI URI_TC1_CONTROL_3 = URI.create("sics://hdb/sample/tc1/sensor/ctrl_Loop_3");
	
	private static URI URI_TC1_CONTROL_4 = URI.create("sics://hdb/sample/tc1/sensor/ctrl_Loop_4");
	
	private static URI URI_TC2_SENSOR_A = URI.create("sics://hdb/sample/tc2_cntrl/sensorA/value");
	
	private static URI URI_TC2_SENSOR_B = URI.create("sics://hdb/sample/tc2_cntrl/sensorB/value");
	
	private static URI URI_TC2_SENSOR_C = URI.create("sics://hdb/sample/tc2_cntrl/sensorC/value");
	
	private static URI URI_TC2_SENSOR_D = URI.create("sics://hdb/sample/tc2_cntrl/sensorD/value");
	
	private static URI URI_TC2_SENSOR_CONTROL = URI.create("sics://hdb/sample/tc2_cntrl/controlsensor");
	
	public TemperatureSensorGadget(Composite parent, int style) {
		super(parent, SHOW_ICON);
		setDeviceURIs(
				URI_TC1_SENSOR_A + "," +
				URI_TC1_SENSOR_B + "," +
				URI_TC1_SENSOR_C + "," +
				URI_TC1_SENSOR_D + "," +
				"," +
				URI_TC2_SENSOR_A + "," +
				URI_TC2_SENSOR_B + "," +
				URI_TC2_SENSOR_C + "," +
				URI_TC2_SENSOR_D
		);
		setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				URI uri = (URI) element;
				if (uri.equals(URI_TC1_SENSOR_A) || uri.equals(URI_TC2_SENSOR_A)) {
					return InternalImage.A.getImage();
				} else if (uri.equals(URI_TC1_SENSOR_B) || uri.equals(URI_TC2_SENSOR_B)) {
					return InternalImage.B.getImage();
				} else if (uri.equals(URI_TC1_SENSOR_C) || uri.equals(URI_TC2_SENSOR_C)) {
					return InternalImage.C.getImage();
				} else if (uri.equals(URI_TC1_SENSOR_D) || uri.equals(URI_TC2_SENSOR_D)) {
					return InternalImage.D.getImage();
				}
				return null;
			}
			public String getText(Object element) {
				URI uri = (URI) element;
				if (uri.equals(URI_TC1_SENSOR_A)) {
					return "TC1 SensorA";
				} else if (uri.equals(URI_TC1_SENSOR_B)) {
					return "TC1 SensorB";
				} else if (uri.equals(URI_TC1_SENSOR_C)) {
					return "TC1 SensorC";
				} else if (uri.equals(URI_TC1_SENSOR_D)) {
					return "TC1 SensorD";
				} else if (uri.equals(URI_TC2_SENSOR_A)) {
					return "TC2 SensorA";
				} else if (uri.equals(URI_TC2_SENSOR_B)) {
					return "TC2 SensorB";
				} else if (uri.equals(URI_TC2_SENSOR_C)) {
					return "TC2 SensorC";
				} else if (uri.equals(URI_TC2_SENSOR_D)) {
					return "TC2 SensorD";
				}
				return "";
			}
		});
	}

	protected void setupUI() {
		super.setupUI();
		// TC 1
		try {
			getContexts().put(URI_TC1_CONTROL_1, null);
			getContexts().put(URI_TC1_CONTROL_2, null);
			getContexts().put(URI_TC1_CONTROL_3, null);
			getContexts().put(URI_TC1_CONTROL_4, null);
			// Fetch current value	
			updateValue(URI_TC1_CONTROL_1, getDam().get(URI_TC1_CONTROL_1, String.class));	
			updateValue(URI_TC1_CONTROL_2, getDam().get(URI_TC1_CONTROL_2, String.class));
			updateValue(URI_TC1_CONTROL_3, getDam().get(URI_TC1_CONTROL_3, String.class));
			updateValue(URI_TC1_CONTROL_4, getDam().get(URI_TC1_CONTROL_4, String.class));
		} catch (Exception e) {
			// Temperature control does not exist
		}
		
		// TC 2
		try {
			getContexts().put(URI_TC2_SENSOR_CONTROL, null);
			updateValue(URI_TC2_SENSOR_CONTROL, getDam().get(URI_TC2_SENSOR_CONTROL, String.class));
		}  catch (Exception e) {
			// Temperature control does not exist
		}
	}
	
	protected void updateValue(URI uri, String data) {
		if (uri.equals(URI_TC1_CONTROL_1) || uri.equals(URI_TC1_CONTROL_2)
				|| uri.equals(URI_TC1_CONTROL_3) || uri.equals(URI_TC1_CONTROL_4)) {
			// Not efficient, but it is the safest way to do
			// Refresh to initial value
			updateStatusColour(getContexts().get(URI_TC1_SENSOR_A).displayLabel, false);
			updateStatusColour(getContexts().get(URI_TC1_SENSOR_B).displayLabel, false);
			updateStatusColour(getContexts().get(URI_TC1_SENSOR_C).displayLabel, false);
			updateStatusColour(getContexts().get(URI_TC1_SENSOR_D).displayLabel, false);
			// Check all controls
			updateStatusColour(getDam().get(URI_TC1_CONTROL_1, String.class));
			updateStatusColour(getDam().get(URI_TC1_CONTROL_2, String.class));
			updateStatusColour(getDam().get(URI_TC1_CONTROL_3, String.class));
			updateStatusColour(getDam().get(URI_TC1_CONTROL_4, String.class));
		} else if (uri.equals(URI_TC2_SENSOR_CONTROL)) {
			// Refresh to initial value
			updateStatusColour(getContexts().get(URI_TC2_SENSOR_A).displayLabel, false);
			updateStatusColour(getContexts().get(URI_TC2_SENSOR_B).displayLabel, false);
			updateStatusColour(getContexts().get(URI_TC2_SENSOR_C).displayLabel, false);
			updateStatusColour(getContexts().get(URI_TC2_SENSOR_D).displayLabel, false);
			// Check all controls
			updateStatusColour(getDam().get(URI_TC2_SENSOR_CONTROL, String.class));
		} else {
			super.updateValue(uri, data);
		}
	}
	
	private void updateStatusColour(String data) {
		try {
			if (data.startsWith("enabled")) {
				// TC 1
				String[] dataTokens = data.split(",");
				if (dataTokens[1].contains("A")) {
					updateStatusColour(getContexts().get(URI_TC1_SENSOR_A).displayLabel, true);
				} else if (dataTokens[1].contains("B")) {
					updateStatusColour(getContexts().get(URI_TC1_SENSOR_B).displayLabel, true);
				} else if (dataTokens[1].contains("C")) {
					updateStatusColour(getContexts().get(URI_TC1_SENSOR_C).displayLabel, true);
				} else if (dataTokens[1].contains("D")) {
					updateStatusColour(getContexts().get(URI_TC1_SENSOR_D).displayLabel, true);
				}
			} else if (data.startsWith("sensor")) {
				// TC 2
				if (data.contains("A")) {
					updateStatusColour(getContexts().get(URI_TC2_SENSOR_A).displayLabel, true);
				} else if (data.contains("B")) {
					updateStatusColour(getContexts().get(URI_TC2_SENSOR_B).displayLabel, true);
				} else if (data.contains("C")) {
					updateStatusColour(getContexts().get(URI_TC2_SENSOR_C).displayLabel, true);
				} else if (data.contains("D")) {
					updateStatusColour(getContexts().get(URI_TC2_SENSOR_D).displayLabel, true);
				}
			}
		} catch (Exception e) {
			logger.error("Invalid control loop status: " + data, e);
		}	
	}
	
	private void updateStatusColour(Label label, boolean enabled) {
		if (label == null) {
			return;
		}
		if (enabled) {
			label.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));
			label.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		} else {
			label.setBackground(getBackground());
			label.setForeground(getForeground());
		}
	}
	
}
