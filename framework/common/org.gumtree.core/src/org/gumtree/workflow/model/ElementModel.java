package org.gumtree.workflow.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("serial")
public abstract class ElementModel implements Serializable, Cloneable {

	private String name;

	private UUID id;
	
	private Map<String, Object> properties;

	private List<TaskModel> children;

	private List<TaskModel> startTasks;

	private List<TaskModel> endTasks;

	// private String assignedId;

	public ElementModel() {
		id = UUID.randomUUID();
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

	public UUID getId() {
		return id;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
