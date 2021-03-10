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
public class KoalaWorkflowPerspective implements IPerspectiveFactory {

	public static final String ID_KOALA_WORKFLOW_PERSPECTIVE = "au.gov.ansto.bragg.koala.ui.KoalaWorkflowPerspective";
	private static final String ID_KOALA_WORKFLOW_VIEW = "au.gov.ansto.bragg.koala.ui.KoalaWorkflowView";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView(ID_KOALA_WORKFLOW_VIEW, false, 
				IPageLayout.TOP, 1.0f, layout.getEditorArea());
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

	}

}
