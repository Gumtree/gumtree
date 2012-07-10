package org.gumtree.workflow.ui.internal.util;

import static org.gumtree.util.eclipse.ExtensionRegistryConstants.*;
import static org.gumtree.workflow.ui.internal.util.WorkflowRegistryConstants.ELEMENT_CATEGORY;
import static org.gumtree.workflow.ui.internal.util.WorkflowRegistryConstants.ELEMENT_WORKFLOW;
import static org.gumtree.workflow.ui.internal.util.WorkflowRegistryConstants.EXTENSION_WORKFLOWS;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.util.eclipse.ExtensionRegistryReader;
import org.gumtree.workflow.ui.internal.Activator;

public class WorkflowRegistryReader extends ExtensionRegistryReader {

	private WorkflowRegistry registry;

	protected WorkflowRegistryReader(WorkflowRegistry registry) {
		super(Activator.getDefault());
		this.registry = registry;
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(ELEMENT_WORKFLOW)) {
			registry.addWorkflowDescriptor(new WorkflowDescriptor(element));
			return true;
		} else if (element.getName().equals(ELEMENT_CATEGORY)) {
			registry.addCategory(element.getAttribute(ATTRIBUTE_ID),
					element.getAttribute(ATTRIBUTE_NAME));
			return true;
		}
		return false;
	}

	protected void readWorkflows() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, getPlugin().getBundle().getSymbolicName(),
				EXTENSION_WORKFLOWS);
	}

}
