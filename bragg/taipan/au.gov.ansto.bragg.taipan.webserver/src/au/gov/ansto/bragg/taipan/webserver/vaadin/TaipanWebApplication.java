package au.gov.ansto.bragg.taipan.webserver.vaadin;

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

public class TaipanWebApplication extends Application {

	private static final long serialVersionUID = -4829480414071163416L;

	private static final int REFRESH_INTERVAL = 5000;

	private static final Logger logger = LoggerFactory
			.getLogger(TaipanWebApplication.class);

	private Window window;

	private String sicsRestletURI;

	private Map<String, Label> componentMap;
	
	private Label statusLabel;
	
	private Label timeLabel;

	@Override
	public void init() {
		componentMap = new HashMap<String, Label>();

		sicsRestletURI = "http://"
				+ NBIServerProperties.SICS_RESTLET_HOST.getValue() + ":"
				+ NBIServerProperties.SICS_RESTLET_PORT.getInt() + "/sics/rest";
		logger.info("Sics restlet uri is resolved to " + sicsRestletURI + ".");

		// Windows
		setTheme("gumtree-chameleon");
		window = new Window("Taipan Dashboard");
		setMainWindow(window);
		VerticalLayout windowLayout = (VerticalLayout) window.getContent();

		// Main panel
		Panel mainPanel = new Panel();
		mainPanel.setSizeFull();
		windowLayout.addComponent(mainPanel);
		GridLayout mainLayout = new GridLayout();
		mainLayout.setColumns(2);
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		mainPanel.setContent(mainLayout);

		createLeftContent(mainLayout);
		createRightContent(mainLayout);

		// Schedule update
		Job job = new Job("update") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					update();
				} catch (Exception e) {
					logger.error("Failed to update Taipan data", e);
				}
				if (TaipanWebApplication.this.isRunning()) {
					schedule(REFRESH_INTERVAL);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private void createLeftContent(ComponentContainer parent) {
		GridLayout layout = new GridLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		parent.addComponent(layout);

		// Title
		Panel titlePanel = new Panel();
		layout.addComponent(titlePanel);
		GridLayout titlePanelLayout = new GridLayout();
		titlePanelLayout.setMargin(true);
		titlePanelLayout.setSpacing(false);
		titlePanel.setWidth("600px");
		titlePanel.setContent(titlePanelLayout);

		Label titleLabel = new Label("<h1>Taipan</h1>", Label.CONTENT_XHTML);
		titleLabel.setStyleName("big");
		titlePanelLayout.addComponent(titleLabel);

		Label subtitleLabel = new Label("<h2>Thermal 3-Axis Spectrometer</h2>",
				Label.CONTENT_XHTML);
		subtitleLabel.setStyleName("tiny");
		titlePanelLayout.addComponent(subtitleLabel);

		// Image
//		Embedded image = new Embedded("", new ClassResource("taipan.png",
//				window.getApplication()));
		String plotURI = "http://"
				+ NBIServerProperties.SICS_RESTLET_HOST.getValue() + ":"
				+ NBIServerProperties.SICS_RESTLET_PORT.getInt()
				+ "/taipan/rest/plot?height=400&width=600";
		Embedded image = new Embedded("", new ExternalResource(plotURI));
		image.setType(Embedded.TYPE_IMAGE);
		image.setSizeFull();
		layout.addComponent(image);
		layout.setComponentAlignment(image, Alignment.MIDDLE_LEFT);
	}

	private void createRightContent(ComponentContainer parent) {
		GridLayout rightLayout = new GridLayout(2, 5);
		rightLayout.setMargin(true);
		rightLayout.setSpacing(true);
		rightLayout.setColumns(2);
		parent.addComponent(rightLayout);

		// Refresh UI
		Refresher refresher = new Refresher();
		refresher.setRefreshInterval(REFRESH_INTERVAL / 2);
		parent.addComponent(refresher);
		
		// SICS status
		Panel panel = new Panel("SICS Status");
		panel.setWidth("408px");
		panel.setStyleName("bubble");
		rightLayout.addComponent(panel, 0, 0, 1, 0);
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		panelLayout.setSpacing(true);
		panelLayout.setWidth("100%");
		
		statusLabel = new Label();
		statusLabel.setWidth("100%");
		panelLayout.addComponent(statusLabel);
		
		// Experiment details panel
		createExperimentPanel(rightLayout);
		
		// Beam monitors panel
		createBeamMonitorsPanel(rightLayout);
		
		// Real motors panel
		createRealMotorsPanel(rightLayout);
		
		// Virtual motors panel
		createVirtualMotorsPanel(rightLayout);
		
		// Time
		timeLabel = new Label();
		rightLayout.addComponent(timeLabel, 0, 4, 1, 4);
	}

	private void createExperimentPanel(GridLayout parent) {
		Panel panel = new Panel("Experiment Details");
		panel.setWidth("408px");
		panel.setStyleName("bubble");
		parent.addComponent(panel, 0, 1, 1, 1);
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		panelLayout.setSpacing(true);
		panelLayout.setWidth("100%");
		
		createDynamicLabel(panelLayout, "/experiment/title", "Title");
		createDynamicLabel(panelLayout, "/sample/name", "Sample");
	}
	
	private void createBeamMonitorsPanel(GridLayout parent) {
		Panel panel = new Panel("Beam Monitors");
		panel.setWidth("408px");
		panel.setStyleName("bubble");
		parent.addComponent(panel, 0, 2, 1, 2);
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		panelLayout.setSpacing(true);
		panelLayout.setWidth("100%");
		
		// BM 1
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setWidth("100%");
		panelLayout.addComponent(horizontalLayout);
		
		Label label = new Label("BM1 ");
		label.setStyleName("white");
		horizontalLayout.addComponent(label);
		
		Label bm1Label = new Label("--");
		bm1Label.setStyleName("bigger white");
		bm1Label.setWidth("100%");
		componentMap.put("/monitor/bm1_counts", bm1Label);
		horizontalLayout.addComponent(bm1Label);
		
		label = new Label(" counts/sec");
		label.setStyleName("white");
		horizontalLayout.addComponent(label);
		
		// BM 2
		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();
		panelLayout.addComponent(horizontalLayout);
		
		label = new Label("BM2 ");
		label.setStyleName("white");
		horizontalLayout.addComponent(label);
		
		Label bm2Label = new Label("--");
		bm2Label.setStyleName("bigger white");
		bm2Label.setWidth("100%");
		componentMap.put("/monitor/bm2_counts", bm2Label);
		horizontalLayout.addComponent(bm2Label);
		
		label = new Label(" counts/sec");
		label.setStyleName("white");
		horizontalLayout.addComponent(label);
	}

	private void createRealMotorsPanel(ComponentContainer parent) {
		Panel panel = new Panel("Real Motors");
		panel.setWidth("200px");
		panel.setStyleName("bubble");
		parent.addComponent(panel);
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		panelLayout.setSpacing(true);
		panelLayout.setWidth("100%");
		
		createDynamicLabel(panelLayout, "/instrument/crystal/m1", "m1");
		createDynamicLabel(panelLayout, "/instrument/crystal/m2", "m2");
		createDynamicLabel(panelLayout, "/sample/s1", "s1");
		createDynamicLabel(panelLayout, "/sample/s2", "s2");
		createDynamicLabel(panelLayout, "/instrument/crystal/a1", "a1");
		createDynamicLabel(panelLayout, "/instrument/detector/a2", "a2");
	}
	
	private void createVirtualMotorsPanel(ComponentContainer parent) {
		Panel panel = new Panel("Virtual Motors");
		panel.setWidth("200px");
		panel.setStyleName("bubble");
		parent.addComponent(panel);
		VerticalLayout panelLayout = (VerticalLayout) panel.getContent();
		panelLayout.setSpacing(true);
		panelLayout.setWidth("100%");
		
		createDynamicLabel(panelLayout, "/sample/ei", "ei");
		createDynamicLabel(panelLayout, "/sample/ef", "ef");
		createDynamicLabel(panelLayout, "/sample/en", "en");
		createDynamicLabel(panelLayout, "/sample/qh", "qh");
		createDynamicLabel(panelLayout, "/sample/qk", "qk");
		createDynamicLabel(panelLayout, "/sample/ql", "ql");
	}
	
	private void createDynamicLabel(ComponentContainer parent, String path, String deviceLabel) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.setColumns(2);
		gridLayout.setWidth("100%");
		parent.addComponent(gridLayout);
				
		Label label = new Label(deviceLabel + " ");
		label.setStyleName("white");
		gridLayout.addComponent(label);
		gridLayout.setComponentAlignment(label, Alignment.TOP_LEFT);
				
		Label valueLabel = new Label("--");
		valueLabel.setStyleName("bigger white");
		valueLabel.setWidth("100%");
		componentMap.put(path, valueLabel);
		gridLayout.addComponent(valueLabel);
		gridLayout.setComponentAlignment(valueLabel, Alignment.TOP_RIGHT);
	}
	
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
			statusLabel.setStyleName("green bigger");
		} else {
			statusLabel.setStyleName("red bigger");
		}
		statusLabel.setValue(value);
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
		timeLabel.setValue("Last update: " + dateFormat.format(Calendar.getInstance().getTime()));
	}

}
