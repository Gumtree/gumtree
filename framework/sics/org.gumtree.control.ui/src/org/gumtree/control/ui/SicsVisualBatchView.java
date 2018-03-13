package org.gumtree.control.ui;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.control.ui.batch.SicsBatchUIUtils;
import org.gumtree.control.ui.batch.taskeditor.SicsVisualBatchEditor;
import org.gumtree.workflow.ui.viewer.IWorkflowViewer;

public class SicsVisualBatchView extends ViewPart {

	private IWorkflowViewer viewer;
	
	public SicsVisualBatchView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		viewer = new SicsVisualBatchEditor();
		viewer.setWorkflow(SicsBatchUIUtils.createDefaultWorkflow());
		viewer.createPartControl(parent);
	}
	
	@Override
	public void setFocus() {
		if (viewer != null && !viewer.isDisposed()) {
			viewer.setFocus();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}
	
}
