package org.gumtree.control.ui.viewer.parts;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.gumtree.ui.util.jface.ITreeNode;

public class ControlViewerLabelProvider extends LabelProvider implements
		ITableLabelProvider, ITableColorProvider {
	
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

	public Color getBackground(final Object element, int columnIndex) {
		if(element instanceof ITreeNode) {
			ITreeNode node = (ITreeNode)element;
//			SafeUIRunner.asyncExec(new SafeRunnable() {
//				@Override
//				public void run() throws Exception {
//					fireLabelProviderChanged(new LabelProviderChangedEvent(ControlViewerLabelProvider.this));	
//				}
//			});
			return node.getColumnBackground(columnIndex);
		}
		return null;
	}

	public Color getForeground(Object element, int columnIndex) {
		if(element instanceof ITreeNode) {
			ITreeNode node = (ITreeNode)element;
			return node.getColumnForeground(columnIndex);
		}
		return null;
	}

}
