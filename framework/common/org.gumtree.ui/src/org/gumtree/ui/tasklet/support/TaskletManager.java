package org.gumtree.ui.tasklet.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.gumtree.ui.tasklet.IActivatedTasklet;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.ITaskletLauncher;
import org.gumtree.ui.tasklet.ITaskletManager;
import org.gumtree.util.messaging.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskletManager implements ITaskletManager {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskletManager.class);

	private List<ITasklet> tasklets;

	private Map<String, IActivatedTasklet> activatedTasklets;

	private IExtensionRegistry extensionRegistry;

	private ITaskletLauncher taskletLauncher;
	
	private TaskletPersistor persistor;

	public TaskletManager() {
		tasklets = new ArrayList<ITasklet>(2);
		activatedTasklets = new HashMap<String, IActivatedTasklet>(2);
		persistor = new TaskletPersistor();
	}

	/*************************************************************************
	 * Life cycle
	 *************************************************************************/
	
	public void activate() {
		// Load from extension point
		if (getExtensionRegistry() != null) {
			TaskletExtensionReader reader = new TaskletExtensionReader();
			reader.setExtensionRegistry(getExtensionRegistry());
			List<ITasklet> registeredTasklet = reader.getRegisteredTasklets();
			tasklets.addAll(registeredTasklet);
		}
		// Load from local disk
		List<ITasklet> persistedTasklets = persistor.loadTasklet();
		tasklets.addAll(persistedTasklets);
	}

	public void deactivate() {
		if (tasklets != null) {
			tasklets.clear();
			tasklets = null;
		}
		if (activatedTasklets != null) {
			for (IActivatedTasklet activatedTasklet : activatedTasklets
					.values()) {
				activatedTasklet.disposeObject();
			}
			activatedTasklets.clear();
			activatedTasklets = null;
		}
		extensionRegistry = null;
	}
	
	/*************************************************************************
	 * Registry
	 *************************************************************************/
	
	@Override
	public void addTasklet(ITasklet tasklet) {
		tasklets.add(tasklet);
		// Notify
		new EventBuilder(EVENT_TASKLET_REGISTRAION_ADD).append("tasklet",
				tasklet).post();
		// Persist
		try {
			persistor.saveTasklet(tasklet);
		} catch (Exception e) {
			logger.error("Failed to save tasklet: " + tasklet.getLabel());
		}
	}

	@Override
	public void removeTasklet(ITasklet tasklet) {
		tasklets.remove(tasklet);
		// Notify
		new EventBuilder(EVENT_TASKLET_REGISTRAION_REMOVED).append("tasklet",
				tasklet).post();
	}

	@Override
	public List<ITasklet> getTasklets() {
		return Collections.unmodifiableList(tasklets);
	}

	@Override
	public IActivatedTasklet activatedTasklet(ITasklet tasklet) {
		IActivatedTasklet activatedTasklet = new ActivatedTasklet(tasklet, this);
		activatedTasklets.put(activatedTasklet.getId(), activatedTasklet);
		ITaskletLauncher launcher = getLauncher(tasklet);
		launcher.launchTasklet(activatedTasklet);
		return activatedTasklet;
	}

	@Override
	public void deactivatedTasklet(IActivatedTasklet activatedTasklet) {
		activatedTasklets.remove(activatedTasklet.getId());
		activatedTasklet.disposeObject();
	}

	@Override
	public IActivatedTasklet getActivatedTasklet(String id) {
		return activatedTasklets.get(id);
	}

	@Override
	public List<IActivatedTasklet> getActivatedTasklets() {
		return new ArrayList<IActivatedTasklet>(activatedTasklets.values());
	}

	@Override
	public ITaskletLauncher getLauncher(ITasklet tasklet) {
		// We only support single launcher at this stage
		return getTaskletLauncher();
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

	public ITaskletLauncher getTaskletLauncher() {
		return taskletLauncher;
	}

	@Inject
	public void setTaskletLauncher(ITaskletLauncher taskletLauncher) {
		this.taskletLauncher = taskletLauncher;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

}
