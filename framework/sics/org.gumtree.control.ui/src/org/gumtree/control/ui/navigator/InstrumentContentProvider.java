package org.gumtree.control.ui.navigator;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroPart;
import org.gumtree.control.core.ISicsModel;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.ui.editors.ControlModelEditorInput;
import org.gumtree.control.ui.viewer.model.SicsModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentContentProvider implements ITreeContentProvider {

	static final String ID_EDITOR = "org.gumtree.control.ui.editors.ControlModelEditor";
	private static final String PROP_OPEN_EDITOR_PAGE = "gumtree.sics.controlModelEditorPageID";

	private static Object[] EMPTY_ARRAY = new Object[0];

	private static Logger logger;

	private StructuredViewer viewer;

	private IWorkspaceRoot parentElement;

	private ISicsModel model;

	private SicsProxyListenerAdapter proxyListener;

	public InstrumentContentProvider() {
		proxyListener = new ProxyListener();
		SicsManager.getSicsProxy().addProxyListener(proxyListener);
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IWorkspaceRoot) {
			this.parentElement = (IWorkspaceRoot) parentElement;
			model = SicsManager.getSicsModel();
			if (model != null) {
				SicsModelNode sicsModelNode = new SicsModelNode(model, null);
				sicsModelNode.setViewer(viewer);
				return new Object[] { sicsModelNode };
			}
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
		return getChildren(inputElement);
	}

	public void dispose() {
		SicsManager.getSicsProxy().removeProxyListener(proxyListener);
		proxyListener = null;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (StructuredViewer) viewer;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(InstrumentContentProvider.class);
		}
		return logger;
	}

	class ProxyListener extends SicsProxyListenerAdapter {

		@Override
		public void connect() {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (viewer != null && !viewer.getControl().isDisposed()) {
						viewer.refresh(parentElement);
					}
				}
			});
		}

		@Override
		public void modelUpdated(final ISicsModel sicsModel) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							getLogger().error("Failed to open SICS control editor", exception);
						}

						public void run() throws Exception {
							String pageId = System.getProperty(PROP_OPEN_EDITOR_PAGE);
							IWorkbenchPage page = findPage(pageId);
							IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
							boolean wasStandby = PlatformUI.getWorkbench().getIntroManager().isIntroStandby(introPart);
							IEditorReference[] editors = page.findEditors(null, ID_EDITOR, IWorkbenchPage.MATCH_ID);
							for (IEditorReference reference : editors) {
								page.closeEditor(reference.getEditor(false), false);
							}
							page.openEditor(new ControlModelEditorInput(null), ID_EDITOR);
							if (introPart != null && !wasStandby) {
								PlatformUI.getWorkbench().getIntroManager().setIntroStandby(introPart, false);
							}
						}
					});
				}
			});
		}

		@Override
		public void disconnect() {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (viewer != null && !viewer.getControl().isDisposed()) {
						viewer.refresh(parentElement);
					}
				}
			});
		}
	}

	private IWorkbenchPage findPage(String pageId) {
		if (pageId != null && pageId.trim().length() > 0) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow window : windows) {
				try {
					IWorkbenchPage subPage = window.getActivePage();
					for (IPerspectiveDescriptor perspective : subPage.getSortedPerspectives()) {
						if (perspective.getId().equals(pageId)) {
							return subPage;
						}
					}
				} catch (Exception e) {
				}
			}
			for (IWorkbenchWindow window : windows) {
				try {
					for (IWorkbenchPage subPage : window.getPages()) {
						if (subPage.getPerspective().getId().equals(pageId)) {
							return subPage;
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

}
