package org.gumtree.data.ui.viewers;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.grid.data.DefaultCornerDataProvider;
import net.sourceforge.nattable.grid.data.DefaultRowHeaderDataProvider;
import net.sourceforge.nattable.grid.layer.DefaultGridLayer;

public class ArrayGridLayer extends DefaultGridLayer {

	public ArrayGridLayer(IDataProvider bodyDataProvider) {
		super(true);
		IDataProvider columnHeaderDataProvider = new ColumnHeaderDataProvider(bodyDataProvider);
		IDataProvider rowHeaderDataProvider = new RowHeaderDataProvider(bodyDataProvider);
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		init(bodyDataProvider, columnHeaderDataProvider, rowHeaderDataProvider, cornerDataProvider);
	}
	
	private class ColumnHeaderDataProvider implements IDataProvider {

		private IDataProvider bodyDataProvider;
		
		public ColumnHeaderDataProvider(IDataProvider bodyDataProvider) {
			this.bodyDataProvider = bodyDataProvider;
		}
		
		public Object getDataValue(int columnIndex, int rowIndex) {
			return columnIndex;
		}

		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			throw new UnsupportedOperationException();
		}

		public int getColumnCount() {
			return bodyDataProvider.getColumnCount();
		}

		public int getRowCount() {
			return 1;
		}
		
	}

	private class RowHeaderDataProvider extends DefaultRowHeaderDataProvider {

		public RowHeaderDataProvider(IDataProvider bodyDataProvider) {
			super(bodyDataProvider);
		}

		public Object getDataValue(int columnIndex, int rowIndex) {
			return rowIndex;
		}
		
	}
	
}


