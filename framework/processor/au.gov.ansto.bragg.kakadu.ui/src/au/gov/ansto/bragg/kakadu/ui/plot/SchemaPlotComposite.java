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
import org.gumtree.data.Factory;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IIndex;

import au.gov.ansto.bragg.datastructures.core.plot.Data;

/**
 * The widget displays data as key-value pairs in a table.
 * 
 * @author Danil Klimontov (dak)
 */
public class SchemaPlotComposite extends Composite {
	private final Plot plot;
	private TableViewer viewer;
	private TableColumn keyTableColumn;
	private TableColumn valueTableColumn;
	private TableColumn errorTableColumn;
	private TableViewerSorter tableViewerSorter;
	private final SortSelectionListener sortSelectionListener = new SortSelectionListener();
	private final List<TableColumn> tableColumns = new ArrayList<TableColumn>();

	/**
	 * Table data model.
	 */
	private List<List<SchemaDataItem>> dataItemsList = new ArrayList<List<SchemaDataItem>>();

	/**
	 * Creates a new <code>SchemaPlotComposite</code> instance.
	 * @param parent parent Composite
	 * @param style SWT stile
	 * @param plot Plot reference
	 */
	public SchemaPlotComposite(Composite parent, int style, Plot plot) {
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
		keyTableColumn.setWidth(100);
		tableColumns.add(keyTableColumn);
		keyTableColumn.addListener(SWT.Selection, sortSelectionListener);

		valueTableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		valueTableColumn.setText("Value");
		valueTableColumn.setWidth(100);
		tableColumns.add(valueTableColumn);
		valueTableColumn.addListener(SWT.Selection, sortSelectionListener);

		errorTableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
		errorTableColumn.setText("Error");
		errorTableColumn.setWidth(100);
		tableColumns.add(errorTableColumn);
		errorTableColumn.addListener(SWT.Selection, sortSelectionListener);

		viewer.setContentProvider(new SchemaListContentProvider());
		viewer.setLabelProvider(new SchemaLabelProvider());
		tableViewerSorter = new TableViewerSorter();
		viewer.setSorter(tableViewerSorter);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		viewer.setInput(dataItemsList);
	}

	private int getColumnIndex(TableColumn column) {
		return tableColumns.indexOf(column);
	}

