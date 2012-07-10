package org.gumtree.ui.service.sidebar.support;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.util.xwt.XwtComposite;

public class XwtBasedGadget extends AbstractGadget {

	private URL fileUrl;
	
	public XwtBasedGadget() {
		super();
	}
	
	public XwtBasedGadget(URL fileUrl) {
		this();
		setFileUrl(fileUrl);
	}

	@Override
	public Composite createGadget(Composite parent) {
		XwtComposite composite = new XwtComposite(parent, SWT.NONE);
		composite.setFileUrl(getFileUrl());
		return composite;
	}

	public URL getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(URL fileUrl) {
		this.fileUrl = fileUrl;
	}
	
	@Override
	public String toString() {
		return createToStringHelper().add("fileUrl", getFileUrl()).toString();
	}
	
}
