package au.gov.ansto.bragg.nbi.workbench;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.control.ui.widgets.InterruptWidget;
//import org.gumtree.gumnix.sics.widgets.swt.SicsInterruptWidget;
import org.gumtree.ui.cruise.support.CruisePanel;
import org.gumtree.ui.util.resource.SharedImage;

import au.gov.ansto.bragg.nbi.workbench.internal.Activator;
import au.gov.ansto.bragg.nbi.workbench.internal.InternalImage;

@SuppressWarnings("restriction")
public class NBICruisePanel extends CruisePanel {

	private static final String GUMTREE_USE_LARGE_STOP_BUTTON = "gumtree.sics.useLargeStopButton";

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

		InterruptWidget interruptWidget = new InterruptWidget(parent,
				SWT.NONE);
		String useLargeStopButton = System.getProperty(GUMTREE_USE_LARGE_STOP_BUTTON, "true");
		if (Boolean.parseBoolean(useLargeStopButton)) {
			interruptWidget.setButtonImage(InternalImage.STOP_128.getImage());
		} else {
			interruptWidget.setButtonImage(InternalImage.STOP_64.getImage());
		}
		ContextInjectionFactory.inject(interruptWidget, Activator.getDefault()
				.getEclipseContext());
		interruptWidget.render();
		interruptWidget.setBackgroundImage(SharedImage.CRUISE_BG.getImage());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(interruptWidget);
	}

}
