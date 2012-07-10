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
package au.gov.ansto.bragg.kakadu.dom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.vis.interfaces.IPlot;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.kakadu.core.data.DataType;
import au.gov.ansto.bragg.kakadu.ui.plot.Plot;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotDataItem;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotException;
import au.gov.ansto.bragg.kakadu.ui.plot.PlotManager;

/**
 * @author nxi
 * Created on 27/01/2009
 */
public class PlotDOM {

	private Plot plot;
	
//	public PlotDOM(Composite parent){
//		super();
//		createNewPlot(parent);
//	}
//	
	public PlotDOM(){
		super();
	}
	
	public PlotDOM(DataType dataType) throws PartInitException{
		super();
		plot = PlotManager.openPlot(Plot.getDefaultPlotType(dataType));
	}
	
	public PlotDOM(Composite parent, DataType dataType){
		super();
		createNewPlot(parent, dataType);
	}
	
	public static PlotDOM getPlotDOM(Composite parent, DataType dataType){
		return new PlotDOM(parent, dataType);
	}
	
	public void createNewPlot(Composite parent){
		plot = new Plot(parent, SWT.NONE);
		parent.redraw();
	}
	
	public void setPlot(Plot plot){
		this.plot = plot;
	}
	
	public Plot getPlot(){
		return plot;
	}
	
	public void createNewPlot(Composite parent, DataType dataType){
		createNewPlot(parent);
		plot.init(Plot.getDefaultPlotType(dataType));
	}
	
	public void plot(final PlotDataItem plotDataItem, final IGroup groupData) throws PlotException{
		plot.getDisplay().asyncExec(new Runnable(){

			public void run() {
				// TODO Auto-generated method stub
				int idx = plot.getMultiPlotDataManager().getPlotDataItems().indexOf(plotDataItem);
				if (idx < 0){
					try {
						plot.getMultiPlotDataManager().addPlotDataItem(plotDataItem);
					} catch (PlotException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
					plot.getMultiPlotDataManager().updatePlotDataContents(plotDataItem, groupData);
			}});
	}
	
	public void addMarker(double horizontalValue, double verticalValue) throws PlotDOMException{
		IPlot kuranda = plot.getCurrentPlotWidget();
		try {
//			kuranda.addMarker(horizontalValue, verticalValue);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PlotDOMException(e);
		}
	}
	
	public boolean isDisposed(){
		return plot.isDisposed();
	}

	public void setTitle(final String title) {
		// TODO Auto-generated method stub
		plot.setForceTitle(title);
		Display.getDefault().asyncExec(new Runnable(){

			public void run() {	
				plot.updateTitle();
			}
		});
	}
	
//	public void redraw(){
//		plot.
//		plot.redraw();
//	}
	
	public static PlotDOM plot(final au.gov.ansto.bragg.datastructures.core.plot.Plot plotData) 
	throws PartInitException, PlotException{
		final DataType dataType = getDataType(plotData);
		final PlotDOM plot = new PlotDOM();
		Display.getDefault().asyncExec(new Runnable(){

			public void run() {
				// TODO Auto-generated method stub
				try{
					Plot kakaduPlot = PlotManager.directOpenPlot(Plot.getDefaultPlotType(dataType));
					plot.setPlot(kakaduPlot);
//					Thread.currentThread().sleep(1000);
					PlotDataItem dataItem = new PlotDataItem(plotData, dataType);
//					plot.plot(dataItem, plotData);
					int idx = kakaduPlot.getMultiPlotDataManager().getPlotDataItems().indexOf(dataItem);
					if (idx < 0){
						try {
							kakaduPlot.getMultiPlotDataManager().addPlotDataItem(dataItem);
						} catch (PlotException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
						kakaduPlot.getMultiPlotDataManager().updatePlotDataContents(dataItem, plotData);
					kakaduPlot.layout();
					kakaduPlot.redraw();
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}});
		while(plot.getPlot() == null){
			try {
				Thread.currentThread().sleep(200);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return plot;
	}
	
	public static PlotDOM plot(Composite parent, au.gov.ansto.bragg.datastructures.core.plot.Plot plotData) 
	throws PlotException{
		DataType dataType = getDataType(plotData);
		PlotDOM plot = getPlotDOM(parent, dataType);
		PlotDataItem dataItem = new PlotDataItem(plotData, dataType);
		plot.plot(dataItem, plotData);
		return plot;
	}
	
	public static DataType getDataType(au.gov.ansto.bragg.datastructures.core.plot.Plot plotData){
		DataDimensionType dimensionType = plotData.getDimensionType();
		DataType dataType = null;
		switch (dimensionType) {
		case pattern:
			dataType = DataType.Pattern;
			break;
		case patternset: 
			dataType = DataType.PatternSet;
			break;
		case map:
			dataType = DataType.Map;
			break;
		case mapset:
			dataType = DataType.MapSet;
			break;
		case volume:
			dataType = DataType.Volume;
			break;
		case volumeset:
			dataType = DataType.VolumeSet;
		default:
			dataType = DataType.Undefined;
			break;
		}
		return dataType;
	}

	public void dispose() {
		if (plot != null)
			plot.dispose();
	}
}
