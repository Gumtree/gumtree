package au.gov.ansto.bragg.echidna.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.echidna.workbench.internal.Activator;

@SuppressWarnings("restriction")
public class EchidnaCruisePage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Echidna";
	}

	@Override
	public Composite createNormalWidget(Composite parent) {
		EchidnaCruisePageWidget widget = new EchidnaCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget.render();
	}

	@Override
	public Composite createFullWidget(Composite parent) {
		EchidnaCruisePageWidget widget = new EchidnaCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget.render();
	}

}
