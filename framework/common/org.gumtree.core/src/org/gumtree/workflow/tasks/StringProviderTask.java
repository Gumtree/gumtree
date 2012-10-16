package org.gumtree.workflow.tasks;

import org.gumtree.workflow.AbstractTask;

@SuppressWarnings("serial")
public class StringProviderTask extends AbstractTask<Void, String> {

	@Override
	public void run() {
		setOutput("Hello, World");
	}

}
