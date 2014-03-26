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
package au.gov.ansto.bragg.nbi.ui.realtime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IComponentData;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsCoreException;
import org.gumtree.vis.dataset.XYTimeSeriesSet;
import org.gumtree.vis.interfaces.ITimeSeriesSet;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

/**
 * @author nxi
 *
 */
public class SicsRealtimeResource implements IRealtimeResource {

	private static final double VALUE_TOLLERANCE = 1E-4;
	private double value1;
	private double value2;
	private String name;
	private String fullName;
	private TimeSeries timeSeries;
	private ITimeSeriesSet seriesSet;
	
	public SicsRealtimeResource(String name) {
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
		} catch (SicsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getValue(String idOrPath) throws SicsCoreException{
		if (idOrPath.contains(":")) {
			String[] parts = idOrPath.split(":");
			idOrPath = parts[1].trim();
		}
		IComponentController controller = null;
		try{
			controller = getDevice(idOrPath);
		}catch (Exception e) {
			controller = getController(idOrPath);
		}
		if (controller == null)
			controller = getController(idOrPath);
		if (controller == null)
			throw new SicsCoreException("device does not exist: " + idOrPath);
		if (controller instanceof IDynamicController) {
			IDynamicController dynamicController = (IDynamicController) controller;
			try {
				IComponentData data = dynamicController.getValue();
				return data.getSicsString();
			} catch (Exception e) {
				throw new SicsCoreException(e.getLocalizedMessage());
			}
		} else {
			throw new SicsCoreException("device does not have an status available");
		}
	}
	
	public static IComponentController findDevice(String idOrPath) {
		if (idOrPath.contains(":")) {
			String[] parts = idOrPath.split(":");
			idOrPath = parts[1].trim();
		}
		IComponentController controller = null;
		try{
			controller = getDevice(idOrPath);
		}catch (Exception e) {
		}
		if (controller == null) {
			controller = getController(idOrPath);
		}
		return controller;
	}
	
	public static List<IComponentController> getEnvironmentControllers() {
		IComponentController parent = SicsCore.getSicsController().findComponentController("/control");
		if (parent != null) {
			IComponentController[] children = parent.getChildControllers();
			if (children != null & children.length > 0) {
				return Arrays.asList(children);
			}
		}
		return new ArrayList<IComponentController>();
	}
	
	public static IComponentController getDevice(String deviceId) {
		return SicsCore.getSicsController().findDeviceController(deviceId);
	}
	
	public static IComponentController getController(String componentPath){
		return SicsCore.getSicsController().findComponentController(componentPath);
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
