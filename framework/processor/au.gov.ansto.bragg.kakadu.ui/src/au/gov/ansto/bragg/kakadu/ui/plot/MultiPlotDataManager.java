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
package au.gov.ansto.bragg.kakadu.ui.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.gdm.dataset.ArraySeries;
import org.gumtree.vis.gdm.dataset.Hist2DDataset;
import org.gumtree.vis.interfaces.IPlot;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.plot1d.MarkerShape;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import au.gov.ansto.bragg.cicada.core.exception.FailedToExecuteException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.cicada.core.exception.TransferFailedException;
import au.gov.ansto.bragg.cicada.core.exception.TunerNotReadyException;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.kakadu.core.AlgorithmTask;
import au.gov.ansto.bragg.kakadu.core.OperationManager;
import au.gov.ansto.bragg.kakadu.core.Util;
import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.kakadu.core.data.Operation;
import au.gov.ansto.bragg.kakadu.core.data.OperationDataListener;
import au.gov.ansto.bragg.kakadu.core.data.OperationStatus;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;

/**
 * The class is used for managing of multiple plot data items.
 *  
 * @author Danil Klimontov (dak)
 */
public class MultiPlotDataManager {
	
	private Plot plot;
	/**<plotDataItemId,PlotDataItem>*/
	private final Map<Integer, PlotDataItem> plotDataItems = new LinkedHashMap<Integer, PlotDataItem>();
	private Map<PlotDataItem, OperationDataListener> operationDataListeners = new HashMap<PlotDataItem, OperationDataListener>();
	private final List<MultiPlotDataListener> multiPlotDataListeners = new ArrayList<MultiPlotDataListener>();
	private int startColorIndex = 0;
	/**
	 * Stores last selected single visible PlotDataItem.
	 * Single visible item mode is used for Intensity plot for Map and MapSet data types.
	 */
	private PlotDataItem singleVisiblePlotDataItem;
	
	
	public MultiPlotDataManager(Plot plot) {
		this.plot = plot;
	}

	public void addPlotDataItem(PlotDataReference plotDataReference) throws PlotException {
		final AlgorithmTask algorithmTask = ProjectManager.getAlgorithmTask(plotDataReference.getTaskId());
		if (algorithmTask != null) {
			final OperationManager operationManager = algorithmTask.getOperationManager(plotDataReference.getDataItemIndex());
			final Operation operation = operationManager.getOperation(plotDataReference.getOperationIndex());
			final IGroup outputData = (IGroup)operation.getOutputData();
			addPlotDataItem(new PlotDataItem(outputData, plotDataReference, operation.getDataType()));
		} else {
			throw new PlotException("AlgorithmTask assosiated with PlotDataReference not found.");
		}
	}
	

