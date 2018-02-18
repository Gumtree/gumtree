package org.gumtree.control.ui.viewer.parts;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.ui.viewer.model.INodeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ControlViewerContentProvider implements ITreeContentProvider {

	protected static Object[] EMPTY_ARRAY = new Object[0];
	
	private Logger logger = LoggerFactory.getLogger(ControlViewerContentProvider.class);
	
	private ISicsProxyListener proxyListener;
	
	private TreeViewer viewer;
	
	private INodeSet filter;
	
	public ControlViewerContentProvider(INodeSet filter) {
		proxyListener = new ProxyListener();
		this.filter = filter;
		SicsManager.getSicsProxy().addProxyListener(proxyListener);
	}

	public void dispose() {
		SicsManager.getSicsProxy().removeProxyListener(proxyListener);
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

	class ProxyListener implements ISicsProxyListener {
		public void connect() {
			logger.debug("Proxy connected");
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if(viewer != null && !viewer.getControl().isDisposed()) {
						viewer.setInput(SicsManager.getSicsModel());
						viewer.refresh();
						viewer.expandToLevel(2);
						logger.debug("Viewer refreshed");
					}
				}
			});
		}

		@Override
		public void disconnect() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void interrupt(boolean isInterrupted) {
			// TODO Auto-generated method stub
			
		}
	}
	
}
