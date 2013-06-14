package au.gov.ansto.bragg.taipan.webserver.vaadin;

import au.gov.ansto.bragg.nbi.server.vaadin.InstrumentStatutsNavigationView;

import com.vaadin.addon.touchkit.ui.TouchKitApplication;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

@SuppressWarnings("serial")
public class TaipanMobileApplication extends TouchKitApplication {

	@Override
	public void onBrowserDetailsReady() {
		setTheme("mobile");
		getMainWindow().setWebAppCapable(true);
		getMainWindow().setSizeFull();
		getMainWindow().addApplicationIcon(
				getURL() + "VAADIN/themes/mobile/g-icon_32x32.png");

		InstrumentStatutsNavigationView view = new InstrumentStatutsNavigationView();
		getMainWindow().setContent(view);
		view.setCaption("Taipan");
		view.setSizeFull();
		view.createStatusGroup("Experiment Status")
				.addStatusItem("/experiment/title", "Title")
				.addStatusItem("/sample/name", "Sample");
		view.createStatusGroup("Beam Monitors")
				.addStatusItem("/monitor/bm1_counts", "Monitor")
				.addStatusItem("/monitor/bm2_counts", "Detector")
				.addStatusItem("/monitor/time", "Time");
		view.createStatusGroup("Real Motors")
				.addStatusItem("/instrument/crystal/m1", "m1")
				.addStatusItem("/instrument/crystal/m2", "m2")
				.addStatusItem("/sample/s1", "s1")
				.addStatusItem("/sample/s2", "s2")
				.addStatusItem("/instrument/crystal/a1", "a1")
				.addStatusItem("/instrument/detector/a2", "a2");
		view.createStatusGroup("Virtual Motors")
				.addStatusItem("/sample/ei", "ei")
				.addStatusItem("/sample/ef", "ef")
				.addStatusItem("/sample/en", "en")
				.addStatusItem("/sample/qh", "qh")
				.addStatusItem("/sample/qk", "qk")
				.addStatusItem("/sample/ql", "ql");
		view.createStatusGroup("Scan Status")
				.addStatusItem("/commands/scan/bmonscan/scan_variable", "Variable")
				.addStatusItem("/commands/scan/bmonscan/feedback/scan_variable_value", "Value")
				.addStatusItem("/experiment/currpoint", "Index");
				
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
