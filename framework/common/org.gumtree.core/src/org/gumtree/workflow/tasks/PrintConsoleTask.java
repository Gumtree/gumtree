package org.gumtree.workflow.tasks;

import org.gumtree.workflow.AbstractTask;

@SuppressWarnings("serial")
public class PrintConsoleTask extends AbstractTask<Object, Object> {

	@Override
	public void run() {
		if (getInput() != null) {
			System.out.println(getInput().toString());
		}
		setOutput(getInput());
	}


}
