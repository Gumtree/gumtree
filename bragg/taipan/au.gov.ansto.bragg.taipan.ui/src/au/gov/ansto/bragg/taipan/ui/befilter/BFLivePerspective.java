/**
 * 
 */
package au.gov.ansto.bragg.taipan.ui.befilter;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author nxi
 *
 */
public class BFLivePerspective implements IPerspectiveFactory {

	public static final String ID_PERSPECTIVE_BFLIVE= "au.gov.ansto.bragg.nbi.taipan.BFLivePerspective";
	private static final String ID_VIEW_BFLIVE = "au.gov.ansto.bragg.nbi.taipan.BFLiveView";

	/**
	 * 
	 */
	public BFLivePerspective() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView(ID_VIEW_BFLIVE, false, 
				IPageLayout.LEFT, 1f, layout.getEditorArea());
//		layout.addPlaceholder(ID_VIEW_PLOT3, IPageLayout.TOP, 0.66f, layout.getEditorArea());
//		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		
	}

}
