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
package au.gov.ansto.bragg.wombat.ui.internal;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;

public class TCLRunnerPerspective implements IPerspectiveFactory {

	public static final String EXPERIMENT_PERSPECTIVE_ID = "au.gov.ansto.bragg.wombat.ui.internal.TCLRunnerPerspective";
//	public static final String PLOT_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.PlotView";
//	public static final String MASK_PROPERTIES_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView";
//	public static final String ALGORITHM_LIST_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.AlgorithmListView";
//	public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.instrument.InstrumentDataSourceView";
//	public static final String OPERATION_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView";
//	public static final String ANALYSIS_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.wombat.ui.views.WombatAnalysisControlView";
//	public static final String EXPORT_ALL_VIEW_ID = "au.gov.ansto.bragg.kowari.ui.views.ExportAllView";
//	public static final String BEANSHELL_VIEW_ID = "org.gumtree.ui.cli.beanShellTerminalview";
//	public static final String WORKFLOW_VIEW_ID = "au.gov.ansto.bragg.wombat.ui.views.WombatBatchEditingView";
	public static final String WORKFLOW_VIEW_ID = "au.gov.ansto.bragg.wombat.ui.views.WombatTclEditorView";
//	public static final String FILTERED_STATUS_MONITOR_VIEW_ID = "org.gumtree.dashboard.ui.rcp.FilteredStatusMonitorView";
	public static final String COMMAND_LINE_VIEW_ID="org.gumtree.scripting.ui.commandLineView";
	public static final String SICS_TERMINAL_VIEW_ID = "au.gov.ansto.bragg.nbi.ui.SicsTerminalView";
//	public static final String SICS_TELNET_ADAPTOR_ID = "org.gumtree.gumnix.sics.ui.serverCommunicationAdapter";
	public static final String PROJECT_EXPLORER_VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer";
//	public static final String CONTROL_VIEW_ID = "au.gov.ansto.bragg.echidna.ui.views.EchidnaControlView";
	public static final String FILTERED_STATUS_MONITOR_VIEW_ID = "org.gumtree.dashboard.ui.rcp.FilteredStatusMonitorView";
	public static final String SICS_BUFFER_MANAGER_VIEW_ID = "org.gumtree.gumnix.sics.batch.ui.batchBufferManagerView";
//	public static final String ID_VIEW_SPY_VIEW = "org.gumtree.dashboard.ui.rcp.spyView";
	public static final String SICS_BUFFER_RUNNER_VIEW_ID = "org.gumtree.gumnix.sics.batch.ui.batchBufferRunnerView";
	public static final String SICS_BUFFER_VALIDATOR_VIEW_ID = "org.gumtree.gumnix.sics.batch.ui.batchBufferValidatorView";
	public static final String EXPERIMENT_PERSPECTIVE_THEME = "au.gov.ansto.bragg.wombat.ui.theme.exp";
	public static final String DEFAULT_PERSPECTIVE_THEME = "au.gov.ansto.bragg.wombat.ui.theme.default";
	public static final String ID_VIEW_ACTIVITY_MONITOR = "au.gov.ansto.bragg.nbi.ui.SicsRealtimeDataView";

	private static Logger logger;

	
//	private IFolderLayout top;
//	private IPlaceholderFolderLayout rightTop;
//	private IPlaceholderFolderLayout rightCenter;
//	private IPlaceholderFolderLayout rightBottom;
//	private IFolderLayout bottom;
//	private static KakaduDOM kakadu = KakaduDOMFactory.getKakaduDOM();
//	private static CicadaDOM cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();

