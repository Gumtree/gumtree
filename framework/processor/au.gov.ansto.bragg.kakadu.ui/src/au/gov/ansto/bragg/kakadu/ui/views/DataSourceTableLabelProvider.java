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
package au.gov.ansto.bragg.kakadu.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeColumn;

import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;

/**
 * @author Danil Klimontov (dak)
 */
public class DataSourceTableLabelProvider implements ITableLabelProvider {
	private static final String NOT_DEFINED_ATTRIBUTE_VALUE = "<not defined>";
	protected static final int NAME_COLUMN_INDEX = 0;
	protected static final int SIZE_COLUMN_INDEX = 1;
	// The listeners
	private List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();
	private final List<TreeColumn> tableColumns = new ArrayList<TreeColumn>();

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element != null && element instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) element;
		
			Object userObject = treeNode.getUserObject();
			//File
			if (userObject instanceof File) {
				File file = (File) userObject;
				switch (columnIndex) {
				case NAME_COLUMN_INDEX:
					return file.getAbsolutePath();
//				case SIZE_COLUMN_INDEX:
//					return "" + file.length();
				default:
					return "";
				}
				
			} else if (userObject instanceof DataSourceFile) {
				DataSourceFile dataSourceFile = (DataSourceFile) userObject;
				switch (columnIndex) {
				case NAME_COLUMN_INDEX:
					return dataSourceFile.getName();
//				case SIZE_COLUMN_INDEX:
//					return "" + dataSourceFile.getSize();
				default:
					return "";
				}
				
			} else if (userObject instanceof DataItem) {
				DataItem dataItem = (DataItem) userObject;
				
				switch (columnIndex) {
				case NAME_COLUMN_INDEX:
					return dataItem.getName();
//				case SIZE_COLUMN_INDEX:
//					return "second column";
				default:
					Object attributeValue = dataItem.getAttribute(getColumnName(columnIndex));
					return convertAttributeValueToString(attributeValue);
				}
				
			} else {
				return userObject != null ? userObject.toString() : "null";
			}
			
		}
		return null;
	}

	protected String convertAttributeValueToString(Object attributeValue) {
		if (attributeValue == null) {
			return NOT_DEFINED_ATTRIBUTE_VALUE;
		}
		if (attributeValue instanceof String) {
			//string
			return (String) attributeValue;
		} else if (attributeValue instanceof Boolean) {
			//boolean
			return ((Boolean) attributeValue).toString();
		} else if (attributeValue instanceof Number) {
			//numeric value
			if (attributeValue instanceof Integer) {
				return "" + ((Number) attributeValue).intValue();
			} else {
				return "" + ((Number) attributeValue).doubleValue();
			}
		} else if (attributeValue.getClass().isArray()) {
			//array value
			String result = "[";
			for (Object arrayAttributeValue : ((Object[])attributeValue)) {
				result += convertAttributeValueToString(arrayAttributeValue);
				result += ",";
			}
			return (result.length() > 1 ? result.substring(0, result.length() - 1) : result) + "]" ;
		}
			
		return attributeValue.toString();
	}

	/**
	 * Adds a listener to this label provider
	 *
	 * @param arg0 the listener
	 */
	public void addListener(ILabelProviderListener arg0) {
		listeners.add(arg0);
	}

	public void dispose() {

	}

	/**
	 * Returns whether changes to the specified property on the specified element
	 * would affect the label for the element
	 *
	 * @param arg0 the element
	 * @param arg1 the property
	 * @return boolean
	 */
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	/**
	 * Removes the listener
	 *
	 * @param arg0 the listener to remove
	 */
	public void removeListener(ILabelProviderListener arg0) {
		listeners.remove(arg0);
	}
	
	public void addTableColumn(TreeColumn tableColumn) {
		tableColumns.add(tableColumn);
	}
	
	public boolean isColumnExist(String columnName) {
		for (TreeColumn column : tableColumns) {
			if (column.getText().equals(columnName)) {
				return true;
			}
		}
		return false;
	}
	
	public String getColumnName(int columnIndex) {
		if (columnIndex > 0 && columnIndex < tableColumns.size()) {
			TreeColumn column = tableColumns.get(columnIndex);
			return column.getText();
			
		}
		return null;
	}

	public int getColumnIndex(TreeColumn column) {
		return tableColumns.indexOf(column);
	}

}
