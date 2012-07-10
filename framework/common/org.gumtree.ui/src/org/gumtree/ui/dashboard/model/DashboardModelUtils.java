package org.gumtree.ui.dashboard.model;

import java.io.InputStream;

import org.gumtree.util.collection.IParameters;
import org.gumtree.util.collection.Parameters;
import org.gumtree.util.xml.ParametersConverter;

import com.thoughtworks.xstream.XStream;

public final class DashboardModelUtils {

	private static volatile XStream xStream;

	public static XStream getXStream() {
		if (xStream == null) {
			synchronized (DashboardModelUtils.class) {
				if (xStream == null) {
					xStream = new XStream();
					xStream.alias("dashboard", Dashboard.class);
					xStream.alias("widget", Widget.class);
					xStream.alias("parameters", IParameters.class, Parameters.class);
					xStream.aliasAttribute(Dashboard.class, "title", "title");
					xStream.aliasAttribute(Dashboard.class, "layoutConstraints", "layoutConstraints");
					xStream.aliasAttribute(Dashboard.class, "rowConstraints", "rowConstraints");
					xStream.aliasAttribute(Dashboard.class, "colConstraints", "colConstraints");
					xStream.aliasAttribute(Widget.class, "classname", "classname");
					xStream.aliasAttribute(Widget.class, "id", "id");
					xStream.aliasAttribute(Widget.class, "style", "style");
					xStream.aliasAttribute(Widget.class, "title", "title");
					xStream.aliasAttribute(Widget.class, "collapsible", "collapsible");
					xStream.aliasAttribute(Widget.class, "collapsed", "collapsed");
					xStream.aliasAttribute(Widget.class, "hideTitleBar", "hideTitleBar");
					xStream.aliasAttribute(Widget.class, "layoutData", "layoutData");
					xStream.aliasAttribute(Widget.class, "listenToWidgets", "listenToWidgets");
					xStream.addImplicitCollection(Dashboard.class, "widgets", Widget.class);
					xStream.registerConverter(new ParametersConverter(xStream.getMapper()));
				}
			}
		}
		return xStream;
	}
	
	public static Dashboard createEmptyDashboard() {
		return new Dashboard();
	}
	
	public static Dashboard loadModel(InputStream inputStream) {
		return (Dashboard) DashboardModelUtils.getXStream().fromXML(inputStream);
	}
	
	private DashboardModelUtils() {
		super();
	}
	
}
