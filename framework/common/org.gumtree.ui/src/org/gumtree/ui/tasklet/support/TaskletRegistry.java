package org.gumtree.ui.tasklet.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.ITaskletRegistry;

public class TaskletRegistry implements ITaskletRegistry {

	private List<ITasklet> tasklets;
	
	private IExtensionRegistry extensionRegistry;

	public TaskletRegistry() {
		tasklets = new ArrayList<ITasklet>(2);
	}
	
	@Override
	public void addTasklet(ITasklet tasklet) {
		tasklets.add(tasklet);
		// Notify
		// Persist
	}

	@Override
	public void removeTasklet(ITasklet tasklet) {
		tasklets.remove(tasklet);
	}

	@Override
	public List<ITasklet> getTasklets() {
		return Collections.unmodifiableList(tasklets);
	}
	
	public void init() {
		// Load from extension point
		if (getExtensionRegistry() != null) {
			TaskletExtensionReader reader = new TaskletExtensionReader();
			reader.setExtensionRegistry(getExtensionRegistry());
			List<ITasklet> registeredTasklet = reader.getRegisteredTasklets();
			tasklets.addAll(registeredTasklet);
		}
	}
	
	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IExtensionRegistry getExtensionRegistry() {
		return extensionRegistry;
	}

	@Inject
	public void setExtensionRegistry(IExtensionRegistry extensionRegistry) {
		this.extensionRegistry = extensionRegistry;
	}
	
	/*************************************************************************
	 * Utilities
	 *************************************************************************/
	
}
