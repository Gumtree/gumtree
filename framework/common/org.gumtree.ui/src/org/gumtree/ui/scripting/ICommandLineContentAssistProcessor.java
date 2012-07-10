package org.gumtree.ui.scripting;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.gumtree.scripting.IScriptExecutor;

public interface ICommandLineContentAssistProcessor extends IContentAssistProcessor {

	public IScriptExecutor getScriptExecutor();
	
	public void setScriptExecutor(IScriptExecutor executor);
	
}
