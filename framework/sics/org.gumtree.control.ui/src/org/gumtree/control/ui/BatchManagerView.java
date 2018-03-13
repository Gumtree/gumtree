package org.gumtree.control.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.control.ui.batch.BatchManagerViewer;

public class BatchManagerView extends ViewPart {

	public BatchManagerView() {
		super();
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		new BatchManagerViewer(parent, SWT.NONE);
	}

	public void setFocus() {
	}

}
