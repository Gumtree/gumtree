package org.gumtree.workflow.ui.internal.util;

import static org.gumtree.util.eclipse.ExtensionRegistryConstants.*;
import static org.gumtree.workflow.ui.internal.util.WorkflowRegistryConstants.ATTRIBUTE_CATEGORY;
import static org.gumtree.workflow.ui.internal.util.WorkflowRegistryConstants.ATTRIBUTE_CONFIG;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gumtree.util.eclipse.EclipseUtils;
import org.gumtree.util.eclipse.ExtensionRegistryReader;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.WorkflowException;
import org.gumtree.workflow.ui.internal.Activator;
import org.gumtree.workflow.ui.internal.InternalImage;
import org.gumtree.workflow.ui.util.IWorkflowDescriptor;
import org.gumtree.workflow.ui.util.WorkflowFactory;

public class WorkflowDescriptor implements IWorkflowDescriptor {

	private IConfigurationElement element;

	private String id;

	private String label;

	private String description;

	private String category;

	private String[] tags;

	private ImageDescriptor icon;

	protected WorkflowDescriptor(IConfigurationElement element) {
		this.element = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.workflow.ui.IWorkflowDescriptor#getId()
	 */
	public String getId() {
		if (id == null) {
			id = getElement().getAttribute(ATTRIBUTE_ID);
		}
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.workflow.ui.IWorkflowDescriptor#getLabel()
	 */
	public String getLabel() {
		if (label == null) {
			label = getElement().getAttribute(ATTRIBUTE_LABEL);
		}
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.workflow.ui.IWorkflowDescriptor#getDescription()
	 */
	public String getDescription() {
		if (description == null) {
			description = ExtensionRegistryReader.getDescription(getElement());
		}
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.workflow.ui.IWorkflowDescriptor#getCategory()
	 */
	public String getCategory() {
		if (category == null) {
			category = getElement().getAttribute(ATTRIBUTE_CATEGORY);
		}
		return category;
	}

	public String[] getTags() {
		if (tags == null) {
			if (getElement().getAttribute(ATTRIBUTE_TAGS) != null) {
				try {
					tags = getElement().getAttribute(ATTRIBUTE_TAGS).split(",");
					for (int i = 0; i < tags.length; i++) {
						tags[i] = tags[i].trim();
					}
				} catch (Exception e) {
				}
			}
			if (tags == null) {
				tags = new String[0];
			}
		}
		return tags;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.workflow.ui.IWorkflowDescriptor#getIcon()
	 */
	public ImageDescriptor getIcon() {
		if (icon == null) {
			String iconFile = element.getAttribute(ATTRIBUTE_ICON);
			if (iconFile != null) {
				icon = Activator.imageDescriptorFromPlugin(
						element.getNamespaceIdentifier(), iconFile);
			}
			if (icon == null) {
				// Use default
				icon = InternalImage.WORKFLOW.getDescriptor();
				// icon = ImageDescriptor.getMissingImageDescriptor();
			}
		}
		return icon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.workflow.ui.IWorkflowDescriptor#createWorkflow()
	 */
	public IWorkflow createWorkflow() throws WorkflowException {
		try {
			String configFileLocation = getElement().getAttribute(
					ATTRIBUTE_CONFIG);
			IFileStore configFile = EclipseUtils.find(
					element.getNamespaceIdentifier(), configFileLocation);
			return WorkflowFactory.createWorkflow(configFile.openInputStream(
					EFS.NONE, new NullProgressMonitor()));
		} catch (Exception e) {
			throw new WorkflowException(
					"Cannot create workflow from descriptor.", e);
		}
	}

	private IConfigurationElement getElement() {
		return element;
	}

}
