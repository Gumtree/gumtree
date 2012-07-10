package org.gumtree.gumnix.sics.batch.ui.buffer;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class BatchBufferRunnerView extends ViewPart {

	public BatchBufferRunnerView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		new BatchBufferManagerViewer(parent, BatchBufferManagerViewer.HIDE_VALIDATOR);
	}

	@Override
	public void setFocus() {
	}

}
