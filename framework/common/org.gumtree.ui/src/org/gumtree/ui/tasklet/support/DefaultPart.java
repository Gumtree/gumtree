package org.gumtree.ui.tasklet.support;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.widgets.ExtendedComposite;

@SuppressWarnings("restriction")
public class DefaultPart extends ExtendedComposite {

	@Inject
	public DefaultPart(Composite parent, @Optional int style) {
		super(parent, style);
		setLayout(new FillLayout());
	}

	@Override
	protected void disposeWidget() {
	}

}
