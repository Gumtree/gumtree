package org.gumtree.control.ui.viewer.parts;

import org.eclipse.ui.PlatformUI;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.model.ModelStatus;
import org.gumtree.control.ui.viewer.model.INodeSet;
import org.gumtree.control.ui.viewer.model.SicsModelNode;
import org.gumtree.ui.util.jface.ITreeNode;

public class HierarchicalContentProvider extends ControlViewerContentProvider {

	private ISicsModel model;

	private SicsModelNode rootNode;
	
	public HierarchicalContentProvider(INodeSet filter) {
		super(filter);
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITreeNode) {
			ITreeNode parentNode = (ITreeNode)parentElement;
			return parentNode.getChildren();
		}
		return EMPTY_ARRAY;
	}

	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof ISicsModel) {
			model = (ISicsModel)inputElement;
//			controller.addComponentListener(this);
			rootNode = new SicsModelNode(model, getFilter());
			rootNode.setViewer(getViewer());
			return new Object[] { rootNode };
		} else {
			return getChildren(inputElement);
		}
	}

	public void componentStatusChanged(ModelStatus newStatus) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if(getViewer() != null && !getViewer().getControl().isDisposed() && rootNode != null) {
					getViewer().update(rootNode, null);
					getViewer().getControl().redraw();
				}
			}
		});
	}

	public void dispose() {
		if (rootNode != null) {
			rootNode.dispose();
			rootNode = null;
		}
		super.dispose();
	}
	
}
