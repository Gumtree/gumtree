package au.gov.ansto.bragg.nbi.server.vaadin;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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
import au.gov.ansto.bragg.nbi.server.internal.Activator;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.Application;
import com.vaadin.addon.touchkit.ui.NavigationBar;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

@SuppressWarnings("serial")
public class InstrumentStatutsNavigationView extends NavigationView {

	private static final int DEFAULT_REFRESH_INTERVAL = 10000;

	private static final Logger logger = LoggerFactory
			.getLogger(InstrumentStatutsNavigationView.class);

	private int refreshInterval = DEFAULT_REFRESH_INTERVAL;

	private String sicsRestletURI;

	private Map<String, UIContext> contextMap;

	private List<StatusGroup> groups;

	private Label statusLabel;

	private NavigationBar toolbar;

	public InstrumentStatutsNavigationView() {
		super();
		groups = new ArrayList<StatusGroup>(2);
		contextMap = new HashMap<String, UIContext>();
		sicsRestletURI = "http://"
				+ NBIServerProperties.SICS_RESTLET_HOST.getValue() + ":"
				+ NBIServerProperties.SICS_RESTLET_PORT.getInt() + "/sics/rest";
		logger.info("Sics restlet uri is resolved to " + sicsRestletURI + ".");
	}

	public void initialise() {
		CssLayout content = new CssLayout();
		content.setSizeFull();

		// Refresh UI
		Refresher refresher = new Refresher();
		refresher.setRefreshInterval(getRefreshInterval());
		content.addComponent(refresher);

		createSicsStatusGroup(content);

		for (StatusGroup group : groups) {
			createStatusGroup(content, group);
		}

		setContent(content);
		setToolbar(createToolbar(getApplication()));

		// Schedule update
		Job job = new Job("update") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					update();
				} catch (Exception e) {
					logger.error("Failed to update Taipan data", e);
				}
				if (getApplication().isRunning()) {
					schedule(getRefreshInterval());
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private void createSicsStatusGroup(ComponentContainer parent) {
		VerticalComponentGroup componentGroup = new VerticalComponentGroup();
		componentGroup.setCaption("SICS Status");

		statusLabel = new Label("");
		componentGroup.addComponent(statusLabel);

		parent.addComponent(componentGroup);
	}

	private void createStatusGroup(ComponentContainer parent, StatusGroup group) {
		VerticalComponentGroup componentGroup = new VerticalComponentGroup();
		componentGroup.setCaption(group.getLabel());

		for (StatusItem item : group.getItems()) {
			Label label = new Label("<b>" + item.getLabel() + "</b>: --",
					Label.CONTENT_XHTML);
			contextMap.put(item.getPath(),
					new UIContext(item.getLabel(), label));
			componentGroup.addComponent(label);
		}

		parent.addComponent(componentGroup);
	}

	private Component createToolbar(Application application) {
		toolbar = new NavigationBar();

		Button refresh = new Button();
		try {
//			refresh.setIcon(new OsgiResource(Activator.PLUGIN_ID,
//					"/images/reload-icon.png", application));
		} catch (Exception e) {
			e.printStackTrace();
		}
		toolbar.setLeftComponent(refresh);

		SimpleDateFormat formatter = new SimpleDateFormat("M/d/yy hh:mm:ss");
		toolbar.setCaption("Updated "
				+ formatter.format(Calendar.getInstance().getTime()));

		return toolbar;
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
		statusLabel.setValue(value);
	}

	private void updateHDB() throws Exception {
		// Prepare url
		StringBuffer buffer = new StringBuffer();
		buffer.append(sicsRestletURI + "/hdbs?components=");
		for (Entry<String, UIContext> entry : contextMap.entrySet()) {
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
				UIContext context = contextMap
						.get(deviceData.getString("path"));
				Label label = context.label;
				if (label != null) {
					if (deviceData.has("value")) {
						String value = deviceData.getString("value");
						if (StringUtils.isNumber(value)) {
							value = formatter.format(Double.parseDouble(value));
						}
						label.setValue("<b>" + context.deviceLabel + "</b>: "
								+ value);
					}
					continue;
				}
			}
		}

		SimpleDateFormat dateFormatter = new SimpleDateFormat("M/d/yy hh:mm:ss");
		toolbar.setCaption("Updated "
				+ dateFormatter.format(Calendar.getInstance().getTime()));
	}

	public StatusGroup createStatusGroup(String label) {
		StatusGroup statusGroup = new StatusGroup(label);
		groups.add(statusGroup);
		return statusGroup;
	}

	public int getRefreshInterval() {
		return refreshInterval;
	}

	public void setRefreshInterval(int refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	private class UIContext {
		String deviceLabel;
		Label label;

		private UIContext(String deviceLabel, Label label) {
			this.deviceLabel = deviceLabel;
			this.label = label;
		}
	}

	public class StatusGroup {
		private String label;
		private List<StatusItem> items;

		public StatusGroup(String label) {
			this.label = label;
			items = new ArrayList<StatusItem>(2);
		}

		public String getLabel() {
			return label;
		}

		public List<StatusItem> getItems() {
			return items;
		}

		public StatusGroup addStatusItem(String path, String label) {
			items.add(new StatusItem(path, label));
			return this;
		}
	}

	public class StatusItem {
		private String path;
		private String label;

		public StatusItem(String path, String label) {
			this.path = path;
			this.label = label;
		}

		public String getPath() {
			return path;
		}

		public String getLabel() {
			return label;
		}
	}

}
