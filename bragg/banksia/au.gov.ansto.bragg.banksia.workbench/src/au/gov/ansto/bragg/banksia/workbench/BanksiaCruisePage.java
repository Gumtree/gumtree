package au.gov.ansto.bragg.banksia.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;


public class BanksiaCruisePage implements ICruisePanelPage{

	@Override
	public String getName() {
		return "Bilby";
	}

	@Override
	public Composite create(Composite parent) {
		BanksiaCruisePageWidget widget = new BanksiaCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}
}
