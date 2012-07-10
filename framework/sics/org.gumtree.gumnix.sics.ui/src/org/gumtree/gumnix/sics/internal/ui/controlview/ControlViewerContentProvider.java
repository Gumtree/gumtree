package org.gumtree.gumnix.sics.internal.ui.controlview;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.events.IComponentControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ControlViewerContentProvider implements ITreeContentProvider, IComponentControllerListener {

	protected static Object[] EMPTY_ARRAY = new Object[0];
	
	private Logger logger = LoggerFactory.getLogger(ControlViewerContentProvider.class);
	
	private ISicsProxyListener proxyListener;
	
	private TreeViewer viewer;
	
	private INodeSet filter;
	
	public ControlViewerContentProvider(INodeSet filter) {
		proxyListener = new ProxyListener();
		this.filter = filter;
		SicsCore.getDefaultProxy().addProxyListener(proxyListener);
	}

	public void dispose() {
		SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
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
	
	public Object getParent(Object element) {
		return null;
	}
	
	public boolean hasChildren(Object element) {
		return getChildren(element).length != 0;
	}
	
	protected TreeViewer getViewer() {
		return viewer;
	}

	protected INodeSet getFilter() {
		return filter;
	}

	class ProxyListener extends SicsProxyListenerAdapter {
		public void proxyConnected() {
			logger.debug("Proxy connected");
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if(viewer != null && !viewer.getControl().isDisposed()) {
						viewer.setInput(SicsCore.getSicsController());
						viewer.refresh();
						viewer.expandToLevel(2);
						logger.debug("Viewer refreshed");
					}
				}
			});
		}
	}
	
}
