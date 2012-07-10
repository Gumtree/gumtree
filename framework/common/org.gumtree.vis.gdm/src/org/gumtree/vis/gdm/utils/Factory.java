/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.vis.gdm.utils;

import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.ShapeNotMatchException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.vis.dataset.XYErrorDataset;
import org.gumtree.vis.gdm.dataset.ArraySeries;
import org.gumtree.vis.gdm.dataset.Hist2DDataset;
import org.gumtree.vis.gdm.dataset.Preview2DDataset;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IPreview2DDataset;


/**
 * @author nxi
 *
 */
public class Factory {

//	public static Hist2DDataset create2DDataset(Group group) throws StructureTypeException {
//		Hist2DDataset dataset = new Hist2DDataset();
//		if (group instanceof Plot) {
//			Plot plot = (Plot) group;
//			try {
//				Array z = plot.findSignalArray();
//				while (z.getRank() > 2) {
//					z = z.slice(0, 0);
//				}
//				List<Axis> axes = plot.getAxisList();
//				Axis xAxis = axes.get(axes.size() - 1);
//				Axis yAxis = axes.get(axes.size() - 2);
//				Array x = xAxis.getData();
//				Array y = yAxis.getData();
//				dataset.setData(x, y, z);
//				dataset.setTitles(xAxis.getTitle() + " (" + xAxis.getUnits() + ")", 
//						yAxis.getTitle() + " (" + yAxis.getUnits() + ")", 
//						plot.getTitle());
//			} catch (Exception e) {
//				throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
//			}
//		} else {
//			DataItem signal = NexusUtils.getNexusSignal(group);
//			if (signal != null) {
//				try{
//					Array z = signal.getData();
//					List<DataItem> axes = NexusUtils.getNexusAxis(group);
//					DataItem xAxis = axes.get(axes.size() - 1);
//					DataItem yAxis = axes.get(axes.size() - 2);
//					Array x = axes.get(axes.size() - 1).getData();
//					Array y = axes.get(axes.size() - 2).getData();
//					dataset.setData(x, y, z);
//					dataset.setTitles(xAxis.getShortName() + " (" + xAxis.getUnits() + ")", 
//							yAxis.getShortName() + " (" + yAxis.getUnits() + ")", 
//							signal.getShortName());
//				} catch (Exception e) {
//					throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
//				}
//			}
//		}
//		return dataset;
//	}
//	
//	public static ArraySeries createSeries(Group group) throws StructureTypeException {
//		String name = group.getShortName();
//		ArraySeries series = new ArraySeries(name);
//		if (group instanceof Plot) {
//			Plot plot = (Plot) group;
//			try {
//				series.setKey(plot.getTitle());
//				Array y = plot.findSignalArray();
//				while (y.getRank() > 1) {
//					y = y.slice(0, 0);
//				}
//				List<Axis> axes = plot.getAxisList();
//				Array x = axes.get(axes.size() - 1).getData();
//				
//				Array error = plot.findVarianceArray();
//				if (error != null) {
//					error = error.toSqrt();
//				}
//				series.setData(x, y, error);
//			} catch (Exception e) {
//				e.printStackTrace();
//				throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
//			}
//		} else {
//			DataItem signal = NexusUtils.getNexusSignal(group);
//			if (signal != null) {
//				try{
//					Array y = signal.getData();
//					List<DataItem> axes = NexusUtils.getNexusAxis(group);
//					Array x = axes.get(axes.size() - 1).getData();
//					DataItem varianceItem = NexusUtils.getNexusVariance(group);
//					Array error = null;
//					if (varianceItem != null) {
//						error = varianceItem.getData().toSqrt();
//					} else {
//						error = y.toSqrt();
//					}
//					series.setData(x, y, error);
//				} catch (Exception e) {
//					throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
//				}
//			}
//		}
//		return series;
//	}
//	
//	public static void updateSeries(ArraySeries series, Group group) throws StructureTypeException {
////		ArraySeries series = new ArraySeries(name);
//		if (group instanceof Plot) {
//			Plot plot = (Plot) group;
//			series.setKey(plot.getTitle());
//			try {
//				Array y = plot.findSignalArray();
//				List<Axis> axes = plot.getAxisList();
//				Array x = axes.get(axes.size() - 1).getData();
//				Array error = plot.findVarianceArray();
//				if (error != null) {
//					error = error.toSqrt();
//				}
//				series.setData(x, y, error);
//			} catch (Exception e) {
//				throw new StructureTypeException("can not create 1D dataset: " + e.getMessage());
//			}
//		} else {
//			DataItem signal = NexusUtils.getNexusSignal(group);
//			if (signal != null) {
//				try{
//					series.setKey(group.getShortName());
//					Array y = signal.getData();
//					List<DataItem> axes = NexusUtils.getNexusAxis(group);
//					Array x = axes.get(axes.size() - 1).getData();
//					DataItem varianceItem = NexusUtils.getNexusVariance(group);
//					Array error = null;
//					if (varianceItem != null) {
//						error = varianceItem.getData().toSqrt();
//					} else {
//						error = y.toSqrt();
//					}
//					series.setData(x, y, error);
//				} catch (Exception e) {
//					throw new StructureTypeException("can not create 2D dataset: " + e.getMessage());
//				}
//			}
//		}
//	}
	
