package org.gumtree.widgets.swt.tree;

import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class TreePathContentProvider implements ITreePathContentProvider {

	protected static Object[] EMPTY_ARRAY = new Object[0];

	private TreeViewer viewer;

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ITreeNode[]) {
			return (ITreeNode[]) inputElement;
		} else if (inputElement instanceof ITreeNode) {
			return new Object[] { inputElement };
		}
		return EMPTY_ARRAY;
	}

	@Override
	public Object[] getChildren(TreePath parentPath) {
		if (parentPath.getLastSegment() instanceof ITreeNode) {
			return ((ITreeNode) parentPath.getLastSegment()).getChildren();
		}
		return EMPTY_ARRAY;
	}

	@Override
	public TreePath[] getParents(Object element) {
		return null;
	}
	
	@Override
	public boolean hasChildren(TreePath path) {
		Object[] children = getChildren(path);
		return children == null || children.length != 0;
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(viewer instanceof TreeViewer) {
			this.viewer = (TreeViewer)viewer;
		}
	}

	@Override
	public void dispose() {
		viewer = null;
	}

	protected TreeViewer getViewer() {
		return viewer;
	}

}
