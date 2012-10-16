package org.gumtree.workflow.tasks;


@SuppressWarnings("serial")
public class StringProviderTask extends AbstractTask<Void, String> {

	@Override
	public void run() {
		setOutput("Hello, World");
	}

}
