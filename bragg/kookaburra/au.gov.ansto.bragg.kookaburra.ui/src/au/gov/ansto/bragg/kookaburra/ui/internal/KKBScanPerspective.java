/**
 * 
 */
package au.gov.ansto.bragg.kookaburra.ui.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


/**
 * @author nxi
 *
 */
public class KKBScanPerspective implements IPerspectiveFactory {

	public final static String KOOKABURRA_SCAN_PERSPECTIVE_ID = "au.gov.ansto.bragg.kookaburra.ui.KKBScanPerspective";
	
	public final static String STANDALONE_SCRIPTING_VIEW_ID = "au.gov.ansto.bragg.kookaburra.ui.KKBScanView";
	
	@Override
	public void createInitialLayout(IPageLayout factory) {
		
		factory.addPerspectiveShortcut(KOOKABURRA_SCAN_PERSPECTIVE_ID);
		
//		factory.addView(STANDALONE_SCRIPTING_VIEW_ID, IPageLayout.LEFT, 0.6f, factory.getEditorArea());
		factory.addStandaloneView(STANDALONE_SCRIPTING_VIEW_ID, false, IPageLayout.LEFT, 0.6f, factory.getEditorArea());
		
		factory.setEditorAreaVisible(false);
		
//		factory.getViewLayout(STANDALONE_SCRIPTING_VIEW_ID).setCloseable(false);
//		factory.getViewLayout(STANDALONE_SCRIPTING_VIEW_ID).setMoveable(false);
	}

	
}
