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
package au.gov.ansto.bragg.pelican.ui.internal;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class TCLRunnerPerspective implements IPerspectiveFactory {

	public static final String EXPERIMENT_PERSPECTIVE_ID = "au.gov.ansto.bragg.pelican.ui.internal.TCLRunnerPerspective";
//	public static final String EXPERIMENT_PERSPECTIVE_THEME = "au.gov.ansto.bragg.pelican.ui.theme";
//	public static final String DEFAULT_PERSPECTIVE_THEME = "au.gov.ansto.bragg.nbi.ui.EmptyPerspective";
	public static final String WORKFLOW_VIEW_ID = "au.gov.ansto.bragg.pelican.ui.views.TclEditorView";
	public static final String COMMAND_LINE_VIEW_ID="org.gumtree.scripting.ui.commandLineView";
	public static final String SICS_TERMINAL_VIEW_ID = "org.gumtree.gumnix.sics.ui.SicsTerminalView";
	public static final String PROJECT_EXPLORER_VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer";
//	public static final String CONTROL_VIEW_ID = "au.gov.ansto.bragg.pelican.ui.views.KowariControlView";
	public static final String SICS_BUFFER_RUNNER_VIEW_ID = "org.gumtree.gumnix.sics.batch.ui.batchBufferRunnerView";
	public static final String SICS_BUFFER_VALIDATOR_VIEW_ID = "org.gumtree.gumnix.sics.batch.ui.batchBufferValidatorView";

	public static final String ID_VIEW_ACTIVITY_MONITOR = "au.gov.ansto.bragg.nbi.ui.SicsRealtimeDataView";

	
	public void createInitialLayout(IPageLayout factory) {
		
		
		factory.addPerspectiveShortcut(EXPERIMENT_PERSPECTIVE_ID);

		IFolderLayout top2 = factory.createFolder(
				"top_folder", //NON-NLS-1
				IPageLayout.TOP,
				0.73f,
				factory.getEditorArea());
		top2.addView(WORKFLOW_VIEW_ID);
		top2.addView(SICS_BUFFER_VALIDATOR_VIEW_ID);
//		top2.addView(SICS_BUFFER_RUNNER_VIEW_ID);

		factory.addStandaloneView(SICS_BUFFER_RUNNER_VIEW_ID, false, 
				IPageLayout.RIGHT, 0.6f, "top_folder");
		
		factory.addStandaloneView(PROJECT_EXPLORER_VIEW_ID, false, 
				IPageLayout.LEFT, 0.2f, "top_folder");

		factory.addStandaloneView(SICS_TERMINAL_VIEW_ID, false, 
				IPageLayout.RIGHT, 0.23f, factory.getEditorArea());

		factory.addStandaloneView(ID_VIEW_ACTIVITY_MONITOR, false, 
		IPageLayout.RIGHT, 0.50f, SICS_TERMINAL_VIEW_ID);

		factory.getViewLayout(WORKFLOW_VIEW_ID).setCloseable(false);
		factory.getViewLayout(WORKFLOW_VIEW_ID).setMoveable(false);
		factory.getViewLayout(SICS_BUFFER_RUNNER_VIEW_ID).setCloseable(false);
		factory.getViewLayout(SICS_BUFFER_RUNNER_VIEW_ID).setMoveable(false);
		factory.getViewLayout(SICS_BUFFER_VALIDATOR_VIEW_ID).setCloseable(false);
		factory.getViewLayout(SICS_BUFFER_VALIDATOR_VIEW_ID).setMoveable(false);
//		factory.getViewLayout(PROJECT_EXPLORER_VIEW_ID).setMoveable(false);
		factory.setEditorAreaVisible(false);
		
//		factory.setFixed(true);
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new IPerspectiveListener() {
//			
//			@Override
//			public void perspectiveChanged(IWorkbenchPage page,
//					IPerspectiveDescriptor perspective, String changeId) {
//				System.out.println("perspective changed");
//			}
//			
//			@Override
//			public void perspectiveActivated(IWorkbenchPage page,
//					IPerspectiveDescriptor perspective) {
//				if (perspective.getId().equals(EXPERIMENT_PERSPECTIVE_ID)) {
//					PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(
//							EXPERIMENT_PERSPECTIVE_THEME);
//				} else {
//					PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(
//							DEFAULT_PERSPECTIVE_THEME);
//				}
//				
//			}
//		});
		
		
	}


}