	/**
	 * Adds PlotDataItem to the list of available PlotDataItems to be displayed in one plot.
	 * @param plotDataItem PlotDataItem to be added.
	 * @throws PlotException can be thrown if Data type of added item not computable with existed items.
	 */
	public void addPlotDataItem(PlotDataItem plotDataItem) throws PlotException {
		
		if (plotDataItems.get(plotDataItem.getId()) != null) {
			throw new PlotException("The PlotDataItem already exists in the plot.");
		}
		
		if (plotDataItem.getDataType().equals(DataType.Undefined)) {
			
			final PlotDataReference plotDataReference = plotDataItem.getPlotDataReference();
			if (plotDataReference != null) {
				final AlgorithmTask algorithmTask = ProjectManager.getAlgorithmTask(plotDataReference.getTaskId());
				if (algorithmTask != null) {
					plotDataItem.setDataType(algorithmTask.getOperationDataType(plotDataReference.getOperationIndex()));
				}
			}
			
			if (plotDataItem.getDataType().equals(DataType.Undefined)) {
				throw new PlotException("The PlotDataItem has Undefined data type.");
			}
		}
		
//		plotDataItem.setColor(getNextColor());
//		if (plotDataItem.getDataType().equals(DataType.Pattern)) {
//			for (PlotDataItem dataItem : plotDataItems.values()){
//				try{
//					//				System.out.println("change color");
//					if (plotDataItem.getColor().equals(dataItem.getColor()) && plotDataItem != dataItem){
//						plotDataItem.setColor(getNextColor());
//					}
//				}catch (Exception e) {
//				}
//			}
//		}
		//set plot type dependent to added data type.
		if (plotDataItems.size() == 0) {
			//the first element is going to be added
			//we need define default data type and plot type
			final DataType dataType = plotDataItem.getDataType();
//			final DataType dataType = Plot.getDataType(plotDataItem.getData());
			plot.setCurrentDataType(Util.getChildDataType(dataType));
			final PlotType defaultPlotType = Plot.getDefaultPlotType(dataType);
			plot.setPlotType(defaultPlotType);
			
		} else {
			//validate new PlotDataItem to be compatible with the first one.
			final DataType dataType = plotDataItem.getDataType();
//			final DataType dataType = Plot.getDataType(plotDataItem.getData());
			if (plot.getCurrentDataType() == DataType.Undefined) {
				//for the case if first item was with Undefined type
				plot.setCurrentDataType(Util.getChildDataType(dataType));
				final PlotType defaultPlotType = Plot.getDefaultPlotType(dataType);
				plot.setPlotType(defaultPlotType);
			}
			if (dataType != DataType.Undefined 
//						&& (plot.getCurrentDataType()==DataType.Map && dataType!=DataType.MapSet)
//						&& (plot.getCurrentDataType()==DataType.Pattern && dataType!=DataType.PatternSet)
						&& plot.getCurrentDataType() != dataType 
						&& plot.getCurrentDataType() != Util.getChildDataType(dataType)
							) {
				throw new PlotException("Data type of added item (" + dataType 
						+ ") not compatible with existed items (" +
						plot.getCurrentDataType() + ").");
			}
			
			
		}
		//add the item to internal cache
		plotDataItems.put(plotDataItem.getId(), plotDataItem);
		plotDataItem.setPlotId(plot.getId());
		
		switch (plotDataItem.getDataType()) {
		case Pattern:
//			plotDataItem.setColor(plot.getDisplay().getSystemColor(SWT.COLOR_BLUE).getRGB());
//			dataSourceWrapper.setColor(plot.getDisplay().getSystemColor(SWT.COLOR_BLUE).getRGB());
			try {
				addVisualisationItem(plotDataItem);
			} catch (Exception e) {
				throw new PlotException(e.getMessage(),e);
			}
			break;
		case Map:
			//add data object to kuranda widget
			try {
				addVisualisationItem(plotDataItem);
			} catch (Exception e) {
				throw new PlotException(e.getMessage(),e);
			}
			break;
		case PatternSet:
		case MapSet:
			addChildItems(plotDataItem);
			break;

		default:
			break;
		}
		
		//establish linkage with algorithm task if PlotDataItem is marked as linked  
		setLinked(plotDataItem, plotDataItem.isLinked());
		
		fireItemAddedEvent(plotDataItem);
	}

	/**
	 * Parses data object of the plot data item and 
	 * adds child items to the item if here is Set type of data.  
	 * @param plotDataItem item to be processed.
	 * @throws PlotException
	 */
	private void addChildItems(PlotDataItem plotDataItem) throws PlotException {
		final IGroup plotData = plotDataItem.getData();
		if (plotData == null) {
			//data not ready yet. Child items will be added in time of update.
			return;
		}
		try {
			final List<IGroup> subGroups = Util.getSubGroups(plotData);
			for (IGroup group : subGroups) {
				final PlotDataItem childPlotDataItem = new PlotDataItem(group, Util.getChildDataType(plotDataItem.getDataType()));
				childPlotDataItem.setParent(plotDataItem);
				plotDataItem.addChild(childPlotDataItem);
				
				addPlotDataItem(childPlotDataItem);
			}
		} catch (StructureTypeException e) {
			plot.handleException(e);
		} catch (PlotFactoryException e) {
			plot.handleException(e);
		}
	}

