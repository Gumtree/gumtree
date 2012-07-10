package org.gumtree.gumnix.sics.internal.ui.actions;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.editors.ComponentEditorInput;
import org.gumtree.gumnix.sics.ui.util.DefaultControllerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentControlEditorOpenAction extends Action {

	private static Logger logger;

	private StructuredViewer viewer;

	public ComponentControlEditorOpenAction(StructuredViewer viewer) {
		super("Open component", Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/plugin_obj.gif"));
		this.viewer = viewer;
	}

	public void run() {
		SafeRunner.run(new ISafeRunnable() {
			public void run() throws Exception {
				Object selection = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
				ComponentEditorInput input = null;
				if(selection instanceof DefaultControllerNode) {
					input = new ComponentEditorInput(((DefaultControllerNode)selection).getController());
				} else if(selection instanceof IComponentController) {
					input = new ComponentEditorInput((IComponentController)selection);
				}
				if(input == null) {
					return;
				}
				IEditorDescriptor desc = (IEditorDescriptor)Platform.getAdapterManager().loadAdapter(input.getController(), IEditorDescriptor.class.getName());
				if(desc == null) {
					return;
				}
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, desc.getId());
			}
			public void handleException(Throwable exception) {
				getLogger().error("Cannot open sics control in editor", exception);
			}
		});
	}

	private static Logger getLogger() {
		if(logger == null) {
			logger = LoggerFactory.getLogger(SicsControlEditorOpenAction.class);
		}
		return logger;
	}

}
