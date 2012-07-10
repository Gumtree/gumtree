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

package org.gumtree.gumnix.sics.batch.ui.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;
import org.gumtree.gumnix.sics.batch.ui.CommandBlockTask;
import org.gumtree.gumnix.sics.batch.ui.internal.InternalImage;
import org.gumtree.gumnix.sics.batch.ui.model.ISicsCommandElement;
import org.gumtree.gumnix.sics.batch.ui.model.SicsCommandType;
import org.gumtree.gumnix.sics.batch.ui.views.ISicsCommandView;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.control.controllers.IDrivableController;
import org.gumtree.gumnix.sics.control.controllers.IDynamicController;
import org.gumtree.gumnix.sics.core.PropertySelectionCriterion;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.config.TaskConfig;
import org.gumtree.workflow.ui.config.WorkflowConfig;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.DataType;
import ch.psi.sics.hipadaba.SICS;

public final class SicsBatchUIUtils {

	private static final Logger logger = LoggerFactory.getLogger(SicsBatchUIUtils.class);
	
	private static List<String> sicsVariableCache;
	
	private static List<IDrivableController> drivableCache;
	
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
				sicsModel = SicsCore.getSicsManager().service().getOnlineModel();
			} catch (SicsIOException e) {
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
			Component[] components = SicsUtils.findComponentsFromProperties(sicsModel, selectionCriteria);
			/*****************************************************************
			 * Final selection:
			 * 	  a component must have a sics device id with text data type
			 *****************************************************************/
			sicsVariableCache = new ArrayList<String>();
			for (Component component : components) {
				String deviceId = SicsUtils.getPropertyFirstValue(component, "sicsdev");
				if (deviceId != null && component.getDataType().equals(DataType.TEXT_LITERAL)) {
					sicsVariableCache.add(deviceId);
				}
			}
		}
		return sicsVariableCache.toArray(new String[sicsVariableCache.size()]);
	}
	
	// Note: this is not thread safe
	private static IDrivableController[] getSicsDrivables() {
		if (drivableCache == null) {
			// SICS proxy is not yet ready
			if (SicsCore.getSicsController() == null) {
				logger.warn("SICS model is not available");
				return new IDrivableController[0];
			}
			drivableCache = new ArrayList<IDrivableController>();
			for (IComponentController childController : SicsCore.getSicsController().getComponentControllers()) {
				findDrivableControllers(childController, drivableCache);
			}
		}
		return drivableCache.toArray(new IDrivableController[drivableCache.size()]);
	}
	
	public static String[] getSicsDrivableIds() {
		if (drivableIdCache == null) {
			// SICS proxy is not yet ready
			if (SicsCore.getSicsController() == null) {
				logger.warn("SICS model is not available");
				return new String[0];
			}
			drivableIdCache = new ArrayList<String>();
			for (IDrivableController controller : getSicsDrivables()) {
				drivableIdCache.add(controller.getDeviceId());
			}
		}
		return drivableIdCache.toArray(new String[drivableIdCache.size()]);
	}
	
	public static String[] getDrivableAttributes(String controllerId) {
		if (drivableAttributeCache == null) {
			// SICS proxy is not yet ready
			if (SicsCore.getSicsController() == null) {
				logger.warn("SICS model is not available");
				return new String[0];
			}
			// Start caching
			drivableAttributeCache = new HashMap<String, List<String>>();
			for (IDrivableController controller : getSicsDrivables()) {
				List<String> attributes = new ArrayList<String>();
				for (IComponentController childController : controller.getChildControllers()) {
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
	
	private static void findDrivableControllers(IComponentController controller, List<IDrivableController> buffer) {
		if (controller instanceof IDrivableController) {
			buffer.add((IDrivableController) controller);
		}
		for (IComponentController childController : controller.getChildControllers()) {
			findDrivableControllers(childController, buffer);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static ISicsCommandView<? extends ISicsCommandElement> createCommandView(ISicsCommandElement command) throws ObjectCreateException {
		SicsCommandType type = SicsCommandType.getType(command.getClass());
		ISicsCommandView commandView = ObjectFactory.instantiateObject(type.getCommandViewClass());
		// Pass the reference of command object to the view
		commandView.setCommand(command);
		return commandView;
	}
	
	public static IWorkflow createDefaultWorkflow() {
		WorkflowConfig config = new WorkflowConfig();
		config.getTaskConfigs().add(new TaskConfig(CommandBlockTask.class.getName()));
		return WorkflowFactory.createWorkflow(config);
	}
	
	private SicsBatchUIUtils() {
		super();
	}
	
	public static Image getBatchEditorImage(String imageName) throws FileNotFoundException{
		InternalImage imageEnum = null;
		try {
			imageEnum = InternalImage.valueOf(imageName);
		} catch (Exception e) {
			throw new FileNotFoundException("can not find image " + imageName);
		}
		return imageEnum.getImage();
	}
}