	/**
	 * Gets the list of PlotDataItems marked as visible and do not have any children.
	 * So the items can be visualized.
	 * @return list of PlotDataItem objects
	 */
	public List<PlotDataItem> getDisplayablePlotDataItems() {
		final ArrayList<PlotDataItem> result = new ArrayList<PlotDataItem>();
		for (PlotDataItem plotDataItem : plotDataItems.values()) {
			if (plotDataItem.isVisible() && plotDataItem.getChildrenCount() == 0) {
				result.add(plotDataItem);
			}
		}
		return result;
	}

	/**
	 * Adds PlotDataItem to visualization widget.
	 * 
	 * @param plotDataItem an item to be visualized
	 * @throws KurandaException 
	 */
	private void addVisualisationItem(PlotDataItem plotDataItem) throws Exception {
		final IPlot currentPlotWidget = plot.getCurrentPlotWidget();
		
		XYDataset dataset = currentPlotWidget.getDataset();
		IGroup group = plotDataItem.getData();
//		plotDataItem.setKurandaPlotDataId(PlotDataItem.getNextID());
		
//		final int kurandaPlotDataId = currentPlotWidget.getMultiPlotManager().addDataSource(plotDataItem.getData());
		PlotType type = plot.getCurrentPlotType();
		switch (type) {
		case OffsetPlot:
		case OverlayPlot:
//			Group dataGroup = plotDataItem.getData();
			ArraySeries series = au.gov.ansto.bragg.kakadu.ui.util.Util.createSeries(
					group);
			((IXYErrorDataset) currentPlotWidget.getDataset()).addSeries(series);
			plotDataItem.setPlotData(series);
			break;
		case IntensityPlot:
			Hist2DDataset dataset2D = au.gov.ansto.bragg.kakadu.ui.util.Util.create2DDataset(
					group);
			dataset2D.setTitle(group.getShortName());
			currentPlotWidget.setDataset(dataset2D);
			plotDataItem.setPlotData(dataset2D);
		default:
			break;
		}
//		plot.getViewPlotPropertiesComposite().updateUI();
//		plotDataItem.setKurandaPlotDataId(kurandaPlotDataId);
//		final AbstractDataSource dataSourceWrapper = currentPlotWidget.getMultiPlotManager().
//		getDataSourceWrapper(kurandaPlotDataId);
//		dataSourceWrapper.setVisible(plotDataItem.isVisible());
////			plotDataItem.setVisible(dataSourceWrapper.isVisible());
//		if (plotDataItem.getColor() == null) {
//			plotDataItem.setColor(dataSourceWrapper.getColor());
//		} else {
//			dataSourceWrapper.setColor(plotDataItem.getColor());
//		}
		currentPlotWidget.updatePlot();
	}
	
	public List<PlotDataItem> getPlotDataItems() {
		return new ArrayList<PlotDataItem>(plotDataItems.values());
	}
	
	public void removePlotDataItem(PlotDataItem plotDataItem) {

		setLinked(plotDataItem, false);
		plotDataItems.remove(plotDataItem.getId());
		PlotType type = plot.getCurrentPlotType();
		switch (type) {
		case OffsetPlot:
		case OverlayPlot:
			IXYErrorDataset dataset1D = (IXYErrorDataset) plot.getCurrentPlotWidget().getDataset();
			List<IXYErrorSeries> seriesList = dataset1D.getSeries();
			for (IXYErrorSeries series : seriesList) {
				if (series == plotDataItem.getPlotData()) {
					int seriesIndex = seriesList.indexOf(series);
					dataset1D.removeSeries(series);
					XYItemRenderer renderer = plot.getCurrentPlotWidget().getXYPlot().getRenderer();
					if (renderer instanceof AbstractRenderer) {
						((AbstractRenderer) renderer).removeSeries(seriesIndex);
					}
				}
			}
			break;
		case IntensityPlot:
			IGroup group = plotDataItem.getData();
			Hist2DDataset dataset2D = (Hist2DDataset) plot.getCurrentPlotWidget().getDataset();
			if (group.getShortName().equals(dataset2D.getTitle())) {
				plot.getCurrentPlotWidget().setDataset(new Hist2DDataset());
			}
			break;
		default:
			break;
		}
		try {
			plot.getCurrentPlotWidget().updatePlot();
		} catch (Exception e) {
			plot.handleException(e);
		}
		fireItemRemovedEvent(plotDataItem);
		
		for (PlotDataItem pItem : plotDataItem.getChildren()) {
			removePlotDataItem(pItem);
		}
	}
	
