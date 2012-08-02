package org.gumtree.ui.util.workbench;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.gumtree.ui.internal.Activator;

@SuppressWarnings("restriction")
public class E4Processor {

	@Inject
	private IEclipseContext eclipseContext;

	@Execute
	public void execute() {
		Activator.getDefault().getEclipseContext().set(E4Processor.class, this);
	}

	public IEclipseContext getEclipseContext() {
		return eclipseContext;
	}

}
