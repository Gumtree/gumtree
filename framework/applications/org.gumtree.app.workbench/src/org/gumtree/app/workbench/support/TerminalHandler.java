package org.gumtree.app.workbench.support;

import javax.script.ScriptEngine;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.ScriptingUI;

public class TerminalHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Support opening different engine
		ScriptEngine engine = ServiceUtils.getService(IScriptingManager.class).createEngine();
		ScriptExecutor scriptExecutor = new ScriptExecutor(engine);
		ScriptingUI.launchNewCommandLineView(scriptExecutor);
		return null;
	}

}
