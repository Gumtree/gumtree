package au.gov.ansto.bragg.pelican.ui.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import au.gov.ansto.bragg.pelican.exp.viewer.ExperimentConfigViewer;

public class ExperimentConfigView extends ViewPart {

	public ExperimentConfigView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		ExperimentConfigViewer viewer = new ExperimentConfigViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(viewer);
	}

	@Override
	public void setFocus() {
	}

}
