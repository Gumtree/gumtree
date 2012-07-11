package au.gov.ansto.bragg.echidna.ui;

import org.gumtree.gumnix.sics.core.IInstrumentProfile;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.ui.util.workbench.OpenBrowserViewAction;

@Deprecated
public class OpenPLCBorwserAction extends OpenBrowserViewAction {

	private String url;

	public OpenPLCBorwserAction() {
		super("Open PLC Site");
	}

	@Override
	public String getURL() {
//		if(url == null) {
//			IInstrumentProfile profile = SicsCore.getSicsManager().service().getCurrentInstrumentProfile();
//			if(profile != null) {
//				url = profile.getProperty(EchidnaUIConstants.PROP_PLC_URL);
//			}
//		}
		return url;
	}

	@Override
	public String getTitle() {
		return "";
	}

}
