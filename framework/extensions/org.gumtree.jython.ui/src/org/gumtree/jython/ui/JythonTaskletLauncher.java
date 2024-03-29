package org.gumtree.jython.ui;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.inject.Inject;
import javax.script.ScriptEngine;

import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.tasklet.IActivatedTasklet;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.ITaskletLauncher;

public class JythonTaskletLauncher implements ITaskletLauncher {

	private IScriptingManager scriptingManager;

	private IDataAccessManager dataAccessManager;

	private IScriptExecutor scriptExecutor;
	
	public JythonTaskletLauncher() {
	}

	@Override
	public void launchTasklet(final IActivatedTasklet activatedTasklet) {
//		SafeUIRunner.asyncExec(new SafeRunnable() {

//			@Override
//			public void run() throws Exception {
				ITasklet tasklet = activatedTasklet.getTasklet();
				// Prepare script engine
				ScriptEngine scriptEngine = getScriptingManager().createEngine(
						"jython");
				scriptExecutor = new ScriptExecutor(
						scriptEngine);
				scriptExecutor.getEngine().put("activatedTasklet",
						activatedTasklet);
				activatedTasklet.getContext().put(IScriptExecutor.class,
						scriptExecutor);

				// Import helper script
				URI scriptURI = URI
						.create("bundle://org.gumtree.jython.ui/scripts/tasklet/TaskletRunner.py");
				InputStream inputStream = getDataAccessManager().get(scriptURI,
						InputStream.class);
				InputStreamReader reader = new InputStreamReader(inputStream);
				scriptExecutor.runScript(reader);

				// Import tasklet script
				scriptURI = URI.create(tasklet.getContributionURI());
				inputStream = getDataAccessManager().get(scriptURI,
						InputStream.class);
				reader = new InputStreamReader(inputStream);
				scriptExecutor.runScript(reader);

				// Run
				scriptExecutor.runScript("__run__()");
//			}
//		});
	}

	@Override
	public void disposeObject() {
		if (scriptExecutor != null) {
			scriptExecutor.shutDown();
			scriptExecutor = null;
		}
		scriptingManager = null;
		dataAccessManager = null;
	}
	
	/**************************************************************************
	 * Components
	 **************************************************************************/

	public IScriptingManager getScriptingManager() {
		return scriptingManager;
	}

	@Inject
	public void setScriptingManager(IScriptingManager scriptingManager) {
		this.scriptingManager = scriptingManager;
	}

	public IDataAccessManager getDataAccessManager() {
		return dataAccessManager;
	}

	@Inject
	public void setDataAccessManager(IDataAccessManager dataAccessManager) {
		this.dataAccessManager = dataAccessManager;
	}

}
