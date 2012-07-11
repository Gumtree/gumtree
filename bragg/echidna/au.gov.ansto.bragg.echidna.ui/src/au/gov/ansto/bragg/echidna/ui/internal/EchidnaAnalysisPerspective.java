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
package au.gov.ansto.bragg.echidna.ui.internal;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;

import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOM;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOMFactory;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;

public class EchidnaAnalysisPerspective implements IPerspectiveFactory {

	public static final String ANALYSIS_PERSPECTIVE_ID = "au.gov.ansto.bragg.echidna.ui.internal.EchidnaAnalysisPerspective";
	public static final String PLOT_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.PlotView";
	public static final String MASK_PROPERTIES_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView";
	public static final String ALGORITHM_LIST_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.AlgorithmListView";
	public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.echidna.ui.views.EchidnaDataSourceView";
//	public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.instrument.InstrumentDataSourceView";
	public static final String OPERATION_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView";
	public static final String ANALYSIS_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.echidna.ui.views.EchidnaAnalysisControlView";
//	public static final String BEANSHELL_VIEW_ID = "org.gumtree.ui.cli.beanShellTerminalview";
//	public static final String COMMAND_LINE_VIEW_ID="org.gumtree.scripting.ui.commandLineView";
	public static final String REDUCTION_ALGORITHM_NAME = "Data Reduction V 1.7";
	
	private static KakaduDOM kakadu = KakaduDOMFactory.getKakaduDOM();
	private static CicadaDOM cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();

