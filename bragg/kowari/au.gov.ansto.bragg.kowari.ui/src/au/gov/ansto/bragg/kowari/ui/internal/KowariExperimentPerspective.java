/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.kowari.ui.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.SicsIOException;
import org.gumtree.scripting.ScriptExecutor;
import org.gumtree.ui.scripting.ScriptingUI;
import org.gumtree.ui.scripting.viewer.ICommandLineViewer;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOM;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOMFactory;
import au.gov.ansto.bragg.kakadu.ui.views.ButtonClickListener;
import au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView;
import au.gov.ansto.bragg.kowari.exp.core.KowariExperiment;

/**
 * @author nxi
 * Created on 09/07/2009
 */
public class KowariExperimentPerspective implements IPerspectiveFactory {
	public static final String EXPERIMENT_PERSPECTIVE_ID = "au.gov.ansto.bragg.kowari.ui.internal.ExperimentPerspective";
	public static final String PLOT_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.PlotView";
	public static final String MASK_PROPERTIES_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView";
	public static final String ALGORITHM_LIST_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.AlgorithmListView";
	public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.DataSourceView";
	public static final String OPERATION_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView";
	public static final String BEANSHELL_VIEW_ID = "org.gumtree.ui.cli.beanShellTerminalview";
	public static final String ID_VIEW_COMMAND_LINE = "org.gumtree.scripting.ui.commandLineView";
//	public static final String COMMAND_LINE_VIEW_ID="org.gumtree.scripting.ui.commandLineView";
	
//	private IFolderLayout top;
	private IPlaceholderFolderLayout rightTop;
	private IPlaceholderFolderLayout rightCenter;
	private IPlaceholderFolderLayout rightBottom;
	private IPlaceholderFolderLayout bottomLeft;
//	private IFolderLayout bottom;
	private static KakaduDOM kakadu = KakaduDOMFactory.getKakaduDOM();
	private static CicadaDOM cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();

	public void createInitialLayout(IPageLayout factory) {
//		factory.addShowViewShortcut(DATA_SOURCE_VIEW_ID);
//		factory.addShowViewShortcut(ALGORITHM_LIST_VIEW_ID);
//		factory.addShowViewShortcut(MASK_PROPERTIES_VIEW_ID);
		
		factory.addShowViewShortcut(PLOT_VIEW_ID);
		
		factory.addPerspectiveShortcut(EXPERIMENT_PERSPECTIVE_ID);

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

		rightTop = factory.createFolder(
		"rightTop",
		IPageLayout.RIGHT,
		0.235f,
		factory.getEditorArea());
		rightTop.addPlaceholder(PLOT_VIEW_ID + ":1");

		rightCenter = factory.createFolder(
				"rightCenter",
				IPageLayout.BOTTOM,
				0.33f,
				"rightTop");
		rightCenter.addPlaceholder(PLOT_VIEW_ID + ":2");

		rightBottom = factory.createFolder(
				"rightBottom",
				IPageLayout.BOTTOM,
				0.50f,
				"rightCenter");
		rightBottom.addPlaceholder(PLOT_VIEW_ID + ":3");
//		factory.addStandaloneViewPlaceholder(
//				PLOT_VIEW_ID, IPageLayout.BOTTOM, 0.50f, "rightCenter", false);
//		bottom = factory.createFolder(
//				"bottom",
//				IPageLayout.BOTTOM,
//				0.67f,
//				factory.getEditorArea());
//		bottom.addView(BEANSHELL_VIEW_ID);
		bottomLeft = factory.createFolder("BottomLeft",  
				IPageLayout.BOTTOM, 0.79f, factory.getEditorArea());
		bottomLeft.addPlaceholder(ScriptingUI.ID_VIEW_COMMAND_LINE + ":*");
		final ScriptExecutor scriptExecutor = KowariExperiment.getInstance().getExecutor();
//		ScriptingUI.launchNewCommandLineView(scriptExecutor, ICommandLineViewer.NO_UTIL_AREA);
		ScriptingUI.launchNewCommandLineView(scriptExecutor, ICommandLineViewer.NO_UTIL_AREA, 1);
//		LoopRunnerStatus status = LoopRunner.run(new ILoopExitCondition() {
//			public boolean getExitCondition() {
//				boolean result = false;
//				try {
//					result = scriptExecutor.getEngine() != null && scriptExecutor.getEngine().getContext() != null && scriptExecutor.getEngine().getContext().getWriter() instanceof PrintWriter;
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				return result;
//			}
//		}, 30000);
		// Ready, but need to wait / delay a bit more
//		try {
//			Thread.sleep(50);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		top = factory.createFolder(
//				"top", //NON-NLS-1
//				IPageLayout.TOP,
//				1.0f,
//				factory.getEditorArea());
//		top.addView(OPERATION_PARAMETERS_VIEW_ID);
		factory.addStandaloneView(OPERATION_PARAMETERS_VIEW_ID, false, 
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
		IPerspectiveDescriptor descriptor = factory.getDescriptor();
		try {
			kakadu.loadAlgorithm(cicada.loadAlgorithm("Scan"), descriptor);
		} catch (NoneAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		OperationParametersView.subscribeStopButtonListener(new ButtonClickListener(){

			@Override
			public void onClick() {
				// TODO Auto-generated method stub
				try {
					SicsCore.getSicsController().interrupt();
				} catch (SicsIOException e) {
					// TODO Auto-generated catch block
					LoggerFactory.getLogger(ExperimentPerspective.class);
				}
			}
			
		});
		factory.setEditorAreaVisible(false);
		factory.setFixed(true);
}

}
