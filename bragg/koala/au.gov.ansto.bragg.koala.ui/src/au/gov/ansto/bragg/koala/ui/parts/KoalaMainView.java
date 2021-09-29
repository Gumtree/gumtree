package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class KoalaMainView extends ViewPart {

	private KoalaMainViewer viewer;
	
	public KoalaMainView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new KoalaMainViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer);
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
