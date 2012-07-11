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
package au.gov.ansto.bragg.quokka.dra.algolib.core.internal;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;

import au.gov.ansto.bragg.quokka.dra.algolib.core.DimensionNotMatchException;

public class PowerCentroid {
	
	public static double sumDistinction(IArray array, int power){
		double sum = 0;
		IArrayIterator arrayIterator = array.getIterator();
		while(arrayIterator.hasNext()){
			sum += Math.pow(arrayIterator.getDoubleNext(), power);
		}
		return sum;
	}
	
	public static double[] powerCentroid(IArray position, IArray intensity, int power)
	throws DimensionNotMatchException {
		int[] shape = position.getShape();
		if (shape.length == 1 || shape[1] == 1){
			return new double[]{powerCentroid1D(position, intensity, power)};
		}
		int dimension = shape[1];
		double[] powerSum = new double[dimension];
		for (int i = 0; i < dimension; i ++){
			IArray slice = position.getArrayUtils().slice(1, i).getArray();
			powerSum[i] = powerCentroid1D(slice, intensity, power);
		}
		return powerSum;
	}
	
	public static double powerCentroid1D(IArray position, IArray intensity, int power)
	throws DimensionNotMatchException {
		if (position.getSize() != intensity.getSize()){
			throw new DimensionNotMatchException("the dimensions are not match");
		}
		double powerSum = 0;
		double totalSum = 0;
		if (power == 1){
			IArrayIterator positionIterator = position.getIterator();
			IArrayIterator intensityIterator = intensity.getIterator();
			while(intensityIterator.hasNext()){
				double intensityValue = intensityIterator.getDoubleNext();
				powerSum += intensityValue * positionIterator.getDoubleNext();
				totalSum += intensityValue;
			}
		}else{
			Map<Double, Double> distinctionData = PowerSum.distinguish(position, intensity);
			Set<Double> keySet = distinctionData.keySet();
			double[] position1D = new double[keySet.size()];
			double[] intensity1D = new double[keySet.size()];
			int index = 0;
			for (Iterator<?> iterator = keySet.iterator(); iterator.hasNext();) {
				Double key = (Double) iterator.next();
				position1D[index] = key;
				intensity1D[index] = distinctionData.get(key);
			}
			IArray position1DArray = Factory.createArray(position1D);
			IArray intensity1DArray = Factory.createArray(intensity1D);
			powerSum = PowerSum.powerSum1DDistinction(position1DArray, intensity1DArray, power);
			totalSum = sumDistinction(intensity1DArray, power);
		}
		return powerSum / totalSum;
	}
}