	public void createInitialLayout(final IPageLayout factory) {
		
//		factory.addShowViewShortcut(PLOT_VIEW_ID);
		
		factory.addPerspectiveShortcut(EXPERIMENT_PERSPECTIVE_ID);

		IFolderLayout top2 = factory.createFolder(
		"top2", //NON-NLS-1
		IPageLayout.TOP,
		0.70f,
		factory.getEditorArea());
		top2.addView(WORKFLOW_VIEW_ID);
//		top2.addView(SICS_BUFFER_MANAGER_VIEW_ID);
		top2.addView(SICS_BUFFER_VALIDATOR_VIEW_ID);
//		top2.addView(SICS_BUFFER_RUNNER_VIEW_ID);
		
//		factory.addStandaloneView(WORKFLOW_VIEW_ID, false, 
//				IPageLayout.LEFT, 0.75f, factory.getEditorArea());

//		factory.addStandaloneView(SICS_TERMINAL_VIEW_ID, false, 
//				IPageLayout.RIGHT, 0.25f, factory.getEditorArea());
//		
		factory.addStandaloneView(SICS_BUFFER_RUNNER_VIEW_ID, true, 
				IPageLayout.RIGHT, 0.60f, "top2");
		
		factory.addStandaloneView(ID_VIEW_ACTIVITY_MONITOR, false, 
				IPageLayout.BOTTOM, 0.70f, factory.getEditorArea());
		
		factory.addStandaloneView(SICS_TERMINAL_VIEW_ID, false, 
				IPageLayout.LEFT, 0.60f, ID_VIEW_ACTIVITY_MONITOR);

		factory.addStandaloneView(PROJECT_EXPLORER_VIEW_ID, false, 
				IPageLayout.LEFT, 0.5f, SICS_TERMINAL_VIEW_ID);

//		factory.addStandaloneView(FILTERED_STATUS_MONITOR_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.57f, PROJECT_EXPLORER_VIEW_ID);

//		factory.addStandaloneView(ID_VIEW_SPY_VIEW, false, 
//				IPageLayout.BOTTOM, 0.1f, factory.getEditorArea());

//		IFolderLayout bottomCenter = factory.createFolder("bottomCenter", 
//				IPageLayout.LEFT, 0.382f, PLOT_VIEW_ID + ":3");
//		bottomCenter.addView(ANALYSIS_PARAMETERS_VIEW_ID);
		
//		factory.addStandaloneView(FILTERED_STATUS_MONITOR_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.493f, PLOT_VIEW_ID + ":2");
//

		
		
//		factory.addStandaloneView(EXPORT_ALL_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.94f, factory.getEditorArea());

//		factory.addView(WORKFLOW_VIEW_ID, IPageLayout.TOP, 1f, factory.getEditorArea());
		
//		factory.addStandaloneView(FILTERED_STATUS_MONITOR_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.663f, WORKFLOW_VIEW_ID);

//		IFolderLayout bottomLeft = factory.createFolder(
//				"bottomLeft", //NON-NLS-1
//				IPageLayout.BOTTOM,
//				0.663f,
//				WORKFLOW_VIEW_ID);
//		bottomLeft.addView(FILTERED_STATUS_MONITOR_VIEW_ID);
		
//		IFolderLayout bottomLeft2 = factory.createFolder(
//				"bottomLeft2", //NON-NLS-1
//				IPageLayout.RIGHT,
//				0.53f,
//				"bottomLeft");
//		bottomLeft2.addView(SICS_TERMINAL_VIEW_ID);
				
				
//		IPerspectiveDescriptor descriptor = factory.getDescriptor();
//		try {
//			PlotManager.resetPlotViewId();
//			kakadu.loadAlgorithm(cicada.loadAlgorithm("Online Reduction"), descriptor);
//			AlgorithmTask task = ProjectManager.getCurrentAlgorithmTask();
//			task.runAlgorithm();
////			ExportAllView.setAlgorithmTask(task);
//		} catch (NoneAlgorithmException e) {
//			e.printStackTrace();
//		}
//		factory.setEditorAreaVisible(false);
		factory.getViewLayout(WORKFLOW_VIEW_ID).setCloseable(false);
		factory.getViewLayout(WORKFLOW_VIEW_ID).setMoveable(false);
		factory.getViewLayout(SICS_BUFFER_VALIDATOR_VIEW_ID).setCloseable(false);
		factory.getViewLayout(SICS_BUFFER_VALIDATOR_VIEW_ID).setMoveable(false);
		factory.getViewLayout(SICS_BUFFER_RUNNER_VIEW_ID).setCloseable(false);
		factory.getViewLayout(SICS_BUFFER_RUNNER_VIEW_ID).setMoveable(false);
		factory.getViewLayout(PROJECT_EXPLORER_VIEW_ID).setCloseable(false);
		factory.getViewLayout(PROJECT_EXPLORER_VIEW_ID).setMoveable(false);
		factory.setEditorAreaVisible(false);
//		factory.getViewLayout(FILTERED_STATUS_MONITOR_VIEW_ID).setCloseable(false);
//		factory.getViewLayout(ANALYSIS_PARAMETERS_VIEW_ID).setCloseable(false);
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new IPerspectiveListener() {
			
			@Override
			public void perspectiveChanged(IWorkbenchPage page,
					IPerspectiveDescriptor perspective, String changeId) {
				System.out.println("perspective changed");
			}
			
			@Override
			public void perspectiveActivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				if (perspective.getId().equals(EXPERIMENT_PERSPECTIVE_ID)) {
					PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(
							EXPERIMENT_PERSPECTIVE_THEME);
					page.setEditorAreaVisible(false);
				} else {
					PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(
							DEFAULT_PERSPECTIVE_THEME);
				}
				
			}
		});
//		factory.setFixed(true);
	}

//	/* (non-Javadoc)
//	 * @see java.lang.Object#finalize()
//	 */
//	@Override
//	protected void finalize() throws Throwable {
//		super.finalize();
//		IViewReference viewReferences[] = PlatformUI.getWorkbench()
//		.getActiveWorkbenchWindow().getActivePage().getViewReferences();
//		IViewPart view = null;
//		for (int i = 0; i < viewReferences.length; i++){
//			System.out.println(viewReferences[i].getId());
//			if (viewReferences[i].getId().equals(SICS_TERMINAL_VIEW_ID)){
//				view = viewReferences[i].getView(false);
//			}
//		}
//		if (view != null && view instanceof ICommandLineTerminal){
//			CommandLineTerminal.addViewActivationCount();
//			System.out.println("view available");
//			try {
//				ICommandLineTerminal terminal = (ICommandLineTerminal) view;
//				terminal.selectCommunicationAdapter(SICS_TELNET_ADAPTOR_ID);
//				terminal.connect();
//			} catch (Exception e) {
//				getLogger().error("can not open sics terminal", e);
//			}
//		}
//	}

//	public static CicadaDOM getCicadaDOM(){
//		if (cicada == null)
//			cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
//		return cicada;
//	}

}
