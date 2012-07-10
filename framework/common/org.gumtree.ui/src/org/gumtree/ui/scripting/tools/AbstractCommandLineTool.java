package org.gumtree.ui.scripting.tools;

import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.gumtree.ui.util.workbench.AbstractPartControlProvider;

public abstract class AbstractCommandLineTool extends AbstractPartControlProvider implements ICommandLineTool {
	
	private ICommandLineViewer viewer;
	
	private IScriptExecutor executor;

	public ICommandLineViewer getCommandLineViewer() {
		return viewer;
	}
	
	public void setCommandLineViewer(ICommandLineViewer viewer) {
		this.viewer = viewer;
	}
	
	public IScriptExecutor getScriptExecutor() {
		return executor;
	}
	
	public void setScriptExecutor(IScriptExecutor executor) {
		this.executor = executor;
	}
	
}
