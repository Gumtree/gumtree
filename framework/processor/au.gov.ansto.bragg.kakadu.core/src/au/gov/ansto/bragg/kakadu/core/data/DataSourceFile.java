/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.core.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.UIAlgorithmManager;

/**
 * The class contains information about added to Data Source view file.
 * 
 * @author Danil Klimontov (dak)
 */
public class DataSourceFile {

	/**
	 * Data file reference.
	 */
	private File file;
	/**
	 * Data entries of the file.
	 */
	private List<DataItem> dataItems = new ArrayList<DataItem>();
	/**
	 * Original data object. Contains full information about file. 
	 */
	private String dataObjectPath;
	
	private IGroup rootGroup;
	
	private boolean isSelected;
	
	/**
	 * Creates new DataSourceFile instance.
	 * @param file data file reference.
	 */
	public DataSourceFile(File file) {
		this.file = file;
	}
	
	/**
	 * Creates new DataSourceFile instance.
	 * @param file data file reference.
	 * @param dataObject 
	 */
	public DataSourceFile(File file, IGroup dataObject) {
		this.file = file;
		this.dataObjectPath = dataObject.getName();
		this.rootGroup = dataObject;
	}

	/**
	 * Gets name of data source file.
	 * Currently returns absolute path to the file.
	 * @return String name vale.
	 */
	public String getName() {
		return file.getAbsolutePath();
	}
	
	/**
	 * Gets local name of data source file.
	 * @return String name of file without path
	 */
	public String getLocalName() {
		return file.getName();
	}
	
	/**
	 * Gets file size.
	 * @return file size.
	 */
	public long getSize() {
		return file.length();
	}
	
	/**
	 * Gets DataItems of the DataSourceFile.
	 * @return DataItem objects.
	 */
	public List<DataItem> getDataItems() {
		return dataItems;
	}
	
	/**
	 * Adds DataItem to the DataSourceFile.
	 * @param dataItem data item to be added.
	 */
	public void addDataItem(DataItem dataItem) {
		dataItems.add(dataItem);
	}

	/**
	 * Adds collection of DataItems to the DataSourceFile.
	 * @param dataItems data items to be added.
	 */
	public void addDataItems(Collection<DataItem> dataItems) {
		this.dataItems.addAll(dataItems);
	}

	/**
	 * Gets java file object reference. 
	 * @return java File object.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Gets data object which reference to whole file data.
	 * @return data object or null if the object was not set.
	 */
	public IGroup getDataObject() {
//		Group rootGroup = null;
		try {
			rootGroup = UIAlgorithmManager.getAlgorithmManager().loadDataFromFile(
					ConverterLib.path2URI(file.getAbsolutePath()));
			return (IGroup) rootGroup.findContainerByPath(dataObjectPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Sets data object associated with the file.
	 * @param dataObject data object
	 */
	public void setDataObject(IGroup dataObject) {
		this.dataObjectPath = dataObject.getName();
	}
	
	public void close(){
		try {
			rootGroup.getDataset().close();
			System.out.println("Dataset File Closed");
			rootGroup = null;
		} catch (IOException e) {
		}
	}

	public List<String> getArrributeNames() {
		if (dataItems.size() > 0)
			return dataItems.get(0).getArrributeNames();
		return new ArrayList<String>();
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isSelected() {
		return isSelected;
	}
}
