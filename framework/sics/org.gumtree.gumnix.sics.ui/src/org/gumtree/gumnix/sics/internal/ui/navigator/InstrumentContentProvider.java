package org.gumtree.gumnix.sics.internal.ui.navigator;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.intro.IIntroPart;
import org.gumtree.gumnix.sics.control.ControllerStatus;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.control.events.IComponentControllerListener;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.internal.ui.editors.SicsEditorInput;
import org.gumtree.gumnix.sics.io.ISicsProxyListener;
import org.gumtree.gumnix.sics.io.SicsProxyListenerAdapter;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;
import org.gumtree.gumnix.sics.ui.util.SicsControllerNode;
import org.gumtree.ui.util.jface.ITreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentContentProvider implements ITreeContentProvider,
		IComponentControllerListener {

	private static Object[] EMPTY_ARRAY = new Object[0];

	private static Logger logger;

	private StructuredViewer viewer;

	private IWorkspaceRoot parentElement;

	private ISicsController controller;

	private ISicsProxyListener proxyListener;

	public InstrumentContentProvider() {
		proxyListener = new ProxyListener();
		SicsCore.getDefaultProxy().addProxyListener(proxyListener);
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IWorkspaceRoot) {
			this.parentElement = (IWorkspaceRoot) parentElement;
			controller = SicsCore.getSicsController();
			if (controller != null) {
				// Note: we don't provide filter on the navigator
				ITreeNode sicsControllerNode = new SicsControllerNode(
						controller, null);
				sicsControllerNode.setViewer(viewer);
				return new Object[] { sicsControllerNode };
			}
//		} else if (parentElement instanceof ITreeNode) {
//			return ((ITreeNode) parentElement).getChildren();
		}
		return EMPTY_ARRAY;
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length != 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
		SicsCore.getDefaultProxy().removeProxyListener(proxyListener);
		proxyListener = null;
		// if(controller != null) {
		// controller.removeComponentListener(this);
		// }
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (StructuredViewer) viewer;
	}

	public void componentStatusChanged(ControllerStatus newStatus) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (viewer != null && !viewer.getControl().isDisposed()
						&& controller != null) {
					viewer.refresh(controller);
				}
			}
		});
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(InstrumentContentProvider.class);
		}
		return logger;
	}

	class ProxyListener extends SicsProxyListenerAdapter {
		public void proxyConnected() {

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (viewer != null && !viewer.getControl().isDisposed()) {
						viewer.refresh(parentElement);
					}
					// Also launch the SICS editor
//					if (SicsCore.getSicsManager().control().isControllerAvailable()) {
//						System.err.println("controller available");
//						SafeRunner.run(new ISafeRunnable() {
//							public void handleException(Throwable exception) {
//							}
//
//							public void run() throws Exception {
//								IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
//								boolean wasStandby = PlatformUI.getWorkbench().getIntroManager().isIntroStandby(introPart);
//								PlatformUI
//								.getWorkbench()
//								.getActiveWorkbenchWindow()
//								.getActivePage()
//								.openEditor(
//										new SicsEditorInput(
//												SicsCore.getSicsController()),
//												SicsUIConstants.ID_EDITOR_SICS_CONTROL);
//								// Restore intro if the editor caused the intro to standby
//								// Desirable behaviours when intro is available:
//								// 1. Intro has fully visible but openning editor causes to standby --> restore intro fully
//								// 2. Intro has not visible (in trim area or not in active page) --> do nothing
//								// 3. Intro has standby --> do nothing
//								if(introPart != null && !wasStandby) {
//									PlatformUI.getWorkbench().getIntroManager().setIntroStandby(introPart, false);
//								}
//							}
//
//						});
//					}
				}
			});
			
			Thread tempThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (SicsCore.getSicsManager().control().isControllerAvailable()) {
								SafeRunner.run(new ISafeRunnable() {
									public void handleException(Throwable exception) {
									}

									public void run() throws Exception {
										String pageId = System.getProperty(SicsUIConstants.ID_SICS_OPEN_EDITOR_PAGE);
										IWorkbenchPage page = null;
										if (pageId != null && pageId.trim().length() > 0) {
											IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
											for (IWorkbenchWindow window : windows) {
												try {
													IWorkbenchPage subPage = window.getActivePage();
													IPerspectiveDescriptor[] perspectives = subPage.getSortedPerspectives();
													for (IPerspectiveDescriptor perspective : perspectives) {
														if (perspective.getId().equals(pageId)) {
															page = subPage;
															break;
														}
													}
													if (page != null) {
														break;
													}
												} catch (Exception e) {
												}
											}
											if (page == null) {
												for (IWorkbenchWindow window : windows) {
													try {
														IWorkbenchPage[] pages = window.getPages();
														for (IWorkbenchPage subPage : pages) {
															if (subPage.getPerspective().getId().equals(pageId)) {
																page = subPage;
																break;
															}
														}
														if (page != null) {
															break;
														}
													} catch (Exception e) {
													}
												}
											}
										}
										if (page == null) {
											page = PlatformUI
													.getWorkbench()
													.getActiveWorkbenchWindow()
													.getActivePage();
										}
										IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager().getIntro();
										boolean wasStandby = PlatformUI.getWorkbench().getIntroManager().isIntroStandby(introPart);
//										if (page.isEditorAreaVisible()) {
											page.openEditor(
													new SicsEditorInput(
															SicsCore.getSicsController()),
													SicsUIConstants.ID_EDITOR_SICS_CONTROL);
//										}
										// Restore intro if the editor caused the intro to standby
										// Desirable behaviours when intro is available:
										// 1. Intro has fully visible but openning editor causes to standby --> restore intro fully
										// 2. Intro has not visible (in trim area or not in active page) --> do nothing
										// 3. Intro has standby --> do nothing
										if(introPart != null && !wasStandby) {
											PlatformUI.getWorkbench().getIntroManager().setIntroStandby(introPart, false);
										}
									}

								});
							}
						}
					});
				}
			});
			tempThread.start();
		}
		public void proxyDisconnected() {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (viewer != null && !viewer.getControl().isDisposed()) {
						viewer.refresh(parentElement);
					}
				}
			});
		}
	}

}
