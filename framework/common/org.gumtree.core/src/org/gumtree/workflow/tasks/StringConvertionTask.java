package org.gumtree.workflow.tasks;

import org.gumtree.workflow.AbstractTask;

@SuppressWarnings("serial")
public class StringConvertionTask extends AbstractTask<String, String> {

	@Override
	public void run() {
		String processedString = getInput().toUpperCase();
		setOutput(processedString);
	}

}
