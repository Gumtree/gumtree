package au.gov.ansto.bragg.echidna.ui;

import org.gumtree.gumnix.sics.core.IInstrumentProfile;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.ui.util.workbench.OpenBrowserViewAction;

@Deprecated
public class OpenMonitor2BorwserAction extends OpenBrowserViewAction {

	private String url;

	private String label;

	public OpenMonitor2BorwserAction() {
		super("Beam Monitor 2");
	}

	@Override
	public String getURL() {
//		if(url == null) {
//			IInstrumentProfile profile = SicsCore.getSicsManager().service().getCurrentInstrumentProfile();
//			if(profile != null) {
//				url = profile.getProperty(EchidnaUIConstants.PROP_BEAM_MONITOR_URL + "2");
//			}
//		}
		return url;
	}

	@Override
	public String getTitle() {
//		if(label == null) {
//			IInstrumentProfile profile = SicsCore.getSicsManager().service().getCurrentInstrumentProfile();
//			if(profile != null) {
//				label = profile.getProperty(EchidnaUIConstants.PROP_BEAM_MONITOR_LABEL + "2");
//			}
//		}
		return label;
	}

}
