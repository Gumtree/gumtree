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

}
