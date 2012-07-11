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

import org.eclipse.swt.SWT;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOM;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOMFactory;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;

public class OnlineReductionPerspective implements IPerspectiveFactory {

	public static final String EXPERIMENT_PERSPECTIVE_ID = "au.gov.ansto.bragg.kowari.ui.internal.ExperimentPerspective";
	public static final String PLOT_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.PlotView";
	public static final String MASK_PROPERTIES_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView";
	public static final String ALGORITHM_LIST_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.AlgorithmListView";
	public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.instrument.InstrumentDataSourceView";
	public static final String OPERATION_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView";
	public static final String ANALYSIS_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView";
//	public static final String EXPORT_ALL_VIEW_ID = "au.gov.ansto.bragg.kowari.ui.views.ExportAllView";
	public static final String BEANSHELL_VIEW_ID = "org.gumtree.ui.cli.beanShellTerminalview";
	public static final String WORKFLOW_VIEW_ID = "au.gov.ansto.bragg.wombat.ui.views.WorkflowView";
	public static final String FILTERED_STATUS_MONITOR_VIEW_ID = "org.gumtree.dashboard.ui.rcp.FilteredStatusMonitorView";
//	public static final String COMMAND_LINE_VIEW_ID="org.gumtree.scripting.ui.commandLineView";
//	public static final String SICS_TERMINAL_VIEW_ID = "org.gumtree.gumnix.sics.ui.telnetCommunicationAdapter";
	public static final String SICS_BATCH_VIEW_ID = "org.gumtree.gumnix.sics.ui.sicsBatchView";
	public static final String REMOTE_SYSTEM_VIEW_ID = "org.eclipse.rse.ui.view.systemView";
	
	private IFolderLayout top;
	private IPlaceholderFolderLayout rightTop;
	private IPlaceholderFolderLayout rightCenter;
	private IPlaceholderFolderLayout rightBottom;
	private IFolderLayout bottom;
	private static KakaduDOM kakadu = KakaduDOMFactory.getKakaduDOM();
	private static CicadaDOM cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();

	public void createInitialLayout(IPageLayout factory) {
		
		factory.addShowViewShortcut(PLOT_VIEW_ID);
		
		factory.addPerspectiveShortcut(EXPERIMENT_PERSPECTIVE_ID);


		factory.addStandaloneView(PLOT_VIEW_ID + ":1", false, 
				IPageLayout.RIGHT, 0.50f, factory.getEditorArea());
		factory.addStandaloneView(PLOT_VIEW_ID + ":2", false, 
				IPageLayout.BOTTOM, 0.35f, PLOT_VIEW_ID + ":1");
		factory.addStandaloneView(FILTERED_STATUS_MONITOR_VIEW_ID, false, 
				IPageLayout.BOTTOM, 0.54f, PLOT_VIEW_ID + ":2");


//		factory.addStandaloneView(ANALYSIS_PARAMETERS_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.73f, factory.getEditorArea());
		
		factory.addStandaloneView(ANALYSIS_PARAMETERS_VIEW_ID, false, 
				IPageLayout.RIGHT, 0.5f, FILTERED_STATUS_MONITOR_VIEW_ID);
		
		factory.addStandaloneView(SICS_BATCH_VIEW_ID, false, 
				IPageLayout.BOTTOM, 0.7f, factory.getEditorArea());
		
		factory.addStandaloneView(REMOTE_SYSTEM_VIEW_ID, false, 
				IPageLayout.LEFT, 0.35f, factory.getEditorArea());
//		factory.addStandaloneView(EXPORT_ALL_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.94f, factory.getEditorArea());

//		factory.addStandaloneView(WORKFLOW_VIEW_ID, false, 
//				IPageLayout.TOP, 1f, factory.getEditorArea());
		
//		factory.addStandaloneView(COMMAND_LINE_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.72f, WORKFLOW_VIEW_ID);

		IPerspectiveDescriptor descriptor = factory.getDescriptor();
		try {
			PlotManager.resetPlotViewId();
			kakadu.loadAlgorithm(cicada.loadAlgorithm("Online Reduction"), descriptor);
			AlgorithmTask task = ProjectManager.getCurrentAlgorithmTask();
			task.runAlgorithm();
//			ExportAllView.setAlgorithmTask(task);
		} catch (NoneAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		factory.setEditorAreaVisible(false);

		factory.setFixed(true);
}

	public static CicadaDOM getCicadaDOM(){
		if (cicada == null)
			cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
		return cicada;
	}
}
