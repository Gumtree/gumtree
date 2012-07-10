package org.gumtree.workflow.ui.config;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;

import com.google.common.base.Objects;

public class WorkflowConfig {
	
	private List<TaskConfig> taskConfigs;
	
	private IParameters context;
	
	private IParameters parameters;
	
	public WorkflowConfig() {
		super();
	}
	
	public List<TaskConfig> getTaskConfigs() {
		if (taskConfigs == null) {
			taskConfigs = new ArrayList<TaskConfig>();
		}
		return taskConfigs;
	}

	public void setTaskConfigs(List<TaskConfig> taskConfigs) {
		this.taskConfigs = taskConfigs;
	}
	
	public IParameters getContext() {
		if (context == null) {
			context = new Parameters();
		}
		return context;
	}

	public void setContext(IParameters context) {
		this.context = context;
	}

	public IParameters getParameters() {
		if (parameters == null) {
			parameters = new Parameters();
		}
		return parameters;
	}

	public void setParameters(IParameters parameters) {
		this.parameters = parameters;
	}

	public String toString() {
		return Objects.toStringHelper(this).toString();
	}
	
}
