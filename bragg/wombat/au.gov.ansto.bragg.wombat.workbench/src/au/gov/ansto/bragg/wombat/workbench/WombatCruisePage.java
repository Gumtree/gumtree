package au.gov.ansto.bragg.wombat.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.wombat.workbench.internal.Activator;

@SuppressWarnings("restriction")
public class WombatCruisePage implements ICruisePanelPage {

	@Override
	public String getName() {
		return "Wombat";
	}

	@Override
	public Composite create(Composite parent) {
		WombatCruisePageWidget widget = new WombatCruisePageWidget(parent, SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget.render();
	}

}
