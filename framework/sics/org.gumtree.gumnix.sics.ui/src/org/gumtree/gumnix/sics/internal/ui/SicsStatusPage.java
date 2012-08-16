package org.gumtree.gumnix.sics.internal.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.cruise.ICruisePanelPage;

public class SicsStatusPage implements ICruisePanelPage {

	public String getName() {
		return "SICS";
	}

	public Composite create(Composite parent) {
		return new SicsStatusPageWidget(parent, SWT.NONE);
	}

}