package au.gov.ansto.bragg.echidna.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.gumtree.gumnix.sics.control.controllers.IComponentController;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.gumnix.sics.ui.componentview.IComponentViewContent;

@Deprecated
public class BeamMonitorViewContent implements IComponentViewContent {

	private IComponentController controller;

	public void createPartControl(Composite parent, IComponentController controller) {
		this.controller = controller;
		parent.setLayout(new FillLayout());
		Browser browser = new Browser(parent, SWT.NONE);
//		String hmURL = SicsCore.getSicsManager().service().getCurrentInstrumentProfile().getProperty(EchidnaUIConstants.PROP_BEAM_MONITOR_URL);
//		browser.setUrl(hmURL);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public IComponentController getController() {
		return controller;
	}

}
