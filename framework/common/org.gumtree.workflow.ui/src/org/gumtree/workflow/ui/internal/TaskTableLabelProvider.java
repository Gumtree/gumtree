package org.gumtree.workflow.ui.internal;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class TaskTableLabelProvider extends LabelProvider implements ITableLabelProvider {

	private static final String EMPTY_STRING = "";
	
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof String) {
			if (element.toString().equals("Experiment")) {
				return InternalImage.CHECKED.getImage();
			} else if (element.toString().equals("Sample")) {
				return InternalImage.POINTER.getImage();
			} else if (element.toString().equals("Acquisition") ||
					element.toString().equals("Analysis") ||
					element.toString().equals("Report")) {
				return InternalImage.UNAVAILABLE.getImage();
			}
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof String) {
			return element.toString();
		}
		return EMPTY_STRING;
	}

}
