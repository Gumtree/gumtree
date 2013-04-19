package au.gov.ansto.bragg.platypus.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.platypus.workbench.internal.Activator;

public class PlatypusCruisePage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Platypus";
	}

	@Override
	public Composite create(Composite parent) {
		PlatypusCruisePageWidget widget = new PlatypusCruisePageWidget(parent, SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget.render();
	}

}
