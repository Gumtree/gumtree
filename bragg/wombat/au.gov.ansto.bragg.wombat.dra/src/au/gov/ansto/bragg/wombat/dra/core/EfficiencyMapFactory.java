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
package au.gov.ansto.bragg.wombat.dra.core;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.math.EData;
import org.gumtree.data.math.EMath;
import org.gumtree.data.utils.Utilities;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.nbi.dra.correction.DetectorMode;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 20/02/2009
 */
public class EfficiencyMapFactory extends ConcreteProcessor {

	private URI efficiencyMapURI;
	private URI backgroundForEfficiencyMapURI;
	private Boolean efficiency_enable = true;
	private Boolean efficiency_stop = false;
	private String normalisationFactor = "detector_time";
	
	private final static double EFFICIENCY_LOWER_LIMIT = 0.2;
	private final static double EFFICIENCY_UPPER_LIMIT = 5;
	
	private boolean isMapSourceChanged = true;
//	private Array efficiencyMap;
	private EData<IArray> mapEdata;
	private List<IDataItem> efficiencyMapAxes;
	/**
	 * 
	 */
	public EfficiencyMapFactory() {
		super();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#process()
	 */
	@Override
	public Boolean process() throws Exception {
		if (!efficiency_enable || efficiencyMapURI == null)
			return efficiency_stop;
//		if (efficiency_inputPlot != null){
//			efficiency_outputPlot = efficiency_inputPlot;
//			return efficiency_stop;
//		}
		if (isMapSourceChanged){

			IGroup efficiencyData = null;
			String dictionaryPath = null;
			efficiencyMapAxes = new ArrayList<IDataItem>();
			try {
//				dictionaryPath = NexusUtils.getDictionaryPath();
//				efficiencyData = (IGroup) Utilities.findObject(efficiencyMapURI, dictionaryPath);
				efficiencyData = NexusUtils.getNexusEntry(efficiencyMapURI.getPath());
//				try{
//					efficiencyData = NexusUtils.getNexusEntryList(efficiencyData).get(0);
//					efficiencyData = NexusUtils.getNexusData(efficiencyData);
//				}catch (Exception e) {
//					if (efficiencyData.isRootGroup())
//						efficiencyData = efficiencyData.findGroup("data");
//				}
				if (efficiencyData.isRoot()){
					efficiencyData = (IGroup) efficiencyData.getGroupList().get(0);
				}
				DataStructureType type = Util.getDataStructureType(efficiencyData);
				switch (type) {
				case nexusEntry:
					efficiencyData = NexusUtils.getNexusData(efficiencyData);
					break;
				case nexusData:
					break;
				case plot:
					break;
				case plotset:
					efficiencyData = (IGroup) efficiencyData.getGroupList().get(0);
				default:
					break;
				}
				List<IDataItem> dataAxes = ((NcGroup) efficiencyData).findAxes();
				efficiencyMapAxes.add(dataAxes.get(dataAxes.size() - 2));
				efficiencyMapAxes.add(dataAxes.get(dataAxes.size() - 1));
				efficiencyMapAxes.get(0).setParent(null);
				efficiencyMapAxes.get(1).setParent(null);
			} catch (Exception e) {
				efficiencyData = NexusUtils.getNexusData(efficiencyData);
			}
			IArray efficiencyMap = overlap(efficiencyData);
			IArray efficiencyVariance = null;
			try {
				efficiencyVariance = efficiencyData.getDataItemWithAttribute("signal",
						StaticDefinition.DATA_VARIANCE_REFERENCE_NAME).getData();
			}catch (Exception e) {
			}
			if (backgroundForEfficiencyMapURI != null){
				IGroup backgroundData = null;
				if (backgroundForEfficiencyMapURI != null){
					try {
//						backgroundData = (IGroup) Utilities.findObject(backgroundForEfficiencyMapURI, dictionaryPath);
						backgroundData = NexusUtils.getNexusEntry(backgroundForEfficiencyMapURI);
//						backgroundData = NexusUtils.getNexusEntryList(backgroundData).get(0);
						backgroundData = NexusUtils.getNexusData(backgroundData);
					} catch (Exception e) {
						backgroundData = NexusUtils.getNexusData(backgroundForEfficiencyMapURI);
					}
				}
				IArray backgroundMap = overlap(backgroundData);
				DetectorMode mode = DetectorMode.getInstance(normalisationFactor);
				IArray efficiencyReading = getFactor(efficiencyData, mode);
				IArray backgroundReading = getFactor(backgroundData, mode);
				double fraction = efficiencyReading.getArrayMath().sum() / backgroundReading.getArrayMath().sum();
				efficiencyMap.getArrayMath().add(backgroundMap.getArrayMath().scale(- fraction));
			}
			if (efficiencyVariance != null)
				mapEdata = new EData<IArray>(efficiencyMap, efficiencyVariance);
			else{
				double average = efficiencyMap.getArrayMath().sum() / efficiencyMap.getSize();
				//			if (Math.abs(average) - 1 > 0.1)
				mapEdata = EMath.toScale(efficiencyMap, 1 / average, efficiencyMap, 0);
				//			if (Math.abs(average) - 1 > 0.1) 
				//				efficiencyMap = efficiencyMap.toScale(1 / average).eltInverseSkipZero();
				removeUnacceptableValue(mapEdata.getData(), mapEdata.getVariance());
			}
			isMapSourceChanged = false;
		}

		return efficiency_stop;
	}

	private void removeUnacceptableValue(IArray array, IArray variance){
		IArrayIterator iterator = array.getIterator();
		IArrayIterator varianceIterator = variance.getIterator();
		while(iterator.hasNext() && varianceIterator.hasNext()){
			double value = iterator.getDoubleNext();
			varianceIterator.next();
			if (value < EFFICIENCY_LOWER_LIMIT){
//				iterator.setDoubleCurrent(EFFICIENCY_LOWER_LIMIT);
				iterator.setDoubleCurrent(1);
				varianceIterator.setDoubleCurrent(0);
			}
			else if (value > EFFICIENCY_UPPER_LIMIT){
//				iterator.setDoubleCurrent(EFFICIENCY_UPPER_LIMIT);
				iterator.setDoubleCurrent(1);
				varianceIterator.setDoubleCurrent(0);
			}
		}
	}
	
	private IArray getFactor(IGroup backgroundData, DetectorMode mode) {
		IArray mapReading = null;
		if (mode == DetectorMode.TIME){
			try{
				mapReading = backgroundData.getDataItem("detector_time").getData();
			}catch (Exception e) {
				try{
					mapReading = backgroundData.getParentGroup().findGroup("instrument").
						findGroup("detector").findDataItem("time").getData();
				}catch (Exception e1) {}
			}
		}else if (mode == DetectorMode.DETECTOR_TOTAL){
			try {
				mapReading = ((NcGroup) backgroundData).getSignalArray().getArrayMath().sumForDimension(0, false).getArray();
			} catch (Exception e) {
			}
		}else if (mode == DetectorMode.MONITOR1){
			try{
				mapReading = backgroundData.getDataItem("monitor1_counts").getData();
			}catch (Exception e) {
				try{
					mapReading = backgroundData.getParentGroup().findGroup("monitor").
						findDataItem("bm1_counts").getData();
				}catch (Exception e1) {}
			}
		}else if (mode == DetectorMode.MONITOR2){
			try{
				mapReading = backgroundData.getDataItem("monitor2_counts").getData();
			}catch (Exception e) {
				try{
					mapReading = backgroundData.getParentGroup().findGroup("monitor").
						findDataItem("bm2_counts").getData();
				}catch (Exception e1) {}
			}
		}else if (mode == DetectorMode.MONITOR3){
			try{
				mapReading = backgroundData.getDataItem("monitor3_counts").getData();
			}catch (Exception e) {
				try{
					mapReading = backgroundData.getParentGroup().findGroup("monitor").
						findDataItem("bm3_counts").getData();
				}catch (Exception e1) {}
			}
		}
		return mapReading;
	}

	private IArray overlap(IGroup efficiencyData) throws SignalNotAvailableException, 
	ShapeNotMatchException, InvalidRangeException {
		IArray dataArray = ((NcGroup) efficiencyData).getSignalArray();
		int rank = dataArray.getRank();
		if (rank <= 2) 
			return dataArray;
		else{
			IArray resultArray = null;
			ISliceIterator sliceIterator = dataArray.getSliceIterator(2);
			while (sliceIterator.hasNext()){
				if (resultArray == null)
					resultArray = Utilities.copyToDoubleArray(sliceIterator.getArrayNext());
				else
					resultArray.getArrayMath().add(sliceIterator.getArrayNext());
			}
			return resultArray;
		}
	}

	
	/**
	 * @param backgroundForEfficiencyMapURI the backgroundForEfficiencyMapURI to set
	 */
	public void setBackgroundForEfficiencyMapURI(URI backgroundForEfficiencyMapURI) {
		if ((this.backgroundForEfficiencyMapURI == null && backgroundForEfficiencyMapURI != null)
				|| (this.backgroundForEfficiencyMapURI != null 
						&& backgroundForEfficiencyMapURI != this.backgroundForEfficiencyMapURI)){
			this.backgroundForEfficiencyMapURI = backgroundForEfficiencyMapURI;
			isMapSourceChanged = true;
		}
	}

	/**
	 * @param efficiencyMapURI the efficiencyMapURI to set
	 */
	public void setEfficiencyMapURI(URI efficiencyMapURI) {
		if ((this.efficiencyMapURI == null && efficiencyMapURI != null) 
				|| (this.efficiencyMapURI != null && this.efficiencyMapURI != efficiencyMapURI)){
			this.efficiencyMapURI = efficiencyMapURI;
			isMapSourceChanged = true;
		}
	}

	/**
	 * @param efficiencySkip the efficiencySkip to set
	 */
	public void setEfficiency_enable(Boolean efficiency_enable) {
		this.efficiency_enable = efficiency_enable;
	}

	/**
	 * @param efficiencyStop the efficiencyStop to set
	 */
	public void setEfficiency_stop(Boolean efficiency_stop) {
		this.efficiency_stop = efficiency_stop;
	}

//	/**
//	 * @return the efficiencyMap
//	 */
//	public Array getEfficiencyMap() {
//		return efficiencyMap;
//	}

	public IGroup getEfficiencyMapGroup() throws IOException, 
	InvalidArrayTypeException, PlotFactoryException{
		if (mapEdata == null)
			return null;
		IGroup entryGroup = NexusUtils.createNexusEntry(null, "entry1");
//		Group dataGroup = NexusUtils.createNexusDataGroup(entryGroup, "data", efficiencyMap, null);
//		Group dataGroup = NexusUtils.createnexusDataPlot(entryGroup, "data", DataDimensionType.map, 
//				mapEdata.getData(), mapEdata.getVariance(), efficiencyMapAxes);
		
		Plot dataGroup = PlotFactory.createPlot(entryGroup, "data", DataDimensionType.map);
		dataGroup.addData("hmm", mapEdata.getData(), "efficiency data", "", mapEdata.getVariance());
		if (efficiencyMapAxes != null && efficiencyMapAxes.size() > 0){
			int index = 0;
			for (IDataItem axis : efficiencyMapAxes){
				dataGroup.addAxis(axis.getShortName(), axis.getData(), axis.getShortName(), axis.getUnitsString(), index);
				index ++;
			}
		}
		return dataGroup;
	}

}