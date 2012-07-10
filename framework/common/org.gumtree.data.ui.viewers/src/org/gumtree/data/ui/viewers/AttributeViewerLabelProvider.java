package org.gumtree.data.ui.viewers;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.gumtree.data.interfaces.IAttribute;

public class AttributeViewerLabelProvider extends LabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof IAttribute) {
			IAttribute attribute = (IAttribute) element;
			if (columnIndex == 0) {
			} else if (columnIndex == 1) {
			}
			return null;
		}
		return getImage(element);
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IAttribute) {
			IAttribute attribute = (IAttribute) element;
			if (columnIndex == 0) {
				return attribute.getName();
			} else if (columnIndex == 1) {
				return attribute.getStringValue();
			}
		}
		return getText(element);
	}

}
