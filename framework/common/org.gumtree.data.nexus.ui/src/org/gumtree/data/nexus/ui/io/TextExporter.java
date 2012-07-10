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
package org.gumtree.data.nexus.ui.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.utils.NexusUtils;
import org.gumtree.vis.gdm.io.AbstractExporter;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.nexus.dataset.Hist2DNXDataset;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;

public class TextExporter extends AbstractExporter {

	
	@Override
	public void export(File file, IDataset signal) throws IOException {
		if (signal instanceof IXYErrorDataset) {
			List<IXYErrorSeries> seriesList = ((IXYErrorDataset) signal).getSeries();
			if (seriesList.size() > 1) {
				int index = 0;
				for (IXYErrorSeries series : seriesList) {
					if (series instanceof NXDatasetSeries) {
						INXDataset nxDataset = ((NXDatasetSeries) series).getNxDataset();
						File subFile = new File(file.getAbsolutePath() + "/" + 
								nxDataset.getTitle() + "_" + (index++) + "." + getExtensionName());
						INXdata data = NexusUtils.getNXdata(nxDataset);
						signalExport(subFile, data);
					}
				}				
			} else if (seriesList.size() > 0) {
				INXdata data = NexusUtils.getNXdata(((NXDatasetSeries) seriesList.get(0)).getNxDataset());
				signalExport(file, data);
			}
		} else if (signal instanceof Hist2DNXDataset) {
			INXdata data = NexusUtils.getNXdata(((Hist2DNXDataset) signal).getNXDataset());
			signalExport(file, data);
		}
		
	}
	
	
	/**
	 * @param filename
	 * @param signal
	 * @throws IOException
	 */
//	public void signalExport(File file, IGroup signal) throws IOException{
//		FileWriter fileWriter = null;
//		try {
//			fileWriter = new FileWriter(file);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		BufferedWriter bufferWriter = new BufferedWriter (fileWriter);
//		resultBufferExport(bufferWriter, signal);
//		bufferWriter.close();
//		fileWriter.close();
//	}

	/**
	 * @param filename
	 * @param signal
	 * @throws Exception 
	 */
	public void signalExport(URI fileURI, Object signal, String title) 
	throws IOException{
		String filename = fileURI.getPath();
		if (!filename.endsWith(".txt")) filename = filename.concat(".txt");
		File file = new File(filename);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter bufferWriter = new BufferedWriter (fileWriter);
		if (title != null) bufferWriter.append(title);
		bufferExport(bufferWriter, signal, title);
		bufferWriter.close();
		fileWriter.close();
	}

	/**
	 * @param filename
	 * @param signal
	 * @throws Exception 
	 */
	public void signalExport(File file, INXdata signal) 
	throws IOException{
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String title = "Export Data";
		if (signal instanceof IGroup){
			title = ((IGroup) signal).getShortName();
		}
		BufferedWriter bufferWriter = new BufferedWriter (fileWriter);
//		if (title != null) bufferWriter.append(title);
		bufferExport(bufferWriter, signal, title);
		bufferWriter.close();
		fileWriter.close();
	}
	
	public void signalExport(URI fileURI, Object signal, boolean transpose) throws IOException{
		String filename = fileURI.getPath();
		if (!filename.endsWith(".txt")) filename = filename.concat(".txt");
		File file = new File(filename);
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter bufferWriter = new BufferedWriter (fileWriter);
		bufferExport(bufferWriter, signal, transpose);
		bufferWriter.close();
		fileWriter.close();
	}

	private static void resultBufferExport(BufferedWriter bufferWriter, IGroup signal) throws IOException{
//		String result;
//		double[][] resultData = signal.getResultData();
//		bufferWriter.append("dimension = (" + resultData.length + ", " + resultData[0].length + ")");
//		for (int i = 0; i < resultData.length; i++){
//		result = "\n";
//		for (int j = 0; j < resultData[0].length; j++)
//		result += String.valueOf(resultData[i][j]) + " ";
//		bufferWriter.append(result);
//		}
//		bufferExport(bufferWriter, signal.getResultData());
		System.out.println("Write databag to the buffer");
	}

	private void bufferExport(BufferedWriter bufferWriter, Object signal, String title) throws IOException{
		if (signal instanceof IGroup) bufferExportGroup(bufferWriter, (IGroup) signal, title);
		else if (signal instanceof IDataset) bufferExportVariable(bufferWriter, (IDataItem) signal);
		else if (signal instanceof IArray) bufferExportArray(bufferWriter, (IArray) signal);
		else bufferExportPrime(bufferWriter, signal);
	}

