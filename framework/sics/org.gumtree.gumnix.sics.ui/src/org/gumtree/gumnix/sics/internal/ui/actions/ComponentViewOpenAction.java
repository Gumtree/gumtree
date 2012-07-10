package org.gumtree.gumnix.sics.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.componentview.ComponentView;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;
import org.gumtree.gumnix.sics.ui.componentview.IComponentView;
import org.gumtree.gumnix.sics.ui.util.DefaultControllerNode;

public class ComponentViewOpenAction extends Action {

	private StructuredViewer viewer;

	public ComponentViewOpenAction(StructuredViewer viewer) {
		super("Open Component View", Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/plugin_obj.gif"));
		this.viewer = viewer;
	}

	public void run() {
		Object selection = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
		if (selection instanceof DefaultControllerNode) {
			IComponentController controller = ((DefaultControllerNode) selection).getController();
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			String secondaryId = Integer.toString(ComponentView.getAndIncreaseViewActivationCount());
			try {
				page.showView(SicsUIConstants.ID_COMPONENT_VIEW,
						secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
				for (IViewReference ref : page.getViewReferences()) {
					if (ref.getId().equals(
							SicsUIConstants.ID_COMPONENT_VIEW)
							&& ref.getSecondaryId().equals(secondaryId)) {
						IComponentView view = (IComponentView) ref
								.getView(false);
						view.setComponentController(controller);
						return;
					}
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

	private StructuredViewer getViewer() {
		return viewer;
	}

}
