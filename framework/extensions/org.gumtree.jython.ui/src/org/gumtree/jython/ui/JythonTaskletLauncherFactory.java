package org.gumtree.jython.ui;

import javax.inject.Inject;

import org.gumtree.scripting.IScriptingManager;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.tasklet.ITaskletLauncher;
import org.gumtree.ui.tasklet.ITaskletLauncherFactory;

public class JythonTaskletLauncherFactory implements ITaskletLauncherFactory {

	private IScriptingManager scriptingManager;

	private IDataAccessManager dataAccessManager;
	
	@Override
	public ITaskletLauncher createLauncher() {
		JythonTaskletLauncher launcher = new JythonTaskletLauncher();
		launcher.setScriptingManager(getScriptingManager());
		launcher.setDataAccessManager(getDataAccessManager());
		return launcher;
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
