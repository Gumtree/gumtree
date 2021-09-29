/**
 * 
 */
package au.gov.ansto.bragg.koala.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author nxi
 *
 */
public class ChemistryWorkflowPerspective implements IPerspectiveFactory {

	public static final String ID_CHEMISTRY_WORKFLOW_PERSPECTIVE = "au.gov.ansto.bragg.koala.ui.ChemistryWorkflowPerspective";
	private static final String ID_CHEMISTRY_WORKFLOW_VIEW = "au.gov.ansto.bragg.koala.ui.ChemistryWorkflowView";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView(ID_CHEMISTRY_WORKFLOW_VIEW, false, 
				IPageLayout.TOP, 1.0f, layout.getEditorArea());
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

	}

}
