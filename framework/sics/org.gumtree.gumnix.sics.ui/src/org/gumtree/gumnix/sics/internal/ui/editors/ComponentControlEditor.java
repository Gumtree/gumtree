package org.gumtree.gumnix.sics.internal.ui.editors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.ui.componentview.IComponentViewContent;

public class ComponentControlEditor extends EditorPart {

	private IComponentViewContent content;
	
	public ComponentControlEditor() {
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
		IComponentController controller = (IComponentController)getEditorInput().getAdapter(IComponentController.class);
		Assert.isNotNull(controller);
		content = (IComponentViewContent)Platform.getAdapterManager().getAdapter(controller, IComponentViewContent.class);
		Assert.isNotNull(content);
		content.createPartControl(parent, controller);
		// Set title
		setPartName(controller.getComponent().getId());
	}

	@Override
	public void setFocus() {
	}

	public void dispose() {
		if (content != null) {
			content.dispose();
			content = null;
		}
	}
	
}
