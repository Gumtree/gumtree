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

package org.gumtree.gumnix.sics.ui.widgets;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.service.dataaccess.IDataHandler;
import org.gumtree.ui.util.SafeUIRunner;

public class SynopticsStatusWidget extends AbstractSicsDeviceWidget {

	private static final String PROP_DATA = "data";
	
	private static final String PROP_UNIT = "unit";
	
	private static final String PROP_LABEL = "label";
	
	private String imageURI;
	
	private Image backgroundImage;
	
	private List<String> labelList = new ArrayList<String>();
	
	private List<Integer> xCoordinateList = new ArrayList<Integer>();
	
	private List<Integer> yCoordinateList = new ArrayList<Integer>();
	
	public SynopticsStatusWidget(Composite parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
	}

	protected void widgetDispose() {
		imageURI = null;
		if (backgroundImage != null) {
			backgroundImage.dispose();
			backgroundImage = null;
		}
		if (labelList != null) {
			labelList.clear();
			labelList = null;
		}
		if (xCoordinateList != null) {
			xCoordinateList.clear();
			xCoordinateList = null;
		}
		if (yCoordinateList != null) {
			yCoordinateList.clear();
			yCoordinateList = null;
		}
		super.widgetDispose();
	}
	
	protected void initialise() {
	}
	
	protected void createUI() {
		/*********************************************************************
		 * Initialise
		 *********************************************************************/
		for (Control child : this.getChildren()) {
			child.dispose();
		}
		setLayout(new FormLayout());
		/*********************************************************************
		 * Devices
		 *********************************************************************/
		for (int i = 0; i < deviceURIs.size(); i++) {
			Label label = getToolkit().createLabel(this, "");
			label.setData(PROP_LABEL, labelList.get(i));
			label.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT));
			FormData formData = new FormData();
			formData.left = new FormAttachment(xCoordinateList.get(i), 0);
			formData.top = new FormAttachment(yCoordinateList.get(i), 0);
			label.setLayoutData(formData);
			
			// Register control
			uriMap.put(deviceURIs.get(i), label);
			URI nameURI = URI.create(deviceURIs.get(i).toString() + "?sicsdev");
			uriMap.put(nameURI, label);
			URI unitURI = URI.create(deviceURIs.get(i).toString() + "?units");
			uriMap.put(unitURI, label);
			URI statusURI = URI.create(deviceURIs.get(i).toString() + "?status");
			uriMap.put(statusURI, label);
			
			// Fetch initial value
			getDataAccessManager().get(nameURI, String.class, new DataHandler<String>());
			getDataAccessManager().get(unitURI, String.class, new DataHandler<String>());
			getDataAccessManager().get(statusURI, ControllerStatus.class, new DataHandler<ControllerStatus>());
			getDataAccessManager().get(deviceURIs.get(i), String.class, new DataHandler<String>());
		}
		/*********************************************************************
		 * Background
		 *********************************************************************/
		Label imageLabel = getToolkit().createLabel(this, "");
		if (backgroundImage != null) {
			backgroundImage.dispose();
		}
		backgroundImage = getDataAccessManager().get(URI.create(imageURI), Image.class);
		imageLabel.setImage(backgroundImage);
		FormData formData = new FormData();
		formData.left = new FormAttachment(0, 0);
		formData.top = new FormAttachment(0, 0);
		imageLabel.setLayoutData(formData);
		/*********************************************************************
		 * Final preparation
		 *********************************************************************/
		setEnabled(true);
		getParent().getParent().layout(true, true);
	}

	protected void updateWidgetData(URI uri, Object widget, Object data) {
		Control control = (Control) widget;
		/*********************************************************************
		 * Handle status
		 *********************************************************************/
		if (uri.toString().endsWith("status")) {
			ControllerStatus status = (ControllerStatus) data;
			if (status.equals(ControllerStatus.OK)) {
				control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
				control.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
			} else if (status.equals(ControllerStatus.RUNNING)) {
				control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
				control.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			} else if (status.equals(ControllerStatus.ERROR)) {
				control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				control.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			}
			return;
		}
		
		/*********************************************************************
		 * Store data
		 *********************************************************************/
		if (uri.toString().endsWith("sicsdev")
				&& (control.getData(PROP_LABEL) == null ||
					((String) control.getData(PROP_LABEL)).length() == 0)) {
			control.setData(PROP_LABEL, data);
		} else if (uri.toString().endsWith("units")) {
			control.setData(PROP_UNIT, data);
		} else {
			control.setData(PROP_DATA, data);
		}
		
		/*********************************************************************
		 * Reconstruct data
		 *********************************************************************/
		StringBuilder builder = new StringBuilder();
		if (control.getData(PROP_LABEL) != null) {
			builder.append(control.getData(PROP_LABEL));
			builder.append(": ");
		}
		if (control.getData(PROP_DATA) != null) {
			builder.append(control.getData(PROP_DATA));
		}
		if (control.getData(PROP_UNIT) != null) {
			builder.append(" ");
			builder.append(control.getData(PROP_UNIT));
		}
		
		/*********************************************************************
		 * Display data
		 *********************************************************************/
		if (control instanceof Label) {
			((Label) control).setText(builder.toString());
			layout(true, true);
		}
	}

	/*************************************************************************
	 * Getters and setters
	 *************************************************************************/

	public void setImageURI(String imageURI) {
		this.imageURI = imageURI;
	}

	public void setLabels(String labels) {
		String[] labelTokens = labels.split(",");
		labelList = new ArrayList<String>();
		for (String label : labelTokens) {
			labelList.add(label.trim());
		}
	}
	
	public void setxCoordinates(String xCoordinates) {
		String[] xCoordinateTokens = xCoordinates.split(",");
		xCoordinateList = new ArrayList<Integer>();
		for (String xCoordinate : xCoordinateTokens) {
			xCoordinate = xCoordinate.trim();
			if (xCoordinate.length() == 0) {
				xCoordinateList.add(0);
			} else {
				xCoordinateList.add(Integer.parseInt(xCoordinate.trim()));
			}
		}
	}
	
	public void setyCoordinates(String yCoordinates) {
		String[] yCoordinateTokens = yCoordinates.split(",");
		yCoordinateList = new ArrayList<Integer>();
		for (String yCoordinate : yCoordinateTokens) {
			yCoordinate = yCoordinate.trim();
			if (yCoordinate.length() == 0) {
				yCoordinateList.add(0);
			} else {
				yCoordinateList.add(Integer.parseInt(yCoordinate.trim()));
			}
		}
	}
	
	/*************************************************************************
	 * Internal classes
	 *************************************************************************/
	
	private class DataHandler<T> implements IDataHandler<T> {
		public void handleData(final URI uri, final T data) {
			SafeUIRunner.asyncExec(new SafeRunnable() {
				public void run() throws Exception {
					if (isDisposed()) {
						return;
					}
					updateWidgetData(uri, uriMap.get(uri), data);
				}
			});
		}
		public void handleError(URI uri, Exception exception) {
		}		
	}

}