	public void removeAllPlotDataItems() {
		//remove OperationDataListeners from linked operations
		for (PlotDataItem plotDataItem : plotDataItems.values()) {
			setLinked(plotDataItem, false);
			fireItemRemovedEvent(plotDataItem);
		}
		plotDataItems.clear();
	}
	
	public PlotDataItem getPlotDataItem(int plotDataItemId) {
		return plotDataItems.get(plotDataItemId);
	}

	public PlotDataItem getPlotDataItem(int algorithmTaskId, int dataItemIndex, int operationIndex) {
		for (PlotDataItem plotDataItem : plotDataItems.values()) {
			final PlotDataReference plotDataReference = plotDataItem.getPlotDataReference();
			if (plotDataReference != null 
					&& plotDataReference.getTaskId() == algorithmTaskId
					&& plotDataReference.getDataItemIndex() == dataItemIndex
					&& plotDataReference.getOperationIndex() == operationIndex) {
				return plotDataItem;
			}
		}
		return null;
	}
	
//	public void setMarker(final PlotDataItem plotDataItem, boolean isMarker) {
//		plotDataItem.setMarker(isMarker);
//		final AbstractDataSource dataSourceWrapper = plot.getCurrentPlotWidget().getMultiPlotManager().
//						getDataSourceWrapper(plotDataItem.getKurandaPlotDataId());
//		if (dataSourceWrapper != null) {
//			dataSourceWrapper.setLineVisible(!isMarker);
//			// TODO: Choose marker
//			dataSourceWrapper.setMarkerShape(isMarker ? MarkerShape.DEFAULT : MarkerShape.NONE);
//			try {
//				plot.getCurrentPlotWidget().refreshPlot();
//			} catch (KurandaException e) {
//				plot.handleException(e);
//			}
//		}
//		fireItemUpdatedEvent(plotDataItem);
//	}

	public void setMarker(final PlotDataItem plotDataItem, MarkerShape markerShape) {
//		boolean isMarker = markerShape != MarkerShape.NONE;
//		plotDataItem.setMarkerShape(markerShape);
//		final AbstractDataSource dataSourceWrapper = plot.getCurrentPlotWidget().getMultiPlotManager().
//						getDataSourceWrapper(plotDataItem.getKurandaPlotDataId());
//		if (dataSourceWrapper != null) {
//			dataSourceWrapper.setLineVisible(markerShape.doesShowLine());
//			// TODO: Choose marker
//			dataSourceWrapper.setMarkerShape(isMarker ? markerShape : MarkerShape.NONE);
//			try {
//				plot.getCurrentPlotWidget().refreshPlot();
//			} catch (KurandaException e) {
//				plot.handleException(e);
//			}
//		}
//		fireItemUpdatedEvent(plotDataItem);
	}

	public void initMarkerShape(final PlotDataItem plotDataItem, MarkerShape markerShape) {
//		boolean isMarker = markerShape != MarkerShape.NONE;
//		plotDataItem.setMarkerShape(markerShape);
//		final AbstractDataSource dataSourceWrapper = plot.getCurrentPlotWidget().getMultiPlotManager().
//						getDataSourceWrapper(plotDataItem.getKurandaPlotDataId());
//		if (dataSourceWrapper != null) {
//			dataSourceWrapper.setLineVisible(markerShape.doesShowLine());
//			// TODO: Choose marker
//			dataSourceWrapper.setMarkerShape(isMarker ? markerShape : MarkerShape.NONE);
//		}
	}
	
