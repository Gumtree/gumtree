package au.gov.ansto.bragg.kookaburra.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.kookaburra.workbench.internal.Activator;

@SuppressWarnings("restriction")
public class KookaburraCruisePage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Kookaburra";
	}

	@Override
	public Composite create(Composite parent) {
		KookaburraCruisePageWidget widget = new KookaburraCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}

}
