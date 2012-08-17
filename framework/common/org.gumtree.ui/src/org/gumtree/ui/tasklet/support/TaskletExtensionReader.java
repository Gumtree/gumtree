package org.gumtree.ui.tasklet.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

public class TaskletExtensionReader extends ExtensionRegistryReader {

	public static String EXTENSION_TASKLETS = "tasklets";

	public static String ELEMENT_TASKLET = "tasklet";

	public static String ATTRIBUTE_LABEL = "label";

	public static String ATTRIBUTE_CONTRIBUTION_URI = "contributionUri";

	public static String ATTRIBUTE_TAGS = "tags";

	public static String ATTRIBUTE_NEW_WINDOW = "newWindow";

	public static String EXTENTION_POINT_TASKLETS = Activator.PLUGIN_ID + "."
			+ EXTENSION_TASKLETS;

	private IExtensionRegistry extensionRegistry;

	private List<ITasklet> registeredTasklets;

	public TaskletExtensionReader() {
		super(Activator.getDefault());
	}

	@Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(ELEMENT_TASKLET)) {
			ITasklet tasklet = new Tasklet();
			tasklet.setLabel(element.getAttribute(ATTRIBUTE_LABEL));
			tasklet.setContributionURI(element
					.getAttribute(ATTRIBUTE_CONTRIBUTION_URI));
			tasklet.setTags(element.getAttribute(ATTRIBUTE_TAGS));
			tasklet.setNewWindow(Boolean.parseBoolean(element
					.getAttribute(ATTRIBUTE_NEW_WINDOW)));
			registeredTasklets.add(tasklet);
		}
		return true;
	}

	public IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistry == null) {
			extensionRegistry = Platform.getExtensionRegistry();
		}
		return extensionRegistry;
	}

	public void setExtensionRegistry(IExtensionRegistry extensionRegistry) {
		this.extensionRegistry = extensionRegistry;
	}

	// This is not thread safe
	public List<ITasklet> getRegisteredTasklets() {
		if (registeredTasklets == null) {
			registeredTasklets = new ArrayList<ITasklet>(2);
			readRegistry(getExtensionRegistry(), Activator.PLUGIN_ID,
					EXTENSION_TASKLETS);
		}
		return Collections.unmodifiableList(registeredTasklets);
	}

}
