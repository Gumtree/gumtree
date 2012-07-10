package org.gumtree.ui.widgets;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.ui.util.forms.FormControlWidget;

public class BrowserWidget extends FormControlWidget {

	private Browser browser;
	
	public BrowserWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		browser = new Browser(this, getOriginalStyle());
	}

	protected void widgetDispose() {
		browser = null;
	}

	public String getUrl() {
		return browser.getUrl();
	}
	
	public void setUrl(String url) {
		browser.setUrl(url);
	}

	public void afterParametersSet() {
	}
	
}
