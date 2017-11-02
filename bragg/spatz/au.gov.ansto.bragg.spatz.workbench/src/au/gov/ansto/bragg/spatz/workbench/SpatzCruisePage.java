package au.gov.ansto.bragg.spatz.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;


public class SpatzCruisePage implements ICruisePanelPage{

	@Override
	public String getName() {
		return "Spatz";
	}

	@Override
	public Composite create(Composite parent) {
		SpatzCruisePageWidget widget = new SpatzCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}
}
