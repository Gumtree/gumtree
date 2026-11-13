package au.gov.ansto.bragg.kookaburra.ui.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class KKBConfigView extends ViewPart {

	public final static String ID_VIEW_STANDALONESCRIPTING = "au.gov.ansto.bragg.kookaburra.ui.KKBConfigView";
	private KKBConfigViewer viewer;
	
	public KKBConfigView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new KKBConfigViewer(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(viewer);
	}

	@Override
	public void setFocus() {
	}

	public KKBConfigViewer getViewer() {
		return viewer;
	}
}
