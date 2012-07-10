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
package au.gov.ansto.bragg.kakadu.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;

/**
 * The class manages data source for Kuranda Data Source View.
 * 
 * @author Danil Klimontov (dak)
 */
public class DataSourceManager {
	
	private final Collection<DataSourceFile> files = new ArrayList<DataSourceFile>();
	private final Collection<DataListener<DataSourceFile>> dataListeners = new ArrayList<DataListener<DataSourceFile>>();
	private static ListenerList selectionChangedListeners = new ListenerList();
	private static DataSourceFile selectedFile;
	private static DataSourceManager instance = new DataSourceManager();
	/**
	 * Private constructor.
	 */
	private DataSourceManager() {
	}

	/**
	 * Returns the Singleton instance.
	 * <p>
	 * @return The Singleton instance.
	 */
	public static DataSourceManager getInstance() {
		return instance;
	}
	

	/**
	 * Adds data file to be processed be the DataSourceManager.
	 * File duplications should be checked before calling of the method.
	 * @param fileName name of the file to be added
	 * @return DataSourceFile object associated with the data file.
	 * @throws Exception 
	 */
	public DataSourceFile addFile(String fileName) throws Exception {
		File file = new File(fileName);
		//load data object form file.
		IGroup dataObject = UIAlgorithmManager.getAlgorithmManager().loadDataFromFile(file.toURI());//new URI("file:/" + file.getAbsolutePath().replace("\\", "/")
		
		DataSourceFile dataSourceFile = new DataSourceFile(file, dataObject);
		dataSourceFile.addDataItems(parseDataItems(dataObject));
		
		files.add(dataSourceFile);
		
		fireDataAdded(dataSourceFile);
		
		return dataSourceFile;
	}
	
	public DataSourceFile addDataset(IDataset dataset) throws Exception {
		String location = dataset.getLocation();
		File file = null;
		if (location != null)
			file = new File(dataset.getLocation());
		else file = new File("CombinedGroup");
		IGroup dataObject = dataset.getRootGroup();
		DataSourceFile dataSourceFile = new DataSourceFile(file, dataObject);
		dataSourceFile.addDataItems(parseDataItems(dataObject));
		
		files.add(dataSourceFile);
		
		fireDataAdded(dataSourceFile);
		
		return dataSourceFile;
		
	}	/**
	 * Gets DataSourceFile object associated with the file.
	 * @param fileName data file name.
	 * @return DataSourceFile object or null if file association not found. 
	 */
	public DataSourceFile getDataSourceFile(String fileName) {
		return getDataSourceFile(new File(fileName));
	}

