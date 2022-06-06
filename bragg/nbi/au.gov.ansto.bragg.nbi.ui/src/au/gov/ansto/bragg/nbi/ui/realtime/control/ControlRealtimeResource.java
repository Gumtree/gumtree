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
package au.gov.ansto.bragg.nbi.ui.realtime.control;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.vis.dataset.XYTimeSeriesSet;
import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

import au.gov.ansto.bragg.nbi.ui.realtime.IRealtimeResource;

/**
 * @author nxi
 *
 */
public class ControlRealtimeResource implements IRealtimeResource {

	private static final double VALUE_TOLLERANCE = 1E-4;
	private double value1;
	private double value2;
	private String name;
	private String fullName;
	private TimeSeries timeSeries;
	private ITimeSeriesSet seriesSet;
	
	public ControlRealtimeResource(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.ui.realtime.IRealtimeResource#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ITimeSeriesSet getTimeSeriesSet() {
		if (seriesSet == null) {
			timeSeries = new TimeSeries(name);
//			timeSeries.setNotify(false);
			seriesSet = new XYTimeSeriesSet();
//			seriesSet.setTitle(name);
			seriesSet.setYTitle(name);
			seriesSet.addSeries(timeSeries);
		}
		return seriesSet;
	}

	@Override
	public void update() {
		try {
			String value = getValue(getFullName());
			Double doubleValue = Double.valueOf(value);
			if (Math.abs(value1 - value2) < VALUE_TOLLERANCE) {
				if (Math.abs(doubleValue - value2) < VALUE_TOLLERANCE) {
					int counts = timeSeries.getItemCount();
					if (counts >= 2) {
						timeSeries.delete(counts - 1, counts - 1);
						doubleValue = value2;
					}
				}
			}
			timeSeries.add(new Millisecond(), doubleValue);
			value1 = value2;
			value2 = doubleValue;
		} catch (SicsModelException e) {
			e.printStackTrace();
		}
	}
	
	private String getValue(String idOrPath) throws SicsModelException{
		if (idOrPath.contains(":")) {
			String[] parts = idOrPath.split(":");
			idOrPath = parts[1].trim();
		}
		ISicsController controller = getController(idOrPath);
		if (controller == null)
			controller = getController(idOrPath);
		if (controller == null)
			throw new SicsModelException("device does not exist: " + idOrPath);
		if (controller instanceof IDynamicController) {
			IDynamicController dynamicController = (IDynamicController) controller;
			try {
				Object data = dynamicController.getValue();
				return data.toString();
			} catch (Exception e) {
				throw new SicsModelException(e.getLocalizedMessage());
			}
		} else {
			throw new SicsModelException("device does not have an status available");
		}
	}
	
	public static ISicsController findDevice(String idOrPath) {
		if (idOrPath.contains(":")) {
			String[] parts = idOrPath.split(":");
			idOrPath = parts[1].trim();
		}
		ISicsController controller = null;
//		try{
//			controller = getDevice(idOrPath);
//		}catch (Exception e) {
//		}
		if (controller == null) {
			controller = getController(idOrPath);
		}
		return controller;
	}
	
	public static List<ISicsController> getEnvironmentControllers() {
		ISicsController parent = SicsManager.getSicsModel().findController("/control");
		if (parent != null) {
			return parent.getChildren();
		}
		return new ArrayList<ISicsController>();
	}
	
//	public static ISicsController getDevice(String deviceId) {
//		return SicsManager.getSicsModel().findController(deviceId);
//	}
	
	public static ISicsController getController(String componentPath){
		return SicsManager.getSicsModel().findController(componentPath);
	}

	@Override
	public void clear() {
		timeSeries.clear();
		seriesSet.removeSeries(timeSeries);
		timeSeries = null;
		seriesSet = null;
	}

	/**
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return fullName;
	}
	
	public static String getSimpleName(String fullName) {
		if (fullName.contains(":")) {
			String[] parts = fullName.split(":");
			return parts[0].trim();
		}
		if (fullName.contains("/")) {
			String[] parts = fullName.split("/");
			for (int i = parts.length - 1; i >= 0; i--) {
				String part = parts[i];
				if (part.trim().length() > 0) {
					return part;
				}
			}
		}
		return fullName;
	}
}
