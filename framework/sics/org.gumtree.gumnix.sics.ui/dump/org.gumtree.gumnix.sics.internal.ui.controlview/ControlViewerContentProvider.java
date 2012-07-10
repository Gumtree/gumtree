package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.IComponentListener;
import org.gumtree.gumnix.sics.control.IInstrumentController;
import org.gumtree.gumnix.sics.control.IComponentController.ComponentStatus;
import org.gumtree.gumnix.sics.core.ISicsManager;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.ui.util.ITreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlViewerContentProvider implements ITreeContentProvider, IComponentListener {

	private static final Logger logger = LoggerFactory.getLogger(ControlViewerContentProvider.class);

	private static Object[] EMPTY_ARRAY = new Object[0];

	private TreeViewer viewer;

	private IInstrumentController controller;

	private InstrumentControllerNode rootNode;

	private ISicsProxyListener proxyListener;

	public ControlViewerContentProvider() {
		proxyListener = new ProxyListener();
		ISicsManager.INSTANCE.proxy().addProxyListener(proxyListener);
	}

	public void dispose() {
		ISicsManager.INSTANCE.proxy().removeProxyListener(proxyListener);
		proxyListener = null;
//		if(controller != null) {
//			controller.removeComponentListener(this);
//		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(viewer instanceof TreeViewer) {
			this.viewer = (TreeViewer)viewer;
		}
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITreeNode) {
			ITreeNode parentNode = (ITreeNode)parentElement;
			return parentNode.getChildren();
		}
		return EMPTY_ARRAY;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length != 0;
	}

	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof IInstrumentController) {
			controller = (IInstrumentController)inputElement;
//			controller.addComponentListener(this);
			rootNode = new InstrumentControllerNode(controller, viewer);
			return new Object[] { rootNode };
		} else {
			return getChildren(inputElement);
		}
	}

	public void componentStatusChanged(ComponentStatus newStatus) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if(viewer != null && !viewer.getControl().isDisposed() && rootNode != null) {
					viewer.update(rootNode, null);
					viewer.getControl().redraw();
				}
			}
		});
	}

	class ProxyListener extends SicsProxyListenerAdapter {
		public void proxyConnected() {
//			logger.debug("Proxy connected");
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if(viewer != null && !viewer.getControl().isDisposed()) {
//						viewer.setInput(ISicsManager.INSTANCE.control().instrument().getInstrumentController());
						viewer.refresh();
						viewer.expandToLevel(2);
//						logger.debug("Viewer refreshed");
					}
				}
			});
		}
	}

}
