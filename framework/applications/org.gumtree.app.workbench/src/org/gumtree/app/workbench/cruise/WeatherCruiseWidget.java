package org.gumtree.app.workbench.cruise;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.gumtree.service.httpclient.HttpClientAdapter;
import org.gumtree.service.httpclient.IHttpClient;
import org.gumtree.service.httpclient.IHttpClientFactory;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.ui.util.SafeUIRunner;
import org.gumtree.widgets.swt.util.UIResources;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherCruiseWidget extends AbstractCruisePageWidget {

	private static final Logger logger = LoggerFactory.getLogger(WeatherCruiseWidget.class);

	// The old Google Weather API is defunct. Switched to wttr.in for a working alternative.
	private static final String WEATHER_API_URL = "https://wttr.in/Sydney?format=j1";

	private IHttpClientFactory httpClientFactory;

	private IHttpClient httpClient;

	private UIContext context;

	public WeatherCruiseWidget(Composite parent, int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);
		context = new UIContext();

		// Icon
		context.weatherIconLabel = getWidgetFactory().createLabel(this, "");
		context.weatherIconLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));
		GridDataFactory.swtDefaults().span(1, 2).applyTo(context.weatherIconLabel);

		// Temperature
		context.temperatureLabel = getWidgetFactory().createLabel(this, "-- C");
		context.temperatureLabel.setFont(UIResources.getDefaultFont(SWT.BOLD));

		// Location
		context.locationLabel = getWidgetFactory().createLabel(this, "Sydney");

		httpClient = getHttpClientFactory().createHttpClient();
		httpClient.performGet(URI.create(WEATHER_API_URL), new HttpClientAdapter() {
			@Override
			public void handleResponse(InputStream in) {
				handleData(in);
			}
		});
	}

	private void handleData(InputStream in) {
		try {
			// Read InputStream into a String
			StringBuilder textBuilder = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
				int c;
				while ((c = reader.read()) != -1) {
					textBuilder.append((char) c);
				}
			}
			String jsonString = textBuilder.toString();

			// Parse the JSON response
			JSONObject json = new JSONObject(jsonString);
			JSONObject currentCondition = json.getJSONArray("current_condition").getJSONObject(0);

			// Extract temperature
			final String temperature = currentCondition.getString("temp_C");
			SafeUIRunner.asyncExec(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					context.temperatureLabel.setText(temperature + " C");
				}
			});

			// Extract icon URL and fetch the image
			String iconUrl = currentCondition.getJSONArray("weatherIconUrl").getJSONObject(0).getString("value");
			httpClient.performGet(URI.create(iconUrl), new HttpClientAdapter() {
				@Override
				public void handleResponse(InputStream in) {
					handleIcon(in);
				}
			});
		} catch (Exception e) {
			logger.error("Failed to parse weather JSON data", e);
		}
	}

	private void handleIcon(final InputStream in) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				if (isDisposed()) {
					return;
				}
				if (context.icon != null) {
					context.icon.dispose();
				}
				context.icon = new Image(getDisplay(), in);
				context.weatherIconLabel.setImage(context.icon);
			}
		});
	}

	@Override
	protected void disposeWidget() {
		if (httpClient != null) {
			httpClient.disposeObject();
			httpClient = null;
		}
		if (context != null) {
			if (context.icon != null) {
				context.icon.dispose();
			}
			context = null;
		}
		httpClientFactory = null;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IHttpClientFactory getHttpClientFactory() {
		return httpClientFactory;
	}

	@Inject
	public void setHttpClientFactory(IHttpClientFactory httpClientFactory) {
		this.httpClientFactory = httpClientFactory;
	}

	class UIContext {
		private Image icon;
		private Label temperatureLabel;
		private Label locationLabel;
		private Label weatherIconLabel;
	}

}