	/**
	 * Sets the data to be displayed in table. 
	 * @param plotData 
	 */
	public void setSchemaData(Object plotData) {

		clearData();
		final ArrayList<String> columnNames = new ArrayList<String>();


		if (plotData != null && plotData instanceof IGroup) {
			IGroup group = (IGroup)plotData;

			IArray array = null;
			IArray varianceArray = null;
			List<IArray> axesArrayList = null;
			try {
				array = ((NcGroup) group).getSignalArray();
				axesArrayList = ((NcGroup) group).getAxesArrayList();
			} catch (SignalNotAvailableException e) {
				plot.handleException(e);
				return;
			}

			try {
				varianceArray = ((au.gov.ansto.bragg.datastructures.core.plot.Plot) group).findVarianceArray();
			} catch (Exception e) {
			}
			
			int[] shape = array.getShape();
			int rank = array.getRank();
			Class<?> type = array.getElementType();
			IIndex index = array.getIndex();

			if (rank == 1) {
				//get X Axis array info
				final IArray xAxisArray = axesArrayList.get(0);
				final Class<?> xAxisType = xAxisArray.getElementType();
				final int[] xAxisShape = xAxisArray.getShape();
				final IIndex xAxisIndex = Factory.createIndex(xAxisShape);

				//add columns
				columnNames.add("X");
				columnNames.add("Y");

				if (varianceArray == null){
					for (int i = 0; i < shape[0]; i++) {
						//extract value
						index.set(i);
						Object value = array.getObject(index);

						//extract X Axis info
						xAxisIndex.set(i);
						Object xAxisValue = xAxisArray.getObject(xAxisIndex);

						//add data items to table model
						List<SchemaDataItem> rowList = new ArrayList<SchemaDataItem>();
						dataItemsList.add(rowList);
						rowList.add(new SchemaDataItem(i, 0, xAxisValue, xAxisType, value, type, xAxisValue, xAxisType));
						rowList.add(new SchemaDataItem(i, 1, xAxisValue, xAxisType, value, type, value, type));

					}
				}else{
					columnNames.add("Sigma");
					for (int i = 0; i < shape[0]; i++) {
						//extract value
						index.set(i);
						Object value = array.getObject(index);

						Object variance = varianceArray.getObject(index);
						double error = Double.NaN;
						try{
							error = Math.sqrt(Double.valueOf(variance.toString()));
						}catch (Exception e) {
						}

						//extract X Axis info
						xAxisIndex.set(i);
						Object xAxisValue = xAxisArray.getObject(xAxisIndex);

						//add data items to table model
						List<SchemaDataItem> rowList = new ArrayList<SchemaDataItem>();
						dataItemsList.add(rowList);
						rowList.add(new SchemaDataItem(i, 0, xAxisValue, xAxisType, value, type, xAxisValue, xAxisType));
						rowList.add(new SchemaDataItem(i, 1, xAxisValue, xAxisType, value, type, value, type));
						rowList.add(new SchemaDataItem(i, 2, xAxisValue, xAxisType, value, type, error, Double.TYPE));
					}					
				}
			} else if (rank == 2) {

				//First column for Y Axis values
				columnNames.add("Y");

				//get X Axis array info
				final IArray xAxisArray = axesArrayList.get(1);
				final Class<?> xAxisType = xAxisArray.getElementType();
				final int[] xAxisShape = xAxisArray.getShape();
				final IIndex xAxisIndex = Factory.createIndex(xAxisShape);

				List<Object> xAxisValueList = new ArrayList<Object>();

				//get Y Axis array info
				final IArray yAxisArray = axesArrayList.get(0);
				final Class<?> yAxisType = yAxisArray.getElementType();
				final int[] yAxisShape = yAxisArray.getShape();
				final IIndex yAxisIndex = Factory.createIndex(yAxisShape);

				for (int i = 0; i < shape[0]; i++) {
					index.set(i);

					//extract Y Axis info
					yAxisIndex.set(i);
					Object yAxisValue = yAxisArray.getObject(yAxisIndex);


					List<SchemaDataItem> rowList = new ArrayList<SchemaDataItem>();
					dataItemsList.add(rowList);
					//Y Axis value for first column in the table
					rowList.add(new SchemaDataItem(i, 0, null, xAxisType, yAxisValue, yAxisType, "Y:" + getString(yAxisValue, yAxisType), String.class));


					for (int j = 0; j < shape[1]; j++) {
						index.set1(j);
						Object value = array.getObject(index);

						if (i == 0) {
							//extract X Axis info
							xAxisIndex.set(j);
							Object xAxisValue = xAxisArray.getObject(xAxisIndex);
							xAxisValueList.add(xAxisValue);

							//create column title with X Axis value
							columnNames.add("X:" + getString(xAxisValue, xAxisType));
						}

						rowList.add(new SchemaDataItem(i, j + 1, xAxisValueList.get(j), xAxisType, yAxisValue, yAxisType, value, type));
					}
				}

			} else {
				plot.handleException(new IllegalArgumentException("Not possible to create schema for the data object. Rank = " + rank));
				return;
			}

		}




		//1D data
		//		double[] xAxis = new double[15];
		//		double[] data = new double[xAxis.length];
		//		columnNames.add("X");
		//		columnNames.add("Y");
		//		
		//		for (int i = 0; i < xAxis.length; i++) {
		//			xAxis[i] = i;
		//
		//			List<SchemaDataItem> rowList = new ArrayList<SchemaDataItem>();
		//			dataItemsList.add(rowList);
		//			rowList.add(new SchemaDataItem(i, 0, xAxis[i], 0, xAxis[i]));
		//			
		//			final double value = data[i] = i * 10;
		//			rowList.add(new SchemaDataItem(i, 0, xAxis[i], 0, value));
		//		}


		//2D data
		//		double[] xAxis = new double[15];
		//		double[] yAxis = new double[7];
		//		double[][] data = new double[xAxis.length][yAxis.length];
		//		columnNames.add("Y \\ X");
		//		
		//		for (int j = 0; j < yAxis.length; j++) {
		//			yAxis[j] = j;
		//
		//			List<SchemaDataItem> rowList = new ArrayList<SchemaDataItem>();
		//			dataItemsList.add(rowList);
		//			rowList.add(new SchemaDataItem(0, j, 0, yAxis[j], yAxis[j]));
		//			
		//			for (int i = 0; i < xAxis.length; i++) {
		//				xAxis[i] = i;
		//				if (j == 0) {
		//					columnNames.add("X:" + String.valueOf(xAxis[i]));
		//				}
		//				final double value = data[i][j] = i * j;
		//				rowList.add(new SchemaDataItem(i + 1, j, xAxis[i], yAxis[j], value));
		//			}
		//		}

		updateColumns(columnNames);

		viewer.refresh();
	}

