/**
 * 
 */
package au.gov.ansto.bragg.taipan.ui.befilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * @author nxi
 *
 */
public class BFLiveView extends ViewPart {

	private BFLiveViewer viewer;
	/**
	 * 
	 */
	public BFLiveView() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		viewer = new BFLiveViewer(parent, SWT.NONE);
	}

	public BFLiveViewer getViewer() {
		return viewer;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
