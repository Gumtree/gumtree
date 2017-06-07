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
public class BFAnalysisView extends ViewPart {

	private BFAnalysisViewer viewer;
	/**
	 * 
	 */
	public BFAnalysisView() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		// TODO Auto-generated method stub
		viewer = new BFAnalysisViewer(parent, SWT.NONE);
	}

	public BFAnalysisViewer getViewer() {
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
