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
package au.gov.ansto.bragg.nbi.ui.scripting;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.gumtree.data.ui.part.PlotView;

public class ScriptingPerspective implements IPerspectiveFactory {

	public static final String SCRIPTING_PERSPECTIVE_ID = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	public static final String PLOT_VIEW_ID = "org.gumtree.data.ui.PlotView";
//	public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.wombat.ui.views.WombatDataSourceView";
	public static final String SCRIPT_CONTROL_VIEW_ID = "au.gov.ansto.bragg.nbi.ui.scripting.ControlView";
	public static final String SCRIPT_DATASOURCE_VIEW_ID = "au.gov.ansto.bragg.nbi.ui.scripting.DataSourceView";
	public static final String SCRIPT_CONSOLE_VIEW_ID = "au.gov.ansto.bragg.nbi.ui.scripting.ConsoleView";
	public static final String DUMMY_VIEW_ID = "au.gov.ansto.bragg.nbi.ui.scripting.DummyView";
	public static final String PROJECT_EXPLORER_VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer";
	
	public void createInitialLayout(final IPageLayout factory) {
//		factory.addShowViewShortcut(DATA_SOURCE_VIEW_ID);
//		factory.addShowViewShortcut(ALGORITHM_LIST_VIEW_ID);
//		factory.addShowViewShortcut(MASK_PROPERTIES_VIEW_ID);
//		factory.addShowViewShortcut(PLOT_VIEW_ID);
		
		factory.addPerspectiveShortcut(SCRIPTING_PERSPECTIVE_ID);

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
//		factory.addStandaloneView(PLOT_VIEW_ID + ":1", false, 
//				IPageLayout.RIGHT, 0.22f, factory.getEditorArea());
//		factory.addStandaloneView(PLOT_VIEW_ID + ":2", false, 
//				IPageLayout.BOTTOM, 0.33f, PLOT_VIEW_ID + ":1");
//		factory.addStandaloneView(PLOT_VIEW_ID + ":3", false, 
//				IPageLayout.BOTTOM, 0.50f, PLOT_VIEW_ID + ":2");

//		factory.addStandaloneViewPlaceholder(
//				PLOT_VIEW_ID, IPageLayout.BOTTOM, 0.50f, "rightCenter", false);
//		bottom = factory.createFolder(
//				"bottom",
//				IPageLayout.BOTTOM,
//				0.67f,
//				factory.getEditorArea());
//		bottom.addView(BEANSHELL_VIEW_ID);
		factory.addStandaloneView(DUMMY_VIEW_ID + ":1", false, 
				IPageLayout.LEFT, 0.2f, factory.getEditorArea());

		factory.addStandaloneViewPlaceholder(SCRIPT_DATASOURCE_VIEW_ID,  
				IPageLayout.TOP, 0.32f, DUMMY_VIEW_ID + ":1", false);
		
		factory.addPlaceholder(SCRIPT_CONTROL_VIEW_ID, 
				IPageLayout.BOTTOM, 0.05f, DUMMY_VIEW_ID + ":1");
		
//		factory.addPlaceholder(PROJECT_EXPLORER_VIEW_ID, 
//				IPageLayout.BOTTOM, 0.05f, DUMMY_VIEW_ID + ":1");
		
		factory.addStandaloneView(DUMMY_VIEW_ID + ":2", false, 
				IPageLayout.BOTTOM, 0.75f, factory.getEditorArea());
		
		factory.addStandaloneViewPlaceholder(SCRIPT_CONSOLE_VIEW_ID, 
				IPageLayout.LEFT, 0.67f, DUMMY_VIEW_ID + ":2", false);
		
		factory.addStandaloneView(DUMMY_VIEW_ID + ":3", false, 
				IPageLayout.RIGHT, 0.75f, factory.getEditorArea());
		
		factory.addStandaloneViewPlaceholder(PLOT_VIEW_ID + ":1", 
				IPageLayout.TOP, 0.50f, DUMMY_VIEW_ID + ":3", false);

		factory.addStandaloneViewPlaceholder(PLOT_VIEW_ID + ":2", 
				IPageLayout.BOTTOM, 0.05f, DUMMY_VIEW_ID + ":3", false);
		
		factory.addPlaceholder(PLOT_VIEW_ID + ":3", 
				IPageLayout.RIGHT, 0.05f, DUMMY_VIEW_ID + ":2");

		
		
//		factory.addPlaceholder(PLOT_VIEW_ID + ":*", 
//				IPageLayout.BOTTOM, 0.50f, PLOT_VIEW_ID + ":2");
				
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
		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		workbenchWindow.addPerspectiveListener(new PerspectiveAdapter() {
			@Override
			public void perspectiveOpened(IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				super.perspectiveOpened(page, perspective);
				final PerspectiveAdapter adapter = this;
				final ScriptPageRegister register = new ScriptPageRegister();
				Display.getDefault().asyncExec(new Runnable(){

					public void run() {
						try{
							registerViews(register);
//							factory.getViewLayout(SCRIPT_CONTROL_VIEW_ID).setMoveable(false);
//							factory.getViewLayout(SCRIPT_CONTROL_VIEW_ID).setCloseable(false);

							workbenchWindow.removePerspectiveListener(adapter);
						}catch (Exception e) {
							e.printStackTrace();
						}
					}});
			}
		});
		factory.setEditorAreaVisible(false);
		factory.getViewLayout(PLOT_VIEW_ID + ":3").setMoveable(false);
		factory.getViewLayout(PLOT_VIEW_ID + ":3").setCloseable(false);
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

	public static void registerViews(final ScriptPageRegister register) throws PartInitException{
		
		IWorkbenchWindow workbenchWindow = null;
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			if (window.getActivePage().getPerspective().getId().equals(SCRIPTING_PERSPECTIVE_ID)){
				workbenchWindow = window;
			}
		}
		if (workbenchWindow == null) {
			return;
		}
		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		register.setWorkbenchPage(workbenchPage);
		
		DataSourceView dataSourceView = (DataSourceView) workbenchPage.showView(
				SCRIPT_DATASOURCE_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
		register.setDataSourceViewer(dataSourceView.getViewer());

		ConsoleView consoleView = (ConsoleView) workbenchPage.showView(
				SCRIPT_CONSOLE_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
		register.setConsoleViewer(consoleView.getCommandLineViewer());
		
		ControlView controlView = (ControlView) workbenchPage.showView(
				SCRIPT_CONTROL_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
		ScriptPageRegister.registPage(controlView.getViewer().getScriptRegisterID(), register);
		register.setControlViewer(controlView.getViewer());
		
//		workbenchPage.showView(
//				PROJECT_EXPLORER_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);

		PlotView plot1 = (PlotView) workbenchPage.showView(
				PLOT_VIEW_ID, "1", IWorkbenchPage.VIEW_CREATE);
		register.setPlot1(plot1);
		
		PlotView plot2 = (PlotView) workbenchPage.showView(
				PLOT_VIEW_ID, "2", IWorkbenchPage.VIEW_CREATE);
		register.setPlot2(plot2);
		
		PlotView plot3 = (PlotView) workbenchPage.showView(
				PLOT_VIEW_ID, "3", IWorkbenchPage.VIEW_CREATE);
		register.setPlot3(plot3);
		PlotView.setCurrentIndex(4);
		
		workbenchPage.hideView(workbenchPage.findViewReference(DUMMY_VIEW_ID, "1"));
		workbenchPage.hideView(workbenchPage.findViewReference(DUMMY_VIEW_ID, "2"));
		workbenchPage.hideView(workbenchPage.findViewReference(DUMMY_VIEW_ID, "3"));
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		controlView.getViewer().runInitialScripts();

	}
}
