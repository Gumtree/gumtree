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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.kakadu.core.UIAlgorithmManager;


/**
 * The composite to display statistic information in Plot properties section.
 * 
 * @author Danil Klimontov (dak)
 */
public class StatisticPlotPropertiesComposite extends Composite {

	private final Plot plot;
	private TableViewer viewer;
	private TableColumn keyTableColumn;
	private TableColumn valueTableColumn;
	private TableViewerSorter tableViewerSorter;
	private final SortSelectionListener sortSelectionListener = new SortSelectionListener();
	private final List<TableColumn> tableColumns = new ArrayList<TableColumn>();

	private List<StatisticItem> statisticItemsList = new ArrayList<StatisticItem>();

	/**
	 * @param parent
	 * @param style
	 */
	public StatisticPlotPropertiesComposite(Composite parent, int style, Plot plot) {
		super(parent, style);
		this.plot = plot;
		
		initialise();
	}

	private void initialise() {
		setLayout(new FillLayout());
		
		viewer = new TableViewer(this, SWT.SINGLE
				| SWT.H_SCROLL
				| SWT.V_SCROLL
				| SWT.FULL_SELECTION);

		keyTableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		keyTableColumn.setText("Key");
		keyTableColumn.setWidth(70);
		keyTableColumn.setMoveable(true);
		tableColumns.add(keyTableColumn);
		keyTableColumn.addListener(SWT.Selection, sortSelectionListener);

		valueTableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		valueTableColumn.setText("Value");
		valueTableColumn.setWidth(70);
		valueTableColumn.setMoveable(true);
		tableColumns.add(valueTableColumn);
		valueTableColumn.addListener(SWT.Selection, sortSelectionListener);


		viewer.setContentProvider(new StatisticListContentProvider());
		viewer.setLabelProvider(new StatisticLabelProvider());
		tableViewerSorter = new TableViewerSorter();
		viewer.setSorter(tableViewerSorter);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		
		viewer.setInput(statisticItemsList);
	}
	
	private int getColumnIndex(TableColumn column) {
		return tableColumns.indexOf(column);
	}
	
	public void addItem(String key, String value) {
		statisticItemsList.add(new StatisticItem(key, value));
		viewer.refresh();
	}
	
	public void removeAllItems() {
		statisticItemsList.clear();
	}


	class StatisticListContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return statisticItemsList.toArray();
		}
	}
	
	class StatisticLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			StatisticItem statisticItem = (StatisticItem) obj;
			switch (index) {
			case 0:
				return statisticItem.getKey();
			case 1:
				return statisticItem.getValue();
			default:
				return "";
			}
		}
		public Image getColumnImage(Object obj, int index) {
//			StatisticItem statisticItem = (StatisticItem) obj;
//			Image icon = new Image(this.getShell().getDisplay(),  parent.getParent().getParent().getWorkspaceDir() + "/" + algorithm.getIcon());
			switch (index) {
			case 1:
			case 0:
				return null;
			default:
				return PlatformUI.getWorkbench().
				getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			}
		}
	}


	private final class SortSelectionListener implements Listener {
		public void handleEvent(Event event) {
			//apply sorting
			tableViewerSorter.setSortColumn((TableColumn)event.widget);
			viewer.refresh(false);
		}
	}

	private final class TableViewerSorter extends ViewerSorter {

		private TableColumn column;

		private int direction = SWT.NONE;

		/**
		 * Does the sort. If it's a different column from the previous sort, do an
		 * ascending sort. If it's the same column as the last sort, toggle the sort
		 * direction.
		 *
		 * @param column
		 */
		public void setSortColumn(TableColumn column) {
			final Table table = viewer.getTable();
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

				table.setSortColumn(column);
			}
			
			table.setSortDirection(direction);
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
	
	public class StatisticItem {
		private String key;
		private String value;
		
		public StatisticItem(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}
		public String getValue() {
			return value;
		}
	}

}
