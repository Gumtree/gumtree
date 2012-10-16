package org.gumtree.workflow.tasks;

import org.gumtree.scripting.IScriptExecutor;

@SuppressWarnings("serial")
public class ScriptExecutionTask extends
		AbstractTask<IScriptExecutor, IScriptExecutor> {

	@Override
	public void run() {
		IScriptExecutor executor = getInput();
		if (executor != null) {
			String script = (String) getProperties().get("script");
			executor.runScript(script);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while(executor.isBusy()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		setOutput(executor);
	}

}