	/**
	 * Establishes link for PlotDataItem and current AlgorithmTask.
	 * PlotDataReference for PlotDataItem should contain a valid reference to AlgorithmTask, DataItem and Operation.
	 * @param plotDataItem PlotDataItem to be linked.
	 * @param isLinked true if the PlotDataItem should be linked or false otherwise. 
	 */
	public void setLinked(final PlotDataItem plotDataItem, boolean isLinked) {
		
		boolean isCurrentryLinkedByTheManager = operationDataListeners.get(plotDataItem) != null;
		if (isCurrentryLinkedByTheManager == isLinked) {
			if (plotDataItem != null)
				plotDataItem.setLinked(isLinked);
			return;
		}
//		if (plotDataItem.isLinked() == isLinked) {
//			return;
//		}
		
		
		if (isLinked) {
			//validate PlotDataReference. Check is AlgorithmTask still live.
			final PlotDataReference plotDataReference = plotDataItem.getPlotDataReference();
			if (plotDataReference != null) {
				final AlgorithmTask algorithmTask = ProjectManager.getAlgorithmTask(plotDataReference.getTaskId());
				if (algorithmTask != null) {
					final OperationManager operationManager = algorithmTask.getOperationManager(plotDataReference.getDataItemIndex());
					final Operation operation = operationManager.getOperation(plotDataReference.getOperationIndex());
				
					
					//subscribe for operation data updates
					final OperationDataListener operationDataListener = new OperationDataListener(){
						public void outputDataUpdated(Operation operation,
								IGroup oldData, final IGroup newData) {
							if (plot != null && !plot.isDisposed())
								DisplayManager.getDefault().asyncExec(new Runnable() {
									public void run() {
										updatePlotData(plotDataItem, newData);
									}
								});
						}
					};
					operation.addOperationDataListener(operationDataListener);
					operationDataListeners.put(plotDataItem, operationDataListener);
					
					if (operation.isActual() && operation.getStatus() == OperationStatus.Done) {
						updatePlotData(plotDataItem, (IGroup) operation.getOutputData());
					} else {
						//data not ready. Calculations must be run
						//data will be handled by OperationDataListener
						try {
							algorithmTask.runAlgorithmForOperation(operation.getName(), plotDataReference.getDataItemIndex());
						} catch (NoneAlgorithmException e) {
							plot.handleException(e);
						} catch (FailedToExecuteException e) {
							plot.handleException(e);
						} catch (TunerNotReadyException e) {
							plot.handleException(e);
						} catch (TransferFailedException e) {
							plot.handleException(e);
						}
						
					}
					plotDataItem.setLinked(true);
			
				} else {
					//link not possible because PlotDataReference contains not valid data
					//For example AlgorithmTask has been removed.
					
					plot.handleException(new Exception("Linking failed. Algorithm Task '" + plotDataReference.getTaskId() + "' not found."));
					plotDataItem.setLinked(false);
					plotDataItem.setLinkEnabled(false);
				}
			} else {
				//link not possible due to absence of PlotDataReference specified for PlotDataItem
				plotDataItem.setLinked(false);
			}
		} else {
			//unsubscribe from data updates.
			final OperationDataListener operationDataListener = operationDataListeners.remove(plotDataItem);
			if (operationDataListener != null) {
				final PlotDataReference plotDataReference = plotDataItem.getPlotDataReference();
				if (plotDataReference != null) {
					final AlgorithmTask algorithmTask = ProjectManager.getAlgorithmTask(plotDataReference.getTaskId());
					if (algorithmTask != null) {
						final OperationManager operationManager = algorithmTask.getOperationManager(plotDataReference.getDataItemIndex());
						final Operation operation = operationManager.getOperation(plotDataReference.getOperationIndex());
						operation.removeOperationDataListener(operationDataListener);
					}
				}
			}
			plotDataItem.setLinked(false);
		}

		fireItemUpdatedEvent(plotDataItem);
	}
	