	/**
	 * Sets the data to be displayed in table. 
	 * @param plotData 
	 */
	public void setMataDataSchema(Object plotData) {

		clearData();
		final ArrayList<String> columnNames = new ArrayList<String>();
		columnNames.add("Name");
		columnNames.add("Value");


		if (plotData != null && plotData instanceof au.gov.ansto.bragg.datastructures.core.plot.Plot) {
			au.gov.ansto.bragg.datastructures.core.plot.Plot group = 
				(au.gov.ansto.bragg.datastructures.core.plot.Plot)plotData;

			List<Data> dataList = group.getCalculationData();
			int rowIndex = 0;
			boolean isFirstRow = true;
			for (Data data : dataList){
				IArray array = null;
				String dataName = data.getTitle();
				if (dataName == null)
					dataName = data.getShortName();
				try {
					array = data.getData();
				} catch (Exception e) {
					plot.handleException(e);
					return;
				}

				int[] shape = array.getShape();
				int rank = array.getRank();
				Class<?> type = array.getElementType();
				IIndex index = array.getIndex();

				if (rank == 1) {
					//get X Axis array info
					//add columns
					IArrayIterator arrayIterator = array.getIterator();
					isFirstRow = true;
					while (arrayIterator.hasNext()){
						Object value = arrayIterator.getDoubleNext();
						List<SchemaDataItem> rowList = new ArrayList<SchemaDataItem>();
						dataItemsList.add(rowList);
						if (isFirstRow){
							rowList.add(new SchemaDataItem(rowIndex, 0, dataName, String.class, dataName, String.class, dataName, String.class));
							isFirstRow = false;
						}
						else
							rowList.add(new SchemaDataItem(rowIndex, 0, "", String.class, "", String.class, "", String.class));
						rowList.add(new SchemaDataItem(rowIndex, 1, value, type, value, type, value, type));
						rowIndex ++;
					}
//					for (int i = 0; i < shape[0]; i++) {
//						//extract value
//						index.set(i);
//						Object value = array.getObject(index);
//
//						//extract X Axis info
//						xAxisIndex.set(i);
//						Object xAxisValue = xAxisArray.getObject(xAxisIndex);
//
//
//						//add data items to table model
//						List<SchemaDataItem> rowList = new ArrayList<SchemaDataItem>();
//						dataItemsList.add(rowList);
//						rowList.add(new SchemaDataItem(i, 0, xAxisValue, xAxisType, value, type, xAxisValue, xAxisType));
//						rowList.add(new SchemaDataItem(i, 1, xAxisValue, xAxisType, value, type, value, type));
//					}
				} else if (rank == 2) {

					//First column for Y Axis values
					columnNames.add("Y");

					//get X Axis array info
//					final Array xAxisArray = axesArrayList.get(1);
//					final Class<?> xAxisType = xAxisArray.getElementType();
//					final int[] xAxisShape = xAxisArray.getShape();
//					final Index xAxisIndex = Factory.createIndex(xAxisShape);
//
//					List<Object> xAxisValueList = new ArrayList<Object>();
//
//					//get Y Axis array info
//					final Array yAxisArray = axesArrayList.get(0);
//					final Class<?> yAxisType = yAxisArray.getElementType();
//					final int[] yAxisShape = yAxisArray.getShape();
//					final Index yAxisIndex = Factory.createIndex(yAxisShape);
//
//					for (int i = 0; i < shape[0]; i++) {
//						index.set(i);
//
//						//extract Y Axis info
//						yAxisIndex.set(i);
//						Object yAxisValue = yAxisArray.getObject(yAxisIndex);
//
//
//						List<SchemaDataItem> rowList = new ArrayList<SchemaDataItem>();
//						dataItemsList.add(rowList);
//						//Y Axis value for first column in the table
//						rowList.add(new SchemaDataItem(i, 0, null, xAxisType, yAxisValue, yAxisType, "Y:" + getString(yAxisValue, yAxisType), String.class));
//
//
//						for (int j = 0; j < shape[1]; j++) {
//							index.set1(j);
//							Object value = array.getObject(index);
//
//							if (i == 0) {
//								//extract X Axis info
//								xAxisIndex.set(j);
//								Object xAxisValue = xAxisArray.getObject(xAxisIndex);
//								xAxisValueList.add(xAxisValue);
//
//								//create column title with X Axis value
//								columnNames.add("X:" + getString(xAxisValue, xAxisType));
//							}
//
//							rowList.add(new SchemaDataItem(i, j + 1, xAxisValueList.get(j), xAxisType, yAxisValue, yAxisType, value, type));
//						}
//					}
				}

				else {
					plot.handleException(new IllegalArgumentException("Not possible to create schema for the data object. Rank = " + rank));
					return;
				}
			}
		}


		updateColumns(columnNames);

		viewer.refresh();
	}

