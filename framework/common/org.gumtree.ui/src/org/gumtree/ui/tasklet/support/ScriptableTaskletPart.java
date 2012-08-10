package org.gumtree.ui.tasklet.support;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.util.forms.FormComposite;
import org.gumtree.ui.widgets.ExtendedComposite;

@SuppressWarnings("restriction")
public class ScriptableTaskletPart extends FormComposite {

	@Inject
	public ScriptableTaskletPart(Composite parent, @Optional int style) {
		super(parent, style);
		setLayout(new FillLayout());
		getWidgetFactory().createLabel(this, "Hello");
	}

	@Override
	protected void disposeWidget() {
	}
	
}