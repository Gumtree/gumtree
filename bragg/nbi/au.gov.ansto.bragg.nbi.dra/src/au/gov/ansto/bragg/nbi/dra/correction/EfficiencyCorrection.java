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
package au.gov.ansto.bragg.nbi.dra.correction;

import java.io.File;
import java.net.URI;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.io.NcHdfWriter;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.math.EData;
import org.gumtree.data.math.EMath;
import org.gumtree.data.utils.Register;

import au.gov.ansto.bragg.datastructures.core.plot.Data;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.Variance;
import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 01/10/2008
 */
public class EfficiencyCorrection extends ConcreteProcessor {

	protected Plot efficiencyCorrection_inputPlot;
	protected Plot efficiencyCorrection_outputPlot;
	protected URI efficiencyCorrection_mapURI;
	protected Boolean efficiencyCorrection_enable = false;
	protected Boolean efficiencyCorrection_stop = false;
	protected Boolean efficiencyCorrection_useCorrectedData = false;
	
//	private Group efficiencyData;
	protected EData<IArray> efficiencyMap;
	protected boolean isEfficiencyFileChanged = true;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		if (! efficiencyCorrection_enable || efficiencyCorrection_mapURI == null){
			efficiencyCorrection_outputPlot = efficiencyCorrection_inputPlot;
			setReprocessable(false);
			return efficiencyCorrection_stop;
		}
		if (isEfficiencyFileChanged || efficiencyMap == null){
			IGroup efficiencyData = null;
			if (efficiencyCorrection_useCorrectedData)
				efficiencyData = NexusUtils.getCorrectedNexusData(efficiencyCorrection_mapURI);
			else
				efficiencyData = NexusUtils.getNexusData(efficiencyCorrection_mapURI);
			//			throw new StructureTypeException("invalid efficiency map");
			IArray mapArray = NexusUtils.getNexusSignal(efficiencyData).getData().getArrayUtils().reduce().getArray();
			//		mapArray = prepareTestData().reduce();
			//		EData<Double> avg = EMath.sum(mapArray, mapArray);
			//		double average = avg.getData() / mapArray.getSize();
			//		double avgVar = avg.getVariance() / mapArray.getSize() / mapArray.getSize();
			//		EData<Array> efficiencyMap = EMath.toScale(mapArray, 1 / average, mapArray, avgVar / Math.pow(average, 4));
			double average = mapArray.getArrayMath().sum() / mapArray.getSize();
			efficiencyMap = EMath.toScale(mapArray, 1 / average, mapArray, 0);
			isEfficiencyFileChanged = false;
		}
//		Array efficiencyMap = mapArray.toScale(1 / average);
//		Array efficiencyVariance = efficiencyMap.toScale(1 / average);
		long memorySize = efficiencyCorrection_inputPlot.calculateMemorySize();
//		efficiencyCorrection_outputPlot = efficiencyCorrection_inputPlot.toEltMultiply(efficiencyMap);
		efficiencyCorrection_outputPlot = efficiencyCorrection_inputPlot.toEltDivide(
				efficiencyMap.getData(), efficiencyMap.getVariance());
		((NcGroup) efficiencyCorrection_outputPlot).setLocation(efficiencyCorrection_inputPlot.getLocation());
		if (memorySize > Register.REPROCESSABLE_THRESHOLD){
			efficiencyCorrection_inputPlot.clearData();
			efficiencyMap = null;
			setReprocessable(false);
		}else{
			setReprocessable(true);
		}
		efficiencyCorrection_outputPlot.addProcessingLog("efficiency correction " + efficiencyCorrection_mapURI);
		return efficiencyCorrection_stop;
	}

	public IArray prepareTestData() throws InvalidArrayTypeException{
		double[] testStorage = new double[421 * 421];
//		for(int i = 0; i < testStorage.length; i ++)
//				testStorage[i] = 100;
		for (int i = 0; i < 210 * 421; i ++)
			testStorage[i] = 0.5;
		for (int i = 210 * 421; i < 421 * 421; i ++)
			testStorage[i] = 2;
		IArray testData = Factory.createArray(Double.TYPE, new int[]{1, 1, 421, 421}, testStorage);
		Data testItem = efficiencyCorrection_inputPlot.findSingal();
		testItem.setCachedData(testData, false);
		Variance testVariance = efficiencyCorrection_inputPlot.getVariance();
		testVariance.setCachedData(testData, false);
		try {
			IGroup parentData = efficiencyCorrection_inputPlot.getParentGroup();
			((NcGroup) parentData).findSignal().setCachedData(testData, false);
			NcHdfWriter hdf = new NcHdfWriter(new File("D:/dra/test100.hdf"));
			hdf.writeToRoot(efficiencyCorrection_inputPlot.getParentGroup().getParentGroup());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return testData;
	}
	
	public void setEfficiencyCorrection_mapURI(URI efficiencyCorrection_mapURI) 
	throws Exception {
		this.efficiencyCorrection_mapURI = efficiencyCorrection_mapURI;
		isEfficiencyFileChanged = true;
//		if (efficiencyCorrection_mapURI != null){
//			efficiencyData = NexusUtils.getNexusData(efficiencyCorrection_mapURI);
//		}
	}

	/**
	 * @return the efficiencyCorrection_outputPlot
	 */
	public Plot getEfficiencyCorrection_outputPlot() {
		return efficiencyCorrection_outputPlot;
	}

	/**
	 * @param efficiencyCorrection_inputPlot the efficiencyCorrection_inputPlot to set
	 */
	public void setEfficiencyCorrection_inputPlot(
			Plot efficiencyCorrection_inputPlot) {
		this.efficiencyCorrection_inputPlot = efficiencyCorrection_inputPlot;
	}

	/**
	 * @param efficiencyCorrection_skip the efficiencyCorrection_skip to set
	 */
	public void setEfficiencyCorrection_enable(Boolean efficiencyCorrection_enable) {
		this.efficiencyCorrection_enable = efficiencyCorrection_enable;
	}

	/**
	 * @param efficiencyCorrection_stop the efficiencyCorrection_stop to set
	 */
	public void setEfficiencyCorrection_stop(Boolean efficiencyCorrection_stop) {
		this.efficiencyCorrection_stop = efficiencyCorrection_stop;
	}

	/**
	 * @param efficiencyCorrection_useCorrectedData the efficiencyCorrection_useCorrectedData to set
	 */
	public void setEfficiencyCorrection_useCorrectedData(
			Boolean efficiencyCorrection_useCorrectedData) {
		this.efficiencyCorrection_useCorrectedData = efficiencyCorrection_useCorrectedData;
		isEfficiencyFileChanged = true;
	}
	
	
}
