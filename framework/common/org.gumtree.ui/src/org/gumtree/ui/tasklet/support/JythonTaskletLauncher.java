package org.gumtree.ui.tasklet.support;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.inject.Inject;
import javax.script.ScriptEngine;

import org.eclipse.jface.util.SafeRunnable;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.tasklet.IActivatedTasklet;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.ITaskletLauncher;
import org.gumtree.ui.util.SafeUIRunner;

public class JythonTaskletLauncher implements ITaskletLauncher {

	private IScriptingManager scriptingManager;

	private IDataAccessManager dataAccessManager;

	public JythonTaskletLauncher() {
	}

	@Override
	public void launchTasklet(final IActivatedTasklet activatedTasklet) {
		SafeUIRunner.asyncExec(new SafeRunnable() {

			@Override
			public void run() throws Exception {
				ITasklet tasklet = activatedTasklet.getTasklet();
				// Prepare script engine
				ScriptEngine scriptEngine = getScriptingManager().createEngine(
						"jython");
				IScriptExecutor scriptExecutor = new ScriptExecutor(
						scriptEngine);
				scriptExecutor.getEngine().put("activatedTasklet", activatedTasklet);
				activatedTasklet.getContext().put(IScriptExecutor.class,
						scriptExecutor);
				if (tasklet.isSimpleLayout()) {
					// Import helper script
					URI scriptURI = URI
							.create("bundle://org.gumtree.ui/scripts/tasklet/SimpleRunner.py");
					InputStream inputStream = getDataAccessManager().get(
							scriptURI, InputStream.class);
					InputStreamReader reader = new InputStreamReader(
							inputStream);
					scriptExecutor.runScript(reader);

					// Import tasklet script
					scriptURI = URI.create(tasklet.getContributionURI());
					inputStream = getDataAccessManager().get(scriptURI,
							InputStream.class);
					reader = new InputStreamReader(inputStream);
					scriptExecutor.runScript(reader);
					
					// Run
					scriptExecutor.runScript("run()");
				}
			}
		});
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
