package au.gov.ansto.bragg.banksia.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class BanksiaWorkflowView extends ViewPart {

	public BanksiaWorkflowView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		BanksiaWorkflowViewer viewer = new BanksiaWorkflowViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(viewer);
	}

	@Override
	public void setFocus() {
	}

}
