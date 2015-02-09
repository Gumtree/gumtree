/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


/**
 * @author nxi
 *
 */
public class StandAloneScriptingPerspective implements IPerspectiveFactory {

	public final static String KOWARI_SCRIPTING_PERSPECTIVE_ID = "au.gov.ansto.bragg.kowari.ui.KowariScriptingPerspective";
	
	public final static String STANDALONE_SCRIPTING_VIEW_ID = "au.gov.ansto.bragg.nbi.ui.scripting.StandAloneScriptingView";
	
	@Override
	public void createInitialLayout(IPageLayout factory) {
		
		factory.addPerspectiveShortcut(KOWARI_SCRIPTING_PERSPECTIVE_ID);
		
//		factory.addView(STANDALONE_SCRIPTING_VIEW_ID, IPageLayout.LEFT, 0.6f, factory.getEditorArea());
		factory.addStandaloneView(STANDALONE_SCRIPTING_VIEW_ID, false, IPageLayout.LEFT, 0.6f, factory.getEditorArea());
		
		factory.setEditorAreaVisible(false);
		
//		factory.getViewLayout(STANDALONE_SCRIPTING_VIEW_ID).setCloseable(false);
//		factory.getViewLayout(STANDALONE_SCRIPTING_VIEW_ID).setMoveable(false);
	}

	
}
