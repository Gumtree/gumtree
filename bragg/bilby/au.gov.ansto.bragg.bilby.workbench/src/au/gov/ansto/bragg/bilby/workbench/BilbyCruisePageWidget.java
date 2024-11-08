package au.gov.ansto.bragg.bilby.workbench;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.pgroup.AbstractGroupStrategy;
import org.eclipse.nebula.widgets.pgroup.ChevronsToggleRenderer;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.nebula.widgets.pgroup.SimpleGroupStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.gumtree.control.ui.widgets.ControllerStatusWidget;
import org.gumtree.control.ui.widgets.EnvironmentStatusWidget;
import org.gumtree.control.ui.widgets.PauseStatusWidget;
import org.gumtree.control.ui.widgets.ServerStatusWidget;
import org.gumtree.control.ui.widgets.ShutterGroupWidget;
import org.gumtree.gumnix.sics.ui.widgets.HMVetoGadget;
import org.gumtree.gumnix.sics.widgets.swt.DeviceStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.EnvironmentControlWidget;
import org.gumtree.gumnix.sics.widgets.swt.ShutterStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.SicsStatusWidget;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.util.messaging.ReducedDelayEventExecutor;

import au.gov.ansto.bragg.nbi.core.NBISystemProperties;
import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.workbench.ReactorStatusWidget;

public class BilbyCruisePageWidget extends AbstractCruisePageWidget {

	@Inject
	private IEclipseContext eclipseContext;

	private IDelayEventExecutor delayEventExecutor;

	@Inject
	private IDataAccessManager dataAccessManager;

