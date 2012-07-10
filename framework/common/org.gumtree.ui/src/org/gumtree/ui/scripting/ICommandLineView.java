package org.gumtree.ui.scripting;

import org.eclipse.ui.IViewPart;
import org.gumtree.scripting.IScriptExecutor;

public interface ICommandLineView extends IViewPart {

	public static final String DIR_KEY_STYLE = ICommandLineView.class.getName() + ".style";
	
	// Can only be set once in this stage
	public void setEngineExecutor(IScriptExecutor executor);

	public boolean isEngineSet();
	
}
