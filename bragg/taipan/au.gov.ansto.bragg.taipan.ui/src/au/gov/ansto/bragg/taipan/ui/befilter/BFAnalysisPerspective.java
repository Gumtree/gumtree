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
public class BFAnalysisPerspective implements IPerspectiveFactory {

	public static final String ID_PERSPECTIVE_BFANALYSIS = "au.gov.ansto.bragg.nbi.taipan.BFAnalysisPerspective";
	private static final String ID_VIEW_BFANALYSIS = "au.gov.ansto.bragg.nbi.taipan.BFAnalysisView";

	/**
	 * 
	 */
	public BFAnalysisPerspective() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView(ID_VIEW_BFANALYSIS, false, 
				IPageLayout.LEFT, 0.65f, layout.getEditorArea());
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
		
	}

}
