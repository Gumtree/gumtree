package au.gov.ansto.bragg.wombat.ui;

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
//				url = profile.getProperty(WombatUIConstants.PROP_HISTOGRAM_URL);
//			}
//		}
		return url;
	}

	@Override
	public String getTitle() {
		return "";
	}
}
