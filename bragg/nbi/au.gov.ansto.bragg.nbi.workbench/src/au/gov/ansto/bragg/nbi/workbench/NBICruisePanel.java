package au.gov.ansto.bragg.nbi.workbench;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.ui.cruise.support.CruisePanel;

import au.gov.ansto.bragg.nbi.workbench.internal.InternalImage;

@SuppressWarnings("restriction")
public class NBICruisePanel extends CruisePanel {

	@Inject
	public NBICruisePanel(Composite parent, @Optional int style) {
		super(parent, style);
	}

	protected void createCruisePanel(Composite parent) {
		GridLayoutFactory.swtDefaults().applyTo(parent);
		
		Composite originalComposite = getWidgetFactory()
				.createComposite(parent);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(originalComposite);
		super.createCruisePanel(originalComposite);

		Label label = getWidgetFactory().createLabel(parent, "");
		label.setImage(InternalImage.STOP_128.getImage());
	}

}
