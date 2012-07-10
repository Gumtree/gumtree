package org.gumtree.ui.util.xwt;

import java.net.MalformedURLException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.gumtree.ui.util.xwt.XwtComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XwtCompositeEditor extends EditorPart {

	private static final Logger logger = LoggerFactory.getLogger(XwtCompositeEditor.class);
	
	public XwtCompositeEditor() {
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
		XwtComposite composite = new XwtComposite(parent, SWT.NONE);
		if (getEditorInput() instanceof IURIEditorInput) {
			try {
				composite.setFileUrl(((IURIEditorInput) getEditorInput()).getURI().toURL());
			} catch (MalformedURLException e) {
				logger.error("Failed to load XWT file in the editor", e);
			}
		}
	}

	@Override
	public void setFocus() {
	}

}
