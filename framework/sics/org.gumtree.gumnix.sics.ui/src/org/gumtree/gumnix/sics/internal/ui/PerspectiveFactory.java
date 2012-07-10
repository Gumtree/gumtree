package org.gumtree.gumnix.sics.internal.ui;

//import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PerspectiveFactory implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
		defineLayout(layout);
	}

	private void defineActions(IPageLayout layout) {
	}

	private void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		// Top left.
		IFolderLayout topLeft = layout.createFolder(
				"topLeft", IPageLayout.LEFT, (float) 0.20, editorArea);//$NON-NLS-1$
//		topLeft.addView(ISEEConstants.ID_VIEW_WORKBENCH_EXPLORER);
//		layout.getViewLayout(ISEEConstants.ID_VIEW_WORKBENCH_EXPLORER)
//		.setCloseable(false);
//		layout.getViewLayout(ISEEConstants.ID_VIEW_WORKBENCH_EXPLORER)
//		.setMoveable(false);
		topLeft.addView(WorkbenchUIConstants.ID_VIEW_PROJECT_EXPLORER);
//		topLeft.addView(WorkbenchUIConstants.ID_VIEW_REMOTE_SYSTEM);
		layout.getViewLayout(WorkbenchUIConstants.ID_VIEW_PROJECT_EXPLORER).setCloseable(false);
//		layout.getViewLayout(WorkbenchUIConstants.ID_VIEW_REMOTE_SYSTEM).setCloseable(false);
		
		// Bottom left. disabled by nxi on 28/03/2008
		/*
		IFolderLayout bottomLeft = layout.createFolder(
				"bottomLeft", IPageLayout.BOTTOM, (float) 0.50,//$NON-NLS-1$
				"topLeft");//$NON-NLS-1$
		bottomLeft.addView(ISEEConstants.ID_VIEW_PROJECT_EXPLORER);
		layout.getViewLayout(ISEEConstants.ID_VIEW_PROJECT_EXPLORER)
				.setCloseable(false);
		layout.getViewLayout(ISEEConstants.ID_VIEW_PROJECT_EXPLORER)
				.setMoveable(false);
		 */
//		bottomLeft.addView(WorkflowUIConstants.ID_VIEW_WORKFLOW);
//		layout.getViewLayout(WorkflowUIConstants.ID_VIEW_WORKFLOW)
//		.setCloseable(false);
//		layout.getViewLayout(WorkflowUIConstants.ID_VIEW_WORKFLOW).setMoveable(
//		false);

		// Top
		// Context sensitive menu + status + control panel
		// [Tony] [2008-06-28] This will be removed from the perspective soon
		layout.addStandaloneView("org.gumtree.gumnix.sics.ui.dashboardView", false,
				IPageLayout.TOP, (float) 0.10, editorArea);

		// Bottom.
		IFolderLayout bottom = layout.createFolder(
				"bottom", IPageLayout.BOTTOM, (float) 0.50,//$NON-NLS-1$
				editorArea);

		bottom.addView(IPageLayout.ID_PROP_SHEET);
//		bottom.addView("org.eclipse.pde.runtime.LogView");

	}

}
