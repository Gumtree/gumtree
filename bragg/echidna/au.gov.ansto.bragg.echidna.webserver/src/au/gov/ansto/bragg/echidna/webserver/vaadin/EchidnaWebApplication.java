package au.gov.ansto.bragg.echidna.webserver.vaadin;

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

public class EchidnaWebApplication extends Application {

	private static final long serialVersionUID = -8015538517245159632L;

	private static final int REFRESH_INTERVAL = 5000;

	private static final Logger logger = LoggerFactory
			.getLogger(EchidnaWebApplication.class);

	private UIContext context;

	private Job job;

	private Map<String, Label> componentMap;

	private String sicsRestletURI;

	/*************************************************************************
	 * Application lifecycle
	 *************************************************************************/

	public EchidnaWebApplication() {
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
		context.window = new Window("Echidna Dashboard");
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
		Label label = new Label("Echidna Dashboard");
		label.setStyleName("h1");
		label.setSizeFull();
		mainLayout.addComponent(label);
		mainLayout.removeComponent(label);
		mainLayout.addComponent(label, 0, 0, 2, 0);

		// Row 1
		createServerStatusPanel(mainLayout);
		createExperimentStatusPanel(mainLayout);
		createRobotChangerPanel(mainLayout);

		// Row 2
		createNeutronBeamPanel(mainLayout);
		createExperimentInfoPanel(mainLayout);
		createSampleEnvironmentPanel(mainLayout);

		// Row 3
		createHMImagePanel(mainLayout);

		// Row 4
		context.timeLabel = new Label();
		mainLayout.addComponent(context.timeLabel);

		// Schedule update
		job = new Job("update") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					update();
				} catch (Exception e) {
					logger.error("Failed to update Echidna data", e);
				}
				if (EchidnaWebApplication.this.isRunning()) {
					schedule(REFRESH_INTERVAL);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
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

	private void createExperimentStatusPanel(ComponentContainer parent) {
		Panel panel = new Panel("Experiment Status");
		panel.setStyleName("bubble");
		panel.setSizeFull();
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		parent.addComponent(panel);

		createDeviceLabel(panelLayout, "stth", "/sample/azimuthal_angle",
				"degrees");
		createDeviceLabel(panelLayout, "Current Point",
				"/experiment/currpoint", null);
		createDeviceLabel(panelLayout, "File Name", "/experiment/file_name",
				null, new ITextFormatter() {
					@Override
					public String formatText(String text) {
						String[] results = text.split("[^\\d]");
						for (String result : results) {
							if (result.length() > 0) {
								return result;
							}
						}
						return StringUtils.EMPTY_STRING;
					}
				});
	}

	private void createRobotChangerPanel(ComponentContainer parent) {
		Panel panel = new Panel("Robot Changer");
		panel.setStyleName("bubble");
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		parent.addComponent(panel);

		createDeviceLabel(panelLayout, "Pallet Name",
				"/sample/robby/Control/Pallet_Nam", null);
		createDeviceLabel(panelLayout, "Sample Position",
				"/sample/robby/Control/Pallet_Idx", null);
		createDeviceLabel(panelLayout, "Robot Status",
				"/sample/robby/setpoint", null);
	}

	private void createNeutronBeamPanel(ComponentContainer parent) {
		Panel panel = new Panel("Neutron Beam");
		panel.setStyleName("bubble");
		panel.setSizeFull();
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

	private void createExperimentInfoPanel(ComponentContainer parent) {
		Panel panel = new Panel("Experiment Info");
		panel.setStyleName("bubble");
		panel.setSizeFull();
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		parent.addComponent(panel);

		createDeviceLabel(panelLayout, "Proposal", "/experiment/title", null);
		createDeviceLabel(panelLayout, "Sample", "/sample/name", null);
		createDeviceLabel(panelLayout, "User", "/user/name", null);
	}

	private void createSampleEnvironmentPanel(ComponentContainer parent) {
		Panel panel = new Panel("Sample Environment");
		panel.setStyleName("bubble");
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		parent.addComponent(panel);

		createDeviceLabel(panelLayout, "TC1 Sensor A",
				"/sample/tc1/sensor/sensorValueA", null);
		createDeviceLabel(panelLayout, "TC1 Sensor B",
				"/sample/tc1/sensor/sensorValueB", null);
		createDeviceLabel(panelLayout, "TC1 Sensor C",
				"/sample/tc1/sensor/sensorValueC", null);
		createDeviceLabel(panelLayout, "TC1 Sensor D",
				"/sample/tc1/sensor/sensorValueD", null);
		createDeviceLabel(panelLayout, "Furnace Temperature",
				"/sample/tempone/sensorA", null);
		createDeviceLabel(panelLayout, "Furnace Setpoint",
				"/sample/tempone/setpoint", null);
	}

	private void createDeviceLabel(ComponentContainer parent, String name,
			String path, String unit) {
		createDeviceLabel(parent, name, path, unit, null);
	}

	@SuppressWarnings("serial")
	private void createDeviceLabel(ComponentContainer parent, String name,
			String path, String unit, final ITextFormatter formatter) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("100%");
		parent.addComponent(horizontalLayout);

		Label label = new Label(name);
		label.setStyleName("white");
		horizontalLayout.addComponent(label);

		label = new Label("--") {
			public void setValue(Object newValue) {
				if (formatter != null) {
					newValue = formatter.formatText(newValue.toString());
				}
				super.setValue(newValue);
			}
		};
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
		parent.addComponent(panel, 0, 3, 2, 3);

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

	private interface ITextFormatter {

		String formatText(String text);

	}

}
