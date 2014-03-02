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
package au.gov.ansto.bragg.kowari.ui.views;

import java.net.URI;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.part.ViewPart;
import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.dom.core.CicadaDOM;
import au.gov.ansto.bragg.datastructures.core.region.RectilinearRegion;
import au.gov.ansto.bragg.datastructures.core.region.RegionFactory;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.DataListener;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kowari.ui.internal.Activator;
import au.gov.ansto.bragg.kowari.ui.internal.KowariAnalysisPerspective;
import au.gov.ansto.bragg.process.agent.ProcessorAgent;
import au.gov.ansto.bragg.process.port.Tuner;
import au.gov.ansto.bragg.process.port.TunerPortListener;

/**
 * @author nxi
 * Created on 01/12/2008
 */
public class ExportAllView extends ViewPart {

	public final static String EXPORT_ALL_ALGORITHM = "Export All";
	protected Menu stripChoiceMenu;
	private final static int[] numberOfStrips = new int[]{1, 2, 3, 4, 5};
	private Button exportAllButton;
	private ProgressBar progressBar;
	private InnerTunerListener currentStepIndexListener;
	private InnerTunerListener numberOfStepsListener;
//	private Button chooseEfficiencyMapButton;
//	private URI efficiencyMapURI;
	private Composite composite;
	private static AlgorithmTask algorithmTask;
	private static String EFFICIENCY_TUNER_NAME = "frame_efficiencyMapURI";
	private static String ENABLE_EFFICIENCY_TUNER_NAME = "frame_efficiencyCorrectionEnable";
	private static String GEOMETRY_ENABLE_TUNER_NAME = "frame_geometryEnable";
	private static String APPLY_REGION_TUNER_NAME = "frame_region";
	private DataListener<DataSourceFile> dataListener;
//	private AlgorithmStatus currentStatus = AlgorithmStatus.Idle;
	/**
	 * 
	 */
	public ExportAllView() {
	}

	private abstract class InnerTunerListener extends TunerPortListener{

		boolean isEnabled = true;
		public InnerTunerListener(Tuner tuner) {
			super(tuner);
		}
		
		public void setEnabled(boolean isEnabled){
			this.isEnabled = isEnabled;
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite composite) {
		this.composite = composite;
		GridLayout parametersCompositeGridLayout = new GridLayout();
		parametersCompositeGridLayout.numColumns = 2;
		parametersCompositeGridLayout.verticalSpacing = 0;
		parametersCompositeGridLayout.marginHeight = 3;
		parametersCompositeGridLayout.marginWidth = 3;
		composite.setLayout(parametersCompositeGridLayout);
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);
		
		exportAllButton = new Button(composite, SWT.PUSH);
		exportAllButton.setText("Export All");
		exportAllButton.setImage(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/file-export-16x16.png").createImage());
		exportAllButton.setToolTipText("Export reduction results of all opened nexus files into 3-column files");
		exportAllButton.setEnabled(isDataSourceAvailable());
		stripChoiceMenu = new Menu(composite.getShell(), SWT.POP_UP);
		GridData data = new GridData ();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		exportAllButton.setLayoutData (data);

		progressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.NULL);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		progressBar.setLayoutData (data);
		progressBar.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		progressBar.setEnabled(false);
		initExportAllMenu();
		initListener();
	}

