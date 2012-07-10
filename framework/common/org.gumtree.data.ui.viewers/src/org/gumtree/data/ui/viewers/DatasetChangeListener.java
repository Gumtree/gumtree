/**
 * 
 */
package org.gumtree.data.ui.viewers;

import org.gumtree.data.interfaces.IDataset;

/**
 * @author nxi
 *
 */
public interface DatasetChangeListener {

	public void datasetAdded(IDataset dataset);
	
	public void datasetRemoved(IDataset dataset);
	
}
