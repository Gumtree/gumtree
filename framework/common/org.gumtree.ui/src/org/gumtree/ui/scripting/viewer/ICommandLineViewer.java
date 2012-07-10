package org.gumtree.ui.scripting.viewer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.ui.util.workbench.IPartControlProvider;

public interface ICommandLineViewer extends IPartControlProvider {

	public static final int NO_INPUT_TEXT = 1 << 1;
	
	public static final int NO_UTIL_AREA = 1 << 2;
	
	public int getStyle();
	
	public IScriptExecutor getScriptExecutor();
	
	public void setScriptExecutor(IScriptExecutor executor);
	
	public void appendScript(Reader reader);
	
	public void createPartControl(Composite parent, int style);
	
	public boolean isContentAssistEnabled();
	
	public void setContentAssistEnabled(boolean enabled);
	
	public boolean isScrollLocked();
	
	public void setScrollLocked(boolean locked);
	
	public void clearConsole();

	public String getConsoleText();
	
	public String[] getCommandHistory();
	
	public void exportConsoleText(Writer writer) throws IOException;
	
}
