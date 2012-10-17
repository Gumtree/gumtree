package au.gov.ansto.bragg.wombat.webserver.vaadin;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.gumtree.util.string.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.server.NBIServerProperties;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

@SuppressWarnings("serial")
public class WombatWebApplication extends Application {

	private static final int REFRESH_INTERVAL = 5000;

	private static final Logger logger = LoggerFactory
			.getLogger(WombatWebApplication.class);

	private UIContext context;

	private Job job;

	private Map<String, Label> componentMap;

	private String sicsRestletURI;

	/*************************************************************************
	 * Application lifecycle
	 *************************************************************************/
	public WombatWebApplication() {
		super();
	}

	@Override
	public void init() {
		context = new UIContext();
		componentMap = new HashMap<String, Label>();

		sicsRestletURI = "http://"
				+ NBIServerProperties.SICS_RESTLET_HOST.getValue() + ":"
				+ NBIServerProperties.SICS_RESTLET_PORT.getInt() + "/sics/rest";
		logger.info("Sics restlet uri is resolved to " + sicsRestletURI + ".");

		// Windows
		setTheme("gumtree-chameleon");
		context.window = new Window("Wombat Dashboard");
		setMainWindow(context.window);
		VerticalLayout windowLayout = (VerticalLayout) context.window
				.getContent();

		// Main panel
		Panel mainPanel = new Panel();
		mainPanel.setSizeFull();
		windowLayout.addComponent(mainPanel);
		GridLayout mainLayout = new GridLayout();
		mainLayout.setWidth("100%");
		mainLayout.setColumns(3);
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainPanel.setContent(mainLayout);

		// Refresh UI
		Refresher refresher = new Refresher();
		refresher.setRefreshInterval(REFRESH_INTERVAL / 2);
		windowLayout.addComponent(refresher);

		// Row 0
		Label label = new Label("Wombat Dashboard");
		label.setStyleName("h1");
		label.setSizeFull();
		mainLayout.addComponent(label);
		mainLayout.removeComponent(label);
		mainLayout.addComponent(label, 0, 0, 2, 0);

		// Row 1
		createServerStatusPanel(mainLayout);
		createNeutronBeamPanel(mainLayout);
		createInstrumentConfigurationPanel(mainLayout);

		// Row 2
		createHMImagePanel(mainLayout);

		// Row 3
		context.timeLabel = new Label();
		mainLayout.addComponent(context.timeLabel);

		// Schedule update
		job = new Job("update") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					update();
				} catch (Exception e) {
					logger.error("Failed to update Taipan data", e);
				}
				if (WombatWebApplication.this.isRunning()) {
					schedule(REFRESH_INTERVAL);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();

		// Close application when page is closed
		getMainWindow().addListener(new CloseListener() {
			@Override
			public void windowClose(CloseEvent e) {
				getMainWindow().getApplication().close();
			}
		});
	}

	public void close() {
		if (job != null) {
			job.cancel();
			job = null;
		}
		super.close();
	}

	/*************************************************************************
	 * UI layout
	 *************************************************************************/

	private void createServerStatusPanel(ComponentContainer parent) {
		Panel panel = new Panel("Server Status");
		panel.setStyleName("bubble");
		panel.setSizeFull();
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();

		parent.addComponent(panel);

		context.statusLabel = new Label();
		panelLayout.addComponent(context.statusLabel);
	}

	private void createNeutronBeamPanel(ComponentContainer parent) {
		Panel panel = new Panel("Neutron Beam");
		panel.setStyleName("bubble");
		// panel.setSizeFull();
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		parent.addComponent(panel);

		createDeviceLabel(panelLayout, "Reactor Power",
				"/instrument/source/power", "MW");
		createDeviceLabel(panelLayout, "Beam Monitor 1", "/monitor/bm1_counts",
				"counts/sec");
		createDeviceLabel(panelLayout, "Beam Monitor 2", "/monitor/bm2_counts",
				"counts/sec");
		createDeviceLabel(panelLayout, "Rate on Detector",
				"/instrument/detector/total_maprate", "counts/sec");
		createDeviceLabel(panelLayout, "Rate on Pixel",
				"/instrument/detector/max_binrate", "counts/sec");
	}

