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
package au.gov.ansto.bragg.kakadu.ui.plot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.kakadu.ui.widget.tree.DefaultTableLabelProvider;
import au.gov.ansto.bragg.kakadu.ui.widget.tree.DefaultTreeContentProvider;

/**
 * The widget used to display not plotable data in table tree presentation.
 * 
 * @author Danil Klimontov (dak)
 */
public class CalculationPlotComposite extends Composite {

	private DefaultMutableTreeNode rootNode;
	private Listener sortSelectionListener = new SortSelectionListener();
	private DefaultTreeViewerSorter treeViewerSorter;
	private TreeViewer treeViewer;
	private DefaultTableLabelProvider defaultTableLabelProvider;
	private final Plot plot;



	public CalculationPlotComposite(Composite parent, int style, Plot plot) {
		super(parent, style);
		this.plot = plot;
		initialise();
		initListeners();
	}

	private void initialise() {
		setLayout(new FillLayout());
		
		treeViewer = new TreeViewer(this, SWT.FULL_SELECTION);
		
		treeViewer.setContentProvider(new DefaultTreeContentProvider());
		defaultTableLabelProvider = new DefaultTableLabelProvider() {
			public String getColumnString(Object userObject, int columnIndex) {
				if (userObject instanceof CalculationParameter) {
					CalculationParameter calculationParameter = (CalculationParameter) userObject;
					
					switch (columnIndex) {
					case 0://name column
						return calculationParameter.getName();
					case 1://value column
						return calculationParameter.getValue().toString();
					}
					//display attributes
					List<Object> list = calculationParameter.getAttributes();
					if (list.size() <= columnIndex - 2) {
						return "";
					}
					return list.get(columnIndex - 2).toString();
					
					
				} else if (userObject instanceof List) {
					List list = (List) userObject;
					if (list.size() <= columnIndex) {
						return "";
					}
					return list.get(columnIndex).toString();
				} else {
					return userObject != null && columnIndex == 0 ? userObject.toString() : "";
				}
			}
		};
		treeViewer.setLabelProvider(defaultTableLabelProvider);

		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.setInput(getRootNode()); // pass a non-null that will be ignored

		final TreeColumn nameTreeColumn = new TreeColumn(treeViewer.getTree(), SWT.NONE);
		nameTreeColumn.setText("Name");
		nameTreeColumn.setWidth(70);
		nameTreeColumn.addListener(SWT.Selection, sortSelectionListener);
		defaultTableLabelProvider.addTreeColumn(nameTreeColumn);
		
		final TreeColumn valueTreeColumn = new TreeColumn(treeViewer.getTree(), SWT.NONE);
		valueTreeColumn.setText("Value");
		valueTreeColumn.setWidth(70);
		valueTreeColumn.addListener(SWT.Selection, sortSelectionListener);
		defaultTableLabelProvider.addTreeColumn(valueTreeColumn);
		
		final TreeColumn unitsTreeColumn = new TreeColumn(treeViewer.getTree(), SWT.NONE);
		unitsTreeColumn.setText("Units");
		unitsTreeColumn.setWidth(70);
		unitsTreeColumn.addListener(SWT.Selection, sortSelectionListener);
		defaultTableLabelProvider.addTreeColumn(unitsTreeColumn);
		
		treeViewerSorter = new DefaultTreeViewerSorter();
		treeViewer.setSorter(treeViewerSorter);

		
		
	}
	
	/**
	 * Adds not existed columns to table.
	 * @param columnNames
	 */
	private void updateTableColumns(List<String> columnNames) {
//		for (String columnName : columnNames) {
//			if (!dataSourceTableLabelProvider.isColumnExist(columnName)) {
//				final Table table = tableTreeViewer.getTableTree().getTable();
//				TableColumn tableColumn = new TableColumn(table, SWT.None);
//				tableColumn.setText(columnName);
//				tableColumn.setWidth(200);
//				tableColumn.setMoveable(true);
//				tableColumn.addListener(SWT.Selection, sortSelectionListener);
//				dataSourceTableLabelProvider.addTableColumn(tableColumn);
//			}
//		}
		
	}

	


