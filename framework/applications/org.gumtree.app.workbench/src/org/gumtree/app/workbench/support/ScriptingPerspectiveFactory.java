package org.gumtree.app.workbench.support;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.scripting.ScriptingUI;
import org.gumtree.ui.util.SafeUIRunner;

public class ScriptingPerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
        defineLayout(layout);
	}
	
	private void defineActions(IPageLayout layout) {
//		layout.addShowViewShortcut(ISEEConstants.ID_VIEW_WORKBENCH_EXPLORER);
//		layout.addShowViewShortcut(ISEEConstants.ID_VIEW_ACTION_SHORTCUT);
		layout.addShowViewShortcut(WorkbenchUIConstants.ID_VIEW_PROJECT_EXPLORER);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
//        layout.addFastView(ISEEConstants.ID_VIEW_ACTION_SHORTCUT);
	}

	private void defineLayout(IPageLayout layout) {
		// Editors are placed for free.
        String editorArea = layout.getEditorArea();

        // Bottom.
		IFolderLayout bottom = layout.createFolder(
                "bottom", IPageLayout.BOTTOM, (float) 0.65,//$NON-NLS-1$
                editorArea);

		bottom.addView(ScriptingUI.ID_VIEW_COMMAND_LINE);
		bottom.addView(IPageLayout.ID_PROP_SHEET);

        // Left.
        IFolderLayout left = layout.createFolder(
                "left", IPageLayout.LEFT, (float) 0.26, editorArea);//$NON-NLS-1$
//        left.addView(ISEEConstants.ID_VIEW_WORKBENCH_EXPLORER);
        left.addView(WorkbenchUIConstants.ID_VIEW_PROJECT_EXPLORER);
//        left.addView("org.gumtree.app.workbench.sidebar");
//        left.addView(WorkbenchUIConstants.ID_VIEW_REMOTE_SYSTEM);
//        left.addView(ISEEConstants.ID_VIEW_WORKBENCH_EXPLORER);
//        left.addPlaceholder(ISEEConstants.ID_VIEW_ACTION_SHORTCUT);
        
        // Top right.
//        IFolderLayout topRight = layout.createFolder(
//                "topRight", IPageLayout.RIGHT, (float) 0.74,//$NON-NLS-1$
//                editorArea);//$NON-NLS-1$
//        topRight.addView(IPageLayout.ID_OUTLINE);

//        layout.getViewLayout(ISEEConstants.ID_VIEW_ACTION_SHORTCUT).setCloseable(false);
//        layout.getViewLayout(ISEEConstants.ID_VIEW_ACTION_SHORTCUT).setMoveable(false);
        
        layout.getViewLayout(WorkbenchUIConstants.ID_VIEW_PROJECT_EXPLORER).setCloseable(false);
//        layout.getViewLayout("org.eclipse.rse.ui.view.systemView").setCloseable(false);
        
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
//				.addPerspectiveListener(new PerspectiveAdapter() {
//					public void perspectiveActivated(final IWorkbenchPage page,
//							IPerspectiveDescriptor perspective) {
//						SafeUIRunner.asyncExec(new SafeRunnable() {
//							@Override
//							public void run() throws Exception {
//								page.showView("org.gumtree.app.workbench.cruisePanel", null, IWorkbenchPage.VIEW_ACTIVATE);
//							}
//						});
//						
////						page.setPartState(page.findViewReference("org.gumtree.app.workbench.sidebar"),
////				        	    IWorkbenchPage.STATE_MAXIMIZED);
//					}
//				});
	}
	

}