	public void createInitialLayout(IPageLayout factory) {
//		factory.addShowViewShortcut(DATA_SOURCE_VIEW_ID);
//		factory.addShowViewShortcut(ALGORITHM_LIST_VIEW_ID);
//		factory.addShowViewShortcut(MASK_PROPERTIES_VIEW_ID);
		IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
		if (introPart != null)
			PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
//		IExtendedWorkbenchWindow windowService = (IExtendedWorkbenchWindow) PlatformUI.getWorkbench()
//		.getActiveWorkbenchWindow().getService(IExtendedWorkbenchWindow.class);
//		windowService.hideSideBar();
		
		factory.addShowViewShortcut(PLOT_VIEW_ID);
		
		factory.addPerspectiveShortcut(ANALYSIS_PERSPECTIVE_ID);

//		bottom =
//			factory.createFolder(
//				"bottomRight", //NON-NLS-1
//				IPageLayout.BOTTOM,
//				0.80f,
//				factory.getEditorArea());
//		bottom.addPlaceholder("au.gov.ansto.bragg.kakadu.ui.views.TunerPropertiesView");
		
//		top =
//			factory.createFolder(
//				"top", //NON-NLS-1
//				IPageLayout.TOP,
//				0.10f,
//				factory.getEditorArea());
//		top.addView("au.gov.ansto.bragg.cicada.vi.eclipse.views.TopBar");
//		factory.getViewLayout("top").setMoveable(false);
//		bottom = factory.createFolder(
//				"bottom", 
//				IPageLayout.BOTTOM, 
//				0.75f, 
//				factory.getEditorArea());
//		bottom.addView(COMMAND_LINE_VIEW_ID);
//		left = factory.createFolder(
//				"right", //NON-NLS-1
//				IPageLayout.RIGHT,
//				0.50f,
//				factory.getEditorArea());
//		left.addView(DATA_SOURCE_VIEW_ID);
//		
//		left2 = factory.createFolder(
//				"right2", //NON-NLS-1
//				IPageLayout.BOTTOM,
//				0.50f,
//				"right");
//		left2.addView(ALGORITHM_LIST_VIEW_ID);
		
//		left3 =
//			factory.createFolder(
//				"left3", //NON-NLS-1
//				IPageLayout.BOTTOM,
//				0.50f,
//				"left2");
//		left3.addPlaceholder("au.gov.ansto.bragg.kakadu.ui.views.TunerPropertiesView");

//		right = factory.createFolder(
//		"right", //NON-NLS-1
//		IPageLayout.RIGHT,
//		0.1f,
//		factory.getEditorArea());
//right.addPlaceholder("au.gov.ansto.bragg.kakadu.ui.views.RegionView");

//		rightTop = factory.createFolder(
//		"rightTop",
//		IPageLayout.RIGHT,
//		0.20f,
//		factory.getEditorArea());
//		rightTop.addPlaceholder(PLOT_VIEW_ID + ":1");

//
//		rightCenter = factory.createFolder(
//				"rightCenter",
//				IPageLayout.BOTTOM,
//				0.33f,
//				"rightTop");
//		rightCenter.addPlaceholder(PLOT_VIEW_ID + ":2");
//
//		rightBottom = factory.createFolder(
//				"rightBottom",
//				IPageLayout.BOTTOM,
//				0.50f,
//				"rightCenter");
//		rightBottom.addPlaceholder(PLOT_VIEW_ID + ":3");
		factory.addStandaloneView(PLOT_VIEW_ID + ":1", false, 
				IPageLayout.RIGHT, 0.22f, factory.getEditorArea());
		factory.addStandaloneView(PLOT_VIEW_ID + ":2", false, 
				IPageLayout.BOTTOM, 0.33f, PLOT_VIEW_ID + ":1");
		factory.addStandaloneView(PLOT_VIEW_ID + ":3", false, 
				IPageLayout.BOTTOM, 0.50f, PLOT_VIEW_ID + ":2");

//		factory.addStandaloneViewPlaceholder(
//				PLOT_VIEW_ID, IPageLayout.BOTTOM, 0.50f, "rightCenter", false);
//		bottom = factory.createFolder(
//				"bottom",
//				IPageLayout.BOTTOM,
//				0.67f,
//				factory.getEditorArea());
//		bottom.addView(BEANSHELL_VIEW_ID);
		factory.addStandaloneView(ANALYSIS_PARAMETERS_VIEW_ID, false, 
				IPageLayout.BOTTOM, 0.32f, factory.getEditorArea());
		
		factory.addStandaloneView(DATA_SOURCE_VIEW_ID, true, 
				IPageLayout.TOP, 1.0f, factory.getEditorArea());

//		factory.addStandaloneView(EXPORT_ALL_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.94f, factory.getEditorArea());
//		top = factory.createFolder(
//				"top", //NON-NLS-1
//				IPageLayout.TOP,
//				1.0f,
//				factory.getEditorArea());
//		top.addView(OPERATION_PARAMETERS_VIEW_ID);
		
//		factory.addPlaceholder(MASK_PROPERTIES_VIEW_ID, IPageLayout.RIGHT, 0.8f, factory.getEditorArea());
//		factory.addPlaceholder(OPERATION_PARAMETERS_VIEW_ID, IPageLayout.RIGHT, 0.8f, factory.getEditorArea());
//		factory.addPlaceholder(PLOT_VIEW_ID
//				+ ":*"
//				,IPageLayout.BOTTOM, 0.5f,	factory.getEditorArea());
		
//		for (int i = 0; i < 4; i++) {
//			//locate first 4 plots independently under MASK_PROPERTIES_VIEW. Others will be added to the same folder
//		factory.addPlaceholder(
//				PLOT_VIEW_ID + ":" + i,
//				IPageLayout.BOTTOM, 0.5f,
//				MASK_PROPERTIES_VIEW_ID);
//		}
//		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IPerspectiveDescriptor descriptor = factory.getDescriptor();
		try {
			PlotManager.resetPlotViewId();
			kakadu.loadAlgorithm(cicada.loadAlgorithm(REDUCTION_ALGORITHM_NAME), descriptor);
//			Plot plot3 = PlotManager.openPlot(PlotType.OverlayPlot, 3);
//			plot3.setDataViewSelection(true);
		} catch (Exception e) {
			Util.handleException(Display.getCurrent().getShells()[0], e);
			return;
		}
		factory.setEditorAreaVisible(false);
		factory.getViewLayout(DATA_SOURCE_VIEW_ID).setMoveable(false);
		factory.getViewLayout(DATA_SOURCE_VIEW_ID).setCloseable(false);
//		OperationParametersView.subscribeStopButtonListener(new ButtonClickListener(){
//
//			@Override
//			public void onClick() {
//				try {
//					SicsCore.getSicsController().interrupt();
//				} catch (SicsIOException e) {
//					LoggerFactory.getLogger(KowariAnalysisPerspective.class);
//				}
//			}
//			
//		});
		factory.setFixed(true);
}

	public static CicadaDOM getCicadaDOM(){
		if (cicada == null)
			cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
		return cicada;
	}
	
}
