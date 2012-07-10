package au.gov.ansto.bragg.kakadu.ui.plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import au.gov.ansto.bragg.cicada.core.exception.FailedToExecuteException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.exception.TunerNotReadyException;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.ui.Activator;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.views.PlotView;

/**
 * The class is used to manage Plot instances created for the application.
 * 
 * @author Danil Klimontov (dak)
 */
public class PlotManager {

	//the field value is used to generate secondary ID for PlotView views.
	private static int secondaryId = 1;
//	private static Map<AlgorithmTask, List<PlotView>> plotViewCache = new HashMap<AlgorithmTask, List<PlotView>>();
//	private static Map<Integer, Plot> plotMap = new HashMap<Integer, Plot>();
	/**
	 * <plotCompositeId, plotView>
	 */
	private static final Map<Integer,PlotView> plotViewMap = new HashMap<Integer, PlotView>();
	private static final List<OpenNewPlotListener> openPlotListeners = new ArrayList<OpenNewPlotListener>();
	

	
	/**
	 * Closes all the PlotViews related for the Algorithm Task.
	 * The action unregister the Algorithm Task from processing by the PlotManager.
	 * @param algorithmTask an Algorithm Task instance.
	 */
	public static void closeAllPlots(AlgorithmTask algorithmTask) {
//		final List<PlotView> list = plotViewCache.remove(algorithmTask);
//		if (list == null) {
//			return;
//		}
//
//    	//hide Views
//		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
//		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
//		for (PlotView plotView : list) {
//			workbenchPage.hideView(plotView);
//			plotMap.remove(plotView.getPlotComposite().getId());
//		}

	}

