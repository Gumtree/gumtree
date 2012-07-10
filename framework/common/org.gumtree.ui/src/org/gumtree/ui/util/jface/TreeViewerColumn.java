package org.gumtree.ui.util.jface;

import org.eclipse.swt.widgets.TreeColumn;

public class TreeViewerColumn implements ITreeViewerColumn {

	private ITreeColumn column;

	private TreeColumn widget;

	private int originalWidth;

	private boolean isVisible;

	public TreeViewerColumn(ITreeColumn column, TreeColumn widget) {
		this.column = column;
		this.widget = widget;
		isVisible = true;
	}

	public ITreeColumn getColumn() {
		return column;
	}

	public TreeColumn getWidget() {
		return widget;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean visible) {
		if(visible && !isVisible()) {
			widget.setWidth(originalWidth);
			widget.setResizable(true);
			isVisible = true;
		} else if(!visible && isVisible()){
			originalWidth = widget.getWidth();
			widget.setWidth(0);
			widget.setResizable(false);
			isVisible = false;
		}
	}

	public boolean equals(Object object) {
		if(object instanceof TreeViewerColumn) {
			return ((TreeViewerColumn)object).getColumn().equals(getColumn());
		}
		return false;
	}

	public String toString() {
		return getColumn().toString();
	}

}