	private void bufferExportArray(BufferedWriter bufferWriter,
			IArray signal) throws IOException {
		// TODO Auto-generated method stub
//		Object data = signal.copyToNDJavaArray();
//		bufferExportPrime(bufferWriter, data);
		String shapeString = "#shape = [";
		int[] shape = signal.getShape();
		for (int i = 0; i < shape.length - 1; i++) shapeString += shape[i] + ",";
		shapeString += shape[shape.length - 1] + "]\n";
		bufferWriter.append(shapeString);
		recursiveExportArray(bufferWriter, signal);
		bufferWriter.append("\n");
	}

	private void recursiveExportArray(BufferedWriter bufferWriter,
			IArray signal) throws IOException {
		// TODO Auto-generated method stub
//		Object data = signal.copyToNDJavaArray();
//		bufferExportPrime(bufferWriter, data);
		int[] shape = signal.getShape();
		if (shape.length == 1) bufferWriter.append(signal.toString());
		else{
			for (int i = 0; i < shape[0]; i++){
				recursiveExportArray(bufferWriter, signal.getArrayUtils().slice(0, i).getArray());
				bufferWriter.append("\n");
			}
		}
	}

	private void bufferExportVariable(BufferedWriter bufferWriter,
			IDataItem signal) throws IOException {
		// TODO Auto-generated method stub
		bufferExportArray(bufferWriter, signal.getData());
	}

	@SuppressWarnings("unchecked")
	private void bufferExportGroup(BufferedWriter bufferWriter,
			IGroup signal, String title) throws IOException {
		// TODO Auto-generated method stub
//		CachedVariable variable = null;
//		if (title != null) {
//		variable = signal.getVariable(title);
//		bufferExportVariable(bufferWriter, variable);
//		}
//		else 
		if (signal.isRoot()) {
//			HashMap<String, String> dictionary = signal.getDictionary();
////			for (Iterator<Dictionary> iter = dictionaryList.iterator(); iter.hasNext();){
////			variable = signal.getVariable(iter.next().getKey());
////			bufferExportVariable(bufferWriter, variable);
////			}
//			for (Iterator<String> iter = dictionary.keySet().iterator(); iter.hasNext();){
//
//			}
		}else if (ifColumnExport(signal)) columnExport(bufferWriter, signal);
		else{
			List<?> variableList = signal.getDataItemList();
			for (Iterator<?> iter = variableList.iterator(); iter.hasNext();){
				bufferExportVariable(bufferWriter, (IDataItem) iter.next());
			}
		}

	}

	private boolean ifColumnExport(IGroup signal){
		IDataItem variable = ((NcGroup) signal).findSignal();
		if (variable == null) return false;
		int[] shape = variable.getShape();
		if (shape.length == 1 && shape[0] > 1) return true;
		if (shape.length == 2 && shape[0] == 1) return true;
		if (shape.length == 3 && shape[0] == 1 && shape[1] == 1) return true;
		return false;
	}

	private void columnExport(BufferedWriter bufferWriter, IGroup signal) 
	throws IOException{
		IDataItem dataVariable = ((NcGroup) signal).findSignal();
		List<IDataItem> variableList = new ArrayList<IDataItem>();
		String axesAttribute = dataVariable.getAttribute("axes").getStringValue();
		String[] axesName = axesAttribute.split(":");
		for (int i = 0; i < axesName.length; i ++){
			IDataItem axis = (IDataItem) signal.findDataItem(axesName[i]);
			if (axis != null) variableList.add(axis);
		}
		variableList.add(dataVariable);
		String errorName = dataVariable.getAttribute("error").getStringValue();
		IDataItem errorVariable = null;
		if (errorName != null) errorVariable = (IDataItem) signal.findDataItem(errorName);
		if (errorVariable != null) variableList.add(errorVariable);
		bufferWriter.append(signal.getShortName() + "\n");
		columenExport(bufferWriter, variableList);
	}