	/**
	 * Gets DataSourceFile object associated with the file.
	 * @param file data file.
	 * @return DataSourceFile object or null if file association not found. 
	 */
	public DataSourceFile getDataSourceFile(File file) {
		for (DataSourceFile dataSourceFile : files) {
			if (dataSourceFile.getFile().getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath())) {
				return dataSourceFile;
			}
		}
		return null;
	}

	/**
	 * Removes the DataSourceFile from the manager.
	 * @param dataSourceFile DataSourceFile to be removed.
	 */
	public void removeFile(DataSourceFile dataSourceFile) {
		files.remove(dataSourceFile);
		dataSourceFile.close();
		if (selectedFile == dataSourceFile)
			selectedFile = null;
		fireDataRemoved(dataSourceFile);
	}
	
	/**
	 * Removes all added files from inner storage.
	 */
	public void removeAll() {
//		ArrayList<DataSourceFile> removedFiles = new ArrayList<DataSourceFile>(files);

	
		for (DataSourceFile dataSourceFile : files) {
			dataSourceFile.close();
			fireDataRemoved(dataSourceFile);
		}
		files.clear();
		selectedFile = null;
		fireAllDataRemoved(null);
	}
	
	/**
	 * Gets count of added to the manager files.
	 * @return count of files
	 */
	public int getSourceDataFileCount() {
		return files.size();
	}
	
	/**
	 * Adds data listener to the manager.
	 * @param dataListener data listener.
	 */
	public void addDataListener(DataListener<DataSourceFile> dataListener) {
		dataListeners.add(dataListener);
	}
	
	/**
	 * Removes data leistener from the list of processed listeners. 
	 * @param dataListener data listener to be removed.
	 * @return true if the listener was removed or false otherwise.
	 */
	public boolean removeDataListener(DataListener<DataSourceFile> dataListener) {
		return dataListeners.remove(dataListener);
	}
	
	/**
	 * Gets all added data listeners.
	 * @return data listeners as Iterator
	 */
	public Iterator<DataListener<DataSourceFile>> getDataListeners() {
		return dataListeners.iterator();
	}
	
	private void fireDataAdded(DataSourceFile addedData) {
		for (DataListener<DataSourceFile> dataListener : dataListeners) {
			dataListener.dataAdded(addedData);
		}
	}

	private void fireAllDataRemoved(DataSourceFile removedData) {
		for (DataListener<DataSourceFile> dataListener : dataListeners) {
			dataListener.allDataRemoved(removedData);
		}
	}
	
	private void fireDataRemoved(DataSourceFile removedData) {
		for (DataListener<DataSourceFile> dataListener : dataListeners) {
			dataListener.dataRemoved(removedData);
		}
	}

	private void fireDataUpdated(DataSourceFile updatedData) {
		for (DataListener<DataSourceFile> dataListener : dataListeners) {
			dataListener.dataUpdated(updatedData);
		}
	}

	/**
	 * Parses content of source data file and looks for data entries 
	 * to wrap them to DataItem object. 
	 * @param fileDataObject
	 * @return list of data items from file.
	 * @throws Exception 
	 */
	private List<DataItem> parseDataItems(IGroup fileDataObject) throws Exception {
		ArrayList<DataItem> result = new ArrayList<DataItem>();
		
		//get data entry list from cicada for the file
		List<?> entryList = UIAlgorithmManager.getAlgorithmManager().getEntryList(fileDataObject);
		if (entryList.size() > 0){
			for (Object object : entryList) {
				if (object instanceof IGroup){
					IGroup groupData = (IGroup) object;
					//create data item for each data entry.
					SelectableDataItem selectableDataItem = new SelectableDataItem(groupData.getName(), groupData);
					result.add(selectableDataItem);
					//add attributes
					Map<String, Object> defaultAttributes = UIAlgorithmManager.getAlgorithmManager().getDefaultAttributes(groupData);
					selectableDataItem.setAttributes(defaultAttributes);
				}
			}
		}else{
			entryList = fileDataObject.getGroupList();
			for (Object object : entryList) {
				if (object instanceof IGroup){
					IGroup groupData = (IGroup) object;
					//create data item for each data entry.
					SelectableDataItem selectableDataItem = new SelectableDataItem(groupData.getName(), groupData);
					result.add(selectableDataItem);
					//add attributes
//					Map<String, Object> defaultAttributes = UIAlgorithmManager.getAlgorithmManager().getDefaultAttributes(groupData);
					Map<String, Object> defaultAttributes = new HashMap<String, Object>();
					defaultAttributes.put("name", "Combined Data");
					selectableDataItem.setAttributes(defaultAttributes);
				}
			}
		}
		return result;
	}

	public Iterator<DataSourceFile> getFiles() {
		return files.iterator();
	}
	
	public List<DataSourceFile> getDataSourceFiles() {
		return (ArrayList<DataSourceFile>) files;
	}
	
	/**
	 * Gets a list of DataItem objects selected in the DataSourceView.
	 * @return list of selected DataItems.
	 */
	public List<DataItem> getSelectedDataItems() {
		ArrayList<DataItem> result = new ArrayList<DataItem>();
		for (DataSourceFile file : files) {
			for (DataItem dataItem : file.getDataItems()) {
				if (!(dataItem instanceof SelectableDataItem) ||
						((SelectableDataItem) dataItem).isSelected()
						) {
					result.add(dataItem);
				}
			}
		}
		return result;
	}

	public List<DataSourceFile> getSelectedFiles() {
		List<DataSourceFile> result = new ArrayList<DataSourceFile>();
		for (DataSourceFile file : files) {
			if (file.isSelected()) {
				result.add(file);
			}
		}
		return result;
	}

	/**
	 * Return all data items in a list;
	 * @return List of DataItems
	 * Created on 01/12/2008
	 */
	public List<DataItem> getAllDataItems() {
		ArrayList<DataItem> result = new ArrayList<DataItem>();
		for (DataSourceFile file : files) 
			for (DataItem dataItem : file.getDataItems()) 
					result.add(dataItem);
		return result;
	}
	

	/**
	 * @param selectedFile the selectedFile to set
	 */
	public static void setSelectedFile(DataSourceFile selectedFile) {
		if (getSelectedFile() != selectedFile){
			DataSourceManager.selectedFile = selectedFile;
			fireSelectionChangedAction();
		}
	}

	private static void fireSelectionChangedAction() {
		// TODO Auto-generated method stub
		for (Object listener : selectionChangedListeners.getListeners()){
			if (listener instanceof DataSelectionListener)
				((DataSelectionListener) listener).dataSelectionChanged();
		}
	}

	public static void fireAllDataRemovedAction(){
		for (Object listener : selectionChangedListeners.getListeners()){
			if (listener instanceof DataSelectionListener)
				((DataSelectionListener) listener).allDataRemoved();
		}
		getInstance().fireAllDataRemoved(null);
	}
	/**
	 * @return the selectedFile
	 */
	public static DataSourceFile getSelectedFile() {
		return selectedFile;
	}


	/**
	 * The class wraps DataItem to make it selectable.
	 * 
	 * @author Danil Klimontov (dak)
	 */
	public class SelectableDataItem extends DataItem {
		private boolean isSelected = false;

		public SelectableDataItem(String name) {
			super(name);
		}
		
		public SelectableDataItem(String name, IGroup dataObject) {
			super(name, dataObject);
		}

		public boolean isSelected() {
			return isSelected;
		}

		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}
		
	}
	
	public static void addSelectionChangedListener(DataSelectionListener listener){
		selectionChangedListeners.clear();
		selectionChangedListeners.add(listener);
	}
	
	public static void removeSelectionChangedListener(DataSelectionListener listener){
		selectionChangedListeners.remove(listener);
	}
	
	public interface DataSelectionListener{
		public void dataSelectionChanged();
		public void allDataRemoved();
	}

}
