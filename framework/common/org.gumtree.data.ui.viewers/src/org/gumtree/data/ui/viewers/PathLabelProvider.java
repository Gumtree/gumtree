package org.gumtree.data.ui.viewers;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.ui.viewers.internal.InternalImage;

public class PathLabelProvider extends LabelProvider implements ITableLabelProvider {

	private IDataset dataset;
	
	public PathLabelProvider() {
		super();
	}
	
	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof IPath && dataset != null) {
			IContainer container = dataset.getRootGroup().findObjectByPath(
					(IPath) element);
			if (container instanceof IGroup) {
				return InternalImage.GROUP.getImage();
			} else if (container instanceof IDataItem) {
				return InternalImage.DATA_ITEM.getImage();
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IPath) {
			return ((IPath) element).getValue();
		}
		return "";
	}
	
	public void setDataset(IDataset dataset) {
		this.dataset = dataset;
	}

}
