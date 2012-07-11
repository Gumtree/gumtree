/* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Paul Hathaway - April 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOMFactory;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOM;
import au.gov.ansto.bragg.kakadu.dom.KakaduDOMFactory;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;

public class QuokkaAnalysisPerspective implements IPerspectiveFactory {

	public static final String PERSPECTIVE_ID = "au.gov.ansto.bragg.quokka.ui.analysis";
	public static final String PLOT_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.PlotView";
	//public static final String PLOT_VIEW_ID = "au.gov.ansto.bragg.quokka.ui.internal.PlotViewQuokkaCustom";
	public static final String MASK_PROPERTIES_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView";
	public static final String ALGORITHM_LIST_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.AlgorithmListView";
	//public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.DataSourceView";
	//public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.quokka.ui.internal.SourceBrowserView";
	//public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.instrument.InstrumentDataSourceView";
	//public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.kowari.ui.views.KowariDataSourceView";
	//public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.echidna.ui.views.EchidnaDataSourceView";
	public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.quokka.ui.internal.DataSourceView";
	public static final String OPERATION_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView";
	public static final String ANALYSIS_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.quokka.ui.internal.AnalysisControlView";
	public static final String EXPORT_ALL_VIEW_ID = "au.gov.ansto.bragg.quokka.ui.internal.ExportAllView";
	public static final String BEANSHELL_VIEW_ID = "org.gumtree.ui.cli.beanShellTerminalview";
	public static final String ALGORITHM_SET = "Quokka Reduction (V3.1)";
	
	public static final int RAW_DATA_PLOT_ID = 2;
	public static final int ANC_DATA_PLOT_ID = 0; //0;
	public static final int RED_DATA_PLOT_ID = 1; //1;
	
	public static final String RAW_PLOT_ID = PLOT_VIEW_ID + ":" + "1";
	public static final String ANC_PLOT_ID = PLOT_VIEW_ID + ":" + "2"; //0;
	public static final String RED_PLOT_ID = PLOT_VIEW_ID + ":" + "3"; //1;
	
	private static KakaduDOM kakadu = KakaduDOMFactory.getKakaduDOM();
	private static CicadaDOM cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();

	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
        defineLayout(layout);
	}

	private void defineActions(IPageLayout layout) {
		//factory.addShowViewShortcut(PLOT_VIEW_ID);
		layout.addPerspectiveShortcut(PERSPECTIVE_ID);
	}
	
	private void defineLayout(IPageLayout layout) {

		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

		String editorArea = layout.getEditorArea();
		
		IViewLayout view = layout.getViewLayout(PLOT_VIEW_ID);
		view.setCloseable(false);
		view.setMoveable(false);
		
		layout.addStandaloneView(
				ANC_PLOT_ID, //RAW_DATA_PLOT, //"1",
				false, 
				IPageLayout.RIGHT, 
				0.30f, 
				editorArea);
		
		layout.addStandaloneView(
				RED_PLOT_ID, //ANC_DATA_PLOT, //"2", 
				false, 
				IPageLayout.BOTTOM, 
				0.48f, 
				ANC_PLOT_ID); //RAW_DATA_PLOT);
		
		layout.addStandaloneView(
				RAW_PLOT_ID, //RED_DATA_PLOT, //"3", 
				false, 
				IPageLayout.LEFT, 
				0.50f, 
				ANC_PLOT_ID); //RAW_DATA_PLOT);

		layout.addStandaloneView(
				ANALYSIS_PARAMETERS_VIEW_ID, 
				false, 
				IPageLayout.BOTTOM, 
				0.3f, 
				editorArea);
		
		layout.addStandaloneView(
				EXPORT_ALL_VIEW_ID, 
				false, 
				IPageLayout.BOTTOM, 
				0.87f, 
				editorArea); // layout.getEditorArea());
		
		layout.addStandaloneView(
				DATA_SOURCE_VIEW_ID, 
				true, 
				IPageLayout.TOP, 
				1.0f, 
				editorArea); //layout.getEditorArea());
		
		IPerspectiveDescriptor descriptor = layout.getDescriptor();
		try {
			PlotManager.resetPlotViewId();
			//String s = cicada.listAvailableAlgorithms();
			Algorithm algorithm = cicada.loadAlgorithm(ALGORITHM_SET);
			kakadu.loadAlgorithm(algorithm, descriptor);
			AlgorithmTask task = ProjectManager.getCurrentAlgorithmTask();
			ExportAllView.setAlgorithmTask(task);
		} catch (NoneAlgorithmException e) {
			Util.handleException(Display.getCurrent().getShells()[0], e);
			return;
		}
	}
	
	public static CicadaDOM getCicadaDOM(){
		if (cicada == null) {
			cicada = (CicadaDOM) CicadaDOMFactory.getCicadaDOM();
		}
		return cicada;
	}
}
