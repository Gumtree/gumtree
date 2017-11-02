/**
 * 
 */
package au.gov.ansto.bragg.spatz.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author nxi
 *
 */
public class SpatzWorkflowPerspective implements IPerspectiveFactory {

	public static final String ID_SPATZ_WORKFLOW_PERSPECTIVE = "au.gov.ansto.bragg.spatz.ui.SpatzWorkflowPerspective";
	private static final String ID_SPATZ_WORKFLOW_VIEW = "au.gov.ansto.bragg.banksia.ui.SpatzWorkflowView";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView(ID_SPATZ_WORKFLOW_VIEW, false, 
				IPageLayout.TOP, 1.0f, layout.getEditorArea());
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

	}

}
