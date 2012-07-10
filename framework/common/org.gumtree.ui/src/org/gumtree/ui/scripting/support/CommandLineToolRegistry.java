package org.gumtree.ui.scripting.support;

import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_CLASS;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_ID;
import static org.gumtree.util.eclipse.ExtensionRegistryConstants.ATTRIBUTE_LABEL;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.gumtree.ui.scripting.ICommandLineToolRegistry;
import org.gumtree.ui.scripting.tools.ICommandLineTool;

public class CommandLineToolRegistry implements ICommandLineToolRegistry {

	private Map<String, IConfigurationElement> configElementMap;
	
	private CommandLineToolRegistryReader reader;
	
	public CommandLineToolRegistry() {
		super();
		configElementMap = new HashMap<String, IConfigurationElement>();
	}

	public ICommandLineTool createCommandLineTool(String id) throws CoreException {
		checkReader();
		IConfigurationElement element = configElementMap.get(id);
		if (element != null) {
			return (ICommandLineTool) element.createExecutableExtension(ATTRIBUTE_CLASS);
		}
		return null;
	}

	public String getCommandLineToolLabel(String id) {
		checkReader();
		IConfigurationElement element = configElementMap.get(id);
		if (element != null) {
			return element.getAttribute(ATTRIBUTE_LABEL);
		}
		return null;
	}
	
	protected void addTool(IConfigurationElement element) {
		String id = element.getAttribute(ATTRIBUTE_ID);
		if (id != null) {
			configElementMap.put(id, element);
		}
	}
	
	private void checkReader() {
		if (reader == null) {
			synchronized (this) {
				if (reader == null) {
					reader = new CommandLineToolRegistryReader(this);
					reader.readTools();
				}
			}
		}
	}
	
}
