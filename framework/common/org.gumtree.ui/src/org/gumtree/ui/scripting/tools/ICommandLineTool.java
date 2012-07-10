package org.gumtree.ui.scripting.tools;

import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.gumtree.ui.util.workbench.IPartControlProvider;

public interface ICommandLineTool extends IPartControlProvider {

	public ICommandLineViewer getCommandLineViewer();
	
	public void setCommandLineViewer(ICommandLineViewer viewer);

	public IScriptExecutor getScriptExecutor();
	
	public void setScriptExecutor(IScriptExecutor executor);
	
}
