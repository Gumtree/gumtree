package org.gumtree.common.scripts.tests;

import java.io.File;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.scripting.IScriptingManager;
import org.gumtree.util.eclipse.EclipseUtils;
import org.junit.Test;

public class JythonTest {

	@Test
	public void runtest() throws CoreException, ScriptException {
		IScriptingManager manager = ServiceUtils.getService(IScriptingManager.class);
		ScriptEngine engine = manager.createEngine("jython");
		IFileStore scriptFile = EclipseUtils.find(Activator.PLUGIN_ID, "scripts/test.py");
		engine.eval(new InputStreamReader(scriptFile.openInputStream(EFS.NONE, new NullProgressMonitor())));
		
		File resultFolder = EclipseUtils.find(Activator.PLUGIN_ID, "target/surefire-reports").toLocalFile(EFS.NONE, new NullProgressMonitor());
		resultFolder.mkdir();
		
		File resultFile = new File(resultFolder, "jython_test.xml");
		System.out.println(resultFile.toString());
		engine.eval("run('" + resultFile.toString().replace('\\', '/') + "')");
	}
	
}
