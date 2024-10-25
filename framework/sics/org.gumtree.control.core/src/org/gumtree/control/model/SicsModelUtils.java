/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Norman Xiong (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.control.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.control.batch.tasks.PropertySelectionCriterion;
import org.gumtree.control.core.IDriveableController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.SICS;

public final class SicsModelUtils {

	private static final Logger logger = LoggerFactory.getLogger(SicsModelUtils.class);
	
	private static List<String> sicsVariableCache;
	
	private static List<IDriveableController> drivableCache;
	
	private static List<String> drivableIdCache;
	
//	private static List<ISicsUserCommand> userCommandCache;
	
	private static Map<String, List<String>> drivableAttributeCache;
	
	// Note: this is not thread safe
	public static String[] getSicsVariables() {
		if (sicsVariableCache == null) {
			/*****************************************************************
			 * Acquire SICS online model
			 *****************************************************************/
			SICS sicsModel = null;
			try {
				sicsModel = SicsManager.getSicsModel().getBase();
			} catch (Exception e) {
				logger.warn("SICS model is not available", e);
				return new String[0];
			}
			/*****************************************************************
			 * Prepare selection criteria
			 *****************************************************************/
			List<PropertySelectionCriterion> selectionCriteria = new ArrayList<PropertySelectionCriterion>();
			selectionCriteria.add(PropertySelectionCriterion.createEqual("data", "true"));
			selectionCriteria.add(PropertySelectionCriterion.createContain("sdsinfo", "sicsvariable"));
			/*****************************************************************
			 * Seearch the model
			 *****************************************************************/
			Component[] components = ModelUtils.findComponentsFromProperties(sicsModel, selectionCriteria);
			/*****************************************************************
			 * Final selection:
			 * 	  a component must have a sics device id with text data type
			 *****************************************************************/
			sicsVariableCache = new ArrayList<String>();
			for (Component component : components) {
				String deviceId = ModelUtils.getPropertyFirstValue(component, "sicsdev");
				if (deviceId != null && component.getDataType().equals(DataType.TEXT_LITERAL)) {
					sicsVariableCache.add(deviceId);
				}
			}
		}
		return sicsVariableCache.toArray(new String[sicsVariableCache.size()]);
	}
	
	// Note: this is not thread safe
	public static IDriveableController[] getSicsDrivables() {
		if (drivableCache == null) {
			// SICS proxy is not yet ready
			if (SicsManager.getSicsModel() == null) {
				logger.warn("SICS model is not available");
				return new IDriveableController[0];
			}
			drivableCache = new ArrayList<IDriveableController>();
			for (ISicsController childController : SicsManager.getSicsModel().getSicsControllers()) {
				findDrivableControllers(childController, drivableCache);
			}
		}
		return drivableCache.toArray(new IDriveableController[drivableCache.size()]);
	}
	
	public static String[] getSicsDrivableIds() {
		if (drivableIdCache == null) {
			// SICS proxy is not yet ready
			if (SicsManager.getSicsModel() == null) {
				logger.warn("SICS model is not available");
				return new String[0];
			}
			drivableIdCache = new ArrayList<String>();
			for (IDriveableController controller : getSicsDrivables()) {
				drivableIdCache.add(controller.getDeviceId());
			}
		}
		return drivableIdCache.toArray(new String[drivableIdCache.size()]);
	}
	
	public static String[] getDrivableAttributes(String controllerId) {
		if (drivableAttributeCache == null) {
			// SICS proxy is not yet ready
			if (SicsManager.getSicsModel() == null) {
				logger.warn("SICS model is not available");
				return new String[0];
			}
			// Start caching
			drivableAttributeCache = new HashMap<String, List<String>>();
			for (IDriveableController controller : getSicsDrivables()) {
				List<String> attributes = new ArrayList<String>();
				for (ISicsController childController : controller.getChildren()) {
					if (childController instanceof IDynamicController &&
							!childController.getId().equals("target")) {
						attributes.add(childController.getId());
					}
				}
				drivableAttributeCache.put(controller.getDeviceId(), attributes);
			}
		}
		if (!drivableAttributeCache.containsKey(controllerId)) {
			return new String[0];
		}
		List<String> attributeCache = drivableAttributeCache.get(controllerId);
		return attributeCache.toArray(new String[attributeCache.size()]);
	}
	
	private static void findDrivableControllers(ISicsController controller, List<IDriveableController> buffer) {
		if (controller instanceof IDriveableController) {
			buffer.add((IDriveableController) controller);
		}
		for (ISicsController childController : controller.getChildren()) {
			findDrivableControllers(childController, buffer);
		}
	}
	
	private SicsModelUtils() {
		super();
	}
	
}
