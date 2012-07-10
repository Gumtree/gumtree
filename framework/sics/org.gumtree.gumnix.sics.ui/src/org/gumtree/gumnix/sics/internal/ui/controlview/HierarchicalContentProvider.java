package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;
import org.gumtree.gumnix.sics.ui.util.SicsControllerNode;
import org.gumtree.ui.util.jface.ITreeNode;

public class HierarchicalContentProvider extends ControlViewerContentProvider {

	private ISicsController controller;

	private SicsControllerNode rootNode;
	
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
		if(inputElement instanceof ISicsController) {
			controller = (ISicsController)inputElement;
//			controller.addComponentListener(this);
			rootNode = new SicsControllerNode(controller, getFilter());
			rootNode.setViewer(getViewer());
			return new Object[] { rootNode };
		} else {
			return getChildren(inputElement);
		}
	}

	public void componentStatusChanged(ControllerStatus newStatus) {
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