	/**
	 * Manages plot the item visible state and visualization widget update.
	 *  
	 * @param plotDataItem an item
	 * @param isVisible true if visible or false otherwise
	 */
	public void setPlotDataItemVisible(PlotDataItem plotDataItem, final boolean isVisible) {
		plotDataItem.setVisible(isVisible);
//		final AbstractDataSource dataSourceWrapper = plot.getCurrentPlotWidget().getMultiPlotManager().
//					getDataSourceWrapper(plotDataItem.getKurandaPlotDataId());
//		if (dataSourceWrapper != null) {
//			dataSourceWrapper.setVisible(isVisible);
//			try {
//				plot.getCurrentPlotWidget().repaint();
//			} catch (Exception e) {
//				plot.handleException(e);
//			}
//		}
		PlotType type = plot.getCurrentPlotType();
		switch (type) {
		case OffsetPlot:
		case OverlayPlot:
			IXYErrorDataset dataset1D = (IXYErrorDataset) plot.getCurrentPlotWidget().getDataset();
			List<IXYErrorSeries> seriesList = dataset1D.getSeries();
			for (IXYErrorSeries series : seriesList) {
				if (series == plotDataItem.getPlotData()) {
					XYItemRenderer renderer = plot.getCurrentPlotWidget().getXYPlot().getRenderer();
					((XYLineAndShapeRenderer) renderer).setSeriesShapesFilled(
	    					seriesList.indexOf(series), isVisible);
				}
			}
			break;
		case IntensityPlot:
			IGroup group = plotDataItem.getData();
			Hist2DDataset dataset2D = (Hist2DDataset) plot.getCurrentPlotWidget().getDataset();
			if (group.getShortName().equals(dataset2D.getTitle())) {
				plot.getCurrentPlotWidget().setVisible(isVisible);
			}
			break;
		default:
			break;
		}
		if ((plotDataItem.getDataType() == DataType.Map || plotDataItem.getDataType() == DataType.MapSet)
				&& plot.getCurrentPlotType() == PlotType.IntensityPlot) {
			plot.getViewPlotPropertiesComposite().updateUI();
		}
		fireItemUpdatedEvent(plotDataItem);
	}
	
	/**
	 * Sets the PlotDataItem only visible on the plot.
	 * Single visible item mode is used for Intensity plot for Map and MapSet data types.
	 * @param plotDataItem an item to be visible or null if multi mode should apply. 
	 */
	public void setSingleItemVisible(PlotDataItem plotDataItem) {
		
		this.singleVisiblePlotDataItem = plotDataItem;
		
//		for (PlotDataItem pItem : getDisplayablePlotDataItems()) {
//			final AbstractDataSource dataSourceWrapper = plot
//					.getCurrentPlotWidget().getMultiPlotManager()
//					.getDataSourceWrapper(pItem.getKurandaPlotDataId());
//			if (dataSourceWrapper != null) {
//				dataSourceWrapper.setVisible(plotDataItem == null || pItem == plotDataItem);
//			}
//		}
		PlotType type = plot.getCurrentPlotType();
//		switch (type) {
//		case OffsetPlot:
//		case OverlayPlot:
//			IXYErrorDataset dataset1D = (IXYErrorDataset) plot.getCurrentPlotWidget().getDataset();
//			List<IXYErrorSeries> seriesList = dataset1D.getSeries();
//			for (IXYErrorSeries series : seriesList) {
//				if (series.getKey().equals(plotDataItem.getData().getShortName())) {
//					XYItemRenderer renderer = plot.getCurrentPlotWidget().getXYPlot().getRenderer();
//					((XYLineAndShapeRenderer) renderer).setSeriesShapesFilled(
//	    					seriesList.indexOf(series), plotDataItem == null || pItem == plotDataItem);
//				}
//			}
//			break;
//		case IntensityPlot:
//			Group group = plotDataItem.getData();
//			Hist2DDataset dataset2D = (Hist2DDataset) plot.getCurrentPlotWidget().getDataset();
//			if (group.getShortName().equals(dataset2D.getTitle())) {
//				plot.getCurrentPlotWidget().setVisible(plotDataItem == null || pItem == plotDataItem);
//			}
//		default:
//			break;
//		}
		
		plot.getCurrentPlotWidget().updatePlot();
//		DisplayManager.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				try {
//					plot.getCurrentPlotWidget().refreshPlot();
//				} catch (KurandaException e) {
//					plot.handleException(e);
//				}
//			}
//		});
	}
	
