package au.gov.ansto.bragg.dingo.workbench;


import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.dingo.workbench.internal.Activator;


public class DingoCruisePage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Dingo";
	}

	@Override
	public Composite create(Composite parent) {
		DingoCruisePageWidget widget = new DingoCruisePageWidget(parent, SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget.render();
	}

}
