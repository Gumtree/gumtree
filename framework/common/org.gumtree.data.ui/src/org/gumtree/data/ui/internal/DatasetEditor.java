package org.gumtree.data.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.gumtree.data.ui.viewers.DatasetBrowser;
import org.gumtree.data.ui.viewers.DatasetViewer;


public class DatasetEditor extends EditorPart {

	public DatasetEditor() {
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
		DatasetViewer viewer = new DatasetViewer(parent, SWT.NONE);
		DatasetBrowser browser = viewer.getDatasetBrowser();
		if (getEditorInput() instanceof IURIEditorInput) {
			browser.addDataset(((IURIEditorInput) getEditorInput()).getURI());
		}
	}

	@Override
	public void setFocus() {
	}

}
