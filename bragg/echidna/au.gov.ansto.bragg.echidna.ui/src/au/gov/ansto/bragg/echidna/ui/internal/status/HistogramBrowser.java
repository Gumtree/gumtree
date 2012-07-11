package au.gov.ansto.bragg.echidna.ui.internal.status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.ui.componentview.IComponentViewContent;

@Deprecated
public class HistogramBrowser implements IComponentViewContent {

	private static final String HISTOGRAM_URL = "histogramURL";

	private IComponentController controller;

	public HistogramBrowser() {
		// TODO Auto-generated constructor stub
	}

	public void createPartControl(Composite parent, IComponentController controller) {
		this.controller = controller;
		parent.setLayout(new FillLayout());
		Browser browser = new Browser(parent, SWT.BORDER);
//		String hmURL = SicsCore.getSicsManager().service().getCurrentInstrumentProfile().getProperty(HISTOGRAM_URL);
//		browser.setUrl(hmURL);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public IComponentController getController() {
		return controller;
	}

}
