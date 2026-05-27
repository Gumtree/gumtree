package org.gumtree.control.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.gumtree.control.ui.viewer.ControlViewer;
import org.gumtree.control.ui.viewer.model.INodeSet;

public class SicsControlEditor extends EditorPart {

	private ControlViewer viewer;

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
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
		viewer = new ControlViewer(getSite());
		INodeSet nodeSet = null;
		if (getEditorInput() != null) {
			nodeSet = (INodeSet) getEditorInput().getAdapter(INodeSet.class);
			if (nodeSet != null) {
				if (nodeSet.getTitle() != null) {
					setPartName("SICS Server [" + nodeSet.getTitle() + "]");
				} else {
					setPartName("SICS Server [filtered]");
				}
			} else {
				setPartName("SICS Server");
			}
		}
		viewer.createPartControl(parent, nodeSet);
		getSite().setSelectionProvider(viewer.getTreeViewer());
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		if (viewer != null) {
			viewer.dispose();
			viewer = null;
		}
		super.dispose();
	}

}
