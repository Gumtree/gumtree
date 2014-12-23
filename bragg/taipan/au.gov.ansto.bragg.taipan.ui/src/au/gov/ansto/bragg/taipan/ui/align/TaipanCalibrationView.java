package au.gov.ansto.bragg.taipan.ui.align;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class TaipanCalibrationView extends ViewPart {

	public TaipanCalibrationView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		TaipanCalibrationViewer viewer = new TaipanCalibrationViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(viewer);
	}

	@Override
	public void setFocus() {
	}

}
