package org.gumtree.ui.scripting.viewer;

import java.text.SimpleDateFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.gumtree.ui.scripting.viewer.HistoryContentProvider.CommandHistory;

public class HistoryLableProvider extends LabelProvider implements ITableLabelProvider {

	private SimpleDateFormat format;
	
	public HistoryLableProvider() {
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof CommandHistory) {
			CommandHistory history = (CommandHistory) element;
			if (columnIndex == 0) {
				return format.format(history.getTimestamp());
			} else if (columnIndex == 1) {
				return history.getCommand();
			}
		}
		return "";
	}

}
