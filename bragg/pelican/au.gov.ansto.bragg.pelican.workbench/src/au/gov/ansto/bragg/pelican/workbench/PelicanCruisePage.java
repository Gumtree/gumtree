package au.gov.ansto.bragg.pelican.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.pelican.workbench.internal.Activator;

@SuppressWarnings("restriction")
public class PelicanCruisePage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Pelican";
	}

	@Override
	public Composite create(Composite parent) {
		PelicanCruisePageWidget widget = new PelicanCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}

}
