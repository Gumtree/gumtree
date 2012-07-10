/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core.data.region;

import java.util.ArrayList;

import au.gov.ansto.bragg.datastructures.core.region.RegionSet;
import au.gov.ansto.bragg.kakadu.core.Util;
import au.gov.ansto.bragg.kakadu.core.data.OperationParameter;
import au.gov.ansto.bragg.process.port.Tuner;

/**
 * The class handles OperationParameter of Region type.
 * The value of the parameter is the <code>List<UIRegion></code> object.
 * 
 * @author Danil Klimontov (dak)
 */
public class RegionOperationParameter extends OperationParameter {
	
//	private ParameterRegionManager parameterRegionManager;
	/**
	 * The flag used to mark some actions inside the object only.
	 */
	private boolean isInternalAction = false;
	
	/** The parameter is active if the instance created for selected DataItem */
	private boolean isActive;

	public RegionOperationParameter(Tuner tuner) {
		super(tuner);
//		this.parameterRegionManager = parameterRegionManager;
//		
//		parameterRegionManager.addRegionListener(new RegionListener() {
//			public void regionAdded(UIRegion region) {
//				if (!isInternalAction && isActive) {
//					addRegion(region);
//				}
//			}
//			public void regionRemoved(UIRegion region) {
//				if (!isInternalAction && isActive) {
//					removeRegion(region);
//				}
//			}
//			public void regionUpdated(UIRegion region) {
//				if (!isInternalAction && isActive) {
//					setValue(RegionOperationParameter.this.parameterRegionManager.getRegions());
//					isChanged = true;
//				}
//			}
//		});
		
		super.initOperationParameter();
	}
	
	protected void initOperationParameter() {
		//Override to avoid NullPointerException because parameterRegionManager was not defined yet
	}

	
//	public ParameterRegionManager getParameterRegionManager() {
//		return parameterRegionManager;
//	}

//	protected Object prepareServerValue() {
//		final List<UIRegion> existedRegions = parameterRegionManager.getRegions();
//		final List<UIRegion> serverRegions = RegionUtil.convertToUIObject((Group) super.prepareServerValue());
//		
//		if (RegionUtil.areEqual(existedRegions, serverRegions)) {
//			return existedRegions;
//		} else {
//			isInternalAction = true;
//			parameterRegionManager.removeAllRegions();
//			parameterRegionManager.addAllRegions(serverRegions);
//			isInternalAction = false;
//			return serverRegions;
//		}
////		return parameterRegionManager.getRegions();
//	}

//	private void addRegion(UIRegion uiRegion) {
//		List<UIRegion> regions = new ArrayList<UIRegion>((List<UIRegion>) getValue());
//		regions.add(uiRegion);
//		setValue(regions);
////		isChanged = true;
//	}
//	
//	private void removeRegion(UIRegion uiRegion) {
//		List<UIRegion> regions = new ArrayList<UIRegion>((List<UIRegion>) getValue());
//		for (Iterator<UIRegion> iterator = regions.iterator(); iterator.hasNext();) {
//			UIRegion region = iterator.next();
//			if (region == uiRegion) {
//				iterator.remove();
//				break;
////				isChanged = true;
//			}
//		}
//		setValue(regions);
//	}
//	
//	public boolean isContainsRegion(UIRegion uiRegion) {
//		List<UIRegion> regions = (List<UIRegion>) getValue();
//		for (UIRegion region : regions) {
//			if (region.equals(uiRegion)) {
//				return true;
//			}
//		}
//		return false;
//	}
	
	
	
	
	public Class<?> getParameterValueClass() {
		return ArrayList.class;
	}

	
//	public void saveChanges() throws IllegalAccessException {
//		if (isChanged) {
//			try {
//				tuner.setSignal(RegionUtil.convertToServerObject((List<UIRegion>) value));
//			} catch (Exception e) {
//				//just wrap to the one of available exceptions
//				throw new IllegalAccessException("Exceprion during convertion to Region server object: " + e.getMessage());
//			}
//			isChanged = false;
//		}
//	}

	public void loadDefaultValue() {
		isInternalAction = true;
		super.loadDefaultValue();
//		parameterRegionManager.removeAllRegions();
//		parameterRegionManager.addAllRegions((List<UIRegion>) getValue());
		isInternalAction = false;
	}

	public void revertChanges() {
		isInternalAction = true;
		super.revertChanges();
//		parameterRegionManager.removeAllRegions();
//		parameterRegionManager.addAllRegions((List<UIRegion>) getValue());
		isInternalAction = false;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	@Override
	public void setValue(Object value) {
		if (isChanged) {
			this.value = value;
		} else {
			oldValue = this.value;
			this.value = value;
			if (oldValue != value)
				isChanged = true;
		}		
	}
}
