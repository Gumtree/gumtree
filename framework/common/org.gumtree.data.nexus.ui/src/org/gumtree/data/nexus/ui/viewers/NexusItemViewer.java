/**
 * 
 */
package org.gumtree.data.nexus.ui.viewers;

import java.io.IOException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.ui.viewers.ArrayViewer;
import org.gumtree.data.ui.viewers.DataItemViewer;

/**
 * @author nxi
 *
 */
public class NexusItemViewer extends DataItemViewer {

	private List<IAxis> axes;
	private IDataItem varianceItem;
	
	public NexusItemViewer(Composite parent, int style) {
		super(parent, style);
	}

	public void setAxes(List<IAxis> axes) {
		this.axes = axes; 
	}
	
	@Override
	protected ArrayViewer createArrayViewer(Composite parent, int style) {
		NexusArrayViewer viewer = new NexusArrayViewer(parent, style);
		viewer.setAxes(axes);
		if  (varianceItem != null) {
			try {
				viewer.setVariance(varianceItem.getData());
			} catch (IOException e) {
				e.printStackTrace();
				viewer.setVariance(null);
			}
		} else {
			viewer.setVariance(null);
		}
		return viewer;
	}

	public void setVarianceItem(IDataItem varianceItem) {
		this.varianceItem = varianceItem;
	}

	public IDataItem getVarianceItem() {
		return varianceItem;
	}
}
