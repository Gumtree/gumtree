package org.gumtree.ui.scripting.support;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.gumtree.ui.internal.Activator;
import org.gumtree.util.eclipse.ExtensionRegistryReader;

public class CommandLineToolRegistryReader extends ExtensionRegistryReader {

	protected static String EXTENSION_TOOLS = "tools";

	protected static String ELEMENT_TOOL = "tool";
	
	protected static String EXTENTION_POINT_TOOLS = Activator.PLUGIN_ID + "."
			+ EXTENSION_TOOLS;
	
	private CommandLineToolRegistry registry;
	
	protected CommandLineToolRegistryReader(CommandLineToolRegistry registry) {
		super(Activator.getDefault());
		this.registry = registry;
	}
	
	@Override
	protected boolean readElement(IConfigurationElement element) {
		if (element.getName().equals(ELEMENT_TOOL)) {
			registry.addTool(element);
			return true;
		}
		return false;
	}

	protected void readTools() {
		IExtensionRegistry in = Platform.getExtensionRegistry();
		readRegistry(in, getPlugin().getBundle().getSymbolicName(),
				EXTENSION_TOOLS);
	}
	
}
