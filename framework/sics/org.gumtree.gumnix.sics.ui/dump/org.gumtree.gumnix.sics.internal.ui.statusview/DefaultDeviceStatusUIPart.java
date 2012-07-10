package org.gumtree.gumnix.sics.internal.ui.statusview;

import java.awt.Color;
import java.awt.Frame;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.nebula.widgets.pshelf.PShelf;
import org.eclipse.swt.nebula.widgets.pshelf.PShelfItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.gumtree.gumnix.sics.control.IDeviceController;
import org.gumtree.gumnix.sics.control.controller.IComponentController;
import org.gumtree.gumnix.sics.core.SicsUtils;
import org.gumtree.gumnix.sics.ui.statusview.IComponentStatusUIPart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import ch.psi.sics.hipadaba.Device;
import ch.psi.sics.hipadaba.Property;

public class DefaultDeviceStatusUIPart implements IComponentStatusUIPart {

	private FormToolkit toolkit;

	private IComponentController controller;

	private DevicePropertyViewer viewer;

	private Property monitorProperty;

//	private Thread valueReaderThread;

	public DefaultDeviceStatusUIPart() {
	}

	// TODO: refactor to support generic component, not only device
	public void createPartControl(Composite parent, IComponentController controller) {
		this.controller = controller;
		toolkit = new FormToolkit(parent.getDisplay());
		parent.setLayout(new FillLayout());
		PShelf shelf = new PShelf(parent, SWT.BORDER);
//		shelf.setRenderer(new RedmondShelfRenderer());
		PShelfItem itemInfo = new PShelfItem(shelf,SWT.NONE);
		itemInfo.setText("Component Info");
		itemInfo.getBody().setLayout(new FillLayout());
		createInfoSection(itemInfo.getBody());
		if(controller instanceof IDeviceController) {
			PShelfItem itemTable = new PShelfItem(shelf,SWT.NONE);
			itemTable.setText("Device Properties");
			itemTable.getBody().setLayout(new FillLayout());
			createPropertySection(itemTable.getBody());

			PShelfItem itemMonitor = new PShelfItem(shelf,SWT.NONE);
			itemMonitor.setText("Device Property Monitor");
			itemMonitor.getBody().setLayout(new FillLayout());
			createPropertyMonitorSection(itemMonitor.getBody());

			shelf.setSelection(itemMonitor);
		}
	}

	private void createInfoSection(Composite parent) {
		ScrolledForm form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new GridLayout());
		getToolkit().createLabel(form.getBody(), "Component Id: " + getController().getComponent().getId());
		getToolkit().createLabel(form.getBody(), "Component Label: " +  getController().getComponent().getId());
		getToolkit().createLabel(form.getBody(), "Component Path: " +  getController().getPath());
	}

	private void createPropertySection(Composite parent) {
		ScrolledForm form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FillLayout());
		viewer = new DevicePropertyViewer((IDeviceController)getController());
		viewer.createPartControl(form.getBody());
	}

	private void createPropertyMonitorSection(Composite parent) {
		ScrolledForm form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FillLayout());
		Composite embeddedComposite = new Composite(form.getBody(), SWT.EMBEDDED);
		embeddedComposite.setLayout(new FillLayout());
		Frame frame = SWT_AWT.new_Frame(embeddedComposite);

		Device device = ((IDeviceController)getController()).getDevice();
		monitorNewProperty(((IDeviceController)getController()).getDefaultProperty());

		// test
//		for(Property property : (List<Property>)device.getProperty()) {
//			if(property.getId().equals("position")) {
//				monitorNewProperty(property);
//				break;
//			}
//		}

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

	private FormToolkit getToolkit() {
		return toolkit;
	}

}
