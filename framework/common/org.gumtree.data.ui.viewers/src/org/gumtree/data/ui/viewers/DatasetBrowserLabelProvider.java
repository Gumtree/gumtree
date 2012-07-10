package org.gumtree.data.ui.viewers;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.ui.viewers.internal.InternalImage;

public class DatasetBrowserLabelProvider extends LabelProvider {

	public Image getImage(Object element) {
		if (element instanceof IDataset) {
			return InternalImage.FILE.getImage();
		} else if (element instanceof IGroup) {
			return InternalImage.GROUP.getImage();
		} else if (element instanceof IDataItem) {
			return InternalImage.DATA_ITEM.getImage();
		}
		return super.getImage(element);
	}
	
	public String getText(Object element) {
		if (element instanceof IDataset) {
			return ((IDataset) element).getLocation();
		} else if (element instanceof IContainer) {
			return ((IContainer) element).getShortName();
		}
		return super.getText(element);
	}
	
}