	/**
	 * Converts value object to string representation.
	 * @param value a value object.
	 * @param type class type object of the value.
	 * @return string representation of the value.
	 */
	private static String getString(Object value, Class<?> type) {
		if (type == null) {
			return String.valueOf(value);
		}
		if (type.isAssignableFrom(Double.class)) {
			return Double.toString((Double)value);
		} else if (type.isAssignableFrom(Integer.class)) {
			return Integer.toString((Integer)value);
		} else if (type.isAssignableFrom(Float.class)) {
			return Float.toString((Float)value);
		}
		return String.valueOf(value);
	}

	/**
	 * Creates columns for current data structure.
	 * @param columnNames column names.
	 */
	private void updateColumns(List<String> columnNames) {

		for (String columnName : columnNames) {
			TableColumn tableColumn = new TableColumn(viewer.getTable(), SWT.NONE);
			tableColumn.setText(columnName);
			tableColumn.setWidth(70);
			//			tableColumn.setMoveable(true);
			tableColumn.addListener(SWT.Selection, sortSelectionListener);

			tableColumns.add(tableColumn);
		}
	}

	/**
	 * Clear data from the table view.
	 */
	public void clearData() {
		final TableColumn[] columns = viewer.getTable().getColumns();
		for (TableColumn tableColumn : columns) {
			tableColumn.removeListener(SWT.Selection, sortSelectionListener);
			tableColumn.dispose();
		}
		tableColumns.clear();
		dataItemsList.clear();
	}


	class SchemaListContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return dataItemsList.toArray();
		}
	}

	class SchemaLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			List<SchemaDataItem> rowList = (List<SchemaDataItem>) obj;

			SchemaDataItem statisticItem = rowList.get(index);

			return statisticItem.getString();
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
			//			StatisticItem statisticItem = (StatisticItem) obj;
			//			Image icon = new Image(this.getShell().getDisplay(),  parent.getParent().getParent().getWorkspaceDir() + "/" + algorithm.getIcon());
			//			switch (index) {
			//			case 1:
			//			case 0:
			//				return null;
			//			default:
			//				return PlatformUI.getWorkbench().
			//				getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			//			}
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
		 * @param column a column to be sorted
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

			int columnIndex = getColumnIndex(column);
			if (columnIndex == -1) {
				return 0;
			}

			int result = 0;

			if (e1 instanceof List && e2 instanceof List) {
				List<SchemaDataItem> list1 = (List<SchemaDataItem>) e1;
				List<SchemaDataItem> list2 = (List<SchemaDataItem>) e2;

				final SchemaDataItem schemaDataItem1 = list1.get(columnIndex);
				final SchemaDataItem schemaDataItem2 = list2.get(columnIndex);

				Object value1 = schemaDataItem1.getValue();
				Object value2 = schemaDataItem2.getValue();


				if (value1 instanceof Comparable) {
					result = ((Comparable<Object>)value1).compareTo(value2);
				} else {
					// use the comparator to compare the values as string
					result = getComparator().compare(schemaDataItem1.getString(), schemaDataItem2.getString());
				}

			} else {

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
				result = getComparator().compare(name1, name2);

			}

			if (direction == SWT.UP) {
				//opposite direction
				result = -result;
			}
			return result;
		}
	}


	private static class SchemaDataItem {
		private final int row, column;
		private final Object xAxis, yAxis;
		private Class<?> xAxisType, yAxisType;
		//		private String key;
		private Object value;
		private Class<?> type;
		private String stringValue;


		public SchemaDataItem(int row, int column, 
				Object xAxis, Class<?> xAxisType, 
				Object yAxis, Class<?> yAxisType,
				Object value, Class<?> type) {
			this.row = row;
			this.column = column;
			this.xAxis = xAxis;
			this.xAxisType = xAxisType;
			this.yAxis = yAxis;
			this.yAxisType = yAxisType;
			this.value = value;
			this.type = type;

			stringValue = SchemaPlotComposite.getString(value, type);
		}

		/**
		 * @return the row
		 */
		public int getRow() {
			return row;
		}

		/**
		 * @return the column
		 */
		public int getColumn() {
			return column;
		}

		/**
		 * @return the xAxis
		 */
		public Object getXAxis() {
			return xAxis;
		}

		/**
		 * @return the yAxis
		 */
		public Object getYAxis() {
			return yAxis;
		}

		/**
		 * @return the xAxisType
		 */
		public Class<?> getXAxisType() {
			return xAxisType;
		}

		/**
		 * @return the yAxisType
		 */
		public Class<?> getYAxisType() {
			return yAxisType;
		}

		/**
		 * @return the type
		 */
		public Class<?> getType() {
			return type;
		}

		public Object getValue() {
			return value;
		}

		public String getString() {
			return stringValue;
		}
	}

}
