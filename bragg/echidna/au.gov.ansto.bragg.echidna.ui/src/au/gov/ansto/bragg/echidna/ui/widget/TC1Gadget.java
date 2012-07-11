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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.ui.widgets.DeviceStatusGadget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.echidna.ui.internal.Activator;


public class TC1Gadget extends DeviceStatusGadget {

	private static final Logger logger = LoggerFactory.getLogger(TC1Gadget.class);
	
	private static URI URI_TC1_SENSOR_A = URI.create("sics://hdb/sample/tc1/sensor/sensorValueA");
	
	private static URI URI_TC1_SENSOR_B = URI.create("sics://hdb/sample/tc1/sensor/sensorValueB");
	
	private static URI URI_TC1_SENSOR_C = URI.create("sics://hdb/sample/tc1/sensor/sensorValueC");
	
	private static URI URI_TC1_SENSOR_D = URI.create("sics://hdb/sample/tc1/sensor/sensorValueD");
	
	private static URI URI_TC1_CONTROL_1 = URI.create("sics://hdb/sample/tc1/sensor/ctrl_Loop_1");
	
	private static URI URI_TC1_CONTROL_2 = URI.create("sics://hdb/sample/tc1/sensor/ctrl_Loop_2");
	
	private static URI URI_TC1_CONTROL_3 = URI.create("sics://hdb/sample/tc1/sensor/ctrl_Loop_3");
	
	private static URI URI_TC1_CONTROL_4 = URI.create("sics://hdb/sample/tc1/sensor/ctrl_Loop_4");
	
	private static URI URI_TC1_SETPOINT_1 = URI.create("sics://hdb/sample/tc1/sensor/setpoint1");
	
	private static URI URI_TC1_SETPOINT_2 = URI.create("sics://hdb/sample/tc1/sensor/setpoint2");
	
	private static URI URI_TC1_SETPOINT_3 = URI.create("sics://hdb/sample/tc1/sensor/setpoint3");
	
	private static URI URI_TC1_SETPOINT_4 = URI.create("sics://hdb/sample/tc1/sensor/setpoint4");
	
	private static URI URI_TC1_HEATERRANGE_1 = URI.create("sics://hdb/sample/tc1/heater/heaterRange_1");
	
	private static URI URI_TC1_HEATERRANGE_2 = URI.create("sics://hdb/sample/tc1/heater/heaterRange_2");
	
	private static URI URI_TC1_HEATEROUTPUT_1 = URI.create("sics://hdb/sample/tc1/heater/heaterOutput_1");
	
