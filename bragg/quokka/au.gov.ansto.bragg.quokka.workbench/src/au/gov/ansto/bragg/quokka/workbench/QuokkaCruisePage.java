package au.gov.ansto.bragg.quokka.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.quokka.workbench.internal.Activator;

@SuppressWarnings("restriction")
public class QuokkaCruisePage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Quokka";
	}

	@Override
	public Composite create(Composite parent) {
		QuokkaCruisePageWidget widget = new QuokkaCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}

}
