package org.gumtree.ui.util.jface;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider implements ITreeContentProvider {

	protected static Object[] EMPTY_ARRAY = new Object[0];
	
	private TreeViewer viewer;

	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof ITreeNode[]) {
			return (ITreeNode[]) inputElement;
		} else if (inputElement instanceof ITreeNode) {
			return new Object[] { inputElement };
		}
		return new Object[0];
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITreeNode) {
			return ((ITreeNode) parentElement).getChildren();
		}
		return EMPTY_ARRAY;
	}
	
	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children == null || children.length != 0;
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(viewer instanceof TreeViewer) {
			this.viewer = (TreeViewer)viewer;
		}
	}
	
	protected TreeViewer getViewer() {
		return viewer;
	}

}
