package org.gumtree.ui.dashboard.internal;

import java.io.InputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.gumtree.ui.dashboard.model.Dashboard;
import org.gumtree.ui.dashboard.model.DashboardModelUtils;
import org.gumtree.ui.dashboard.viewer.DashboardViewer;
import org.gumtree.ui.dashboard.viewer.IDashboardViewer;
import org.gumtree.ui.internal.Activator;

public class DashboardEditor extends EditorPart {

	private IDashboardViewer dashboardViewer;
	
	public DashboardEditor() {
		super();
	}

	public void doSave(IProgressMonitor monitor) {
	}

	public void doSaveAs() {
	}

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		dashboardViewer = new DashboardViewer(parent, SWT.NONE);
		dashboardViewer.setModel(loadModel());
	}

	private Dashboard loadModel() {
		IFileEditorInput modelFile = (IFileEditorInput) getEditorInput();
		InputStream in = null;
		Dashboard model = null;
		try {
			if (!modelFile.getFile().isSynchronized(IResource.DEPTH_ZERO)) {
				modelFile.getFile().refreshLocal(IResource.DEPTH_ZERO, null);
			}
			in = modelFile.getFile().getContents();
			model = (Dashboard) DashboardModelUtils.getXStream().fromXML(in);
			in.close();
			// Avoid resource our of sync problem when GumTree crashes
			modelFile.getFile().refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (Exception e) {
			StatusManager.getManager().handle(
					new org.eclipse.core.runtime.Status(IStatus.ERROR,
							Activator.PLUGIN_ID,
							"Failed to load dashboard model.", e),
					StatusManager.SHOW);
			if (in != null) {
				try {
					in.close();
					modelFile.getFile().refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			return null;
		}
		return model;
	}
	
	public void setFocus() {
	}

}
