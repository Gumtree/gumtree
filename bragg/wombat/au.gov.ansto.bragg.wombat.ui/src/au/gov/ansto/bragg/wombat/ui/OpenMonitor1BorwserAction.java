package au.gov.ansto.bragg.wombat.ui;

import org.gumtree.gumnix.sics.core.IInstrumentProfile;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.ui.util.workbench.OpenBrowserViewAction;

@Deprecated
public class OpenMonitor1BorwserAction extends OpenBrowserViewAction {

	private String url;

	private String label;

	public OpenMonitor1BorwserAction() {
		super("Beam Monitor 1");
	}

	@Override
	public String getURL() {
//		if(url == null) {
//			IInstrumentProfile profile = SicsCore.getSicsManager().service().getCurrentInstrumentProfile();
//			if(profile != null) {
//				url = profile.getProperty(WombatUIConstants.PROP_BEAM_MONITOR_URL + "1");
//			}
//		}
		return url;
	}

	@Override
	public String getTitle() {
//		if(label == null) {
//			IInstrumentProfile profile = SicsCore.getSicsManager().service().getCurrentInstrumentProfile();
//			if(profile != null) {
//				label = profile.getProperty(WombatUIConstants.PROP_BEAM_MONITOR_LABEL + "1");
//			}
//		}
		return label;
	}

}
