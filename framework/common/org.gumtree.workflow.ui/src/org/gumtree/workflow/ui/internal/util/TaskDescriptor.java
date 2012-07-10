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

package org.gumtree.workflow.ui.internal.util;

import static org.gumtree.util.eclipse.ExtensionRegistryConstants.*;
import static org.gumtree.workflow.ui.internal.util.TaskRegistryConstants.ATTRIBUTE_ICON_32;
import static org.gumtree.workflow.ui.internal.util.TaskRegistryConstants.ATTRIBUTE_PROVIDER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.Image;
import org.gumtree.core.object.ObjectCreateException;
import org.gumtree.core.object.ObjectFactory;
import org.gumtree.util.eclipse.ExtensionRegistryReader;
import org.gumtree.workflow.ui.ITask;
import org.gumtree.workflow.ui.config.WorkflowConfigConstants;
import org.gumtree.workflow.ui.internal.Activator;
import org.gumtree.workflow.ui.util.ITaskDescriptor;

public class TaskDescriptor implements ITaskDescriptor {

	private IConfigurationElement element;

	private String label;

	private String classname;

	private String provider;

	private String description;

	private String colorString;

	private Image icon;

	private Image largeIcon;

	private List<String> tags;

	protected TaskDescriptor(IConfigurationElement element) {
		this.element = element;
		load();
	}

	public String getLabel() {
		return label;
	}

	public String getClassname() {
		return classname;
	}

	public String getColorString() {
		return colorString;
	}

	public ITask createNewTask() throws ObjectCreateException {
		// Since workflow requires this way to reload task, we
		// use object factory instance of Eclipse config element create
		// to ensure it is consistent with the workflow util.
		return ObjectFactory.instantiateObject(getClassname(), ITask.class);
	}

	public Image getIcon() {
		if (icon == null) {
			String iconFile = element.getAttribute(ATTRIBUTE_ICON);
			if (iconFile != null) {
				icon = Activator.imageDescriptorFromPlugin(
						element.getNamespaceIdentifier(), iconFile)
						.createImage(true);
			}
		}
		return icon;
	}

	public List<String> getTags() {
		return Collections.unmodifiableList(tags);
	}

	public Image getLargeIcon() {
		if (largeIcon == null) {
			String iconFile = element.getAttribute(ATTRIBUTE_ICON_32);
			if (iconFile != null) {
				largeIcon = Activator.imageDescriptorFromPlugin(
						element.getNamespaceIdentifier(), iconFile)
						.createImage(true);
			}
		}
		return largeIcon;
	}

	public String getProvider() {
		return provider;
	}

	public String getDescription() {
		return description;
	}

	private void load() {
		label = element.getAttribute(ATTRIBUTE_LABEL);
		classname = element.getAttribute(ATTRIBUTE_CLASS);
		provider = element.getAttribute(ATTRIBUTE_PROVIDER);
		description = ExtensionRegistryReader.getDescription(element);
		colorString = element
				.getAttribute(WorkflowConfigConstants.PARAM_COLOUR);
		String tagsString = element.getAttribute(ATTRIBUTE_TAGS);
		tags = new ArrayList<String>(2);
		if (tagsString != null) {
			// Loop through tags to avoid adding empty tags
			for (String potentialTag : tagsString.split(",")) {
				potentialTag = potentialTag.trim();
				if (potentialTag.length() != 0) {
					tags.add(potentialTag.toLowerCase());
				}
			}
		}
	}

}
