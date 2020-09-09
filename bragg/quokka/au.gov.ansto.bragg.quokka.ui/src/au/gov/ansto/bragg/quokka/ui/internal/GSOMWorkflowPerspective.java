/**
 * 
 */
package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author nxi
 *
 */
public class GSOMWorkflowPerspective implements IPerspectiveFactory {

	public static final String ID_GSOM_WORKFLOW_PERSPECTIVE = "au.gov.ansto.bragg.quokka.ui.GSOMWorkflowPerspective";
	private static final String ID_GSOM_WORKFLOW_VIEW = "au.gov.ansto.bragg.quokka.ui.GSOMWorkflowView";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView(ID_GSOM_WORKFLOW_VIEW, false, 
				IPageLayout.TOP, 1.0f, layout.getEditorArea());
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

	}

}
