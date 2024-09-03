/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Norman Xiong (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.nbi.ui.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class ControlExperimentPerspective implements IPerspectiveFactory {

	public static final String CONTROL_EXPERIMENT_PERSPECTIVE_ID = "au.gov.ansto.bragg.nbi.ui.ControlExperimentPerspective";
	public static final String CONTROL_TABLE_VIEW_ID = "org.gumtree.control.ui.ControlTableView";
	public static final String CONTROL_BATCH_RUNNER_VIEW_ID = "org.gumtree.control.ui.batchScriptManagerView";
	public static final String CONTROL_TERMINAL_VIEW_ID = "org.gumtree.control.ui.ControlTerminalView";
	public static final String PROJECT_EXPLORER_VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer";
	public static final String ID_VIEW_ACTIVITY_MONITOR = "au.gov.ansto.bragg.nbi.ui.SicsRealtimeDataView";
	
	public void createInitialLayout(final IPageLayout factory) {
		factory.addShowViewShortcut(CONTROL_BATCH_RUNNER_VIEW_ID);
		factory.addShowViewShortcut(CONTROL_TERMINAL_VIEW_ID);
		factory.addShowViewShortcut(PROJECT_EXPLORER_VIEW_ID);
		
		factory.addPerspectiveShortcut(CONTROL_EXPERIMENT_PERSPECTIVE_ID);

//		IFolderLayout bottomRight =
//			factory.createFolder(
//				"bottomRight", //NON-NLS-1
//				IPageLayout.BOTTOM,
//				0.70f,
//				factory.getEditorArea());
//		bottomRight.addView(CONTROL_TERMINAL_VIEW_ID);
		factory.addView(CONTROL_TERMINAL_VIEW_ID, IPageLayout.BOTTOM, 0.70f, factory.getEditorArea());
		
//		IFolderLayout bottomLeft  =
//			factory.createFolder(
//				"bottomLeft", //NON-NLS-1
//				IPageLayout.LEFT,
//				0.33f,
//				"bottomRight");
//		bottomLeft.addView(PROJECT_EXPLORER_VIEW_ID);
		factory.addStandaloneView(PROJECT_EXPLORER_VIEW_ID, false, IPageLayout.LEFT, 0.33f, CONTROL_TERMINAL_VIEW_ID);

//		IFolderLayout right = 
//			factory.createFolder(
//				"right", 
//				IPageLayout.RIGHT, 
//				0.50f, 
//				factory.getEditorArea());
//		right.addView(CONTROL_BATCH_RUNNER_VIEW_ID);
		factory.addStandaloneView(CONTROL_BATCH_RUNNER_VIEW_ID, false, IPageLayout.RIGHT, 0.4f, factory.getEditorArea());

		factory.addStandaloneView(CONTROL_TABLE_VIEW_ID, false, IPageLayout.RIGHT, 0.4f, factory.getEditorArea());

		factory.addStandaloneView(ID_VIEW_ACTIVITY_MONITOR, false, IPageLayout.RIGHT, 0.50f, CONTROL_TERMINAL_VIEW_ID);
		
		factory.setEditorAreaVisible(false);
		factory.getViewLayout(CONTROL_TERMINAL_VIEW_ID).setCloseable(false);
		factory.getViewLayout(CONTROL_TERMINAL_VIEW_ID).setMoveable(false);
//		factory.getViewLayout("bottomLeft").setMoveable(false);
//		factory.getViewLayout("bottomLeft").setCloseable(false);
//		factory.getViewLayout("right").setMoveable(false);
//		factory.getViewLayout("right").setCloseable(false);
		
		factory.setFixed(true);
	}


}
