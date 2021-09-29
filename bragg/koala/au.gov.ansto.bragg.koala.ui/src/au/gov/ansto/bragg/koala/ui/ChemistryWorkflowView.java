package au.gov.ansto.bragg.koala.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class ChemistryWorkflowView extends ViewPart {

	private ChemistryWorkflowViewer viewer;
	
	public ChemistryWorkflowView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new ChemistryWorkflowViewer(parent, SWT.NONE);
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
