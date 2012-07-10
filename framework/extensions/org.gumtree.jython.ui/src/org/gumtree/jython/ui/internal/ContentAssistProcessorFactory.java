package org.gumtree.jython.ui.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.gumtree.jython.core.OsgiPyScriptEngine;
import org.gumtree.ui.scripting.ICommandLineContentAssistProcessor;

public class ContentAssistProcessorFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof OsgiPyScriptEngine &&
				adapterType.equals(ICommandLineContentAssistProcessor.class)) {
			return new JythonContentAssistProcessor();
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { OsgiPyScriptEngine.class };
	}
	
}
