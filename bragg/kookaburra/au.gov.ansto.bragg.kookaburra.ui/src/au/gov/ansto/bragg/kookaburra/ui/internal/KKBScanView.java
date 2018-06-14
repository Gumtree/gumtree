package au.gov.ansto.bragg.kookaburra.ui.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class KKBScanView extends ViewPart {

	public final static String ID_VIEW_STANDALONESCRIPTING = "au.gov.ansto.bragg.kookaburra.ui.KKBScanView";
	private KKBScanViewer viewer;
	
	public KKBScanView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new KKBScanViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(viewer);
	}

	@Override
	public void setFocus() {
	}

	public KKBScanViewer getViewer() {
		return viewer;
	}
}
