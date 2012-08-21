package org.gumtree.ui.tasklet;

import java.util.Map;

import org.gumtree.core.object.IDisposable;

public interface IActivatedTasklet extends IDisposable {
	
	public String getId();

	public String getLabel();
	
	public ITasklet getTasklet();
	
	public Map<Object, Object> getContext();
	
}