	private static URI URI_TC1_HEATEROUTPUT_2 = URI.create("sics://hdb/sample/tc1/heater/heaterOutput_2");
	
	
	public TC1Gadget(Composite parent, int style) {
		super(parent, SHOW_ICON);
		setDeviceURIs(
				URI_TC1_SENSOR_A + "," +
				URI_TC1_SENSOR_B + "," +
				URI_TC1_SENSOR_C + "," +
				URI_TC1_SENSOR_D + "," +
				URI_TC1_HEATEROUTPUT_1 + "," +
				URI_TC1_HEATEROUTPUT_2
		);
		setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				URI uri = (URI) element;
				if (uri.equals(URI_TC1_SENSOR_A)) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/document-attribute.png").createImage();
				} else if (uri.equals(URI_TC1_SENSOR_B)) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/document-attribute-b.png").createImage();
				} else if (uri.equals(URI_TC1_SENSOR_C)) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/document-attribute-c.png").createImage();
				} else if (uri.equals(URI_TC1_SENSOR_D)) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/document-attribute-d.png").createImage();
				} else if (uri.equals(URI_TC1_HEATEROUTPUT_1)) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/hh1_16x16.png").createImage();
				}  else if (uri.equals(URI_TC1_HEATEROUTPUT_2)) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/hh2_16x16.png").createImage();
				}
				return null;
			}
			public String getText(Object element) {
				URI uri = (URI) element;
				if (uri.toString().contains("sensor")) {
					return "T/C";
				} else if (uri.equals(URI_TC1_HEATEROUTPUT_1)) {
					return "H1 R/O";
				} else if (uri.equals(URI_TC1_HEATEROUTPUT_2)) {
					return "H2 R/O";
				} else {
					return "";
				}
			}
		});
	}

	protected void setupUI() {
		super.setupUI();
		// TC 1
		try {
			getContexts().put(URI_TC1_SETPOINT_1, null);
			getContexts().put(URI_TC1_SETPOINT_2, null);
			getContexts().put(URI_TC1_SETPOINT_3, null);
			getContexts().put(URI_TC1_SETPOINT_4, null);
			getContexts().put(URI_TC1_CONTROL_1, null);
			getContexts().put(URI_TC1_CONTROL_2, null);
			getContexts().put(URI_TC1_CONTROL_3, null);
			getContexts().put(URI_TC1_CONTROL_4, null);
			getContexts().put(URI_TC1_HEATERRANGE_1, null);
			getContexts().put(URI_TC1_HEATERRANGE_2, null);
			// Fetch current value	
			updateValue(URI_TC1_SETPOINT_1, getDam().get(URI_TC1_SETPOINT_1, String.class));
			updateValue(URI_TC1_SETPOINT_2, getDam().get(URI_TC1_SETPOINT_2, String.class));
			updateValue(URI_TC1_SETPOINT_3, getDam().get(URI_TC1_SETPOINT_3, String.class));
			updateValue(URI_TC1_SETPOINT_4, getDam().get(URI_TC1_SETPOINT_4, String.class));
			updateValue(URI_TC1_CONTROL_1, getDam().get(URI_TC1_CONTROL_1, String.class));	
			updateValue(URI_TC1_CONTROL_2, getDam().get(URI_TC1_CONTROL_2, String.class));
			updateValue(URI_TC1_CONTROL_3, getDam().get(URI_TC1_CONTROL_3, String.class));
			updateValue(URI_TC1_CONTROL_4, getDam().get(URI_TC1_CONTROL_4, String.class));
			updateValue(URI_TC1_HEATERRANGE_1, getDam().get(URI_TC1_HEATERRANGE_1, String.class));
			updateValue(URI_TC1_HEATERRANGE_2, getDam().get(URI_TC1_HEATERRANGE_2, String.class));
		} catch (Exception e) {
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
		} else if (uri.equals(URI_TC1_SETPOINT_1) || uri.equals(URI_TC1_SENSOR_A)) {
			// Refresh to initial value
			Context context = getContexts().get(URI_TC1_SENSOR_A);
			String target = getDam().get(URI_TC1_SETPOINT_1, String.class);
			String current = getDam().get(URI_TC1_SENSOR_A, String.class);
			context.displayLabel.setText("T/C: " + target + "/" + current);
		} else if (uri.equals(URI_TC1_SETPOINT_2) || uri.equals(URI_TC1_SENSOR_B)) {
			// Refresh to initial value
			Context context = getContexts().get(URI_TC1_SENSOR_B);
			String target = getDam().get(URI_TC1_SETPOINT_2, String.class);
			String current = getDam().get(URI_TC1_SENSOR_B, String.class);
			context.displayLabel.setText("T/C: " + target + "/" + current);
		} else if (uri.equals(URI_TC1_SETPOINT_3) || uri.equals(URI_TC1_SENSOR_C)) {
			// Refresh to initial value
			Context context = getContexts().get(URI_TC1_SENSOR_C);
			String target = getDam().get(URI_TC1_SETPOINT_3, String.class);
			String current = getDam().get(URI_TC1_SENSOR_C, String.class);
			context.displayLabel.setText("T/C: " + target + "/" + current);
		} else if (uri.equals(URI_TC1_SETPOINT_4) || uri.equals(URI_TC1_SENSOR_D)) {
			// Refresh to initial value
			Context context = getContexts().get(URI_TC1_SENSOR_D);
			String target = getDam().get(URI_TC1_SETPOINT_4, String.class);
			String current = getDam().get(URI_TC1_SENSOR_D, String.class);
			context.displayLabel.setText("T/C: " + target + "/" + current);
		} else if (uri.equals(URI_TC1_HEATEROUTPUT_1) || uri.equals(URI_TC1_HEATERRANGE_1)) {
			// Refresh to initial value
			Context context = getContexts().get(URI_TC1_HEATEROUTPUT_1);
			String range = getDam().get(URI_TC1_HEATERRANGE_1, String.class);
			String output = getDam().get(URI_TC1_HEATEROUTPUT_1, String.class);
			context.displayLabel.setText("R/O: " + range + "/" + output);
		} else if (uri.equals(URI_TC1_HEATEROUTPUT_2) || uri.equals(URI_TC1_HEATERRANGE_2)) {
			// Refresh to initial value
			Context context = getContexts().get(URI_TC1_HEATEROUTPUT_2);
			String range = getDam().get(URI_TC1_HEATERRANGE_2, String.class);
			String output = getDam().get(URI_TC1_HEATEROUTPUT_2, String.class);
			context.displayLabel.setText("R/O: " + range + "/" + output);
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
