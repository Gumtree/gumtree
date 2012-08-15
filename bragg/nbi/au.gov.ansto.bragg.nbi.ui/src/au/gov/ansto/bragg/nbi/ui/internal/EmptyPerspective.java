/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author nxi
 *
 */
public class EmptyPerspective implements IPerspectiveFactory {

	/**
	 * 
	 */
	public EmptyPerspective() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {

	}

}
