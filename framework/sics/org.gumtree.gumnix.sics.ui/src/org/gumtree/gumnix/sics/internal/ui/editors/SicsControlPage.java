package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.gumtree.gumnix.sics.internal.ui.controlview.ControlViewer;
import org.gumtree.gumnix.sics.ui.controlview.INodeSet;

public class SicsControlPage extends FormPage {

	public static final String ID = "control";

	private static final String TITLE = "Instrument Control";

	private ControlViewer viewer;

	public SicsControlPage(FormEditor editor) {
		super(editor, ID, TITLE);
	}

	protected void createFormContent(IManagedForm managedForm) {
		Composite parent = managedForm.getForm().getBody();
		parent.setLayout(new FillLayout());
		viewer = new ControlViewer();
		viewer.createPartControl(parent, (INodeSet) getEditorInput().getAdapter(INodeSet.class));
		// [GUMTREE-45] register to properties view
		getSite().setSelectionProvider(viewer.getTreeViewer());
	}

	public void dispose() {
//		if(viewer != null) {
//			viewer.dispose();
//			viewer = null;
//		}
		super.dispose();
	}

}
