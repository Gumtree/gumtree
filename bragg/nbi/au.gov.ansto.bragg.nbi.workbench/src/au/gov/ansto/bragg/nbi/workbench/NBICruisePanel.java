package au.gov.ansto.bragg.nbi.workbench;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.support.CruisePanel;

import au.gov.ansto.bragg.nbi.workbench.internal.Activator;

@SuppressWarnings("restriction")
public class NBICruisePanel extends CruisePanel {

	@Inject
	public NBICruisePanel(Composite parent, @Optional int style) {
		super(parent, style);
	}

	protected void createCruisePanel(Composite parent) {
		GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0).applyTo(parent);

		Composite originalComposite = getWidgetFactory()
				.createComposite(parent);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(originalComposite);
		super.createCruisePanel(originalComposite);

		CruiseSicsInterruptWidget interruptWidget = new CruiseSicsInterruptWidget(parent,
				SWT.NONE);
		ContextInjectionFactory.inject(interruptWidget, Activator.getDefault()
				.getEclipseContext());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(interruptWidget);
	}

}
