package org.gumtree.ui.scripting.tools;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.gumtree.ui.scripting.tools.AttributeContentProvider.EngineAttribute;

public class AttributeLabelProvider extends LabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof EngineAttribute) {
			EngineAttribute attribute = (EngineAttribute) element;
			if (columnIndex == 0) {
				return attribute.getName();
			} else if (columnIndex == 1) {
				if (attribute.getValue() != null) {
					return attribute.getValue().getClass().getSimpleName();
				}
			}
		}
		return "";
	}

}
