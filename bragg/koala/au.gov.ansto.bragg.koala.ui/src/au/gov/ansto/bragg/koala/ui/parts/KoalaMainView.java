package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class KoalaMainView extends ViewPart {

	private static final int SCROLL_WIDTH = 2200;
	private static final int SCROLL_HEIGHT = 1280;
	
	private KoalaMainViewer viewer;
	
	public KoalaMainView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		ScrolledComposite container = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer = new KoalaMainViewer(container, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer);
		container.setContent(viewer);
		container.setMinSize(SCROLL_WIDTH, SCROLL_HEIGHT);

	    // Expand both horizontally and vertically
		container.setExpandHorizontal(true);
		container.setExpandVertical(true);
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
