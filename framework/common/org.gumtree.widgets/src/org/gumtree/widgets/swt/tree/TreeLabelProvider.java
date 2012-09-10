package org.gumtree.widgets.swt.tree;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class TreeLabelProvider extends LabelProvider implements ITableLabelProvider {

	private static final String EMPTY_STRING = "";

	public Image getImage(Object element) {
		if(element instanceof ITreeNode) {
			return ((ITreeNode)element).getImage();
		}
		return null;
	}

	public String getText(Object element) {
		if(element instanceof ITreeNode) {
			return ((ITreeNode)element).getText();
		}
		return EMPTY_STRING;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if(element instanceof ITreeNode) {
			return ((ITreeNode)element).getColumnImage(columnIndex);
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if(element instanceof ITreeNode) {
			return ((ITreeNode)element).getColumnText(columnIndex);
		}
		return EMPTY_STRING;
	}

}