	private DefaultMutableTreeNode getRootNode() {
		if (rootNode == null) {
			rootNode = new DefaultMutableTreeNode();
		}		
		return rootNode;
	}
	

	private void adjustColumnSize() {
		//adjust column size
		for (TreeColumn treeColumn : treeViewer.getTree().getColumns()) {
			treeColumn.pack();
		}
	}

	private void addNode(DefaultMutableTreeNode node) {
		addNode(getRootNode(), node);
	}

	private void addNode(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode node) {
		treeViewer.add(parentNode, node);
		parentNode.add(node);
	}

	private void removeNode(DefaultMutableTreeNode node) {
//		fileTableTreeViewer.remove(getRootNode(), new Object[] {fileNode});
		getRootNode().remove(node);
		treeViewer.remove(node);
	}

	private void removeAllNodes() {
		List childrenNodes = new ArrayList();
		for (Enumeration children = getRootNode().children(); children.hasMoreElements();) {
			childrenNodes.add(children.nextElement());
		}
		getRootNode().removeAllChildren();
		treeViewer.remove(childrenNodes.toArray());
	}



	private void initListeners() {
	}
	

	private final class SortSelectionListener implements Listener {
		public void handleEvent(Event event) {
			//apply sorting
			treeViewerSorter.setSortColumn((TreeColumn)event.widget);
			treeViewer.refresh(false);
		}
	}

	public class DefaultTreeViewerSorter extends ViewerSorter {

		private TreeColumn column;

		private int direction = SWT.NONE;

		/**
		 * Does the sort. If it's a different column from the previous sort, do an
		 * ascending sort. If it's the same column as the last sort, toggle the sort
		 * direction.
		 *
		 * @param column
		 */
		public void setSortColumn(TreeColumn column) {
			final Tree tree = treeViewer.getTree();
			if (column == this.column) {
				// Same column as last sort; toggle the direction
				switch (direction) {
				case SWT.DOWN:
					direction = SWT.UP;
					break;
//				case SWT.UP:
//					direction = SWT.NONE;
//					break;

				default:
					direction = SWT.DOWN;
					break;
				}

			} else {
				// New column; do an ascending sort
				this.column = column;
				direction = SWT.DOWN;

				tree.setSortColumn(column);
			}
			
			tree.setSortDirection(direction);
		}

		/**
		 * Compares the object for sorting
		 */
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (direction == SWT.NONE) {
				//not sorted result
				return 0;
			}
	        String name1;
	        String name2;

	        if (viewer == null || !(viewer instanceof ContentViewer)) {
	            name1 = e1.toString();
	            name2 = e2.toString();
	        } else {
	            IBaseLabelProvider prov = ((ContentViewer) viewer)
	                    .getLabelProvider();
	            if (prov instanceof ITableLabelProvider) {
	            	ITableLabelProvider lprov = (ITableLabelProvider) prov;
	                int columnIndex = getColumnIndex(column);
	                
					name1 = lprov.getColumnText(e1, columnIndex);
	                name2 = lprov.getColumnText(e2, columnIndex);
	            } else {
	                name1 = e1.toString();
	                name2 = e2.toString();
	            }
	        }
	        if (name1 == null) {
				name1 = "";//$NON-NLS-1$
			}
	        if (name2 == null) {
				name2 = "";//$NON-NLS-1$
			}

	        // use the comparator to compare the strings
	        int result = getComparator().compare(name1, name2);
	        
