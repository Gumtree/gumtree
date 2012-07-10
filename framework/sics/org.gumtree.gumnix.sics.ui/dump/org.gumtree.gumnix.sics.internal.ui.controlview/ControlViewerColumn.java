package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.swt.widgets.TreeColumn;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewerConstants.Column;

public class ControlViewerColumn {

	private Column column;
	
	private TreeColumn widget;
	
	private int originalWidth;
	
	private boolean isVisible;
	
	public ControlViewerColumn(Column column, TreeColumn widget) {
		this.column = column;
		this.widget = widget;
		isVisible = true;
	}
	
	public Column getColumn() {
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
		if(object instanceof ControlViewerColumn) {
			return ((ControlViewerColumn)object).getColumn().equals(getColumn());
		}
		return false;
	}
	 
	public String toString() {
		return getColumn().toString();
	}
	
}
