/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong initial API and implementation
*    Paul Hathaway (April 2009) modify for Quokka
*******************************************************************************/
package au.gov.ansto.bragg.quokka.ui.internal;

import java.net.URI;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.Algorithm.AlgorithmStatus;
import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTaskStatusListener;
import au.gov.ansto.bragg.kakadu.core.DataListener;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;
import au.gov.ansto.bragg.kakadu.ui.util.Util;

public class ExportAllView extends ViewPart {

	private Button button;
	private Button monitorButton;
	private ProgressBar bar;
	private Composite composite;
	private static AlgorithmTask task;
	private static final String ALGORITHM_SET = "Quokka Reduction (V2.0UI)";
	
	private static String LBL_EXPORT_READY = "Batch Export";
	private static String LBL_MONITOR_BUTTON_ENABLED = "Enabled";
	private static String LBL_MONITOR_BUTTON_DISABLED = "Disabled";
	private static String LBL_EXPORT_BUSY = "Exporting...";
	private static String LBL_EXPORT_DONE = "Exported";
	private static String LBL_EXPORT_ERROR = "Error";
	private static String TTT_MONITOR_BUTTON_ENABLED = "Toggle the button to automatically reduce data from scan workflow: Enabled";
	private static String TTT_MONITOR_BUTTON_DISABLED = "Toggle the button to automatically reduce data from scan workflow: Disabled";
	private static String ICON_MONITOR_BUTTON_ENABLE = "icons/target_16x16.png";
	private static String ICON_MONITOR_BUTTON_DISABLE = "icons/target_disable_16x16.png";
	
	private DataListener<DataSourceFile> dataListener;
	private AlgorithmStatus currentStatus = AlgorithmStatus.Idle;

	/**
	 * 
	 */
	public ExportAllView() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite composite) {
		this.composite = composite;
		GridLayout parametersCompositeGridLayout = new GridLayout();
		parametersCompositeGridLayout.numColumns = 3;
		parametersCompositeGridLayout.verticalSpacing = 0;
		parametersCompositeGridLayout.marginHeight = 3;
		parametersCompositeGridLayout.marginWidth = 3;
		composite.setLayout(parametersCompositeGridLayout);
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		monitorButton = new Button(composite, SWT.TOGGLE);
		monitorButton.setText(LBL_MONITOR_BUTTON_ENABLED);
		monitorButton.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, ICON_MONITOR_BUTTON_ENABLE).createImage());
		monitorButton.setToolTipText(TTT_MONITOR_BUTTON_ENABLED);
