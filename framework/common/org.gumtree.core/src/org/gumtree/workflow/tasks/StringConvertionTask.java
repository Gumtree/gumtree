package org.gumtree.workflow.tasks;


@SuppressWarnings("serial")
public class StringConvertionTask extends AbstractTask<String, String> {

	@Override
	public void run() {
		String processedString = getInput().toUpperCase();
		setOutput(processedString);
	}

}
