package org.gumtree.widgets.swt.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider implements ITreeContentProvider {

	protected static Object[] EMPTY_ARRAY = new Object[0];
	
	private TreeViewer viewer;

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof ITreeNode[]) {
			return (ITreeNode[]) inputElement;
		} else if (inputElement instanceof ITreeNode) {
			return new Object[] { inputElement };
		}
		return EMPTY_ARRAY;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITreeNode) {
			return ((ITreeNode) parentElement).getChildren();
		}
		return EMPTY_ARRAY;
	}
	
	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
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
