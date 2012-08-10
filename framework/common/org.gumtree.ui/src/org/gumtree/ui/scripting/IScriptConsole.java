package org.gumtree.ui.scripting;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.gumtree.scripting.IScriptExecutor;
import org.gumtree.ui.widgets.IWidget;

public interface IScriptConsole extends IWidget {

	public static final int NO_INPUT_TEXT = 1 << 1;

	public static final int NO_UTIL_AREA = 1 << 2;

	public static final String EVENT_TOPIC_SCRIPT_CONSOLE = "org/gumtree/ui/scritping/console";
	
	public static final String EVENT_TOPIC_SCRIPT_CONSOLE_SCROLL_LOCK = EVENT_TOPIC_SCRIPT_CONSOLE + "/scrollLock";
	
	public static final String EVENT_PROP_CONSOLE_ID = "consoleId";
	
	public static final String EVENT_PROP_LOCKED = "locked";
	
	/*************************************************************************
	 * Components
	 *************************************************************************/
	
	public IScriptExecutor getScriptExecutor();
	
	public void setScriptExecutor(IScriptExecutor scriptExecutor);
	
	public ICommandLineToolRegistry getToolRegistry();
	
	public void setToolRegistry(ICommandLineToolRegistry toolRegistry);
	
	/*************************************************************************
	 * Properties
	 *************************************************************************/
	
	public String getId();
	
	public int getOriginalStyle();
	
	public boolean isContentAssistEnabled();
	
	public void setContentAssistEnabled(boolean enabled);
	
	public boolean isScrollLocked();
	
	public void setScrollLocked(boolean locked);
	
	/*************************************************************************
	 * Utilities
	 *************************************************************************/
	
	public boolean setFocus();
	
	public void clearConsole();
	
	public String getConsoleText();
	
	public void exportConsoleText(Writer writer) throws IOException;
	
	public void appendScript(Reader reader);
	
	public String[] getCommandHistory();

}
