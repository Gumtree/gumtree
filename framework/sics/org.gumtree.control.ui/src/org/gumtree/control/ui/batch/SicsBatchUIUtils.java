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

package org.gumtree.control.ui.batch;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.gumtree.control.batch.tasks.ISicsCommand;
import org.gumtree.control.batch.tasks.PropertySelectionCriterion;
import org.gumtree.control.core.IDriveableController;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.model.ModelUtils;
import org.gumtree.control.ui.batch.command.ISicsCommandView;
import org.gumtree.control.ui.batch.command.SicsCommandType;
import org.gumtree.control.ui.batch.taskeditor.CommandBlockTask;
import org.gumtree.control.ui.viewer.InternalImage;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;
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
	
	@SuppressWarnings("unchecked")
	public static ISicsCommandView<? extends ISicsCommand> createCommandView(ISicsCommand command) throws ObjectCreateException {
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
