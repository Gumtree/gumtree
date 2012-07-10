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
package au.gov.ansto.bragg.kakadu.ui.widget.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Label provider class for TreeView widget.
 * Implements handling of <code>DefaultMutableTreeNode</code> object as a node data model class.
 * @author Danil Klimontov (dak)
 */
public class DefaultTableLabelProvider implements ITableLabelProvider {

	// The listeners
	private List<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();
	private final List<TreeColumn> treeColumns = new ArrayList<TreeColumn>();

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/**
	 * Implementation of ITableLabelProvider interface. 
	 * Use {@link #getColumnString(Object, int)} method to specify string extraction.  
	 */
	public String getColumnText(Object element, int columnIndex) {
		if (element != null && element instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) element;
		
			Object userObject = treeNode.getUserObject();
			return getColumnString(userObject, columnIndex);
			
		}
		return "";
	}

	/**
	 * Gets a string to display for the column based on user object specified for the tree node.
	 * Use the method to convert data object to display string.
	 * By default toString() method is used to compose the display string.
	 * @param userObject data object for the tree node.
	 * @param columnIndex a column index.
	 * @return string to be displayed.
	 */
	public String getColumnString(Object userObject, int columnIndex) {
		return userObject != null ? userObject.toString() : "";
	}

	/**
	 * Adds a listener to this label provider.
	 *
	 * @param listener the listener
	 */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
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
	
	
	public void addTreeColumn(TreeColumn tableColumn) {
		treeColumns.add(tableColumn);
	}
	
	public boolean isColumnExist(String columnName) {
		for (TreeColumn column : treeColumns) {
			if (column.getText().equals(columnName)) {
				return true;
			}
		}
		return false;
	}
	
	public String getColumnName(int columnIndex) {
		if (columnIndex > 0 && columnIndex < treeColumns.size()) {
			TreeColumn column = treeColumns.get(columnIndex);
			return column.getText();
			
		}
		return null;
	}

	public int getColumnIndex(TreeColumn column) {
		return treeColumns.indexOf(column);
	}

	public int getColumnIndex(String columnName) {
		for (int i = 0; i < treeColumns.size(); i++) {
			final TreeColumn column = treeColumns.get(i);
			if (column.getText().equals(columnName)) {
				return i;
			}
			
		}
		return -1;
	}

	public TreeColumn getColumn(String columnName) {
		for (TreeColumn column : treeColumns) {
			if (column.getText().equals(columnName)) {
				return column;
			}
		}
		return null;
	}

}
