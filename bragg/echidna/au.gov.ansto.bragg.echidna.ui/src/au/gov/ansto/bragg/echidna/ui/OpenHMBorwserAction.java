package au.gov.ansto.bragg.echidna.ui;

import org.gumtree.gumnix.sics.core.IInstrumentProfile;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.ui.util.workbench.OpenBrowserViewAction;

@Deprecated
public class OpenHMBorwserAction extends OpenBrowserViewAction {

	private String url;

	public OpenHMBorwserAction() {
		super("Open Histogram Site");
	}

	@Override
	public String getURL() {
//		if(url == null) {
//			IInstrumentProfile profile = SicsCore.getSicsManager().service().getCurrentInstrumentProfile();
//			if(profile != null) {
//				url = profile.getProperty(EchidnaUIConstants.PROP_HISTOGRAM_URL);
//				if (url == null) url = "";
//			}
//		}
		return url;
	}

	@Override
	public String getTitle() {
		return "Histogram Data";
	}
}
