package au.gov.ansto.bragg.wombat.webserver.vaadin;

import au.gov.ansto.bragg.nbi.server.vaadin.InstrumentStatutsNavigationView;

import com.vaadin.addon.touchkit.ui.TouchKitApplication;

@SuppressWarnings("serial")
public class WombatMobileApplication extends TouchKitApplication {

	@Override
	public void onBrowserDetailsReady() {
		setTheme("mobile");
		getMainWindow().setWebAppCapable(true);
		getMainWindow().setSizeFull();
		getMainWindow().addApplicationIcon(
				getURL() + "VAADIN/themes/mobile/g-icon_32x32.png");

		InstrumentStatutsNavigationView view = new InstrumentStatutsNavigationView();
		getMainWindow().setContent(view);
		view.setCaption("Wombat");
		view.setSizeFull();
		view.createStatusGroup("Neutron Beam")
				.addStatusItem("/source/power", "Reactor Power")
				.addStatusItem("/instrument/detector/total_maprate", "Rate on Detector")
				.addStatusItem("/instrument/detector/max_binrate", "Rate on Pixel");
		view.createStatusGroup("Experiment Info")
				.addStatusItem("/experiment/title", "Proposal");
		view.createStatusGroup("Instrument Configuration")
				.addStatusItem("/instrument/slits/first/vertical/gap", "Slit 1 Vertical Gap")
				.addStatusItem("/instrument/slits/first/horizontal/gap", "Slit 1 Horizontal Gap");
		view.initialise();		
	}

}
