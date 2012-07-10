package org.gumtree.ui.cruise.support;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class CruisePanelView extends ViewPart {

	@Override
	public void createPartControl(final Composite parent) {
		new CruisePanel(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
	}

}