	/**
	 * Gets last selected single visible PlotDataItem.
	 * Single visible item mode is used for Intensity plot for Map and MapSet data types.
	 * @return last item or null if no selected.
	 */
	public PlotDataItem getSingleVisiblePlotDataItem() {
		return singleVisiblePlotDataItem;
	}

	public void updatePlotDataContents(final PlotDataItem plotDataItem, final IGroup groupData){
		DisplayManager.getDefault().asyncExec(new Runnable() {
			public void run() {
				updatePlotData(plotDataItem, groupData);
			}
		});
	}

	private void updatePlotData(final PlotDataItem plotDataItem, final IGroup groupData) {
		if (plot == null) {
			return;
		}
		if (groupData.getShortName().equals("emptyData")){
			if (plot.isVisible())
				plot.setVisible(false);
			return;
		}
		if (!plot.isVisible())
			plot.setVisible(true);
		final IPlot currentPlotWidget = plot.getCurrentPlotWidget();
		switch (plotDataItem.getDataType()) {
		case Pattern:
			plotDataItem.setData(groupData);
			try {
				Util.checkData(groupData);
				IXYErrorDataset dataset1D = (IXYErrorDataset) currentPlotWidget.getDataset();
				List<IXYErrorSeries> seriesList = dataset1D.getSeries();
				for (IXYErrorSeries series : seriesList) {
					if (series == plotDataItem.getPlotData()) {
						if (series instanceof ArraySeries) {
							au.gov.ansto.bragg.kakadu.ui.util.Util.updateSeries(
									(ArraySeries) series, groupData);
							((IXYErrorDataset) currentPlotWidget.getDataset()).update(series);
						} else {
							ArraySeries newSeries = au.gov.ansto.bragg.kakadu.ui.util.Util.createSeries(
									groupData);
							plotDataItem.setPlotData(newSeries);
							((IXYErrorDataset) currentPlotWidget.getDataset()).removeSeries(series);
							((IXYErrorDataset) currentPlotWidget.getDataset()).addSeries(newSeries);
						}
						break;
					}
				}
//				currentPlotWidget.updatePlot();
			} catch (StructureTypeException e1) {
				plot.handleException(e1);
			}
			break;
		case Map:
			//update holder with new data
			plotDataItem.setData(groupData);
			
			//update visualisation
			try {
				Hist2DDataset dataset2D = au.gov.ansto.bragg.kakadu.ui.util.Util.create2DDataset(
						groupData);
				dataset2D.setTitle(groupData.getShortName());
				currentPlotWidget.setDataset(dataset2D);
//				currentPlotWidget.getMultiPlotManager().updateDataSource(
//						plotDataItem.getKurandaPlotDataId(), groupData);
				
				currentPlotWidget.updatePlot();
//				plot.getViewPlotPropertiesComposite().updateUI();
			} catch (Exception e) {
				plot.handleException(e);
			}
			break;
		case PatternSet:
		case MapSet:
			//update holder with new data
			plotDataItem.setData(groupData);

			if (plotDataItem.getChildrenCount() > 0) {
				//child items was already created
				try {
					final List<IGroup> subGroups = Util.getSubGroups(groupData);
					final List<PlotDataItem> children = plotDataItem.getChildren();
					for (int i = 0; i < subGroups.size(); i++) {
						updatePlotData(children.get(i), subGroups.get(i));
					}
				} catch (StructureTypeException e) {
					plot.handleException(e);
				} catch (PlotFactoryException e) {
					plot.handleException(e);
				}
			} else {
				//previously data was not ready. 
				//child items must be created now
				try {
					addChildItems(plotDataItem);
				} catch (PlotException e) {
					plot.handleException(e);
				}
			}
			break;
		default:
			break;
		}

		fireItemUpdatedEvent(plotDataItem);
	}

