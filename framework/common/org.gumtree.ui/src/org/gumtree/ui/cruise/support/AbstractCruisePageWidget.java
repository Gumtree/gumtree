package org.gumtree.ui.cruise.support;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.ui.util.resource.SharedImage;
import org.gumtree.ui.widgets.ExtendedComposite;
import org.gumtree.ui.widgets.SwtWidgetFactory;

public abstract class AbstractCruisePageWidget extends ExtendedComposite {

	public AbstractCruisePageWidget(Composite parent, int style) {
		super(parent, style);
		setWidgetFactory(new SwtWidgetFactory() {
			public Label createLabel(Composite parent, String text, int style) {
				Label label = super.createLabel(parent, text, style);
				label.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
				return label;
			}
		});
		setBackgroundMode(SWT.INHERIT_FORCE);
		setBackgroundImage(SharedImage.CRUISE_BG.getImage());
	}

}
