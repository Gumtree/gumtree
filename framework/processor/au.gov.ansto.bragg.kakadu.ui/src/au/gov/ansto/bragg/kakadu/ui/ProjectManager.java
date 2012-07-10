/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.exception.ConfigurationException;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationDataListener;
import au.gov.ansto.bragg.kakadu.ui.editors.AlgorithmTaskEditor;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.views.PlotView;
import au.gov.ansto.bragg.process.exception.NullSignalException;

public class ProjectManager {
	
	private static Map<Integer, AlgorithmTask> algorithmTasks = new HashMap<Integer, AlgorithmTask>();
	private static AlgorithmTask currentAlgorithmTask;
	private enum AlgorithmListViewAction{load, run};
	
	
	static {
		//the PartListener listener must be added after active workbenchPage was created.
		//to ensure it the operation should be performed in asyncExec block  
		DisplayManager.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow workbenchWindow = Activator.getDefault()
						.getWorkbench().getActiveWorkbenchWindow();
				
				
				workbenchWindow.getWorkbench().addWorkbenchListener(new IWorkbenchListener(){

					public void postShutdown(IWorkbench workbench) {
					}

					public boolean preShutdown(IWorkbench workbench, boolean forced) {
						PlotManager.closeAllPlots();
						return true;
					}
					
				});
				
				final IWorkbenchPage workbenchPage = workbenchWindow
						.getActivePage();
				workbenchPage.addPartListener(new IPartListener2() {

					public void partActivated(IWorkbenchPartReference partRef) {
						final IWorkbenchPart part = partRef.getPart(false);
						if (part != null && part instanceof AlgorithmTaskEditor) {
							final AlgorithmTask algorithmTask = ((AlgorithmTaskEditor) part)
									.getAlgorithmTask();
							if (currentAlgorithmTask != algorithmTask) {
								setCurrentAlgorithmTask(algorithmTask);
							}
						}
					}

					public void partBroughtToTop(IWorkbenchPartReference partRef) {
					}

					public void partClosed(IWorkbenchPartReference partRef) {
						final IWorkbenchPart part = partRef.getPart(false);
						if (part != null) {
							if (part instanceof AlgorithmTaskEditor) {
								final AlgorithmTask algorithmTask = ((AlgorithmTaskEditor) part)
										.getAlgorithmTask();
								if (currentAlgorithmTask == algorithmTask) {
									setCurrentAlgorithmTask(null);
								}
								
								//close all PlotViews related to the AlgorithmTask 
								PlotManager.closeAllPlots(algorithmTask);
								
								//unregister the AlgorithmTask in the manager
								removeAlgorithmTask(algorithmTask.getId());
							} else if (part instanceof PlotView) {
								//unregister PlotView in PlotManager
								PlotManager.closePlot((PlotView)part);
							}
						}
					}

					public void partDeactivated(IWorkbenchPartReference partRef) {
					}

					public void partHidden(IWorkbenchPartReference partRef) {
					}

					public void partInputChanged(IWorkbenchPartReference partRef) {
					}

					public void partOpened(IWorkbenchPartReference partRef) {
					}

					public void partVisible(IWorkbenchPartReference partRef) {
					}

				});
			}
		});

		
	}
	
	/**
	 * Run the algorithm.
	 * @param algorithm
	 * @throws ConfigurationException 
	 * @throws LoadAlgorithmFileFailedException 
	 * @throws LoadAlgorithmFileFailedException
	 * @throws NoneAlgorithmException
	 * @throws NullSignalException
	 */
	public static AlgorithmTaskEditor runAlgorithm(final Algorithm algorithm) 
	throws LoadAlgorithmFileFailedException, ConfigurationException	{
		return loadAlgorithmRoutine(algorithm, AlgorithmListViewAction.run, null);		
	}
	
	public static AlgorithmTaskEditor runAlgorithm(final Algorithm algorithm, URI fileUri) 
	throws LoadAlgorithmFileFailedException, ConfigurationException	{
		return loadAlgorithmRoutine(algorithm, AlgorithmListViewAction.run, fileUri);		
	}

	private static AlgorithmTaskEditor loadAlgorithmRoutine(final Algorithm algorithm, AlgorithmListViewAction 
			action, URI fileUri) throws LoadAlgorithmFileFailedException, ConfigurationException	{
		return loadAlgorithmRoutine(algorithm, action, fileUri, true, null);
	}
	
	private static AlgorithmTaskEditor loadAlgorithmRoutine(final Algorithm algorithm, AlgorithmListViewAction 
			action, URI fileUri, boolean withUI, IPerspectiveDescriptor descriptor) 
	throws LoadAlgorithmFileFailedException, ConfigurationException	
	 {
		final AlgorithmTask algorithmTask = new AlgorithmTask();
		algorithmTasks.put(algorithmTask.getId(), algorithmTask);
		
		List<DataItem> dataItemList = DataSourceManager.getInstance().getSelectedDataItems();
		if (dataItemList == null)
			dataItemList = new ArrayList<DataItem>();
		if (dataItemList.size() == 0){
			dataItemList.add(new DataItem("none"));
		}
		algorithmTask.load(algorithm, dataItemList);
		currentAlgorithmTask = algorithmTask;
		algorithmTask.setFileUri(fileUri);
		
		System.out.println("Open AlgorithmTaskEditor...");
		IWorkbenchWindow workbenchWindow = null;
		if (descriptor != null)
			for (IWorkbenchWindow win : PlatformUI.getWorkbench().getWorkbenchWindows()) {
				if (win.getActivePage() == null)
					break;
				IPerspectiveDescriptor currentDescriptor = win.getActivePage().getPerspective();
				if (descriptor == currentDescriptor){
					workbenchWindow = win;
				}
			}
		if (workbenchWindow == null)
			workbenchWindow = 
				Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
       	final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
       	AlgorithmTaskEditor editor = null;
		try {
			editor = (AlgorithmTaskEditor) workbenchPage.openEditor(new AlgorithmTaskEditorInput(algorithmTask), 
					"au.gov.ansto.bragg.kakadu.ui.editors.AlgorithmTaskEditor");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		switch (action) {
		case load:
			editor.load();			
			break;
		case run:
			editor.init();
		default:
			break;
		}
		
		//plot result of last operation
		final OperationManager operationManager = algorithmTask.getOperationManager(algorithmTask.getSelectedDataItemIndex());
		final List<Operation> operations = operationManager.getOperations();
		List<Operation> autoPlotOperationList = new ArrayList<Operation>();
		for (Operation operation : operations){
			if (operation.hasAutoPlotSink())
				autoPlotOperationList.add(operation);
		}
		if (autoPlotOperationList.size() == 0){
			final Operation lastOperation = operations.get(operations.size() - 1);
			lastOperation.addOperationDataListener(new OperationDataListener() {
				public void outputDataUpdated(final Operation operation, IGroup oldData, IGroup newData) {
					final OperationDataListener finalListener = this;
					DisplayManager.getDefault().asyncExec(new Runnable() {
						public void run() {
							//plot result of last operation
							PlotManager.plotOperation(algorithmTask, lastOperation, 
									workbenchPage.getWorkbenchWindow());
							operation.removeOperationDataListener(finalListener);
						}
					});
				}
			});	
		}else{
			for (Operation operation : autoPlotOperationList){
				final Operation autoPlotOperation = operation;
				autoPlotOperation.addOperationDataListener(new OperationDataListener() {
					public void outputDataUpdated(final Operation operation, IGroup oldData, IGroup newData) {
						final OperationDataListener finalListener = this;
						DisplayManager.getDefault().asyncExec(new Runnable() {
							public void run() {
								//plot result of last operation
								PlotManager.plotOperation(algorithmTask, autoPlotOperation, 
										workbenchPage.getWorkbenchWindow());
								operation.removeOperationDataListener(finalListener);
							}
						});
					}
				});					
			}
		}
		return editor;
	}
	
	public static void loadAlgorithm(final Algorithm algorithm) 
	throws LoadAlgorithmFileFailedException, ConfigurationException	{
		loadAlgorithmRoutine(algorithm, AlgorithmListViewAction.load, null);
	}

	public static void loadAlgorithmOnWindow(final Algorithm algorithm, IPerspectiveDescriptor descriptor) 
	throws LoadAlgorithmFileFailedException, ConfigurationException	{
		loadAlgorithmRoutine(algorithm, AlgorithmListViewAction.load, null, false, descriptor);
	}

	public static final class AlgorithmTaskEditorInput implements IEditorInput {
		private final AlgorithmTask algorithmTask;

		public AlgorithmTaskEditorInput(AlgorithmTask algorithmTask) {
			this.algorithmTask = algorithmTask;
		}

		public boolean exists() {
			return false;
		}

		public ImageDescriptor getImageDescriptor() {
			return ImageDescriptor.getMissingImageDescriptor();
		}

		public String getName() {
			URI uri = algorithmTask.getFileUri();
			String filename = null;
			if (uri == null)
				filename = "Untitled";
			else{
				filename = (new File(uri)).getName();
			}
				
			return filename + " - " + algorithmTask.getAlgorithm().getName();
		}
		
		public IPersistableElement getPersistable() {
			return null;
		}

		public String getToolTipText() {
			final String shortDescription = algorithmTask.getAlgorithm().getShortDescription();
			return shortDescription != null ? shortDescription : "Algorithm Task";
		}

		public Object getAdapter(Class adapterCalss) {
			if(AlgorithmTask.class.isAssignableFrom(adapterCalss)) {
				return algorithmTask;
			}
			return null;
		}
	}


	public static AlgorithmTask getCurrentAlgorithmTask() {
		return currentAlgorithmTask;
	}
	
	public static void setCurrentAlgorithmTask(AlgorithmTask currentAlgorithmTask) {
		ProjectManager.currentAlgorithmTask = currentAlgorithmTask;
	}


	public static AlgorithmTask getAlgorithmTask(int algorithmTaskId) {
		return algorithmTasks.get(algorithmTaskId);
	}
	
	public static void removeAlgorithmTask(int algorithmTaskId) {
		AlgorithmTask task = algorithmTasks.get(algorithmTaskId);
		if (currentAlgorithmTask == task)
			currentAlgorithmTask = null;
		if (task != null)
			task.clear();
		algorithmTasks.remove(algorithmTaskId);
	}

	public static void init() {
	}
	
}
