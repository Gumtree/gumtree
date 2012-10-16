package org.gumtree.workflow.tasks;

import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.ScriptExecutor;

@SuppressWarnings("serial")
public class ScriptExecutorTask extends AbstractTask<Void, IScriptExecutor> {

	@Override
	public void run() {
		IScriptExecutor scriptExecutor = new ScriptExecutor();
		setOutput(scriptExecutor);
	}

}
