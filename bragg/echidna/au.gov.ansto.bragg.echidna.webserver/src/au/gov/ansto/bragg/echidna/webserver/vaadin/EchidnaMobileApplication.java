package au.gov.ansto.bragg.echidna.webserver.vaadin;

import au.gov.ansto.bragg.nbi.server.vaadin.InstrumentStatutsNavigationView;

import com.vaadin.addon.touchkit.ui.TouchKitApplication;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

@SuppressWarnings("serial")
public class EchidnaMobileApplication extends TouchKitApplication {

	@Override
	public void onBrowserDetailsReady() {
		setTheme("mobile");
		getMainWindow().setWebAppCapable(true);
		getMainWindow().setSizeFull();
		getMainWindow().addApplicationIcon(
				getURL() + "VAADIN/themes/mobile/g-icon_32x32.png");

		InstrumentStatutsNavigationView view = new InstrumentStatutsNavigationView();
		getMainWindow().setContent(view);
		view.setCaption("Echidna");
		view.setSizeFull();
		view.createStatusGroup("Neutron Beam")
				.addStatusItem("/source/power", "Reactor Power")
				.addStatusItem("/monitor/bm1_event_rate", "Beam Monitor 1")
				.addStatusItem("/monitor/bm2_event_rate", "Beam Monitor 2")
				.addStatusItem("/instrument/detector/total_maprate", "Rate on Detector")
				.addStatusItem("/instrument/detector/max_binrate", "Rate on Pixel");
		view.createStatusGroup("Experiment Status")
				.addStatusItem("/sample/azimuthal_angle", "stth")
				.addStatusItem("/experiment/currpoint", "Current Point");
		view.createStatusGroup("Experiment Info")
				.addStatusItem("/experiment/title", "Proposal")
				.addStatusItem("/user/name", "User");
		view.createStatusGroup("Robot Changer")
				.addStatusItem("/sample/robby/Control/Pallet_Name", "Pallet Name")
				.addStatusItem("/sample/robby/Control/Pallet_Idx", "Sample Position")
				.addStatusItem("/sample/robby/setpoint", "Robot Status");
		view.createStatusGroup("Sample Environment")
				.addStatusItem("/sample/tc1/sensor/sensorValueA", "TC1 Sensor A")
				.addStatusItem("/sample/tc1/sensor/sensorValueB", "TC1 Sensor B")
				.addStatusItem("/sample/tc1/sensor/sensorValueC", "TC1 Sensor C")
				.addStatusItem("/sample/tc1/sensor/sensorValueD", "TC1 Sensor D")
				.addStatusItem("/sample/tempone/sensorA", "Furnace Temperature")
				.addStatusItem("/sample/tempone/setpoint", "Furance Setpoint");		
		view.initialise();
		
		// Close application when page is closed
		getMainWindow().addListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				getMainWindow().getApplication().close();
			}
		});
	}

}
