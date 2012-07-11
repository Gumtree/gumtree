package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class AlignmentPerspective implements IPerspectiveFactory {

	private static final String ID_VIEW_SCRIPT_FOLDER = "au.gov.ansto.bragg.experiment.ui.view.PShelfView";
	
	private static final String ID_VIEW_CONSOLE = "org.gumtree.ui.cli.beanShellTerminalview";
	
	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
        defineLayout(layout);
	}

	private void defineActions(IPageLayout layout) {
	}
	
	private void defineLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();

        // Right
        IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, (float) 0.65, editorArea);
        right.addView(ID_VIEW_CONSOLE);

        // Left
        IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, (float) 0.15, editorArea);
        left.addView(ID_VIEW_SCRIPT_FOLDER);
        
	}
	
}
