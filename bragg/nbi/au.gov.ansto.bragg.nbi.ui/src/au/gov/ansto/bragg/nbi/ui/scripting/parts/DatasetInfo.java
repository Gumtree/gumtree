/**
 * 
 */
package au.gov.ansto.bragg.nbi.ui.scripting.parts;

import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.util.string.StringUtils;

import au.gov.ansto.bragg.nbi.ui.scripting.ScriptingConstants;

/**
 * @author nxi
 *
 */
public class DatasetInfo {

	private IDataset dataset;
	private String fileID;
	private String location;
	private List<ColumnProvider> propertyList;
	
	public DatasetInfo(IDataset dataset) {
		this.dataset = dataset;
		this.location = dataset.getLocation();
		String title = location.substring(location.length() - 14, location.length() - 7);
		try {
			int titleValue = Integer.valueOf(title);
			title = String.valueOf(titleValue);
		} catch (Exception e) {
		}
		this.fileID = title;
		dataset.setTitle(title);
		List<String> dataSourceColumns = StringUtils.split(ScriptingConstants.SCRIPTING_DATASOURCE_COLUMNS.getValue(), ",");
		propertyList = new ArrayList<DatasetInfo.ColumnProvider>();
		for (String column : dataSourceColumns) {
			if (column == null || column.trim().length() == 0) {
				continue;
			}
			String[] columnItems = column.split(":");
			IContainer container = null;;
			try {
				container = dataset.getRootGroup().findContainerByPath(columnItems[1]);
				if (container != null) {
					if (container instanceof IDataItem) {
						propertyList.add(new ColumnProvider(columnItems[0], ((IDataItem) container).getData().toString(), 
								Integer.valueOf(columnItems[2])));
					} else {
						propertyList.add(new ColumnProvider(columnItems[0], container.toString(), Integer.valueOf(columnItems[2])));
					}
				} else {
					propertyList.add(new ColumnProvider(columnItems[0], "N/A", Integer.valueOf(columnItems[2])));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public class ColumnProvider {
		private String uiTitle;
		private String value;
		private int width;
		
		public ColumnProvider(String title, String value, int width) {
			this.uiTitle = title;
			this.value = value;
			this.width = width;
		}
		/**
		 * @return the uiTitle
		 */
		public String getUiTitle() {
			return uiTitle;
		}
		/**
		 * @param uiTitle the uiTitle to set
		 */
		public void setUiTitle(String uiTitle) {
			this.uiTitle = uiTitle;
		}
		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}
		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}
		/**
		 * @return the width
		 */
		public int getWidth() {
			return width;
		}
		/**
		 * @param width the width to set
		 */
		public void setWidth(int width) {
			this.width = width;
		}
		
	}
	
	/**
	 * @return the dataset
	 */
	public IDataset getDataset() {
		return dataset;
	}

	public String getFileID() {
		return fileID;
	}

	public String getLocation() {
		return location;
	}

	/**
	 * @return the propertyList
	 */
	public List<ColumnProvider> getPropertyList() {
		return propertyList;
	}


//	/**
//	 * @param dataset the dataset to set
//	 */
//	public void setDataset(IDataset dataset) {
//		this.dataset = dataset;
//	}
}