	public void disposeResources() {
		//TODO check whether we need to dispose smth
//		removeAllPlotDataItems();
		plotDataItems.clear();
		multiPlotDataListeners.clear();
		operationDataListeners.clear();
		singleVisiblePlotDataItem = null;
		plot = null;
	}
	
	public void loadDataItem(PlotDataItem plotDataItem, int dataItemIndex) {
		setLinked(plotDataItem, false);
		plotDataItem.getPlotDataReference().setDataItemIndex(dataItemIndex);
		setLinked(plotDataItem, true);

		// update UI
//		plot.getMultiPlotPropertiesComposite().updateUI();
	}

	
	public List<PlotDataItem> getChildren(PlotDataItem parent) {
		final ArrayList<PlotDataItem> result = new ArrayList<PlotDataItem>();
		
		for (PlotDataItem plotDataItem : plotDataItems.values()) {
			if (plotDataItem.getParent() == parent) {
				result.add(plotDataItem);
			}
		}
		return result;
	}
	
	
	
	public void addMultiPlotDataListener(MultiPlotDataListener multiPlotDataListener) {
		multiPlotDataListeners.add(multiPlotDataListener);
	}
	
	public void removeMultiPlotDataListener(MultiPlotDataListener multiPlotDataListener) {
		multiPlotDataListeners.remove(multiPlotDataListener);
	}
	
	public void removeAllMultiPlotDataListeners() {
		multiPlotDataListeners.clear();
	}
	
	public ArrayList<MultiPlotDataListener> getMultiPlotDataListeners() {
		return new ArrayList<MultiPlotDataListener>(multiPlotDataListeners);
	}
	
	protected void fireItemAddedEvent(PlotDataItem plotDataItem) {
		for (MultiPlotDataListener multiPlotDataListener : getMultiPlotDataListeners() ) {
			multiPlotDataListener.itemAdded(plotDataItem);			
		}
	}

	protected void fireItemRemovedEvent(PlotDataItem plotDataItem) {
		for (MultiPlotDataListener multiPlotDataListener : getMultiPlotDataListeners() ) {
			multiPlotDataListener.itemRemoved(plotDataItem);			
		}
	}

	void fireItemUpdatedEvent(PlotDataItem plotDataItem) {
		for (MultiPlotDataListener multiPlotDataListener : getMultiPlotDataListeners() ) {
			multiPlotDataListener.itemUpdated(plotDataItem);
		}
	}

	public void setStartColorIndex(int index){
		startColorIndex = index;
	}
	
//	private RGB getNextColor(){
////		currentColor = AbstractDataSource.getNextColor(currentColor);
//		RGB[] colorQueue = AbstractDataSource.generateRainbow(AbstractDataSource.COLOR_BINS);
//		for (int i = startColorIndex; i < startColorIndex + colorQueue.length; i ++){
//			int currentIndex = i;
//			if (currentIndex >= colorQueue.length){
//				currentIndex = (int) Math.IEEEremainder(currentIndex, colorQueue.length);
//			}
//			RGB color = colorQueue[currentIndex];
//			boolean isFound = false;
//			for (PlotDataItem dataItem : getPlotDataItems()){
//				if (color.equals(dataItem.getColor())){
//					isFound = true;
//					break;
//				}
//			}
//			if (!isFound)
//				return color;
//		}
//		return colorQueue[startColorIndex];
//	}
}
