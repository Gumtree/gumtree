package org.gumtree.workflow.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class ElementModel implements Serializable, Cloneable {

	private String name;

	private Map<String, Object> properties;

	private List<TaskModel> children;

	private List<TaskModel> startTasks;

	private List<TaskModel> endTasks;

	private String assignedId;

	public ElementModel() {
		properties = new HashMap<String, Object>(2);
		children = new ArrayList<TaskModel>(2);
		startTasks = new ArrayList<TaskModel>(2);
		endTasks = new ArrayList<TaskModel>(2);
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAssignedId() {
		return assignedId;
	}

	public void setAssignedId(String assignedId) {
		this.assignedId = assignedId;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	/*************************************************************************
	 * Structure
	 *************************************************************************/

	public List<TaskModel> getChildren() {
		return children;
	}

	public List<TaskModel> getStartTasks() {
		return startTasks;
	}

	public List<TaskModel> getEndTasks() {
		return endTasks;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	public TaskModel createTaskModel() {
		TaskModel taskModel = new TaskModel();
		taskModel.setParent(this);
		getChildren().add(taskModel);
		return taskModel;
	}

	public void addStartTask(TaskModel task) {
		getStartTasks().add(task);
	}
	
	public void addEndTask(TaskModel task) {
		getEndTasks().add(task);
	}
	
	public ElementModel clone() throws CloneNotSupportedException {
		return (ElementModel) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((assignedId == null) ? 0 : assignedId.hashCode());
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		result = prime * result
				+ ((endTasks == null) ? 0 : endTasks.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		result = prime * result
				+ ((startTasks == null) ? 0 : startTasks.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ElementModel other = (ElementModel) obj;
		if (assignedId == null) {
			if (other.assignedId != null)
				return false;
		} else if (!assignedId.equals(other.assignedId))
			return false;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (endTasks == null) {
			if (other.endTasks != null)
				return false;
		} else if (!endTasks.equals(other.endTasks))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (startTasks == null) {
			if (other.startTasks != null)
				return false;
		} else if (!startTasks.equals(other.startTasks))
			return false;
		return true;
	}

}