	public static void closeAllPlots() {
    	//hide Views
		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		for (final PlotView plotView : plotViewMap.values()) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					workbenchPage.hideView(plotView);
				}
			});
		}
		plotViewMap.clear();
	}

	/**
	 * Hides the view on workbench page and unregister it from processing by the PlotManager. 
	 * @param plotView a PlotView object.
	 */
	public static void closePlot(PlotView plotView) {
    	//hide the View
		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		if (workbenchPage != null) {
			workbenchPage.hideView(plotView);
		}
		
		plotViewMap.remove(plotView.getPlotComposite().getId());
		
//		for (List<PlotView> list : plotViewCache.values()) {
//			if (list.remove(plotView)) {
//				plotMap.remove(plotView.getPlotComposite().getId());
//				break;
//			}
//		}
	}
	
	public static void removePlotView(PlotView plotView){
		try {
			plotViewMap.remove(plotView.getPlotComposite().getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update the operations to ensure that data is actual.
	 * @param algorithmTask the Algorithm Task for plots
	 * @param operationName operation name to be updated.
	 * @throws TunerNotReadyException
	 * @throws TransferFailedException
	 * @throws NoneAlgorithmException
	 * @throws FailedToExecuteException
	 */
	public static void updatePlots(final AlgorithmTask algorithmTask, final String operationName)
			throws TunerNotReadyException, TransferFailedException,
			NoneAlgorithmException, FailedToExecuteException {
//		final TunerNotReadyException[] tunerNotReadyException = new TunerNotReadyException[1]; 
//		final TransferFailedException[] transferFailedException = new TransferFailedException[1];
//		final NoneAlgorithmException[] noneAlgorithmException = new NoneAlgorithmException[1];
//		final FailedToExecuteException[] failedToExecuteException = new FailedToExecuteException[1];
//		
//		
////        BusyIndicator.showWhile(null, new Runnable() {
////            public void run() {
////		
////            	final List<PlotView> list = plotViewCache.get(algorithmTask);
////				if (list == null) {
////					return;
////				}
////				for (PlotView plotView : list) {
////					if (plotView.getPlotComposite().getOperationName().equals(operationName)) {
////						try {
////							algorithmTask.runAlgorithmForOperation(operationName, plotView.getPlotComposite().getDataItemIndex());
////						} catch (TunerNotReadyException e) {
////							tunerNotReadyException[0] = e;
////						} catch (TransferFailedException e) {
////							transferFailedException[0] = e;
////						} catch (NoneAlgorithmException e) {
////							noneAlgorithmException[0] = e;
////						} catch (FailedToExecuteException e) {
////							failedToExecuteException[0] = e;
////						}
////					}
////					
////				}
////            }
////        });
//        
//        
//        
//        final int operationIndex = algorithmTask.getOperationManager(0).getOperationIndex(operationName);
//        
//        for (PlotView plotView : plotViewMap.values()) {
//			final List<PlotDataItem> plotDataItems = plotView.getPlotComposite().getMultiPlotDataManager().getPlotDataItems();
//			for (PlotDataItem plotDataItem : plotDataItems) {
//				final PlotDataReference plotDataReference = plotDataItem.getPlotDataReference();
////				if (plotDataReference != null 
////						&& plotDataReference.getTaskId() == algorithmTask.getId() 
////						&& plotDataReference.getOperationIndex() == operationIndex) {
////					
////			        BusyIndicator.showWhile(null, new Runnable() {
////			            public void run() {
////							try {
////								algorithmTask.runAlgorithmForOperation(operationName, plotDataReference.getDataItemIndex());
////							} catch (TunerNotReadyException e) {
////								tunerNotReadyException[0] = e;
////							} catch (TransferFailedException e) {
////								transferFailedException[0] = e;
////							} catch (NoneAlgorithmException e) {
////								noneAlgorithmException[0] = e;
////							} catch (FailedToExecuteException e) {
////								failedToExecuteException[0] = e;
////							}
////						}
////			        });
////				}
//			}
//		}
//        
//        if (tunerNotReadyException[0] != null) {
//        	throw tunerNotReadyException[0];
//        }
//        if (transferFailedException[0] != null) {
//        	throw transferFailedException[0];
//        }
//        if (noneAlgorithmException[0] != null) {
//        	throw noneAlgorithmException[0];
//        }
//        if (failedToExecuteException[0] != null) {
//        	throw failedToExecuteException[0];
//        }
	}
	
	/**
	 * Gets Plot instance by plot Id.
	 * @param plotId a plot ID
	 * @return plot instance with the ID or null if the plot was not registered in the manager.
	 */
	public static Plot getPlot(int plotId) {
		final PlotView plotView = getPlotView(plotId);
		return plotView != null ? plotView.getPlotComposite() : null;
	}

	public static List<Plot> getPlot(String operationName){
		List<Plot> plotList = new ArrayList<Plot>();
		for (PlotView view : getPlotViews()){
			if (view.getPlotComposite().getOperaton().getName().equals(operationName))
				plotList.add(view.getPlotComposite());
		}
		return plotList;
	}
	
	public static List<Plot> getPlot(Operation operation){
		List<Plot> plotList = new ArrayList<Plot>();
		for (PlotView view : getPlotViews()){
			if (view.getPlotComposite().getOperaton() == operation)
				plotList.add(view.getPlotComposite());
		}
		return plotList;
	}
	
	/**
	 * @param plotId
	 * @return
	 */
	private static PlotView getPlotView(int plotId) {
		return plotViewMap.get(plotId);
	}
	
	public static Collection<PlotView> getPlotViews() {
		return plotViewMap.values();
	}

	/**
	 * Gets secondary Id for a new PlotView. 
	 * @return secondary Id.
	 */
	public static int getSecondaryPlotViewId() {
		return secondaryId++;
	}

	public static void resetPlotViewId(){
		secondaryId = 1;
	}
	
	public static Plot directOpenPlot(final PlotType initPlotType) throws PartInitException {
		final Plot[] plotComposite = new Plot[1];
		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
		PlotView plotView = null;
		try {
			plotView = (PlotView) workbenchPage.showView(
					PlotView.PLOT_VIEW_ID
					,
					"" + PlotManager.getSecondaryPlotViewId()
					, 
					IWorkbenchPage.VIEW_ACTIVATE
			);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		//init PlotComposite
		// [Tony] [2008-12-12]
		plotComposite[0] = plotView.getPlotComposite();
		plotComposite[0].init(initPlotType);
		//				plotComposite[0].setPlotType(initPlotType);

		plotViewMap.put(plotComposite[0].getId(),plotView);

		return plotComposite[0];
	}
	
	public static Plot openPlot(final PlotType initPlotType) throws PartInitException {
		
		return openPlot(initPlotType, 0);
	}

	public static Plot openPlot(final PlotType plotType, int plotViewId) throws PartInitException{
		IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
		
		return openPlot(plotType, workbenchWindow, plotViewId);
	}

	public static Plot openPlot(final PlotType initPlotType, final IWorkbenchWindow workbenchWindow, 
			final int plotViewId) throws PartInitException {
		final Plot[] plotComposite = new Plot[1];
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				//create new View
//				IWorkbenchWindow workbenchWindow = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
				final IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
				PlotView plotView = null;
				try {
					plotView = (PlotView) workbenchPage.showView(
							PlotView.PLOT_VIEW_ID
							,
							"" + (plotViewId > 0 ? plotViewId : PlotManager.getSecondaryPlotViewId())
							, 
							IWorkbenchPage.VIEW_ACTIVATE
					);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
				
				//init PlotComposite
				// [Tony] [2008-12-12]
				plotComposite[0] = plotView.getPlotComposite();
				plotComposite[0].init(initPlotType);
//				plotComposite[0].setPlotType(initPlotType);
				
				plotViewMap.put(plotComposite[0].getId(),plotView);
			}
		});
		
		return plotComposite[0];		
	}

	public static Plot openPlot(final PlotType initPlotType, final IWorkbenchWindow workbenchWindow) throws PartInitException {
		return openPlot(initPlotType, workbenchWindow, 0);
	}
		
	/**
	 * Plots data 
	 * @param algorithmTask
	 * @param operation
	 */
	public static void plotOperation(AlgorithmTask algorithmTask, final Operation operation, 
			final IWorkbenchWindow workbenchWindow) {
		final PlotDataReference plotDataReference = new PlotDataReference(
				algorithmTask.getId(), 
				operation.getID(), 
				algorithmTask.getSelectedDataItemIndex());
		final PlotDataItem plotDataItem = new PlotDataItem(	
				operation.getOutputData(), 
				plotDataReference, 
				operation.getDataType());
		
		final Plot plot;
		try {
			plot = openPlot(Plot.getDefaultPlotType(plotDataItem.getDataType()), workbenchWindow);
			plot.setRefreshingEnabled(false);
			plot.setOperaton(operation);
			fireOperationPlotOpened(plot);
		} catch (PartInitException ex) {
			handleException(ex);
			return;
		}
		final MultiPlotDataManager multiPlotDataManager = plot.getMultiPlotDataManager();
		try {
			multiPlotDataManager.addPlotDataItem(plotDataItem);
		} catch (PlotException e) {
			handleException(e);
		}
		plot.setRefreshingEnabled(true);
		plot.layout();
//		plot.redraw();
//		plot.refresh();
		plot.forceEnableMask();
		System.out.println("finished loading plot " + plot.getId());
//		DisplayManager.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				final Plot plot;
//				try {
//					plot = openPlot(Plot.getDefaultPlotType(plotDataItem.getDataType()), workbenchWindow);
//					plot.setRefreshingEnabled(false);
//					plot.setOperaton(operation);
//					fireOperationPlotOpened(plot);
//				} catch (PartInitException ex) {
//					handleException(ex);
//					return;
//				}
//				final MultiPlotDataManager multiPlotDataManager = plot.getMultiPlotDataManager();
//				try {
//					multiPlotDataManager.addPlotDataItem(plotDataItem);
//				} catch (PlotException e) {
//					handleException(e);
//				}
//				plot.layout();
////				plot.redraw();
//				plot.setRefreshingEnabled(true);
////				plot.refresh();
//				plot.forceEnableMask();
//				System.out.println("finished loading plot");
//			}
//		});
	}
	
	/**
	 * Gets operation referred by the PlotDataReference.
	 * @param plotDataReference a reference
	 * @return the Operation or null if the reference is broken.
	 */
	public static Operation getOperation(PlotDataReference plotDataReference) {
		if (plotDataReference != null) {
			final AlgorithmTask algorithmTask = ProjectManager.getAlgorithmTask(plotDataReference.getTaskId());
			if (algorithmTask != null) {
				final OperationManager operationManager = algorithmTask.getOperationManager(plotDataReference.getDataItemIndex());
				final Operation operation = operationManager.getOperation(plotDataReference.getOperationIndex());
				return operation;
			}
		}

		return null;
	}


	private static void handleException(Throwable throwable) {
		throwable.printStackTrace();
		showErrorMessage(throwable.getMessage());
	}

	private static void showErrorMessage(String message) {
		MessageDialog.openError(
			Display.getCurrent().getActiveShell(),
			"Algorithm Task",
			message);
	}

	public static interface OpenNewPlotListener{
		public void newPlotOpened(Plot plot);
	}
	
	public static void addOpenNewPlotListener(final OpenNewPlotListener listener){
		openPlotListeners.add(listener);
	}
	
	public static void removeOpenNewPlotListener(OpenNewPlotListener listener){
		openPlotListeners.remove(listener);
	}
	
	private static void fireOperationPlotOpened(Plot plot){
		for (OpenNewPlotListener listener : openPlotListeners){
			listener.newPlotOpened(plot);
		}
	}
}

