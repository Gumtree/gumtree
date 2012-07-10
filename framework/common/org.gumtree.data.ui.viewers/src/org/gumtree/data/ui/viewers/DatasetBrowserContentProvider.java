package org.gumtree.data.ui.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

public class DatasetBrowserContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY_ARRAY = new Object[0];
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			List<?> list = (List<?>) inputElement;
			return list.toArray(new Object[list.size()]);
		}
		return EMPTY_ARRAY;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IDataset) {
			List<IGroup> groupList = ((IDataset) parentElement).getRootGroup().getGroupList();
			return groupList.toArray(new Object[groupList.size()]);
		} else if (parentElement instanceof IGroup) {
			List<IGroup> groupList = ((IGroup) parentElement).getGroupList();
			List<IDataItem> dataItemList = ((IGroup) parentElement).getDataItemList();
			List<IContainer> children = new ArrayList<IContainer>();
			children.addAll(groupList);
			children.addAll(dataItemList);
			return children.toArray(new Object[children.size()]);
		}
		return EMPTY_ARRAY;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}
	
	public void dispose() {
	}

}
