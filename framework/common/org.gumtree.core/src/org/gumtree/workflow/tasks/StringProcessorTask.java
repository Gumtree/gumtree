package org.gumtree.workflow.tasks;

import org.gumtree.util.string.StringUtils;
import org.gumtree.workflow.WorkflowParameter;
import org.gumtree.workflow.support.AbstractTask;

public class StringProcessorTask extends AbstractTask<String, String> {

	public enum Operation {
		TO_LOWER, TO_UPPER;
	}
	
	@WorkflowParameter(label="Operation")
	private Operation operation;

	@Override
	protected String runTask(String input) {
		if (StringUtils.isEmpty(getInput())) {
			return StringUtils.EMPTY_STRING;
		}
		if (getOperation().equals(Operation.TO_LOWER)) {
			return getInput().toLowerCase();
		} else if (getOperation().equals(Operation.TO_UPPER)) {
			return getInput().toUpperCase();
		}
		return getInput();
	}

	public Operation getOperation() {
		if (operation == null) {
			operation = Operation.TO_LOWER;
		}
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

}
