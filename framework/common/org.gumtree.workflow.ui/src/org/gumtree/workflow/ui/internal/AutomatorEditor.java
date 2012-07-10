package org.gumtree.workflow.ui.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.workflow.ui.IWorkflow;
import org.gumtree.workflow.ui.util.WorkflowFactory;
import org.gumtree.workflow.ui.viewer.AutomatorWorkflowViewer;
import org.gumtree.workflow.ui.viewer.IWorkflowViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AutomatorEditor extends EditorPart {

	private static Logger logger = LoggerFactory.getLogger(AutomatorEditor.class);
	
	private IWorkflow workflow;
	
	private IWorkflowViewer viewer;
	
	public AutomatorEditor() {
		super();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		workflow = loadWorkflow();
		viewer = new AutomatorWorkflowViewer();
		viewer.setWorkflow(workflow);
		viewer.createPartControl(parent);
	}

	@Override
	public void setFocus() {
		if (viewer != null && !viewer.isDisposed()) {
			viewer.setFocus();
		}
	}

	private IWorkflow loadWorkflow() {
		IWorkflow workflow = null;
		// Load workflow from editor input
		if (getEditorInput() instanceof WorkflowEditorInput) {
			workflow = (IWorkflow) getEditorInput().getAdapter(IWorkflow.class);
		} else if (getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput configFile = (IFileEditorInput) getEditorInput();
			try {
				workflow = WorkflowFactory.createWorkflow(configFile.getFile().getContents());
				// Refresh the file content first
				configFile.getFile().refreshLocal(IResource.DEPTH_ZERO, null);
			} catch (Exception e) {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								IStatus.OK, "Failed to load workflow.", e),
						StatusManager.SHOW);
				try {
					configFile.getFile().refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (Exception e1) {
				}
				logger.error("Cannot load workflow from config.", e);
			}
		}
		return workflow == null ? new Workflow() : workflow;
	}
	
	public void dispose() {
		viewer = null;
		workflow = null;
	}
	
}
