package au.gov.ansto.bragg.kowari.ui;

import org.gumtree.gumnix.sics.core.IInstrumentProfile;
import org.gumtree.gumnix.sics.core.SicsCore;
import org.gumtree.ui.util.workbench.OpenBrowserViewAction;

@Deprecated
public class OpenHMBrowserAction extends OpenBrowserViewAction {

	private String url;

	public OpenHMBrowserAction() {
		super("Open Histogram Site");
	}

	@Override
	public String getURL() {
//		if(url == null) {
//			IInstrumentProfile profile = SicsCore.getSicsManager().service().getCurrentInstrumentProfile();
//			if(profile != null) {
//				url = profile.getProperty(KowariUIConstants.PROP_HISTOGRAM_URL);
//				if (url == null) url = "http://user:sydney@das1-kowari.nbi.ansto.gov.au:8080/admin/viewdata.egi";
//			}
//		}
		return url;
	}

	@Override
	public String getTitle() {
		return "Kowari Histogram Data";
	}
}
