package org.gumtree.widgets.swt.tree;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;

public class TreePathLabelProvider extends BaseLabelProvider implements
		ITreePathLabelProvider {

	@Override
	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		if (elementPath.getLastSegment() instanceof ITreeNode) {
			ITreeNode treeNode = (ITreeNode) elementPath.getLastSegment();
			label.setText(treeNode.getText());
			label.setImage(treeNode.getImage());
			label.setBackground(treeNode.getBackground());
			label.setForeground(treeNode.getForeground());
		}
	}

}
