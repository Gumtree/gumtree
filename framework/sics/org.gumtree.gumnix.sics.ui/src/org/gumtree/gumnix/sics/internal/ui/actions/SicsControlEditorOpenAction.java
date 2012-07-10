package org.gumtree.gumnix.sics.internal.ui.actions;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.PlatformUI;
import org.gumtree.gumnix.sics.control.ISicsController;
import org.gumtree.gumnix.sics.internal.ui.Activator;
import org.gumtree.gumnix.sics.internal.ui.editors.SicsEditorInput;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;
import org.gumtree.gumnix.sics.ui.util.SicsControllerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SicsControlEditorOpenAction extends Action {

	private static Logger logger;

	private StructuredViewer viewer;

	private INodeSet nodeSet;
	
	public SicsControlEditorOpenAction(String name, StructuredViewer viewer, INodeSet nodeSet) {
		super(name, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/full/obj16/server.gif"));
		this.viewer = viewer;
		this.nodeSet = nodeSet;
	}

	public void run() {
		SafeRunner.run(new ISafeRunnable() {
			public void run() throws Exception {
				Object selection = ((IStructuredSelection)viewer.getSelection()).getFirstElement();
				SicsEditorInput input = null;
				if(selection instanceof SicsControllerNode) {
					input = new SicsEditorInput(((SicsControllerNode)selection).getController(), nodeSet);
				} else if(selection instanceof ISicsController) {
					input = new SicsEditorInput((ISicsController)selection, nodeSet);
				}
				if(input == null) {
					return;
				}
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, SicsUIConstants.ID_EDITOR_SICS_CONTROL);
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