	private void initExportAllMenu() {
		for (int i : numberOfStrips){
			MenuItem loadMenuItem = new MenuItem (stripChoiceMenu, SWT.PUSH);
			String menuText;
			if (i == 1)
				menuText = "default - use current mask";
			else
				menuText = "split detector into " + i + " strips";
			loadMenuItem.setText(menuText);
			final int stripCounts = i;
			loadMenuItem.addSelectionListener(new SelectionListener(){

				public void widgetDefaultSelected(SelectionEvent arg0) {

				}

				public void widgetSelected(SelectionEvent e) {
					if (stripCounts == 1){
						defaultProcess();
					}else{
						stripProcess(stripCounts);
					}
				}
			});
		}
		MenuItem intensityItem = new MenuItem(stripChoiceMenu, SWT.PUSH);
		intensityItem.setText("Export intensity curve");
		intensityItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				intensityProcess();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private boolean isDataSourceAvailable() {
		List<DataItem> dataItemList = DataSourceManager.getInstance().getAllDataItems();
		if (dataItemList == null || dataItemList.size() == 0)
			return false;
		return true;
	}

	private void initListener(){
//		chooseEfficiencyMapButton.addSelectionListener(new SelectionListener(){
//
//			public void widgetDefaultSelected(SelectionEvent arg0) {
//				
//			}
//
//			public void widgetSelected(SelectionEvent arg0) {
//				String filename = Util.getFilenameFromShell(composite.getShell(), "*.*", "All");
//				if (filename != null) {
//					try {
//						efficiencyMapURI = ConverterLib.path2URI(filename);
//					} catch (FileAccessException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			
//		});
		

		dataListener = new DataListener<DataSourceFile>() {
			public void dataAdded(DataSourceFile addedData) {
				exportAllButton.setEnabled(true);
				exportAllButton.setText("Export All");
			}
			public void dataRemoved(DataSourceFile removedData) {
				
			}
			public void dataUpdated(DataSourceFile updatedData) {
				exportAllButton.setEnabled(true);
				exportAllButton.setText("Export All");
			}
			public void allDataRemoved(DataSourceFile removedData) {
				exportAllButton.setText("Export All");
				exportAllButton.setEnabled(false);
			}
		};
		DataSourceManager.getInstance().addDataListener(dataListener);

		final Operation operation = algorithmTask.getOperationManager(0).getOperation(AnalysisControlView.NAVIGATION_PROCESSOR_NAME);
		final Tuner numberOfStepsTuner = ((ProcessorAgent) operation.getAgent()).getTuner(AnalysisControlView.NUMBER_OF_STEPS_TUNER_NAME);
		final Tuner currentStepIndexTuner = ((ProcessorAgent) operation.getAgent()).getTuner(AnalysisControlView.CURRENT_STEP_INDEX_TUNER_NAME);
//		final Tuner 

//		AlgorithmTaskStatusListener statusListener = new AlgorithmTaskStatusListener(){
//			int currentIndex = 0;
//			boolean isEnabled = true;
//			public void onChange(final AlgorithmStatus status) {
////				composite.getDisplay().asyncExec(new Runnable(){
//				Display.getDefault().asyncExec(new Runnable(){
//
//					public void run() {
//						if (currentStatus != AlgorithmStatus.Running && status == AlgorithmStatus.Running){
//
//							progressBar.setEnabled(true);
////							progressBar.setMinimum(0);
////							progressBar.setMaximum(0);
//////							progressBar.setMaximum(algorithmTask.getOperationManager(0).getOperations().size());
////							currentIndex = 0;
////							System.out.println("####progress bar started #########");
////							progressBar.setMaximum(6);
//						}else if (status == AlgorithmStatus.End){ //AlgorithmStatus.Running && status != AlgorithmStatus.Running){
//							progressBar.setSelection(0);
//							progressBar.setEnabled(false);
//							currentIndex = 0;
//							System.err.println("####progress bar ended #########");
//						}
//						currentStatus = status;						
//					}});
//			}
//
//			public void setStage(final int operationIndex, final AlgorithmStatus status) {
//				Display.getDefault().asyncExec(new Runnable(){
//
//					public void run() {
//						if (progressBar.isEnabled()){
////							if (progressBar.isEnabled() && progressBar.getMaximum() > operationIndex + 1 && 
////									operationIndex + 1 > progressBar.getSelection()){
////								progressBar.setSelection(operationIndex + 1);
////							}else if (progressBar.getMaximum() == operationIndex + 1){
////								progressBar.setSelection(0);
////								progressBar.setMaximum(0);
////								progressBar.setEnabled(false);
////							}
//							if (operationIndex == 1){
////								if (!(Boolean) isInLoopTuner.getSignal()){
////									progressBar.setMaximum(2);
////									progressBar.setSelection(1);
////								}else{
//								if (!isEnabled){
//									isEnabled = !isEnabled;
//									return;
//								}
//								isEnabled = !isEnabled;
//									if (currentIndex == 0 && status == AlgorithmStatus.Running){
//										try {
////											progressBar.setMaximum(NexusUtils.getNumberOfFrames(
////													algorithmTask.getSelectedAlgorithmInput().getDatabag()));
//											progressBar.setMaximum(6);
//											progressBar.setSelection(++currentIndex);
//											System.out.println("####progress bar set to 6 #########");
//											System.out.println("####progress bar selected " + currentIndex + "#########");
//										} catch (Exception e) {
//										}
//									}else{
//										currentIndex ++;
//										System.out.println("####operation status for " + operationIndex + "#########");
//										System.out.println("####progress bar selected " + currentIndex + "#########");
//										if (currentIndex <= progressBar.getMaximum()){
//											progressBar.setSelection(currentIndex);
//										}else{
//											currentIndex = 0;
//											progressBar.setSelection(0);
//										}
//									}
//								}
////							}
//						}
//					}});
//			}};
		
//		algorithmTask.addStatusListener(statusListener);
			numberOfStepsListener = new InnerTunerListener(numberOfStepsTuner){
				@Override
				public void updateUIMax(Object max) {
				}

				@Override
				public void updateUIMin(Object min) {
				}

				@Override
				public void updateUIOptions(List<?> options) {
				}

				@Override
				public void updateUIValue(final Object newData) {
					if (isEnabled && newData instanceof Integer){
						Display.getDefault().asyncExec(new Runnable(){

							public void run() {
								progressBar.setEnabled(true);
								progressBar.setMaximum((Integer) newData);
								progressBar.setSelection(0);
//								System.err.println("#####Set number of steps to " + newData.toString());
							}});
					}
				}

			};
			numberOfStepsTuner.addVarPortListener(numberOfStepsListener);

			currentStepIndexListener = new InnerTunerListener(currentStepIndexTuner){
				@Override
				public void updateUIMax(Object max) {
				}

				@Override
				public void updateUIMin(Object min) {
				}

				@Override
				public void updateUIOptions(List<?> options) {
				}

				@Override
				public void updateUIValue(final Object newData) {
					if (isEnabled && newData instanceof Integer){
						Display.getDefault().asyncExec(new Runnable(){

							public void run() {
								int index = ((Integer) newData).intValue();
								if (index <= progressBar.getMaximum()){
									progressBar.setSelection(index);
								}else{
									progressBar.setSelection(0);
									progressBar.setEnabled(false);
								}
//								System.err.println("#####Set current index to " + index);
							}});
					}
				}
				
			};
			currentStepIndexTuner.addVarPortListener(currentStepIndexListener);

			exportAllButton.addSelectionListener(new SelectionListener(){

				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				public void widgetSelected(SelectionEvent arg0) {
					Rectangle rect = exportAllButton.getBounds ();
					final Point location = exportAllButton.getLocation();
					final Point size = exportAllButton.getSize();
					Point pt = new Point (rect.x, rect.y + rect.height);
					pt = exportAllButton.getParent().toDisplay (pt);
					stripChoiceMenu.setLocation (pt.x, pt.y);
					stripChoiceMenu.setVisible (true);
				}

			});
			
//			exportAllButton.addSelectionListener(new SelectionListener(){
//
//				public void widgetDefaultSelected(SelectionEvent arg0) {
//					
//				}
//
//				public void widgetSelected(SelectionEvent arg0) {
//					numberOfStepsListener.setEnabled(false);
//					currentStepIndexListener.setEnabled(false);
//					CicadaDOM cicada = KowariAnalysisPerspective.getCicadaDOM();
//					try{
//						String filename = Util.selectDirectoryFromShell(composite.getShell());
//						if (filename == null || filename.trim().length() == 0)
//							return;
//						URI targetFolderURI = null;
//						if (filename != null) {
//							targetFolderURI = ConverterLib.path2URI(filename);
//						}
////						Algorithm exportAllAlgorithm = cicada.loadAlgorithm("Vertical Integration");
//						List<DataItem> dataItemList = DataSourceManager.getInstance().getAllDataItems();
//						if (dataItemList == null || dataItemList.size() == 0)
//							throw new NullPointerException("No data is available");
//						progressBar.setEnabled(true);
//						progressBar.setMinimum(0);
//						progressBar.setMaximum(dataItemList.size());
//						exportAllButton.setText("Exporting");
//						exportAllButton.setEnabled(false);
//						for (DataItem dataItem : dataItemList){
//							progressBar.setSelection(progressBar.getSelection() + 1);
//							Group groupData = dataItem.getDataObject();
//							cicada.loadInputData(groupData);
//							//				System.out.println( amanager.listAvailableAlgorithms() );
//							cicada.loadAlgorithm(EXPORT_ALL_ALGORITHM);
//							//				System.out.println( amanager.listTuners() );
//							if (algorithmTask != null && algorithmTask.getAlgorithmInputs().size() > 0){
//								List<Tuner> tuners = algorithmTask.getAlgorithmInputs().get(0).getAlgorithm().getTunerArray();
//								for (Tuner tuner : tuners){
//									if (tuner.getCoreName().equals(EFFICIENCY_TUNER_NAME))
//										cicada.setTuner(EFFICIENCY_TUNER_NAME, tuner.getSignal());
//									if (tuner.getCoreName().equals(ENABLE_EFFICIENCY_TUNER_NAME))
//										cicada.setTuner(ENABLE_EFFICIENCY_TUNER_NAME, tuner.getSignal());
//									if (tuner.getCoreName().equals(GEOMETRY_ENABLE_TUNER_NAME))
//										cicada.setTuner(GEOMETRY_ENABLE_TUNER_NAME, tuner.getSignal());
//									if (tuner.getCoreName().equals(APPLY_REGION_TUNER_NAME))
//										cicada.setTuner(APPLY_REGION_TUNER_NAME, tuner.getSignal());
//								}
//							}
//							cicada.setTuner("frame_XYFolderName", targetFolderURI);
//							cicada.process();
//						}
//						progressBar.setSelection(0);
//						progressBar.setEnabled(false);
//						exportAllButton.setEnabled(true);
//						exportAllButton.setText("Exported");
//					}catch (Exception e) {
//						exportAllButton.setEnabled(true);
//						exportAllButton.setText("Error");
//						Util.handleException(composite.getShell(), e);
//					}finally{
//						numberOfStepsListener.setEnabled(true);
//						currentStepIndexListener.setEnabled(true);
//					}
//				}
//				
//			});

	}
	
	protected void defaultProcess(){
		numberOfStepsListener.setEnabled(false);
		currentStepIndexListener.setEnabled(false);
		CicadaDOM cicada = KowariAnalysisPerspective.getCicadaDOM();
		try{
			String filename = Util.selectDirectoryFromShell(composite.getShell());
			if (filename == null || filename.trim().length() == 0)
				return;
			URI targetFolderURI = null;
			if (filename != null) {
				targetFolderURI = ConverterLib.path2URI(filename);
			}
//			Algorithm exportAllAlgorithm = cicada.loadAlgorithm("Vertical Integration");
			List<DataItem> dataItemList = DataSourceManager.getInstance().getAllDataItems();
			if (dataItemList == null || dataItemList.size() == 0)
				throw new NullPointerException("No data is available");
			progressBar.setEnabled(true);
			progressBar.setMinimum(0);
			progressBar.setMaximum(dataItemList.size());
			exportAllButton.setText("Exporting");
			exportAllButton.setEnabled(false);
			for (DataItem dataItem : dataItemList){
				progressBar.setSelection(progressBar.getSelection() + 1);
				IGroup groupData = dataItem.getDataObject();
				cicada.loadInputData(groupData);
				//				System.out.println( amanager.listAvailableAlgorithms() );
				cicada.loadAlgorithm(EXPORT_ALL_ALGORITHM);
				//				System.out.println( amanager.listTuners() );
				if (algorithmTask != null && algorithmTask.getAlgorithmInputs().size() > 0){
					List<Tuner> tuners = algorithmTask.getAlgorithmInputs().get(0).getAlgorithm().getTunerArray();
					for (Tuner tuner : tuners){
						if (tuner.getCoreName().equals(EFFICIENCY_TUNER_NAME))
							cicada.setTuner(EFFICIENCY_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(ENABLE_EFFICIENCY_TUNER_NAME))
							cicada.setTuner(ENABLE_EFFICIENCY_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(GEOMETRY_ENABLE_TUNER_NAME))
							cicada.setTuner(GEOMETRY_ENABLE_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(APPLY_REGION_TUNER_NAME))
							cicada.setTuner(APPLY_REGION_TUNER_NAME, tuner.getSignal());
					}
				}
				cicada.setTuner("frame_XYFolderName", targetFolderURI);
				cicada.process();
			}
			progressBar.setSelection(0);
			progressBar.setEnabled(false);
			exportAllButton.setEnabled(true);
			exportAllButton.setText("Exported");
		}catch (Exception e) {
			exportAllButton.setEnabled(true);
			exportAllButton.setText("Error");
			Util.handleException(composite.getShell(), e);
		}finally{
			numberOfStepsListener.setEnabled(true);
			currentStepIndexListener.setEnabled(true);
		}
	}
	
	protected void intensityProcess(){
		numberOfStepsListener.setEnabled(false);
		currentStepIndexListener.setEnabled(false);
		CicadaDOM cicada = KowariAnalysisPerspective.getCicadaDOM();
		try{
			String filename = Util.selectDirectoryFromShell(composite.getShell());
			if (filename == null || filename.trim().length() == 0)
				return;
			URI targetFolderURI = null;
			if (filename != null) {
				targetFolderURI = ConverterLib.path2URI(filename);
			}
//			Algorithm exportAllAlgorithm = cicada.loadAlgorithm("Vertical Integration");
			List<DataItem> dataItemList = DataSourceManager.getInstance().getAllDataItems();
			if (dataItemList == null || dataItemList.size() == 0)
				throw new NullPointerException("No data is available");
			progressBar.setEnabled(true);
			progressBar.setMinimum(0);
			progressBar.setMaximum(dataItemList.size());
			exportAllButton.setText("Exporting");
			exportAllButton.setEnabled(false);
			for (DataItem dataItem : dataItemList){
				progressBar.setSelection(progressBar.getSelection() + 1);
				IGroup groupData = dataItem.getDataObject();
				cicada.loadInputData(groupData);
				//				System.out.println( amanager.listAvailableAlgorithms() );
				cicada.loadAlgorithm("Intensity Export");
				//				System.out.println( amanager.listTuners() );
				if (algorithmTask != null && algorithmTask.getAlgorithmInputs().size() > 0){
					List<Tuner> tuners = algorithmTask.getAlgorithmInputs().get(0).getAlgorithm().getTunerArray();
					for (Tuner tuner : tuners){
						if (tuner.getCoreName().equals(EFFICIENCY_TUNER_NAME))
							cicada.setTuner(EFFICIENCY_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(ENABLE_EFFICIENCY_TUNER_NAME))
							cicada.setTuner(ENABLE_EFFICIENCY_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(GEOMETRY_ENABLE_TUNER_NAME))
							cicada.setTuner(GEOMETRY_ENABLE_TUNER_NAME, tuner.getSignal());
						if (tuner.getCoreName().equals(APPLY_REGION_TUNER_NAME))
							cicada.setTuner(APPLY_REGION_TUNER_NAME, tuner.getSignal());
					}
				}
				cicada.setTuner("frame_XYFolderName", targetFolderURI);
				cicada.process();
			}
			progressBar.setSelection(0);
			progressBar.setEnabled(false);
			exportAllButton.setEnabled(true);
			exportAllButton.setText("Exported");
		}catch (Exception e) {
			exportAllButton.setEnabled(true);
			exportAllButton.setText("Error");
			Util.handleException(composite.getShell(), e);
		}finally{
			numberOfStepsListener.setEnabled(true);
			currentStepIndexListener.setEnabled(true);
		}
	}
	
	protected void stripProcess(int stripCounts){
		numberOfStepsListener.setEnabled(false);
		currentStepIndexListener.setEnabled(false);
		CicadaDOM cicada = KowariAnalysisPerspective.getCicadaDOM();
		try{
			String filename = Util.selectDirectoryFromShell(composite.getShell());
			if (filename == null || filename.trim().length() == 0)
				return;
			URI targetFolderURI = null;
			if (filename != null) {
				targetFolderURI = ConverterLib.path2URI(filename);
			}
//			Algorithm exportAllAlgorithm = cicada.loadAlgorithm("Vertical Integration");
			List<DataItem> dataItemList = DataSourceManager.getInstance().getAllDataItems();
			if (dataItemList == null || dataItemList.size() == 0)
				throw new NullPointerException("No data is available");
			progressBar.setEnabled(true);
			progressBar.setMinimum(0);
			progressBar.setMaximum(dataItemList.size() * stripCounts);
			progressBar.setSelection(0);
			exportAllButton.setText("Exporting");
			exportAllButton.setEnabled(false);
			for (int stripId = 0; stripId < stripCounts; stripId ++){
//				RectilinearRegion region = RegionFactory.createRectilinearRegion(
//						parent, shortName, physicalReference, physicalRange, units, isInclusive);
				IGroup regionSet = Factory.createGroup("region_set");
				for (DataItem dataItem : dataItemList){
					progressBar.setSelection(progressBar.getSelection() + 1);
					IGroup groupData = dataItem.getDataObject();
					cicada.loadInputData(groupData);
					//				System.out.println( amanager.listAvailableAlgorithms() );
					cicada.loadAlgorithm(EXPORT_ALL_ALGORITHM);
					//				System.out.println( amanager.listTuners() );
					RectilinearRegion region = null;
//					if (groupData instanceof Plot){
					IGroup nexusData = NexusUtils.getNexusData(groupData);
					if (nexusData != null){
						List<IDataItem> axes = NexusUtils.getNexusAxis(nexusData);
						if (axes != null && axes.size() >= 2){
							IDataItem yAxis = axes.get(axes.size() - 2);
							IDataItem xAxis = axes.get(axes.size() - 1);
							IArray yAxisArray = yAxis.getData();
							IArray xAxisArray = xAxis.getData();
							double minY = yAxisArray.getArrayMath().getMinimum();
							double maxY = yAxisArray.getArrayMath().getMaximum();
							double yStep = Math.round((maxY - minY) / stripCounts);
							double minX = xAxisArray.getArrayMath().getMinimum();
							double maxX = xAxisArray.getArrayMath().getMaximum();
							double[] reference = new double[]{minY + yStep * stripId, minX};
							double[] range = new double[]{yStep, maxX - minX}; 
							if (stripId == stripCounts - 1)
								range = new double[]{maxY - reference[0], maxX - minX};
							region = (RectilinearRegion) RegionFactory.createRectilinearRegion(
									regionSet, "region" + stripId, reference, range, new String[]{"mm", "mm"}, true);
						}
					}
					if (algorithmTask != null && algorithmTask.getAlgorithmInputs().size() > 0){
						List<Tuner> tuners = algorithmTask.getAlgorithmInputs().get(0).getAlgorithm().getTunerArray();
						for (Tuner tuner : tuners){
							if (tuner.getCoreName().equals(EFFICIENCY_TUNER_NAME))
								cicada.setTuner(EFFICIENCY_TUNER_NAME, tuner.getSignal());
							if (tuner.getCoreName().equals(ENABLE_EFFICIENCY_TUNER_NAME))
								cicada.setTuner(ENABLE_EFFICIENCY_TUNER_NAME, tuner.getSignal());
							if (tuner.getCoreName().equals(GEOMETRY_ENABLE_TUNER_NAME))
								cicada.setTuner(GEOMETRY_ENABLE_TUNER_NAME, tuner.getSignal());
						}
					}
					cicada.setTuner(APPLY_REGION_TUNER_NAME, regionSet);
					cicada.setTuner("frame_XYFolderName", targetFolderURI);
					cicada.process();
				}
			}
			progressBar.setSelection(0);
			progressBar.setEnabled(false);
			exportAllButton.setEnabled(true);
			exportAllButton.setText("Exported");
		}catch (Exception e) {
			exportAllButton.setEnabled(true);
			exportAllButton.setText("Error");
			Util.handleException(composite.getShell(), e);
		}finally{
			numberOfStepsListener.setEnabled(true);
			currentStepIndexListener.setEnabled(true);
		}
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
		ExportAllView.algorithmTask = task;
	}

	
}
