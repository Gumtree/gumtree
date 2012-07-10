package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewer;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;

public class SicsControlEditor extends EditorPart {

	private ControlViewer viewer;
	
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
		viewer = new ControlViewer();
		INodeSet nodeSet = null;
		if (getEditorInput() != null) {
			nodeSet = (INodeSet) getEditorInput().getAdapter(INodeSet.class);
			if (nodeSet != null) {
				if (nodeSet.getTitle() != null) {
					setPartName("SIC Server" + "[" + nodeSet.getTitle() + "]");
				} else {
					setPartName("SIC Server" + "[filtered]");
				}
			} else {
				setPartName("SIC Server");
			}
		}
		viewer.createPartControl(parent, nodeSet);
		// [GUMTREE-45] register to properties view
		getSite().setSelectionProvider(viewer.getTreeViewer());
	}

	@Override
	public void setFocus() {		
	}

}
