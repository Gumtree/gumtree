package org.gumtree.widgets.swt.forms;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.widgets.swt.ExtendedComposite;

public abstract class ExtendedFormComposite extends ExtendedComposite {

	public ExtendedFormComposite(Composite parent, int style) {
		super(parent, style);
		setWidgetFactory(new FormWidgetFactory());
		getToolkit().adapt(this);
	}

	public FormWidgetFactory getWidgetFactory() {
		return (FormWidgetFactory) super.getWidgetFactory();
	}
	
	public FormToolkit getToolkit() {
		return getWidgetFactory().getToolkit();
	}

}
