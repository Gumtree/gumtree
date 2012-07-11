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
package au.gov.ansto.bragg.quokka.dra.algolib.core;

public class SANSStaticLib {

//	public static Array findCenterOfMass(Array sample) throws InvalidArrayTypeException{
//		double[][] sampleData = ConverterLib.get2DDouble(sample);
//		double[] centerData = CenterFinder.getCenterOfMass(sampleData);
//		int[] shape = sample.getShape();
//		double[] centroid = new double[shape.length];
//		double[] sliceSum = sum(sample, 0);
//		double totalSum = 0.;
//		double weightSum = 0.;
//		for (int i = 0; i < sliceSum.length; i ++){
//			totalSum += sliceSum[i];
//			weightSum += i * sliceSum[i];
//		}
//		centroid[0] = weightSum / totalSum;
//		for (int i = 1; i<shape.length; i ++){
//			sliceSum = sum(sample, i);
//			weightSum = 0.;
//			for (int j = 0; j < sliceSum.length; j++) {
//				weightSum += j * sliceSum[j];
//			}
//			centroid[i] = weightSum / totalSum;
//		}
//		Array centerArray = Factory.createArray(Double.class, new int[]{centroid.length}, 
//				centroid);
//		return centerArray;
//	}
//	
//	private static double[] sum(Array array, int dimension) throws InvalidArrayTypeException{
//		int[] shape = array.getShape();
//		double[] result = new double[shape[dimension]];
//		if (dimension >= shape.length) return null;
//		for (int i = 0; i < shape[dimension]; i ++){
//			Array slice = array.slice(dimension, i);
//			result[i] = ArrayMath.sumDouble(slice);
//		}
//		return result;
//	}
//	
//	public static Array findRMS(Array sample){
//		double rms = 0.;
////		Number[] array = (Number[]) sample.copyTo1DJavaArray();
//		int size = size(sample.getShape());
//		Index index = Factory.createIndex(new int[]{size});
//		for (int i = 0; i < size; i ++){
//			index.set0(i);
//			rms += Math.pow(sample.getDouble(index), 2);
//		}
////		index.set0()
////		index.
//		rms = Math.sqrt(rms/size);
//		Array rmsArray = Factory.createArray(Double.class, new int[]{1}, new double[]{rms});
//		return rmsArray;
//	}
//	
//	private static int size(int[] array){
//		int size = 1;
//		for (int i = 0; i < array.length; i ++){
//			size *= array[i];
//		}
//		return size;
//	}
//	
//	public static Double getTotalSum(Array sample){
//		Double sum = 0.;
//		Object storage = sample.copyTo1DJavaArray();
//		
//		return sum;
//	}
		
}
