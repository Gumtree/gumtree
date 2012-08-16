package org.gumtree.ui.tasklet;

import java.util.List;

public interface ITaskletRegistry {

	public void addTasklet(ITasklet tasklet);
	
	public void removeTasklet(ITasklet tasklet);
	
	public List<ITasklet> getTasklets();
	
	/*************************************************************************
	 * Components
	 *************************************************************************/
	
}
