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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;

import au.gov.ansto.bragg.quokka.dra.algolib.core.DimensionNotMatchException;

public class PowerSum {

	public static double[] powerSum(IArray position, IArray intensity, int power) 
	throws DimensionNotMatchException{
		int[] shape = position.getShape();
		if (shape.length == 1 || shape[1] == 1){
			return new double[]{powerSum1D(position, intensity, power)};
		}
		int dimension = shape[1];
		double[] powerSum = new double[dimension];
		for (int i = 0; i < dimension; i ++){
			IArray slice = position.getArrayUtils().slice(1, i).getArray();
			powerSum[i] = powerSum1D(slice, intensity, power);
		}
		return powerSum;
	}

	public static double powerSum1D(IArray position, IArray intensity,
			int power) throws DimensionNotMatchException {
		// TODO Auto-generated method stub
		if (position.getSize() != intensity.getSize()){
			throw new DimensionNotMatchException("the dimensions are not match");
		}
		double powerSum = 0;
		if (power == 1){
			IArrayIterator positionIterator = position.getIterator();
			IArrayIterator intensityIterator = intensity.getIterator();
			while(intensityIterator.hasNext()){
				powerSum += intensityIterator.getDoubleNext() * positionIterator.getDoubleNext();
			}
		}else{
			Map<Double, Double> distinctionData = distinguish(position, intensity);
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
			powerSum = powerSum1DDistinction(position1DArray, intensity1DArray, power);
		}
		return powerSum;
	}

	public static Map<Double, Double> distinguish(IArray position, IArray intensity) 
	throws DimensionNotMatchException {
		// TODO Auto-generated method stub
		if (position.getSize() != intensity.getSize())
			throw new DimensionNotMatchException("the dimensions are not match");
		Map<Double, Double> distinguishData = new HashMap<Double, Double>();
		IArrayIterator positionIterator = position.getIterator();
		IArrayIterator intensityIterator = intensity.getIterator();
		while (positionIterator.hasNext()){
			Double key = positionIterator.getDoubleNext();
			Double value = distinguishData.get(key);
			if (value != null) value += intensityIterator.getDoubleNext();
			else {
				distinguishData.put(key, new Double(intensityIterator.getDoubleNext()));
			}
		}
		return distinguishData;
	}

	public static double powerSum1DDistinction(IArray position, IArray intensity, 
			int power){
		double powerSum = 0;
		IArrayIterator positionIterator = position.getIterator();
		IArrayIterator intensityIterator = intensity.getIterator();
		while(intensityIterator.hasNext()){
			powerSum += Math.pow(intensityIterator.getDoubleNext(), power) * positionIterator.getDoubleNext();
		}
		return powerSum;
	}

}
