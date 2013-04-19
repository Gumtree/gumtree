package au.gov.ansto.bragg.kookaburra.ui.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import au.gov.ansto.bragg.kookaburra.ui.KookaburraUIConstants;

public class KookaburraScanPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
        defineLayout(layout);
	}

	private void defineActions(IPageLayout layout) {
	}
	
	private void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.addStandaloneView(KookaburraUIConstants.ID_VIEW_KOOKABURRA_SCAN, false, IPageLayout.LEFT, 1.0f, editorArea);
		layout.getViewLayout(KookaburraUIConstants.ID_VIEW_KOOKABURRA_SCAN).setCloseable(false);
		layout.getViewLayout(KookaburraUIConstants.ID_VIEW_KOOKABURRA_SCAN).setMoveable(false);
	}

}
