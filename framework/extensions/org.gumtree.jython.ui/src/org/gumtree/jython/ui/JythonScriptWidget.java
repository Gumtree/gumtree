package org.gumtree.jython.ui;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.script.ScriptEngine;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.gumtree.widgets.swt.ExtendedComposite;

public class JythonScriptWidget extends ExtendedComposite {

	private IScriptingManager scriptingManager;

	private IDataAccessManager dataAccessManager;

	private IScriptExecutor scriptExecutor;

	private URI scriptURI;

	public JythonScriptWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
	}

	@PostConstruct
	public void render() {
		// Initialise
		ScriptEngine scriptEngine = getScriptingManager()
				.createEngine("jython");
		scriptExecutor = new ScriptExecutor(scriptEngine);
		scriptExecutor.getEngine().put("parentComposite", this);
		scriptExecutor.runScript("from gumpy.commons.swt import swtFunction");
		scriptExecutor.runScript("from gumpy.commons.swt import refreshWidget");

		// Load script
		InputStream inputStream = getDataAccessManager().get(getScriptURI(),
				InputStream.class);
		InputStreamReader reader = new InputStreamReader(inputStream);
		scriptExecutor.runScript(reader);

		// Run
		scriptExecutor.runScript("create(parentComposite)");
		scriptExecutor.runScript("refreshWidget(parentComposite)");
	}

	@Override
	protected void disposeWidget() {
		if (scriptExecutor != null) {
			scriptExecutor.runScript("del parentComposite");
			JobRunner.run(new ILoopExitCondition() {
				@Override
				public boolean getExitCondition() {
					return !scriptExecutor.isBusy();
				}
			}, new Runnable() {
				@Override
				public void run() {
					scriptExecutor.shutDown();
					scriptExecutor = null;
				}
			});
		}
		scriptURI = null;
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

	/**************************************************************************
	 * Properties
	 **************************************************************************/

	public URI getScriptURI() {
		return scriptURI;
	}

	public void setScriptURI(URI scriptURI) {
		this.scriptURI = scriptURI;
	}

}
