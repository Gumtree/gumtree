/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Norman Xiong initial API and implementation
 *     Paul Hathaway (April 2009) modify for Quokka
 *******************************************************************************/
package au.gov.ansto.bragg.quokka.ui.internal;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.ui.plot.Plot;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataItem;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotException;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView;


/**
 * The view displays operation parameters for 
 * all operations in current AlgorithmTask.
 * 
 * @author nxi
 */
public class AnalysisControlView extends AnalysisParametersView {

	public static final String SOURCE_PROCESSOR_NAME = "Source";
	public static final String TRANS_PROCESSOR_NAME = "Transmission";
	public static final String PLOT_PROCESSOR_NAME = "Plotter";
	protected PlotManager.OpenNewPlotListener plotListener;
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.kakadu.ui.views.AnalysisParametersView#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite composite) {
		super.createPartControl(composite);

		setVisibility();

	}

	private void setVisibility() {
//		configurationButton.dispose();
		defaultParametersButton.dispose();
		revertParametersButton.dispose();
		applyParametersButton.setText("Apply");
//		GridData data = new GridData();
//		data.grabExcessHorizontalSpace = true;
//		applyParametersButton.setLayoutData(data);
	}

	@Override
	protected void initListeners() {
		super.initListeners();
		
		AlgorithmTask task = getAlgorithmTask();
		OperationManager oMgr = task.getOperationManager(0);
		final Operation opSource = oMgr.getOperation(SOURCE_PROCESSOR_NAME);
		final Operation opTrans = oMgr.getOperation(TRANS_PROCESSOR_NAME);
		final Operation opPlot = oMgr.getOperation(PLOT_PROCESSOR_NAME);
		
		//final Operation operation = task.getOperationManager(0).getOperation(NAVIGATION_PROCESSOR_NAME);
		//final OperationParameter parameter = operation.getOperationParameter(SCAN_VARIABLE_NAME);
		//final Operation plotOperation = task.getOperationManager(0).getOperation(ONEDPLOT_PROCESSOR_NAME);		

//		final KurandaMouseListener doubleClickListener = new KurandaMouseListener()
//		{
//			public void mouseDoubleClick(EventData eventData) { 
//				double x = eventData.getX();
//				//eventData.
//			}
//		
////				List<OperationParameterEditor> editors = parameterEditorsMap.get(operation.getName());
////				for (OperationParameterEditor editor : editors){
////					if (editor.getOperationParameter() == parameter){
////						if (editor instanceof OptionOperationParameterEditor)
////							((OptionOperationParameterEditor) editor).setSelection(
////									new Double(eventData.getX()));
////					}
////				}
////				applyParameters();				
////			}
//
//			public void mouseDown(EventData eventData) {}
//
//			public void mouseUp(EventData eventData) {}
//			
//		};
//
//		PlotManager.addOpenNewPlotListener(
//			new PlotManager.OpenNewPlotListener(){
//				public void newPlotOpened(Plot plot) {
//					if (plot.getOperaton() == opPlot)
//						plot.getCurrentPlotWidget().addMouseEventListener(doubleClickListener);
//				}
//			}
//		);
		
		plotListener = new PlotManager.OpenNewPlotListener() {
			
			public void newPlotOpened(final au.gov.ansto.bragg.kakadu.ui.plot.Plot plot) {
				if (plot != null && plot.getOperaton().getName().equals("plotter.op")){
					ToolBar verticalBar = plot.getVerticalToolbar();
					ToolItem transferToolItem = new ToolItem (verticalBar, SWT.NONE);
					//				copyToolItem.setToolTipText("Copy");
					transferToolItem.setImage(Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, 
					"icons/needle_16x16.png").createImage());
					plot.setQuickRemoveEnabled(true);
					transferToolItem.addSelectionListener(new SelectionListener() {
						
						public void widgetSelected(SelectionEvent arg0) {
							List<PlotDataItem> dataItems = plot.getMultiPlotDataManager().getPlotDataItems();
							if (dataItems != null && dataItems.size() > 0){
								try {
									PlotDataItem newDataItem = dataItems.get(0).clone();
//									newDataItem.setColor(AbstractDataSource.getNextColor());
									newDataItem.setLinked(false);
									plot.getMultiPlotDataManager().addPlotDataItem(newDataItem);
								} catch (PlotException e) {
									e.printStackTrace();
								}
							}
						}
						
						public void widgetDefaultSelected(SelectionEvent arg0) {
							
						}
					});
				}
			}
		};
		PlotManager.addOpenNewPlotListener(plotListener);
	}

	@Override
	public void dispose() {
		super.dispose();
		PlotManager.removeOpenNewPlotListener(plotListener);
	}
	
	
}

