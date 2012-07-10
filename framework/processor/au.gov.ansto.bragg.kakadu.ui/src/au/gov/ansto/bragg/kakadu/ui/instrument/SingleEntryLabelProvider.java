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
package au.gov.ansto.bragg.kakadu.ui.instrument;

import java.io.File;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;
import au.gov.ansto.bragg.kakadu.ui.views.DataSourceTableLabelProvider;

/**
 * @author nxi
 * Created on 13/07/2009
 */
public class SingleEntryLabelProvider extends DataSourceTableLabelProvider {

	/**
	 * 
	 */
	public SingleEntryLabelProvider() {
		super();
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
				List<DataItem> dataItems = dataSourceFile.getDataItems();
				switch (columnIndex) {
				case NAME_COLUMN_INDEX:
					String fullName = dataSourceFile.getName();
					File file = new File(fullName);
					fullName = file.getName();
					if (fullName.length() > 3 && Character.isLetter(fullName.charAt(0)) && 
							Character.isLetter(fullName.charAt(1)) && Character.isLetter(fullName.charAt(2)) &&
							Character.isDigit(fullName.charAt(3)))
						fullName = fullName.substring(3, fullName.indexOf("."));
					else
						fullName = fullName.substring(0, fullName.lastIndexOf("."));
					return fullName;
//				case SIZE_COLUMN_INDEX:
//					return "" + dataSourceFile.getSize();
				default:
					DataItem dataItem = null;
					if (dataItems.size() > 0)
						dataItem = dataItems.get(0);
					else
						return "";
					Object attributeValue = dataItem.getAttribute(getColumnName(columnIndex));
					return convertAttributeValueToString(attributeValue);
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

}