	private void createInstrumentConfigurationPanel(ComponentContainer parent) {
		Panel panel = new Panel("Instrument Configuration");
		panel.setStyleName("bubble");
		panel.setSizeFull();
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		parent.addComponent(panel);

		createDeviceLabel(panelLayout, "Slit 1 V Gap",
				"/instrument/slits/first/vertical/gap", null);
		createDeviceLabel(panelLayout, "Slit 1 H Gap",
				"/instrument/slits/first/horizontal/gap", null);
	}

	private void createDeviceLabel(ComponentContainer parent, String name,
			String path, String unit) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("100%");
		parent.addComponent(horizontalLayout);

		Label label = new Label(name);
		label.setStyleName("white");
		horizontalLayout.addComponent(label);

		label = new Label("--");
		label.setStyleName("bigger white");
		label.setWidth("100%");
		componentMap.put(path, label);
		horizontalLayout.addComponent(label);

		if (unit == null) {
			unit = "";
		}
		label = new Label(unit);
		label.setStyleName("white");
		horizontalLayout.addComponent(label);
	}

	private void createHMImagePanel(GridLayout parent) {
		Panel panel = new Panel("");
		panel.setSizeFull();
		// Hack to avoid the grid layout bug
		parent.addComponent(panel);
		parent.removeComponent(panel);
		parent.addComponent(panel, 0, 2, 2, 2);

		// Refresh UI
		Refresher refresher = new Refresher();
		refresher.setRefreshInterval(REFRESH_INTERVAL / 2);
		panel.addComponent(refresher);

		VerticalLayout panelLayout = new VerticalLayout();
		panel.addComponent(panelLayout);

		// updateHMImage();
		context.hmImage = new Embedded(
				"",
				new ExternalResource(
						"http://"
								+ NBIServerProperties.SICS_RESTLET_HOST
										.getValue()
								+ ":"
								+ NBIServerProperties.SICS_RESTLET_PORT
										.getInt()
								+ "/dae/rest/image?type=TOTAL_HISTOGRAM_X&scaling_type=LIN&screen_size_x=900&screen_size_y=500"));
		context.hmImage.setType(Embedded.TYPE_IMAGE);
		panelLayout.addComponent(context.hmImage);
		panelLayout.setComponentAlignment(context.hmImage,
				Alignment.MIDDLE_CENTER);
	}

	/*************************************************************************
	 * Update
	 *************************************************************************/

	private void update() throws Exception {
		updateStatus();
		updateHDB();
	}

	private void updateStatus() throws Exception {
		// Prepare url
		StringBuffer buffer = new StringBuffer();
		buffer.append(sicsRestletURI + "/status");
		buffer.append("?format=json");

		// Get data
		ClientResource clientResource = new ClientResource(URI.create(buffer
				.toString()));
		Representation data = clientResource.get();
		JSONObject object = new JSONObject(data.getText());

		String value = (String) object.get("status");
		if (value.startsWith("EAGER")) {
			context.statusLabel.setStyleName("green bigger");
		} else {
			context.statusLabel.setStyleName("red bigger");
		}
		context.statusLabel.setValue(value);
	}

	private void updateHDB() throws Exception {
		// Prepare url
		StringBuffer buffer = new StringBuffer();
		buffer.append(sicsRestletURI + "/hdbs?components=");
		for (Entry<String, Label> entry : componentMap.entrySet()) {
			buffer.append(entry.getKey()).append(",");
		}
		buffer.append("&format=json");

		// Get data
		ClientResource clientResource = new ClientResource(URI.create(buffer
				.toString()));
		Representation data = clientResource.get();
		JSONObject object = new JSONObject(data.getText());

		JSONArray array = object.getJSONArray("hdbs");
		DecimalFormat formatter = new DecimalFormat("#.###");
		for (int i = 0; i < array.length(); i++) {
			JSONObject deviceData = array.getJSONObject(i);
			// Process component
			if (deviceData.has("path")) {
				Label label = componentMap.get(deviceData.getString("path"));
				if (label != null) {
					String value = deviceData.getString("value");
					if (StringUtils.isNumber(value)) {
						value = formatter.format(Double.parseDouble(value));
					}
					label.setValue(value);
					continue;
				}
			}
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyy.MM.dd HH:mm:ss");
		context.timeLabel.setValue("Last update: "
				+ dateFormat.format(Calendar.getInstance().getTime()));
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	private class UIContext {
		private Window window;
		private Label statusLabel;
		private Label timeLabel;
		private Embedded hmImage;
	}

}
