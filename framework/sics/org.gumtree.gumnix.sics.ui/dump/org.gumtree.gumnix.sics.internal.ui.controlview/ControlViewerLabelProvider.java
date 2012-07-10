package org.gumtree.gumnix.sics.internal.ui.controlview;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.gumtree.ui.util.ITreeNode;

public class ControlViewerLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	
	public Image getColumnImage(Object element, int columnIndex) {
		if(element instanceof ITreeNode) {
			ITreeNode node = (ITreeNode)element;
			return node.getColumnImage(columnIndex);
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if(element instanceof ITreeNode) {
			ITreeNode node = (ITreeNode)element;
			return node.getColumnText(columnIndex);
		}
		return null;
	}

}
