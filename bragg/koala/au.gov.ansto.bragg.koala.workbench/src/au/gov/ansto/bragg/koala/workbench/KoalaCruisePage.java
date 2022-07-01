package au.gov.ansto.bragg.koala.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;


public class KoalaCruisePage implements ICruisePanelPage{

	@Override
	public String getName() {
		return "Instrument status";
	}

	@Override
	public Composite create(Composite parent) {
		KoalaCruisePageWidget widget = new KoalaCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}
}
