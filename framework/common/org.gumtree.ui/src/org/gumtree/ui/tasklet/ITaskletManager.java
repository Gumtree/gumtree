package org.gumtree.ui.tasklet;

import java.util.List;

import org.gumtree.core.service.IService;

public interface ITaskletManager extends IService {

	public static final String EVENT_TASKLET_REGISTRAION_ALL = "org/gumtree/ui/tasklet/registration/*";
	
	public static final String EVENT_TASKLET_REGISTRAION_ADD = "org/gumtree/ui/tasklet/registration/add";
	
	public static final String EVENT_TASKLET_REGISTRAION_REMOVED = "org/gumtree/ui/tasklet/registration/removed";
	
	public static final String EVENT_TASKLET_REGISTRAION_UPDATED = "org/gumtree/ui/tasklet/registration/updated";
	
	public static final String EVENT_TASKLET_ACTIVATED = "org/gumtree/ui/tasklet/activated";
	
	public void addTasklet(ITasklet tasklet);
	
	public void removeTasklet(ITasklet tasklet);
	
	public void updateTasklet(ITasklet tasklet);
	
	public List<ITasklet> getTasklets();
	
	public ITaskletLauncherFactory getTaskletLauncherFactory(ITasklet tasklet);
	
	public IActivatedTasklet activatedTasklet(ITasklet tasklet);
	
	public void deactivatedTasklet(IActivatedTasklet tasklet);
	
	public IActivatedTasklet getActivatedTasklet(String id);
	
	public List<IActivatedTasklet> getActivatedTasklets();
	
}
