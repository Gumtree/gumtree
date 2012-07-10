package org.gumtree.gumnix.sics.batch.ui.buffer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class BatchBufferManagerView extends ViewPart {

	public BatchBufferManagerView() {
		super();
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		new BatchBufferManagerViewer(parent, SWT.NONE);
	}

	public void setFocus() {
	}

}
