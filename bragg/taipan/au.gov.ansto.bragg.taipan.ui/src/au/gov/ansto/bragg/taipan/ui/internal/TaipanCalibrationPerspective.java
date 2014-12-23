/**
 * 
 */
package au.gov.ansto.bragg.taipan.ui.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author nxi
 *
 */
public class TaipanCalibrationPerspective implements IPerspectiveFactory {

	private static final String ID_TAIPAN_CALIBRATION_VIEW = "au.gov.ansto.bragg.taipan.ui.TaipanCalibrationView";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addStandaloneView(ID_TAIPAN_CALIBRATION_VIEW, false, 
				IPageLayout.TOP, 1.0f, layout.getEditorArea());
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

	}

}
