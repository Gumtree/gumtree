package org.gumtree.cs.sics.ui.browser;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class SicsTableTreeViewerLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {

	@Override
	public Color getForeground(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getBackground(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getText(Object element) {
		if (element instanceof ISicsTreeNode) {
			return ((ISicsTreeNode) element).getName();
		}
		return super.getText(element);
	}

}
