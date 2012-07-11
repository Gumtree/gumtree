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
package au.gov.ansto.bragg.kowari.dra.sgf;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.math.EMath;

import au.gov.ansto.bragg.datastructures.nexus.NexusUtils;

/**
 * @author nxi
 * Created on 22/06/2009
 */
public class EfficiencyCorrection extends
		au.gov.ansto.bragg.nbi.dra.correction.EfficiencyCorrection {

	private final static int NUMBER_OF_BIN_TO_SKIP_AT_BOUNDARY = 5;
	
	/**
	 * 
	 */
	public EfficiencyCorrection() {
		super();
	}

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
			IArray efficiencyArray = efficiencyMap.getData();
			if (efficiencyArray.getRank() == 2) {
				for (int i = 0; i < NUMBER_OF_BIN_TO_SKIP_AT_BOUNDARY; i ++) {
					IArray sliceArray = efficiencyArray.getArrayUtils().slice(0, i).getArray();
					IArrayIterator sliceIterator = sliceArray.getIterator();
					while (sliceIterator.hasNext()) {
						sliceIterator.next().setDoubleCurrent(1);
					}
				}
				for (int i = efficiencyArray.getShape()[0] - NUMBER_OF_BIN_TO_SKIP_AT_BOUNDARY; 
					i < efficiencyArray.getShape()[0]; i ++) {
					IArray sliceArray = efficiencyArray.getArrayUtils().slice(0, i).getArray();
					IArrayIterator sliceIterator = sliceArray.getIterator();
					while (sliceIterator.hasNext()) {
						sliceIterator.next().setDoubleCurrent(1);
					}
				}
				IIndex efficiencyIndex = efficiencyArray.getIndex();
				for (int i = 0; i < efficiencyArray.getShape()[0]; i++) {
					efficiencyIndex.set0(i);
					for (int j = 0; j < NUMBER_OF_BIN_TO_SKIP_AT_BOUNDARY; j++) {
						efficiencyIndex.set1(j);
						efficiencyArray.setDouble(efficiencyIndex, 1);
					}
					for (int j = efficiencyArray.getShape()[1] - NUMBER_OF_BIN_TO_SKIP_AT_BOUNDARY; 
						j < efficiencyArray.getShape()[1]; j++) {
						efficiencyIndex.set1(j);
						efficiencyArray.setDouble(efficiencyIndex, 1);
					}
				}
			}
			IArray efficiencyVariance = efficiencyMap.getVariance();
			if (efficiencyVariance.getRank() == 2) {
				for (int i = 0; i < NUMBER_OF_BIN_TO_SKIP_AT_BOUNDARY; i ++) {
					IArray sliceArray = efficiencyVariance.getArrayUtils().slice(0, i).getArray();
					IArrayIterator sliceIterator = sliceArray.getIterator();
					while (sliceIterator.hasNext()) {
						sliceIterator.next().setDoubleCurrent(1);
					}
				}
				for (int i = efficiencyVariance.getShape()[0] - NUMBER_OF_BIN_TO_SKIP_AT_BOUNDARY; 
					i < efficiencyVariance.getShape()[0]; i ++) {
					IArray sliceArray = efficiencyVariance.getArrayUtils().slice(0, i).getArray();
					IArrayIterator sliceIterator = sliceArray.getIterator();
					while (sliceIterator.hasNext()) {
						sliceIterator.next().setDoubleCurrent(1);
					}
				}
				IIndex efficiencyIndex = efficiencyVariance.getIndex();
				for (int i = 0; i < efficiencyVariance.getShape()[0]; i++) {
					efficiencyIndex.set0(i);
					for (int j = 0; j < NUMBER_OF_BIN_TO_SKIP_AT_BOUNDARY; j++) {
						efficiencyIndex.set1(j);
						efficiencyVariance.setDouble(efficiencyIndex, 1);
					}
					for (int j = efficiencyVariance.getShape()[1] - NUMBER_OF_BIN_TO_SKIP_AT_BOUNDARY; 
						j < efficiencyVariance.getShape()[1]; j++) {
						efficiencyIndex.set1(j);
						efficiencyVariance.setDouble(efficiencyIndex, 1);
					}
				}
			}
			isEfficiencyFileChanged = false;
		}
//		Array efficiencyMap = mapArray.toScale(1 / average);
//		Array efficiencyVariance = efficiencyMap.toScale(1 / average);
//		long memorySize = efficiencyCorrection_inputPlot.calculateMemorySize();
//		efficiencyCorrection_outputPlot = efficiencyCorrection_inputPlot.toEltMultiply(efficiencyMap);
		efficiencyCorrection_outputPlot = efficiencyCorrection_inputPlot.toEltDivide(
				efficiencyMap.getData(), efficiencyMap.getVariance());
		((NcGroup) efficiencyCorrection_outputPlot).setLocation(efficiencyCorrection_inputPlot.getLocation());
		efficiencyCorrection_outputPlot.addProcessingLog("efficiency correction " + efficiencyCorrection_mapURI);
		efficiencyCorrection_inputPlot.getGroupList().clear();
		return efficiencyCorrection_stop;
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.process.processor.ConcreteProcessor#isReprocessable()
	 */
	@Override
	public boolean isReprocessable() {
		return false;
	}

	
}
