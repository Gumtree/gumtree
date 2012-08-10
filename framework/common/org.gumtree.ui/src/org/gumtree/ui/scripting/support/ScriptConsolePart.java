package org.gumtree.ui.scripting.support;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.internal.Activator;
import org.gumtree.ui.scripting.IScriptConsole;

public class ScriptConsolePart {

	@Inject
	public void init(Composite parent) {
		IScriptConsole scriptConsole = new ScriptConsole(parent, SWT.NONE);
		ContextInjectionFactory.inject(scriptConsole, Activator.getDefault().getEclipseContext());
	}

}
