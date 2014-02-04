package au.gov.ansto.bragg.bilby.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.bilby.workbench.internal.Activator;


public class BilbyCruisePage implements ICruisePanelPage{

	@Override
	public String getName() {
		return "Bilby";
	}

	@Override
	public Composite create(Composite parent) {
		BilbyCruisePageWidget widget = new BilbyCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}
}
