package org.gumtree.gumnix.sics.internal.ui.statusview;

import java.awt.Color;
import java.awt.Frame;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.gumtree.gumnix.sics.control.IDeviceController;
import org.gumtree.gumnix.sics.control.controller.IComponentController;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.ui.statusview.IComponentStatusUIPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import ch.psi.sics.hipadaba.Component;
import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public class DefaultDeviceStatusUIPart2 implements IComponentStatusUIPart {

	private FormToolkit toolkit;

	private IComponentController controller;

	private DevicePropertyViewer viewer;

	private Property monitorProperty;

	private Thread valueReaderThread;

	public DefaultDeviceStatusUIPart2() {
	}

	// TODO: refactor to support generic component, not only device
	public void createPartControl(Composite parent, IComponentController controller) {
		this.controller = controller;
		parent.setLayout(new FillLayout());
		toolkit = new FormToolkit(parent.getDisplay());
		ScrolledForm form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new GridLayout());

		createInfoSection(form.getBody());

		if(controller instanceof IDeviceController) {
			createPropertyMonitorSection(form.getBody());
			createPropertySection(form.getBody());
		}
	}

	private void createInfoSection(Composite parent) {
		Section infoSection = toolkit.createSection(parent, Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
		infoSection.setText("Component Info");
		GridData infoSectionData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		infoSection.setLayoutData(infoSectionData);
		Composite client = toolkit.createComposite(infoSection);
		client.setLayout(new GridLayout());
		toolkit.createLabel(client, "Component Id: " + getController().getComponent().getId());
		toolkit.createLabel(client, "Component Label: " +  getController().getComponent().getId());
		toolkit.createLabel(client, "Component Path: " +  getController().getPath());
		infoSection.setClient(client);
	}

	private void createPropertySection(Composite parent) {
		Section propertySection = toolkit.createSection(parent, Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
		propertySection.setText("Device Properties");
		GridData propertySectionData = new GridData(SWT.FILL, SWT.FILL, true, false);
		propertySection.setLayoutData(propertySectionData);
		Composite client = toolkit.createComposite(propertySection);
		client.setLayout(new FillLayout());
		viewer = new DevicePropertyViewer((IDeviceController)getController());
		viewer.createPartControl(client);
		propertySection.setClient(client);
	}

	private void createPropertyMonitorSection(Composite parent) {
		Section monitorSection = toolkit.createSection(parent, Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
		monitorSection.setText("Device Property Monitor");
		GridData monitorSectionData = new GridData(SWT.FILL, SWT.FILL, true, true);
		monitorSection.setLayoutData(monitorSectionData);
		Composite client = toolkit.createComposite(monitorSection);
		client.setLayout(new FillLayout());
		monitorSection.setClient(client);

		Composite embeddedComposite = new Composite(client, SWT.EMBEDDED);
		embeddedComposite.setLayout(new FillLayout());
		Frame frame = SWT_AWT.new_Frame(embeddedComposite);

		Device device = ((IDeviceController)getController()).getDevice();

		// test
		for(Property property : (List<Property>)device.getProperty()) {
			if(property.getId().equals("position")) {
				monitorNewProperty(property);
				break;
			}
		}

		PropertyDataSet dataset = new PropertyDataSet(monitorProperty, (IDeviceController)getController());

		JFreeChart chart = ChartFactory.createTimeSeriesChart(SicsUtils.getPath(device) + " position", "Time", "Position", dataset, false, false, false);
		chart.setBackgroundPaint(Color.WHITE);
		ChartPanel panel = new ChartPanel(chart);
		frame.add(panel);

	}

	private void monitorNewProperty(Property property) {
		monitorProperty = property;
	}

	public void dispose() {
		if(viewer != null) {
			viewer.dispose();
		}
	}

	public IComponentController getController() {
		return controller;
	}

}