	        if (direction == SWT.UP) {
	        	//opposite direction
	        	result = -result;
	        }
	        return result;
		}
	}

	public int getColumnIndex(TreeColumn column) {
		return defaultTableLabelProvider.getColumnIndex(column);
	}

	/**
	 * Sets data object to viewer. Parses data to compose UI data model.
	 * It is supports following data structure.
	 * <pre>
	 * CalculationSet:Group
	 *  +-Input:Group
	 *  | +-Parameter:DataItem
	 *  |   +-Name
	 *  |   +-Value
	 *  |   +-attributes:Attributes
	 *  |     +units:Attribute
	 *  +-Output:Group
	 *    +-...
	 *  </pre>
	 * @param data data object
	 */
	public void setCalculationData(Object data) {
		final IGroup groupData = (IGroup) data;

		removeAllNodes();
		
		if (data == null) {
			return;
		}
		
		//Schema data type detection
		final IAttribute signalAttribute = groupData.getAttribute("signal");
		if (signalAttribute != null) {
			final String signalAttributeName = signalAttribute.getStringValue();
			if (signalAttributeName.equalsIgnoreCase("calculation")) {
				//this groupData object is calculation type
				
				parseGroupData(groupData, "Input");
				parseGroupData(groupData, "Output");
			}
		}
		
		treeViewer.expandAll();

		
		adjustColumnSize();
		treeViewer.refresh();
	}

	/**
	 * Parses Group object to find subgroup with the name and 
	 * adds appropriate tree nodes for calculation parameters.
	 * @param groupData
	 * @param groupName
	 */
	private void parseGroupData(final IGroup groupData, final String groupName) {
		final IGroup inputGroup = groupData.findGroup(groupName);
		if (inputGroup != null) {
			final List<?> dataItems = inputGroup.getDataItemList();
			for (Object listItem : dataItems) {
				if (listItem instanceof IDataItem) {
					IDataItem dataItem = (IDataItem) listItem;
					
					//extract data from DataItem
					final String shortName = dataItem.getShortName();
					IArray currentData;
					try {
						currentData = dataItem.getData();
					} catch (IOException e) {
						plot.handleException(e);
						continue;
					}
					IIndex index = currentData.getIndex();
					index.set(0);
					Double value = currentData.getDouble(index);
					
					IAttribute unitAttribute = dataItem.getAttribute("units");
					String units = unitAttribute.getStringValue();

					//Create and populate CalculationParameter
					final CalculationParameter calculationParameter = new CalculationParameter(shortName, value);
					calculationParameter.addAttribute(units);

					addCalculationNode(groupName, calculationParameter);
				}
			}
		}
	}

	
	/**
	 * Adds the calculation parameter as child node to the group.
	 * @param groupName name of a group
	 * @param calculationParameter calculation parameter to be added 
	 * @return newly created node.
	 */
	private DefaultMutableTreeNode addCalculationNode(String groupName, CalculationParameter calculationParameter) {
		final DefaultMutableTreeNode groupNode = getGroupNode(groupName);
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(calculationParameter);
		addNode(groupNode, node);
		return node;
	}

	private DefaultMutableTreeNode getGroupNode(String groupName) {
		//find the node in a list of existed nodes
		for (Enumeration children = getRootNode().children(); children.hasMoreElements();) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
			if (node.toString().equalsIgnoreCase(groupName)) {
				return node; 
			}
		}
		
		//create a new node
		final DefaultMutableTreeNode node = new DefaultMutableTreeNode(groupName);

		//add the node to the root and to the view
		addNode(node);
		return node;
	}


	public class CalculationParameter {
		final private String name;
		final private Object value;
		final private List<Object> attributes = new ArrayList<Object>();

		public CalculationParameter(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the value
		 */
		public Object getValue() {
			return value;
		}

		/**
		 * @return the attributes
		 */
		public List<Object> getAttributes() {
			return new ArrayList<Object>(attributes);
		}
		
		public void addAttribute(Object attribute) {
			attributes.add(attribute);
		}
		
		public void removeAttribute(Object attribute) {
			attributes.remove(attribute);
		}
		
	}
}
