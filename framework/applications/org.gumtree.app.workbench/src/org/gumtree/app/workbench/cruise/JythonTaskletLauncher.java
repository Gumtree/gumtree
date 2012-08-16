package org.gumtree.app.workbench.cruise;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.inject.Inject;
import javax.script.ScriptEngine;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.tasklet.ITasklet;
import org.gumtree.ui.tasklet.ITaskletLauncher;
import org.gumtree.ui.tasklet.support.TaskletUtilities;
import org.gumtree.ui.util.workbench.WorkbenchUtils;

@SuppressWarnings("restriction")
public class JythonTaskletLauncher implements ITaskletLauncher {
	
	private IScriptingManager scriptingManager;
	
	private IDataAccessManager dataAccessManager;
	
	public JythonTaskletLauncher() {
		
	}
	
	@Override
	public void launchTasklet(ITasklet tasklet, MPerspective mPerspective) {
		if (getScriptingManager() == null || tasklet == null) {
			return;
		}
		
		// Prepare script engine
		ScriptEngine scriptEngine = getScriptingManager().createEngine("jython");
		IScriptExecutor scriptExecutor = new ScriptExecutor(scriptEngine);
		URI scriptURI = URI.create("bundle://org.gumtree.app.workbench/scripts/jython/tasklets/runner.py");
		InputStream inputStream = getDataAccessManager().get(scriptURI, InputStream.class);
		InputStreamReader reader = new InputStreamReader(inputStream);
		scriptExecutor.runScript(reader);
		
		// Import tasklet script
		scriptURI = URI.create(tasklet.getContributionURI());
		inputStream = getDataAccessManager().get(scriptURI, InputStream.class);
		reader = new InputStreamReader(inputStream);
		scriptExecutor.runScript(reader);
		
		// Run
		scriptExecutor.getEngine().put("mPerspective", mPerspective);
		scriptExecutor.runScript("runnable = Runnable()");
		scriptExecutor.runScript("runnable.setFunction(createPerspective)");
		scriptExecutor.runScript("runnable.setMPerspective(mPerspective)");
		scriptExecutor.runScript("SafeUIRunner.asyncExec(runnable)");
		
		// TODO: error handling
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
