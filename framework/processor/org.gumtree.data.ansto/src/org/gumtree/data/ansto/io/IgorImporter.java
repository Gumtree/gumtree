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
package org.gumtree.data.ansto.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

/**
 * @author nxi
 */
public class IgorImporter {

	IGroup databag = null;
//	static HeaderProperty properties = null;
	public IGroup importData(String filePath, String importHeaderFile) throws Exception{


		BufferedReader importHeaderReader = new BufferedReader(new FileReader(new File(importHeaderFile)));
		Map<String, HeaderProperty> headerPairs = new HashMap<String, HeaderProperty>();

		while(importHeaderReader.ready()){
			String[] headerPair = importHeaderReader.readLine().split("=");
//			properties = new HeaderProperty(headerPair[1]);
			if (headerPair[1].indexOf("%") >= 0)
				headerPairs.put(headerPair[0], new HeaderProperty(headerPair[1])); 
		}
		importHeaderReader.close();
		BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
		ArrayList<Float> floatList = new ArrayList<Float>();
		ArrayList<String> headerLines = new ArrayList<String>();

		if (br==null)
			throw new FileAccessException("Dictionary file cannot be open!");
		while(br.ready()){
			String temp = br.readLine();
			float tempFloat = 0; 
			try { 
				tempFloat = Float.parseFloat(temp);
				floatList.add(tempFloat);
			}catch (Exception ex){
				headerLines.add(temp);
			}	
//			String[] temp = br.readLine().split("=");
//			dictionary.put(temp[0], temp[1]);
		}
		float[] floatArray = new float[floatList.size()];
		int counter = 0;
		for (Iterator<Float> iterator = floatList.iterator(); iterator.hasNext();) {
			floatArray[counter++] = iterator.next();
		}
//		floatList.toArray(floatArray);
//		float[] aa = (float[]) floatList.toArray();
		int dimension = (int) Math.sqrt(floatArray.length);
		IDataset dataset = Factory.createEmptyDatasetInstance();
//		Dataset dataset = Factory.createTempDataset();
		
		IGroup rootGroup = dataset.getRootGroup();
		IArray array = Factory.createArray(Float.class, new int[]{dimension, dimension}, floatArray);
		IGroup entryGroup = Factory.createGroup(dataset, rootGroup, "entry", true);
//		rootGroup.addSubgroup(entryGroup);
		IAttribute attribute = Factory.createAttribute("NX_class", "NXentry");
		entryGroup.addOneAttribute(attribute);
		IGroup group = Factory.createGroup(dataset, entryGroup, "data", true);
		attribute = Factory.createAttribute("NX_class", "NXdata");
		group.addOneAttribute(attribute);
		attribute = Factory.createAttribute("signal", "data");
		group.addOneAttribute(attribute);
		IDataItem dataItem = Factory.createDataItem(group, "data", array);
		attribute = Factory.createAttribute("signal", "1");
		dataItem.addOneAttribute(attribute);
		group.addDataItem(dataItem);

		IGroup headerGroup = Factory.createGroup(dataset, entryGroup, "header", true);
//		entryGroup.addSubgroup(headerGroup);
		Set<String> keySet = headerPairs.keySet();
		for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
			String headerName = iterator.next();
			HeaderProperty headerProperty = headerPairs.get(headerName);
			String headerValue = parseHeader(headerLines, headerName, headerProperty);
			if (headerValue != null){
//				Object headerValueReal = null;
//				Array headerValueArray = null;
				IDataItem headerValueDataItem = null;
				if (headerProperty.type.equals("N")){
					try{
						double[] headerValueReal = new double[headerProperty.length];
						String valueWords[] = headerValue.split(" ");
						for (int i = 0; i < headerProperty.length; i ++){
							headerValueReal[i] = Double.valueOf(valueWords[i]);
						}
						IArray doubleArray = Factory.createArray(headerValueReal);
						headerValueDataItem = 
							Factory.createDataItem(headerGroup, headerProperty.getDataItemName(), doubleArray);
					}catch(Exception ex){
						continue;
					}
				}else{
//					headerValueReal = headerValue;
					IArray stringArray = null;
					try{
						stringArray = Factory.createArray(headerValue.toCharArray());
						headerValueDataItem = 
							Factory.createDataItem(headerGroup, headerProperty.getDataItemName(), stringArray);
					}catch(Exception ex){
						//stringArray = Factory.createArray("null".toCharArray());
						continue;
					}
				}
//				Array headerValueArray = Factory.createArray(headerValueReal);
//				DataItem headerValueDataItem = 
//				Factory.createDataItem(dataset, headerGroup, headerName, headerValueArray);
				headerGroup.addDataItem(headerValueDataItem);
			}
		}
		databag = rootGroup;
		return rootGroup;
	}

	private static String parseHeader(ArrayList<String> headerLines,
			String headerName, HeaderProperty headerProperty) {
		// TODO Auto-generated method stub
		int counter = 0;
		String value = "";
		boolean found = false;
		for (Iterator<String> iterator = headerLines.iterator(); iterator.hasNext();) {
			counter ++;
			String headerLine = iterator.next();
			int index = headerLine.indexOf(headerName);
			if (index < 0) continue;
			String restLine = headerLine.substring(index + headerName.length());
			String[] words = restLine.split(" ");
			for (int i = 0; i < words.length; i ++){
				if (words[i].equals("") || words[i].matches(":")) continue;
				if (words[i].startsWith("(")){
//					unit = words[i].substring(1, words[i].indexOf(")"));
					continue;
				}
				if (headerProperty.type.matches("S")){
					if (i + headerProperty.length <= words.length){
						for (int j = 0; j < headerProperty.length; j ++)
							value += words[i + j];
						found = true;
						break;
					}
				}
			}
			if (found) return value;
			else return findValueAtNextLine(headerLine, headerLines.get(counter), headerName,
					headerProperty.length);
		}
		return null;
	}

	private static String findValueAtNextLine(String thisLine, String nextLine, 
			String headerName, int length) {
		int valueIndex = 0;
		String[] words = thisLine.split("  ");
		for (int i = 0; i < words.length; i++) {
			if (words[i].length() == 0) continue;
			if (words[i].contains(headerName)) break;
			if (words[i].contains(",")){
				String[] subWords = words[i].split(",");
				valueIndex += subWords.length;
			}else
				valueIndex ++;
		}
		// TODO Auto-generated method stub
		String[] valueWords = nextLine.split(" ");
		int thisIndex = 0;
		for (int i = 0; i < valueWords.length; i++) {
			if (valueWords[i].length() == 0) continue;
			if (thisIndex == valueIndex){
				String returnValue = "";
				int addCounter = 0;
				for (int j = i; j < valueWords.length; j ++){
					if (valueWords[j].length() == 0) continue;
					returnValue += valueWords[j];
					addCounter ++;
					if (addCounter < length ) returnValue += " ";
					else break;
				}
				return returnValue;
			}
			thisIndex ++;
		}
		return null;
	}

	private class HeaderProperty{
		String path = null;
		String type = null;
		int length = 0;

		public HeaderProperty(String line){
			String[] properties = line.split("%");
			if (properties.length >= 2){
			path = properties[0];
			type = properties[1].substring(0, 1);
			length = Integer.valueOf(properties[1].substring(1, 2));
			}else if (properties.length == 1){
				path = properties[0];
			}
		}

		public String getDataItemName(){
			return path.substring(path.lastIndexOf("/") + 1);
		}
	}

}