	private void columenExport(BufferedWriter bufferWriter, List<IDataItem> variableList) throws IOException{
		IArray[] arrays = new IArray[variableList.size()];
		int id = 0;
		String nameLine = "";
		for (Iterator<IDataItem> iter = variableList.iterator(); iter.hasNext();){
			IDataItem variable = iter.next();
			nameLine += variable.getShortName() + " ";
			arrays[id] = variable.getData();
			id ++;
		}
		bufferWriter.append(nameLine + "\n");
		int size = 0;
		for (int i = 0; i < arrays.length; i ++) {
			int arraySize = Integer.valueOf(String.valueOf(arrays[i].getSize()));
			if (arraySize > size) size = arraySize;
		}
//		Index index = new Index1D(new int[]{size});
		IIndex index = Factory.createIndex(new int[]{size});
		for (int i = 0; i < size; i ++){
			index.set(i);
			String line = "";
			for (int j = 0; j < arrays.length; j ++){
				line += arrays[j].getObject(index).toString() + "\t";
			}
			line += "\n";
			bufferWriter.append(line);
		}
	}

	private void bufferExportPrime(BufferedWriter bufferWriter, Object signal) throws IOException{
		String className = signal.getClass().getName();
		if (className.contains("[[[")){
			double[][][] threeDSignal = (double[][][]) signal;
			bufferWriter.append("dimension = (" + threeDSignal.length + ", " + threeDSignal[0].length + ", " + threeDSignal[0][0].length + ")");
			for (int i = 0; i < threeDSignal.length; i ++){
				bufferWriter.append("\n");
				write2DDouble(bufferWriter, threeDSignal[i], false);
			}
		}else if (className.contains("[[")){
			double[][] twoDSignal = (double[][]) signal;
			bufferWriter.append("dimension = (" + twoDSignal.length + ", " + twoDSignal[0].length + ")");
			write2DDouble(bufferWriter, twoDSignal, false);
		}else if (className.contains("[")){
			double[] oneDSignal = (double[]) signal;
			bufferWriter.append("dimension = (" + oneDSignal.length + ")");
			write1DDouble(bufferWriter, oneDSignal, false);
		}else{
			bufferWriter.append(signal.toString());
		}
	}

	private void bufferExport(BufferedWriter bufferWriter, Object signal, Boolean transpose) throws IOException{
		String className = signal.getClass().getName();
		if (className.contains("[[[")){
			double[][][] threeDSignal = (double[][][]) signal;
			bufferWriter.append("dimension = (" + threeDSignal.length + ", " + threeDSignal[0].length + ", " + threeDSignal[0][0].length + ")");
			for (int i = 0; i < threeDSignal.length; i ++){
				bufferWriter.append("\n");
				write2DDouble(bufferWriter, threeDSignal[i], transpose);
			}
		}else if (className.contains("[[")){
			double[][] twoDSignal = (double[][]) signal;
			bufferWriter.append("dimension = (" + twoDSignal.length + ", " + twoDSignal[0].length + ")");
			write2DDouble(bufferWriter, twoDSignal, transpose);
		}else if (className.contains("[")){
			double[] oneDSignal = (double[]) signal;
			bufferWriter.append("dimension = (" + oneDSignal.length + ")");
			write1DDouble(bufferWriter, oneDSignal, transpose);
		}
	}

	private void write2DDouble(BufferedWriter bufferWriter, double[][] twoDSignal, Boolean transpose) throws IOException{
		double[][] outData;
		if (transpose){
			outData = new double[twoDSignal[0].length][twoDSignal.length];
			for (int i = 0; i < twoDSignal.length; i ++){
				for (int j = 0; j < twoDSignal[0].length; j ++)
					outData[j][i] = twoDSignal[i][j];
			}
		}else outData = twoDSignal;

		for (int i = 0; i < outData.length; i++){
			bufferWriter.append("\n");
			write1DDouble(bufferWriter, outData[i], false);
		}		
	}

	private void write1DDouble(BufferedWriter bufferWriter, double[] oneDSignal, Boolean transpose) throws IOException{
		String result = "";
		if (transpose){
			for (int i = 0; i < oneDSignal.length; i++){
				result += String.valueOf(oneDSignal[i]) + "\n";
			}			
		}else{
			for (int i = 0; i < oneDSignal.length; i++){
				result += String.valueOf(oneDSignal[i]) + " ";
			}
		}
		bufferWriter.append(result);
	}
	
	@Override
	public String toString() {
		return "ASCII";
	}
	
	@Override
	public String getExtensionName() {
		return "txt";
	}
}
