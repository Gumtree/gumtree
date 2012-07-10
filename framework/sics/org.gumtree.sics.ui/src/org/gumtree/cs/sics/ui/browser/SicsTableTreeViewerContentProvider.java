package org.gumtree.cs.sics.ui.browser;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class SicsTableTreeViewerContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ISicsTreeNode) {
			return getChildren(inputElement);
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ISicsTreeNode) {
			return ((ISicsTreeNode) parentElement).getChildren();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ISicsTreeNode) {
			return ((ISicsTreeNode) element).hasChildren();
		}
		return false;
	}
	
}
