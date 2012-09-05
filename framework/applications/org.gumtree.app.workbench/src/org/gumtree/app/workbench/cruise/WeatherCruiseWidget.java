package org.gumtree.app.workbench.cruise;

import java.io.InputStream;
import java.net.URI;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.gumtree.util.xml.XMLUtils;
import org.gumtree.widgets.swt.util.UIResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class WeatherCruiseWidget extends AbstractCruisePageWidget {

	private static final Logger logger = LoggerFactory
			.getLogger(WeatherCruiseWidget.class);

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
		httpClient.performGet(
				URI.create("http://www.google.com/ig/api?weather=Sydney"),
				new HttpClientAdapter() {
					@Override
					public void handleResponse(InputStream in) {
						handleData(in);
					}
				});
	}

	private void handleData(InputStream in) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = builder.parse(in);
			Node weatherNode = document.getFirstChild().getFirstChild();
			Node currentConditionNode = XMLUtils.getFirstChild(weatherNode,
					"current_conditions");
			Node temperatureNode = XMLUtils.getFirstChild(currentConditionNode,
					"temp_c");
			final String temperature = XMLUtils.getAttribute(temperatureNode, "data");
			SafeUIRunner.asyncExec(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					context.temperatureLabel.setText(temperature + " C");
				}
			});
			Node iconNode = XMLUtils
					.getFirstChild(currentConditionNode, "icon");
			String icon = "http://www.google.com"
					+ XMLUtils.getAttribute(iconNode, "data");
			httpClient.performGet(URI.create(icon), new HttpClientAdapter() {
				@Override
				public void handleResponse(InputStream in) {
					handleIcon(in);
				}
			});
		} catch (Exception e) {
			logger.error("Failed to parse weather data", e);
		}
	}

	private void handleIcon(final InputStream in) {
		SafeUIRunner.asyncExec(new SafeRunnable() {
			@Override
			public void run() throws Exception {
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
