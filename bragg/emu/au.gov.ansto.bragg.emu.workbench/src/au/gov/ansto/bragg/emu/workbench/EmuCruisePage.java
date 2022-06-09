package au.gov.ansto.bragg.emu.workbench;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

import au.gov.ansto.bragg.emu.workbench.internal.Activator;


public class EmuCruisePage implements ICruisePanelPage{

	@Override
	public String getName() {
		return "Emu";
	}

	@Override
	public Composite create(Composite parent) {
		EmuCruisePageWidget widget = new EmuCruisePageWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(widget, Activator.getDefault()
				.getEclipseContext());
		return widget;
	}
}