	public static Hist2DDataset createHist2DDataset(IArray x1d, IArray y1d, IArray z2d) 
	throws ShapeNotMatchException {
		Hist2DDataset dataset = new Hist2DDataset();
		if (z2d != null) {
			dataset.setData(x1d, y1d, z2d);
		}
		return dataset;
	}
	
	public static ArraySeries createArraySeries(String name, IArray x1d, IArray y1d, IArray e1d) 
	throws ShapeNotMatchException {
		ArraySeries series = new ArraySeries(name);
		if (y1d != null) {
			series.setData(x1d, y1d, e1d);
		}
		return series;
	}
	
	public static XYErrorDataset createSingleXYDataset(String name, IArray x1d, IArray y1d, IArray e1d) 
	throws ShapeNotMatchException {
		XYErrorDataset dataset = new XYErrorDataset();
		dataset.addSeries(createArraySeries(name, x1d, y1d, e1d));
		dataset.setTitle(name);
		return dataset;
	}
	
	public static XYErrorDataset createEmptyXYDataset() {
		return new XYErrorDataset();
	}
	
	public static IDataset createDataset(String name, IArray array) throws ShapeNotMatchException {
		switch (array.getRank()) {
		case 0:
			return null;
		case 1:
			XYErrorDataset dataset = new XYErrorDataset();
			dataset.addSeries(createArraySeries(name, null, array, 
//					array.getArrayMath().toSqrt().getArray()));
					null));
			return dataset;
		case 2:
			return createHist2DDataset(null, null, array);
		default:
//			ISliceIterator sliceIterator = array.getSliceIterator(2);
			int[] shape = array.getShape();
			IArray slice = null;
			try {
				int[] newShape = new int[shape.length];
				for (int i = 0; i < shape.length - 2; i ++) {
					newShape[i] = 1;
				}
				newShape[shape.length - 2] = shape[shape.length - 2];
				newShape[shape.length - 1] = shape[shape.length - 1];
				slice = array.getArrayUtils().section(new int[shape.length], 
						newShape).reduce().getArray();
			} catch (InvalidRangeException e) {
				throw new ShapeNotMatchException("can not create slice", e);
			}
			return createHist2DDataset(null, null, slice);
		}
	}
	
	public static Hist2DDataset create2DDataset(IArray array, int index) 
	throws ShapeNotMatchException {
		return createHist2DDataset(null, null, createSlice(array, index));
	}
	
	public static Hist2DDataset create2DDataset(IArray xArray, IArray yArray, 
			IArray zArray, int index) throws ShapeNotMatchException {
		return createHist2DDataset(xArray, yArray, createSlice(zArray, index));
	}
	
	public static IArray createSlice(IArray array, int index) throws ShapeNotMatchException {
		if (array.getRank() < 2) {
			throw new ShapeNotMatchException("short of dimension, rank = " + array.getRank());
		}
		if (array.getRank() == 2){
			if (index == 0) {
				return array;
			} else {
				throw new ShapeNotMatchException("index out of bound, " + index + " / " + 1);
			}
		}
		int[] shape = array.getShape();
		int[] origin = new int[shape.length];
		int[] newShape = new int[shape.length];
		for (int i = 0; i < shape.length - 2; i ++) {
			newShape[i] = 1;
		}
		newShape[shape.length - 2] = shape[shape.length - 2];
		newShape[shape.length - 1] = shape[shape.length - 1];
		
		int counter = 0;
		boolean found = false;
		for (int i = 0; i < shape.length - 2; i++) {
			for (int j = 0; j < shape[i] - 1; j++) {
				origin[i] = j;
				if (counter == index) {
					found = true;
					break;
				}
				counter ++;
			}
			if (found) {
				break;
			}
		}
		if (!found) {
			for (int i = 0; i < origin.length; i++) {
				origin[i] = 0;
			}
		}
		IArray slice = null;
		try {
			slice = array.getArrayUtils().section(origin, newShape).getArray();
		} catch (InvalidRangeException e) {
			throw new ShapeNotMatchException("can not create slice", e);
		}
		return slice;
	}
	
	public static Hist2DDataset create2DDataset(IArray array, int frame, int layer) 
	throws ShapeNotMatchException {
		int[] shape = array.getShape();
		int index = 0;
		if (array.getRank() < 2) {
			throw new ShapeNotMatchException("short of dimension, rank = " + array.getRank());
		}
		if (array.getRank() == 2){
			if (frame == 0 && layer == 0) {
				return createHist2DDataset(null, null, array);
			} else {
				throw new ShapeNotMatchException("index out of bound, " + 
						frame * layer + " / " + 1);
			}
		}
		index = shape[shape.length - 3] * frame + layer;
		return create2DDataset(array, index);
	}
	
	public static IPreview2DDataset createPreview2DDataset(IArray array) {
		Preview2DDataset dataset = new Preview2DDataset();
		dataset.setStorage(array);
		return dataset;
	}
}
