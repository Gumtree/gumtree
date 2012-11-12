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
package au.gov.ansto.bragg.kowari.ui.internal;

import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.gumtree.ui.util.SafeUIRunner;

import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOM;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOMFactory;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kowari.ui.views.ExportAllView;

public class KowariAnalysisPerspective implements IPerspectiveFactory {

	public static final String ANALYSIS_PERSPECTIVE_ID = "au.gov.ansto.bragg.kowari.ui.internal.KowariAnalysisPerspective";
	public static final String PLOT_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.PlotView";
	public static final String MASK_PROPERTIES_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView";
	public static final String ALGORITHM_LIST_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.AlgorithmListView";
	public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.kowari.ui.views.KowariDataSourceView";
	public static final String OPERATION_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView";
	public static final String ANALYSIS_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.kowari.ui.views.AnalysisControlView";
	public static final String EXPORT_ALL_VIEW_ID = "au.gov.ansto.bragg.kowari.ui.views.ExportAllView";
	public static final String BEANSHELL_VIEW_ID = "org.gumtree.ui.cli.beanShellTerminalview";
//	public static final String COMMAND_LINE_VIEW_ID="org.gumtree.scripting.ui.commandLineView";
	
//	private IFolderLayout top;
//	private IPlaceholderFolderLayout rightTop;
//	private IPlaceholderFolderLayout rightCenter;
//	private IPlaceholderFolderLayout rightBottom;
//	private IFolderLayout bottom;
	private static KakaduDOM kakadu = KakaduDOMFactory.getKakaduDOM();
	private static CicadaDOM cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();

	public void createInitialLayout(final IPageLayout factory) {
//		factory.addShowViewShortcut(DATA_SOURCE_VIEW_ID);
//		factory.addShowViewShortcut(ALGORITHM_LIST_VIEW_ID);
//		factory.addShowViewShortcut(MASK_PROPERTIES_VIEW_ID);
		
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
				IPageLayout.RIGHT, 0.20f, factory.getEditorArea());
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
//		factory.addStandaloneView(ANALYSIS_PARAMETERS_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.55f, factory.getEditorArea());
		
		factory.addStandaloneViewPlaceholder(
				ANALYSIS_PARAMETERS_VIEW_ID, IPageLayout.BOTTOM, 0.64f, factory.getEditorArea(), false);
				
//		factory.addStandaloneView(EXPORT_ALL_VIEW_ID, false, 
//				IPageLayout.BOTTOM, 0.94f, factory.getEditorArea());
		
		factory.addStandaloneViewPlaceholder(
				EXPORT_ALL_VIEW_ID, IPageLayout.BOTTOM, 0.94f, factory.getEditorArea(), false);
		
//		top = factory.createFolder(
//				"top", //NON-NLS-1
//				IPageLayout.TOP,
//				1.0f,
//				factory.getEditorArea());
//		top.addView(OPERATION_PARAMETERS_VIEW_ID);
		factory.addStandaloneView(DATA_SOURCE_VIEW_ID, true, 
				IPageLayout.TOP, 1.0f, factory.getEditorArea());
		
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
		final IPerspectiveDescriptor descriptor = factory.getDescriptor();
		SafeUIRunner.asyncExec(new SafeRunnable() {
			public void run() throws Exception {
				try {
					PlotManager.resetPlotViewId();
					kakadu.loadAlgorithm(cicada.loadAlgorithm("Reduction Algorithms 4.0"), descriptor);
					AlgorithmTask task = ProjectManager.getCurrentAlgorithmTask();
					IWorkbench workbench;
					IWorkbenchWindow workbenchWindow;
					workbench = PlatformUI.getWorkbench();
					workbenchWindow = workbench.getActiveWorkbenchWindow();
					IWorkbenchPage activePage = workbenchWindow.getActivePage();
					activePage.showView(ANALYSIS_PARAMETERS_VIEW_ID);
					ExportAllView.setAlgorithmTask(task);
					activePage.showView(EXPORT_ALL_VIEW_ID);
					activePage.setEditorAreaVisible(false);
//					factory.setEditorAreaVisible(false);
					IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
					PlatformUI.getWorkbench().getIntroManager().closeIntro(introPart);
				} catch (NoneAlgorithmException e) {
					Util.handleException(Display.getCurrent().getShells()[0], e);
//					return;
				}
			}
		});
		factory.setEditorAreaVisible(false);
		factory.getViewLayout(DATA_SOURCE_VIEW_ID).setMoveable(false);
		factory.getViewLayout(DATA_SOURCE_VIEW_ID).setCloseable(false);
		factory.setFixed(true);
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
		
		final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		activeWorkbenchWindow.addPerspectiveListener(new IPerspectiveListener() {
			
			@Override
			public void perspectiveChanged(IWorkbenchPage page,
					IPerspectiveDescriptor perspective, String changeId) {
				if (perspective.getId().equals(ANALYSIS_PERSPECTIVE_ID)) {
					IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
					activePage.hideView(activePage.findViewReference("org.gumtree.app.workbench.cruisePanel"));
				}
			}
			
			@Override
			public void perspectiveActivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
			}
		});
}

	public static CicadaDOM getCicadaDOM(){
		if (cicada == null)
			cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
		return cicada;
	}
}
