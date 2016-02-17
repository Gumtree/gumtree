package au.gov.ansto.bragg.bilby.ui.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class BilbyWorkflowView extends ViewPart {

	public BilbyWorkflowView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		BilbyWorkflowViewer viewer = new BilbyWorkflowViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(viewer);
	}

	@Override
	public void setFocus() {
	}

}
