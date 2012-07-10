package org.gumtree.app.scriptrunner;

import java.io.FileReader;

import javax.script.ScriptEngine;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.scripting.support.ScriptingManager;
import org.gumtree.service.cli.ICommandLineOptions;
import org.gumtree.service.cli.support.CommandLineOptions;

public class ScriptRunnerApplication implements IApplication {

	private static final String OPT_SCRIPT_FILE = "scriptFile";
	
	private static final String OPT_SCRIPT_ENGINE = "scriptEngine";

	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		ICommandLineOptions options = new CommandLineOptions(args);
		String scriptFile = options.getOptionValue(OPT_SCRIPT_FILE);
		String scriptEngine = options.getOptionValue(OPT_SCRIPT_ENGINE);
		if (scriptFile != null) {
			IScriptingManager manager = ServiceUtils.getService(IScriptingManager.class);
			ScriptEngine engine = manager.createEngine(scriptEngine);
			FileReader reader = new FileReader(scriptFile);
			engine.eval(reader);
		}
		return null;
	}

	@Override
	public void stop() {
	}

	
}
