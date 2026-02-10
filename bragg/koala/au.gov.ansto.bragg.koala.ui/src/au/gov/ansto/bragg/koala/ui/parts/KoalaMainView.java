package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


public class KoalaMainView extends ViewPart {

	private static final String DEFAULT_SCROLL_WIDTH = "2200";
	private static final String DEFAULT_SCROLL_HEIGHT = "1280";
	
	private static final String SCROLL_WIDTH_PROPERTY = "gumtree.koala.scrollWidth";
	private static final String SCROLL_HEIGHT_PROPERTY = "gumtree.koala.scrollHeight";
	
	private KoalaMainViewer viewer;
	private int scrollHeight;
	private int scrollWidth;
	
	public KoalaMainView() {
		scrollHeight = Integer.parseInt(System.getProperty(SCROLL_HEIGHT_PROPERTY, DEFAULT_SCROLL_HEIGHT));
		scrollWidth = Integer.parseInt(System.getProperty(SCROLL_WIDTH_PROPERTY, DEFAULT_SCROLL_WIDTH));
	}

	@Override
	public void createPartControl(Composite parent) {
		ScrolledComposite container = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer = new KoalaMainViewer(container, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer);
		container.setContent(viewer);
		container.setMinSize(scrollWidth, scrollHeight);
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