	public BilbyCruisePageWidget(Composite parent, int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		GridLayoutFactory.swtDefaults().spacing(1, 0)
				.applyTo(this);
		getEclipseContext().set(IDelayEventExecutor.class,
				getDelayEventExecutor());
		
		if (NBISystemProperties.USE_NEW_PROXY) {
			// Reactor Source
			PGroup sourceGroup = createGroup("REACTOR SOURCE",
					SharedImage.REACTOR.getImage());
			ReactorStatusWidget reactorWidget = new ReactorStatusWidget(sourceGroup, SWT.NONE);
			reactorWidget.addDevice("reactorPower", "Power", "MW")
					.addDevice("cnsInTemp", "CNS Inlet Temp", "K")
					.addDevice("cnsOutTemp", "CNS Outlet Temp", "K");
			reactorWidget.createWidgetArea();
			configureWidget(reactorWidget);
			sourceGroup.setExpanded(false);
			reactorWidget.setExpandingEnabled(true);

			// Shutter Status
			PGroup shutterGroup = createGroup("SHUTTER STATUS",
					SharedImage.SHUTTER.getImage());
			ShutterGroupWidget shutterStatuswidget = new ShutterGroupWidget(
					shutterGroup, SWT.NONE);
			configureWidget(shutterStatuswidget);
			shutterGroup.setExpanded(true);
			shutterStatuswidget.render();

			// Server Status
			PGroup sicsStatusGroup = createGroup("SERVER STATUS", 
					SharedImage.SERVER.getImage());
			ServerStatusWidget statusWidget = new ServerStatusWidget(sicsStatusGroup,
					SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
			.grab(true, false).applyTo(statusWidget);
			configureWidget(statusWidget);
			statusWidget.render();

			// Pause Counter
			PGroup pauseGroup = createGroup("PAUSE COUNTING",
					SharedImage.SHUTTER.getImage());
			PauseStatusWidget pauseStatuswidget = new PauseStatusWidget(
					pauseGroup, SWT.NONE);
			configureWidget(pauseStatuswidget);
			pauseStatuswidget.render();

			ControllerStatusWidget deviceStatusWidget;

			// Devices
//			PGroup monochromatorGroup = createGroup("ATTENUATOR",
//					SharedImage.SPIN.getImage());
//			deviceStatusWidget = new ControllerStatusWidget(monochromatorGroup, SWT.NONE);
//			deviceStatusWidget
//					.addDevice("/instrument/att", "att", null, null)
//					;
//			configureWidget(deviceStatusWidget);
//			deviceStatusWidget.render();

			PGroup vsGroup = createGroup("VELOCITY SELECTOR",
					SharedImage.SPIN.getImage());
			deviceStatusWidget = new ControllerStatusWidget(vsGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/instrument/nvs067/lambda", "wavelength", null, "\u212B")
					;
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			// Choppers
			PGroup chopperGroup = createGroup("CHOPPERS",
					SharedImage.GEAR.getImage());
			deviceStatusWidget = new ControllerStatusWidget(chopperGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/instrument/t0_chopper_id", "T0 chopper ID", null, "")
					.addDevice("/instrument/t0_chopper_freq", "T0 chopper frequency", null, "Hz")
					.addDevice("/instrument/master1_chopper_id", "master1 chopper", null, "")
					.addDevice("/instrument/master2_chopper_id", "master2 chopper", null, "")
					.addDevice("/instrument/master_chopper_freq", "master chopper frequency", null, "Hz")
					;
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			// Detector
			PGroup detectorGroup = createGroup("DETECTOR",
					SharedImage.POWER.getImage());
			deviceStatusWidget = new ControllerStatusWidget(detectorGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/instrument/att", "att", null, null)
					.addDevice("/instrument/nguide", "guide", null, null)
					.addDevice("/instrument/detector/det", "det", null, "")
					;
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			// Monitor Event Rate
			PGroup monitorGroup = createGroup("NEUTRON COUNTS",
					SharedImage.MONITOR.getImage());
			deviceStatusWidget = new ControllerStatusWidget(monitorGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/monitor/data", "monitor counts", null, "")
					.addDevice("/instrument/detector/total_counts", "detector counts", null, "");
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			// Sample Info
			PGroup sampleGroup = createGroup("SAMPLE",
					SharedImage.BEAKER.getImage());
			deviceStatusWidget = new ControllerStatusWidget(sampleGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/sample/name", "name", null, "")
					;
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			// Sample Info
			PGroup bsGroup = createGroup("BEAM STOPS",
					SharedImage.POSITIONER.getImage());
			deviceStatusWidget = new ControllerStatusWidget(bsGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/instrument/detector/bs3", "bs3", null, "")
					.addDevice("/instrument/detector/bs4", "bs4", null, "")
					.addDevice("/instrument/detector/bs5", "bs5", null, "")
					;
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			// Environment Group
			PGroup environmentGroup = createGroup("ENVIRONMENT CONTROLLERS",
					SharedImage.FURNACE.getImage());
			EnvironmentStatusWidget controlWidget = new EnvironmentStatusWidget(environmentGroup, SWT.NONE);
			configureWidget(controlWidget);
			controlWidget.render();

		} else {
			// Reactor Source
			PGroup sourceGroup = createGroup("REACTOR SOURCE",
					SharedImage.REACTOR.getImage());
			ReactorStatusWidget reactorWidget = new ReactorStatusWidget(sourceGroup, SWT.NONE);
			reactorWidget.addDevice("reactorPower", "Power", "MW")
					.addDevice("cnsInTemp", "CNS Inlet Temp", "K")
					.addDevice("cnsOutTemp", "CNS Outlet Temp", "K");
			reactorWidget.createWidgetArea();
			configureWidget(reactorWidget);
			sourceGroup.setExpanded(false);
			reactorWidget.setExpandingEnabled(true);
	
			// Shutter Status
			PGroup shutterGroup = createGroup("SHUTTER STATUS",
					SharedImage.SHUTTER.getImage());
			ShutterStatusWidget shutterStatuswidget = new ShutterStatusWidget(
					shutterGroup, SWT.NONE);
			configureWidget(shutterStatuswidget);
			shutterGroup.setExpanded(false);
	
			// Server Status
			PGroup sicsStatusGroup = createGroup("SERVER STATUS", 
					SharedImage.SERVER.getImage());
			SicsStatusWidget statusWidget = new SicsStatusWidget(sicsStatusGroup,
					SWT.NONE);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
					.grab(true, false).applyTo(statusWidget);
			configureWidget(statusWidget);
	
			// Pause Counter
			PGroup pauseGroup = createGroup("PAUSE COUNTING",
					SharedImage.SHUTTER.getImage());
			HMVetoGadget pauseStatuswidget = new HMVetoGadget(
					pauseGroup, SWT.NONE);
			configureWidget(pauseStatuswidget);
	
			// Hist Counter
	//		PGroup histGroup = createGroup("HISTOGRAM",
	//				SharedImage.SHUTTER.getImage());
	//		HMImageDisplayWidget hmWidget = new HMImageDisplayWidget(
	//				histGroup, SWT.NONE);
	//		String path = "http://localhost:60030/admin/openimageinformat.egi&#63;type=HISTOPERIOD_XYT&#38;open_format=DISLIN_PNG&#38;open_colour_table=RAIN&#38;open_plot_zero_pixels=AUTO&#38;open_annotations=ENABLE";
	////		String path = "http://localhost:60030/admin/openimageinformat.egi?type=HISTOPERIOD_XYT&open_format=DISLIN_PNG&open_colour_table=RAIN&open_plot_zero_pixels=AUTO&open_annotations=ENABLE";
	//		hmWidget.setDataURI(path);
	//		configureWidget(hmWidget);
	
			// Devices
			PGroup monochromatorGroup = createGroup("DEVICES",
					SharedImage.SPIN.getImage());
			DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(monochromatorGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/instrument/collimator/att", "att", null, null)
					.addDevice("/instrument/cdd", "cdd", null, null)
					.addDevice("/instrument/cdr", "cdr", null, null)
					.addDevice("/instrument/sdh", "sdh", null, null)
					;
			configureWidget(deviceStatusWidget);
	
			// Detector
			PGroup detectorGroup = createGroup("DETECTOR",
					SharedImage.POWER.getImage());
			deviceStatusWidget = new DeviceStatusWidget(detectorGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/instrument/detector/total_counts", "total counts", null, "cts")
					.addDevice("/instrument/detector/cdl", "cdl", null, null)
					.addDevice("/instrument/detector/cdu", "cdu", null, null)
					.addDevice("/instrument/det", "det", null, "")
					;
			configureWidget(deviceStatusWidget);
	
			// Monitor Event Rate
			PGroup monitorGroup = createGroup("NEUTRON COUNTS",
					SharedImage.MONITOR.getImage());
			deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/monitor/bm1_counts", "BM1 counts", null, "cts")
					.addDevice("/monitor/bm2_counts", "BM2 counts", null, "cts");
			configureWidget(deviceStatusWidget);
	
			// Choppers
			PGroup chopperGroup = createGroup("CHOPPERS",
					SharedImage.POSITIONER.getImage());
			deviceStatusWidget = new DeviceStatusWidget(chopperGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/instrument/master_chopper_id", "master chopper", null, "")
					;
			configureWidget(deviceStatusWidget);
	
			// Sample Info
			PGroup sampleGroup = createGroup("SAMPLE",
					SharedImage.BEAKER.getImage());
			deviceStatusWidget = new DeviceStatusWidget(sampleGroup, SWT.NONE);
			deviceStatusWidget
					.addDevice("/sample/name", "Name", null, "")
					;
			configureWidget(deviceStatusWidget);
	
	
			// Furnace Temp
	//		PGroup furnaceGroup = createGroup("FURNACE TEMP",
	//				SharedImage.FURNACE.getImage());
	//		deviceStatusWidget = new DeviceStatusWidget(furnaceGroup, SWT.NONE);
	//		deviceStatusWidget
	//				.addDevice("/sample/tempone/sensorA/value", "temperature")
	//				.addDevice("/sample/tempone/setpoint", "set point");
	//		configureWidget(deviceStatusWidget);
	
	
			// Temperature TC1 Control
	//		PGroup tempControlGroup = createGroup("TEMPERATURE CONTR",
	//				SharedImage.FURNACE.getImage());
	//		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup, SWT.NONE);
	//		deviceStatusWidget
	//				.addDevice("/sample/tc1/sensor/sensorValueA", "TC1A",
	//						SharedImage.A.getImage(), null)
	//				.addDevice("/sample/tc1/sensor/sensorValueB", "TC1B",
	//						SharedImage.B.getImage(), null)
	//				.addDevice("/sample/tc1/sensor/sensorValueC", "TC1C",
	//						SharedImage.C.getImage(), null)
	//				.addDevice("/sample/tc1/sensor/sensorValueD", "TC1D",
	//						SharedImage.D.getImage(), null);
	//		configureWidget(deviceStatusWidget);
	
			// Environment Group
			PGroup environmentGroup = createGroup("ENVIRONMENT CONTROLLERS",
					SharedImage.FURNACE.getImage());
			EnvironmentControlWidget controlWidget = new EnvironmentControlWidget(environmentGroup, SWT.NONE);
			configureWidget(controlWidget);
		}
	}

	@Override
	protected void disposeWidget() {
		if (delayEventExecutor != null) {
			delayEventExecutor.deactivate();
			delayEventExecutor = null;
		}
		eclipseContext = null;
		dataAccessManager = null;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

	public IEclipseContext getEclipseContext() {
		return eclipseContext;
	}

	public void setEclipseContext(IEclipseContext eclipseContext) {
		this.eclipseContext = eclipseContext.createChild();
	}

	public IDataAccessManager getDataAccessManager() {
		return dataAccessManager;
	}

	public void setDataAccessManager(IDataAccessManager dataAccessManager) {
		this.dataAccessManager = dataAccessManager;
	}

	public IDelayEventExecutor getDelayEventExecutor() {
		if (delayEventExecutor == null) {
			delayEventExecutor = new ReducedDelayEventExecutor(1000).activate();
		}
		return delayEventExecutor;
	}

	public void setDelayEventExecutor(IDelayEventExecutor delayEventExecutor) {
		this.delayEventExecutor = delayEventExecutor;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	protected PGroup createGroup(String text, Image image) {
		PGroup group = new PGroup(this, SWT.NONE);
		AbstractGroupStrategy strategy = new SimpleGroupStrategy();
		group.setStrategy(strategy);
		group.setToggleRenderer(new ChevronsToggleRenderer());
		group.setText(text);
		group.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		group.setImage(image);
		group.setForeground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		group.setLinePosition(SWT.CENTER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(1).spacing(1, 1)
				.margins(10, SWT.DEFAULT).applyTo(group);
		return group;
	}

	protected void configureWidget(Control widget) {
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(widget);
		if (getEclipseContext() != null) {
			ContextInjectionFactory.inject(widget, getEclipseContext());
		}
	}

}