//		monitorButton.setEnabled(isDataSourceAvailable());
		monitorButton.setSelection(true);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		monitorButton.setLayoutData (data);
		
		button = new Button(composite, SWT.PUSH);
		button.setText(LBL_EXPORT_READY);
		button.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/file-export-16x16.png").createImage());
		button.setToolTipText("Export reduction results of all opened nexus files into 3-column files");
		button.setEnabled(isDataSourceAvailable());
		data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		button.setLayoutData (data);

		bar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.NULL);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		bar.setLayoutData (data);
		bar.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		bar.setEnabled(false);
		initListener();
	}

	private boolean isDataSourceAvailable() {
		List<DataItem> dataItemList = DataSourceManager.getInstance().getAllDataItems();
		boolean isAvailable = (dataItemList == null || dataItemList.size() == 0);
		return isAvailable;
	}

	private void initListener(){
		
		monitorButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (monitorButton.getSelection()){
					monitorButton.setText(LBL_MONITOR_BUTTON_ENABLED);
					monitorButton.setImage(Activator.imageDescriptorFromPlugin(
							Activator.PLUGIN_ID, ICON_MONITOR_BUTTON_ENABLE).createImage());
					monitorButton.setToolTipText(TTT_MONITOR_BUTTON_ENABLED);
				}
				else{
					monitorButton.setText(LBL_MONITOR_BUTTON_DISABLED);
					monitorButton.setImage(Activator.imageDescriptorFromPlugin(
							Activator.PLUGIN_ID, ICON_MONITOR_BUTTON_DISABLE).createImage());
					monitorButton.setToolTipText(TTT_MONITOR_BUTTON_DISABLED);
				}
//				WorkflowModelListener.setEnabled(monitorButton.getSelection());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		button.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {			
			}

			public void widgetSelected(SelectionEvent arg0) {
				CicadaDOM cicada = QuokkaAnalysisPerspective.getCicadaDOM();
				try{
					String filename = Util.selectDirectoryFromShell(composite.getShell());
					if (filename == null || filename.trim().length() == 0)
						return;
					URI targetFolderURI = null;
					if (filename != null) {
						targetFolderURI = ConverterLib.path2URI(filename);
					}
					
					Algorithm exportAllAlgorithm = cicada.loadAlgorithm(ALGORITHM_SET);
					
					List<DataItem> dataItemList = DataSourceManager.getInstance().getAllDataItems();
					
					if (dataItemList == null || dataItemList.size() == 0)
						return;
						//throw new NullPointerException("No data is available");
					
					bar.setEnabled(true);
					bar.setMinimum(0);
					bar.setMaximum(dataItemList.size());
					button.setText(LBL_EXPORT_BUSY);
					button.setEnabled(false);
					
					for (DataItem dataItem : dataItemList){
						bar.setSelection(bar.getSelection() + 1);
						IGroup groupData = dataItem.getDataObject();
						cicada.loadInputData(groupData);
						cicada.loadAlgorithm(ALGORITHM_SET);
//						if ((null!=task)&&(task.getAlgorithmInputs().size() > 0)) {
//							List<Tuner> tuners = task.getAlgorithmInputs().get(0).getAlgorithm().getTunerArray();
//							for (Tuner tuner : tuners){
//								if (tuner.getCoreName().equals(EFFICIENCY_TUNER_NAME))
//									cicada.setTuner(EFFICIENCY_TUNER_NAME, tuner.getSignal());
//							}
//						}
//						//cicada.setTuner("frame_XYFolderName", targetFolderURI);
//						cicada.process();
					}
					button.setText(LBL_EXPORT_DONE);
				} catch (Exception e) {
					button.setText(LBL_EXPORT_ERROR);
					Util.handleException(composite.getShell(), e);
				} finally {
					bar.setSelection(0);
					bar.setEnabled(false);
					button.setEnabled(true);					
				}
			}
		});
		
		dataListener = new DataListener<DataSourceFile>() {
			public void dataAdded(DataSourceFile addedData) {
				button.setEnabled(true);
				button.setText(LBL_EXPORT_READY);
			}
			
			public void dataRemoved(DataSourceFile removedData) {
			}
			
			public void dataUpdated(DataSourceFile updatedData) {
				button.setEnabled(true);
				button.setText(LBL_EXPORT_READY);
			}
			
			public void allDataRemoved(DataSourceFile removedData) {
				button.setText(LBL_EXPORT_READY);
				button.setEnabled(false);
			}
		};
		DataSourceManager.getInstance().addDataListener(dataListener);

		AlgorithmTaskStatusListener statusListener = new AlgorithmTaskStatusListener(){

			public void onChange(final AlgorithmStatus status) {
				composite.getDisplay().asyncExec(new Runnable(){

					public void run() {
						if (currentStatus != AlgorithmStatus.Running && status == AlgorithmStatus.Running){

							bar.setEnabled(true);
							bar.setMinimum(0);
							bar.setMaximum(task.getOperationManager(0).getOperations().size());
//							progressBar.setMaximum(6);
						}else if (status == AlgorithmStatus.End){ //AlgorithmStatus.Running && status != AlgorithmStatus.Running){
							bar.setSelection(0);
							bar.setEnabled(false);
						}
						currentStatus = status;						
					}});
			}

			public void setStage(final int operationIndex, final AlgorithmStatus status) {
				composite.getDisplay().asyncExec(new Runnable(){

					public void run() {
						if (bar.isEnabled()){
							if (bar.isEnabled() && bar.getMaximum() > operationIndex + 1 && 
									operationIndex + 1 > bar.getSelection()){
								bar.setSelection(operationIndex + 1);
							}else if (bar.getMaximum() == operationIndex + 1){
								bar.setSelection(0);
								bar.setMaximum(0);
								bar.setEnabled(false);
							}
						}
					}});
			}};
		
		task.addStatusListener(statusListener);
			
//		AlgorithmTaskStatusListener statusListener = new AlgorithmTaskStatusListener() {
//
//			public void onChange(final AlgorithmStatus status) {
//				composite.getDisplay().asyncExec(new Runnable(){
//					public void run() {
//						if ((currentStatus != AlgorithmStatus.Running) && (status == AlgorithmStatus.Running)) {
//							bar.setEnabled(true);
//							bar.setMinimum(0);
//							bar.setMaximum(task.getOperationManager(0).getOperations().size());
//						} else {
//							if (status == AlgorithmStatus.End) { 
//								bar.setSelection(0);
//								bar.setEnabled(false);
//							}
//						}
//						currentStatus = status;						
//					}
//				});
//			}
//
//			public void setStage(final int operationIndex, final AlgorithmStatus status) {
//				composite.getDisplay().asyncExec( new Runnable(){
//					public void run() {
//						if (bar.isEnabled()) {
//							int max = bar.getMaximum();
//							int next = operationIndex + 1;
//							if (bar.isEnabled() 
//									&& (next < max) 
//									&& (next > bar.getSelection() ) ) {
//								bar.setSelection(next);
//							} else {
////								if (max > operationIndex) {
////									bar.setSelection(0);
////									System.out.println("set to 0.1");
////									bar.setMaximum(0);
////									bar.setEnabled(false);
////								}
//							}
//						}
//					}}
//				);
//			}
//			
//		};
//		task.addStatusListener(statusListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		DataSourceManager.getInstance().removeDataListener(dataListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {

	}

	/**
	 * @param task the algorithm to set
	 */
	public static void setAlgorithmTask(AlgorithmTask task) {
		ExportAllView.task = task;
	}

}
