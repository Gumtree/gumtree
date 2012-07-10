package org.gumtree.ui.util.jface;

import org.eclipse.swt.widgets.TreeColumn;

public interface ITreeViewerColumn {

	public ITreeColumn getColumn();

	public TreeColumn getWidget();

	public boolean isVisible();

	public void setVisible(boolean visible);

}
