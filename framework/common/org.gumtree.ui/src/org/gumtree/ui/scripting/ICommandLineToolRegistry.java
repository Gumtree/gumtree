package org.gumtree.ui.scripting;

import org.eclipse.core.runtime.CoreException;
import org.gumtree.core.service.IService;
import org.gumtree.ui.scripting.tools.ICommandLineTool;

public interface ICommandLineToolRegistry extends IService {

	// Return null if not found
	public String getCommandLineToolLabel(String id);
	
	// Return null if not found
	public ICommandLineTool createCommandLineTool(String id) throws CoreException;
	
}
