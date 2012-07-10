package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

public class SicsScanEditor extends FormEditor {

	public SicsScanEditor() {
		super();
	}

	@Override
	protected void addPages() {
		try {
			addPage(new SicsScanStatusPage(this));
			addPage(new SicsScanControlPage(this));
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
