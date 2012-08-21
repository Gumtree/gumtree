package org.gumtree.ui.tasklet.support;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.gumtree.ui.tasklet.IActivatedTasklet;
import org.gumtree.ui.tasklet.ITasklet;

public class ActivatedTasklet implements IActivatedTasklet {

	private String id;

	private ITasklet tasklet;

	private Map<Object, Object> context;

	public ActivatedTasklet(ITasklet tasklet) {
		id = UUID.randomUUID().toString();
		this.tasklet = tasklet;
		context = new HashMap<Object, Object>(2);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ITasklet getTasklet() {
		return tasklet;
	}

	@Override
	public Map<Object, Object> getContext() {
		return context;
	}

	@Override
	public void disposeObject() {
		id = null;
		tasklet = null;
		if (context != null) {
			context.clear();
			context = null;
		}
	}

}
