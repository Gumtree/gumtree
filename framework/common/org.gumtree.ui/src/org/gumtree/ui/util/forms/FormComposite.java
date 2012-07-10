package org.gumtree.ui.util.forms;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.gumtree.ui.widgets.ExtendedComposite;

public abstract class FormComposite extends ExtendedComposite {

	public FormComposite(Composite parent, int style) {
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
