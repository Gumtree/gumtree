package org.gumtree.workflow.model;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class TaskModel extends ElementModel {

	private ElementModel parent;

	private List<TaskModel> previousTasks;

	private List<TaskModel> nextTasks;

	private String taskClass;

	private boolean isExecutable;

	private boolean isSynchronous;

	public TaskModel() {
		super();
		previousTasks = new ArrayList<TaskModel>(2);
		nextTasks = new ArrayList<TaskModel>(2);
		isExecutable = true;
		isSynchronous = true;
	}

	/*************************************************************************
	 * Properties
	 *************************************************************************/

	public String getTaskClass() {
		return taskClass;
	}

	public void setTaskClass(String taskClass) {
		this.taskClass = taskClass;
	}

	public boolean isExecutable() {
		return isExecutable;
	}

	public void setExecutable(boolean isExecutable) {
		this.isExecutable = isExecutable;
	}

	public boolean isSynchronous() {
		return isSynchronous;
	}

	public void setSynchronous(boolean isSynchronous) {
		this.isSynchronous = isSynchronous;
	}

	/*************************************************************************
	 * Structure
	 *************************************************************************/

	public ElementModel getParent() {
		return parent;
	}

	public void setParent(ElementModel parent) {
		this.parent = parent;
	}

	public List<TaskModel> getPreviousTasks() {
		return previousTasks;
	}

	public List<TaskModel> getNextTasks() {
		return nextTasks;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	public boolean isSplitter() {
		return getNextTasks().size() > 1;
	}

	public boolean isMerger() {
		return getPreviousTasks().size() > 1;
	}

	public boolean isComposite() {
		return getChildren().size() > 0;
	}

	public void addNextTask(TaskModel taskModel) {
		getNextTasks().add(taskModel);
		taskModel.getPreviousTasks().add(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isExecutable ? 1231 : 1237);
		result = prime * result + (isSynchronous ? 1231 : 1237);
		result = prime * result
				+ ((nextTasks == null) ? 0 : nextTasks.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result
				+ ((previousTasks == null) ? 0 : previousTasks.hashCode());
		result = prime * result
				+ ((taskClass == null) ? 0 : taskClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskModel other = (TaskModel) obj;
		if (isExecutable != other.isExecutable)
			return false;
		if (isSynchronous != other.isSynchronous)
			return false;
		if (nextTasks == null) {
			if (other.nextTasks != null)
				return false;
		} else if (!nextTasks.equals(other.nextTasks))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (previousTasks == null) {
			if (other.previousTasks != null)
				return false;
		} else if (!previousTasks.equals(other.previousTasks))
			return false;
		if (taskClass == null) {
			if (other.taskClass != null)
				return false;
		} else if (!taskClass.equals(other.taskClass))
			return false;
		return true;
	}

}
