/**
 * 
 */
package au.gov.ansto.bragg.banksia.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author nxi
 *
 */
public class BanksiaWorkflowPerspective implements IPerspectiveFactory {

	public static final String ID_BANKSIA_WORKFLOW_PERSPECTIVE = "au.gov.ansto.bragg.banksia.ui.BanksiaWorkflowPerspective";
	private static final String ID_BANKSIA_WORKFLOW_VIEW = "au.gov.ansto.bragg.banksia.ui.BanksiaWorkflowView";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView(ID_BANKSIA_WORKFLOW_VIEW, false, 
				IPageLayout.TOP, 1.0f, layout.getEditorArea());
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

	}

}
