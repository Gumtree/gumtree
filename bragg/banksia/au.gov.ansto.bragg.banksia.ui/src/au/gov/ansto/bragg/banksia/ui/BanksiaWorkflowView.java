package au.gov.ansto.bragg.banksia.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class BanksiaWorkflowView extends ViewPart {

	private BanksiaWorkflowViewer viewer;
	
	public BanksiaWorkflowView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new BanksiaWorkflowViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(viewer);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
		if (viewer != null) {
			viewer.dispose();
		}
	}
}
