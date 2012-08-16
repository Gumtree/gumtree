package au.gov.ansto.bragg.taipan.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.taipan.workbench.interal.Activator;

@SuppressWarnings("restriction")
public class TaipanCruisePage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Taipan";
	}

	@Override
	public Composite create(Composite parent) {
		TaipanCruisePageWidget widget = new TaipanCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}

}
