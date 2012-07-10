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
package au.gov.ansto.bragg.cicada.dra.core;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IGroup;
import org.osgi.framework.Bundle;

import au.gov.ansto.bragg.cicada.dam.core.DataManagerFactory;
import au.gov.ansto.bragg.cicada.dra.core.lib.ConverterLib;
import au.gov.ansto.bragg.cicada.dra.internal.Activator;

public class Source extends ConcreteProcessor{
	IGroup source_groupData = null;
	String source_dataName = "data";
	String source_backgroundFilename = null;
	IGroup source_scanData = null;
	IGroup source_backgroundData = null;
//	public List<Object> getSource(GroupData data, String dataName, String backgroundFileName) 
	@SuppressWarnings("static-access")
	public Boolean process() throws Exception{
//		List<Object> result = new LinkedList<Object>();
//		DataItem dataVariable = null;
//		try {
//			dataVariable = source_groupData.getDataItem(source_dataName);	
//		} catch (Exception e) {
//			// TODO: handle exception
//			dataVariable = source_groupData.getGroup(source_dataName).findSignal();
//		}
//		try{
//			if (dataVariable == null) dataVariable = source_groupData.getGroup(source_dataName).findSignal();
//		} catch (Exception ex){
//			ex.printStackTrace();
//		}
		try {
			source_scanData = source_groupData.getGroup(source_dataName);
		} catch (Exception e) {
			// TODO: handle exception
			source_scanData = source_groupData.getDataItem(source_dataName).getParentGroup();
		}
//		if (dataVariable == null) dataVariable = source_groupData.getGroup("data").findSignal();
		if (source_scanData == null) source_scanData = ((NcGroup) source_groupData).findSignal().getParentGroup();
//		Array dataArray = dataVariable.read();
//		result.add(dataGroup);
//		result.add(dataArray);
//		double[] backgroundData = null;
//		int[] dataShape = dataArray.getShape();
//		if (dataShape.length < 2 || dataShape.length > 3) 
//		throw new Exception("wrong echidna data");
//		else{
//		int row, column;
//		if (dataShape.length == 3){
//		row = dataShape[1];
//		column = dataShape[2];
//		}else{
//		row = dataShape[0];
//		column = dataShape[1];
//		}
//		backgroundData = new double[row * column];
//		for (int i = 0; i < backgroundData.length; i ++) backgroundData[i] = 5.;
//		Array backgroundArray = Array.factory(double.class, new int[]{row, column},
//		backgroundData);
//		result.add(backgroundArray);
//		}
		if (source_backgroundFilename != null){
			Bundle bundle = Platform.getBundle(Activator.getDefault().PLUGIN_ID);
			URL workspaceURL = FileLocator.toFileURL(FileLocator.find(bundle, new Path("xml/path_table.txt"), null));
			String pathTableFilename = workspaceURL.getFile();
//			System.out.println(pathTalbeFilename);
			File backgroundFile = new File(source_backgroundFilename);
			if (! backgroundFile.exists()){
				URL backgroundURL = FileLocator.toFileURL(FileLocator.find(
						bundle, new Path("data/echidna_2007-07-11T00-39-55_00648.nx.hdf"), 
						null));
				source_backgroundFilename = backgroundURL.getFile();
			}
			if (!source_backgroundFilename.startsWith("/")) 
				source_backgroundFilename = "/" + source_backgroundFilename;
			URI uri = new URI("file:" + source_backgroundFilename);
			
			System.out.println(uri.toString());
//			source_backgroundFilename = "E:/nxi/workspace/echidna_2007-07-11T00-39-55_00648.nx.hdf";
			IGroup backgroundRootGroup = 
				DataManagerFactory.getDataManager(ConverterLib.getDictionaryPath()).getGroup(uri);
//			backgroundRootGroup.initialiseDictionary(pathTableFilename);
			if (pathTableFilename != null) {
				IDictionary dictionary = Factory.createDictionary();
				dictionary.readEntries(pathTableFilename);
				backgroundRootGroup.setDictionary(dictionary);
			}
			source_backgroundData = backgroundRootGroup.getGroup("data");
		}
//		Array backgroundArray = backgroundGroup.getVariable("data").read();
//		result.add(backgroundArray);
//		result.add(source_backgroundData);
//		return result;
		return false;
	}
	public IGroup getSource_groupData() {
		return source_groupData;
	}
	public IGroup getSource_backgroundData() {
		return source_backgroundData;
	}
	public void setSource_dataName(String source_dataName) {
		this.source_dataName = source_dataName;
	}
	public void setSource_backgroundFilename(String source_backgroundFilename) {
		this.source_backgroundFilename = source_backgroundFilename;
	}
	public void setSource_scanData(IGroup source_scanData) {
		this.source_scanData = source_scanData;
	}
	public void setSource_groupData(IGroup source_groupData) {
		this.source_groupData = source_groupData;
	}
	public IGroup getSource_scanData() {
		return source_scanData;
	}
	public void setField(String filedName, Object value) {
		// TODO Auto-generated method stub
		
	}

}
