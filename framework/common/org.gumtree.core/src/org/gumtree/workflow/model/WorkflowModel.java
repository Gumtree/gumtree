package org.gumtree.workflow.model;


@SuppressWarnings("serial")
public class WorkflowModel extends ElementModel {

	public WorkflowModel() {
		super();
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	public WorkflowModel clone() throws CloneNotSupportedException {
		return (WorkflowModel) super.clone();
	}

}
