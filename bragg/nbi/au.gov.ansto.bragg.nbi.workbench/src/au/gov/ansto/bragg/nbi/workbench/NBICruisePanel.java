package au.gov.ansto.bragg.nbi.workbench;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.widgets.swt.SicsInterruptWidget;
import org.gumtree.ui.cruise.support.CruisePanel;
import org.gumtree.ui.util.resource.SharedImage;

import au.gov.ansto.bragg.nbi.workbench.internal.Activator;
import au.gov.ansto.bragg.nbi.workbench.internal.InternalImage;

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

		SicsInterruptWidget interruptWidget = new SicsInterruptWidget(parent,
				SWT.NONE);
		interruptWidget.setButtonImage(InternalImage.STOP_128.getImage());
		ContextInjectionFactory.inject(interruptWidget, Activator.getDefault()
				.getEclipseContext());
		interruptWidget.setBackgroundImage(SharedImage.CRUISE_BG.getImage());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(interruptWidget);
	}

}
