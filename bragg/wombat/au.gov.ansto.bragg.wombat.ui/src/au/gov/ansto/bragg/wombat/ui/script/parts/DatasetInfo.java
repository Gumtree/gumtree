/**
 * 
 */
package au.gov.ansto.bragg.wombat.ui.script.parts;

import org.gumtree.data.interfaces.IDataset;

/**
 * @author nxi
 *
 */
public class DatasetInfo {

	private IDataset dataset;
	private String fileID;
	private String location;

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

//	/**
//	 * @param dataset the dataset to set
//	 */
//	public void setDataset(IDataset dataset) {
//		this.dataset = dataset;
//	}
}
