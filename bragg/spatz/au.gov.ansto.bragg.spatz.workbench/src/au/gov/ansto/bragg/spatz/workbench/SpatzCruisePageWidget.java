package au.gov.ansto.bragg.spatz.workbench;

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
import org.gumtree.control.ui.widgets.ControllerStatusWidget.PrecisionConverter;
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

public class SpatzCruisePageWidget extends AbstractCruisePageWidget {

	@Inject
	private IEclipseContext eclipseContext;

	private IDelayEventExecutor delayEventExecutor;

	@Inject
	private IDataAccessManager dataAccessManager;

	public SpatzCruisePageWidget(Composite parent, int style) {
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

			// Monochromator
			PGroup monitorGroup = createGroup("NEUTRON COUNTS",
					SharedImage.MONITOR.getImage());
			deviceStatusWidget = new ControllerStatusWidget(monitorGroup, SWT.NONE);
			deviceStatusWidget
			.addDevice("/monitor/bm1_event_rate", "BM1 counts rate", null, "cts/s")
			.addDevice("/monitor/bm2_event_rate", "BM2 counts rate", null, "cts/s")
					;
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			// Choppers
			PGroup chopperGroup = createGroup("CHOPPERS",
					SharedImage.POSITIONER.getImage());
			deviceStatusWidget = new ControllerStatusWidget(chopperGroup, SWT.NONE);
			deviceStatusWidget
			.addDevice("/instrument/chopper/c01/spee", "C01 speed", null, "rpm")
			.addDevice("/instrument/chopper/c01/phas", "C01 phase offset", null, "\u00b0")
			.addDevice("/instrument/chopper/c02/spee", "C02 speed", null, "rpm")
			.addDevice("/instrument/chopper/c02/phas", "C02 phase offset", null, "\u00b0")
			.addDevice("/instrument/chopper/c2b/spee", "C2b speed", null, "rpm")
			.addDevice("/instrument/chopper/c2b/phas", "C2b phase offset", null, "\u00b0")
			.addDevice("/instrument/chopper/c03/spee", "C03 speed", null, "rpm")
			.addDevice("/instrument/chopper/c03/phas", "C03 phase offset", null, "\u00b0")
			;
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			// Slits Info
			PGroup slits1Group = createGroup("SLITS",
					SharedImage.ONE.getImage());
			deviceStatusWidget = new ControllerStatusWidget(slits1Group, SWT.NONE);
			deviceStatusWidget
			.addDevice("/instrument/slits/second/horizontal/gap", "s2 horizontal gap", null, "mm")
			.addDevice("/instrument/slits/third/horizontal/gap", "s3 horizontal gap", null, "mm")
			.addDevice("/instrument/slits/fourth/horizontal/gap", "s4 horizontal gap", null, "mm")
			.addDevice("/instrument/slits/second/vertical/gap", "s2 vertical gap", null, "mm")
			.addDevice("/instrument/slits/third/vertical/gap", "s3 vertical gap", null, "mm")
			.addDevice("/instrument/slits/fourth/vertical/gap", "s4 vertical gap", null, "mm")
			;
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			// Monochromator
			PGroup sampleStageGroup = createGroup("SAMPLE STAGE",
					SharedImage.POSITIONER.getImage());
			deviceStatusWidget = new ControllerStatusWidget(sampleStageGroup, SWT.NONE);
			deviceStatusWidget
			.addDevice("/sample/som", "som", null, "deg", new PrecisionConverter(3))
			.addDevice("/sample/sc", "sc", null, "mm", new PrecisionConverter(3))
			.addDevice("/sample/stilt", "stilt", null, "mm", new PrecisionConverter(3))
			.addDevice("/sample/sx", "sx", null, "mm", new PrecisionConverter(3))
			.addDevice("/sample/sxtop", "sxtop", null, "mm", new PrecisionConverter(3))
			.addDevice("/sample/sy", "sy", null, "mm", new PrecisionConverter(3))
			.addDevice("/instrument/detector/detrot", "detrot", null, "deg", new PrecisionConverter(3))
			;
			configureWidget(deviceStatusWidget);
			deviceStatusWidget.render();

			PGroup pumpStageGroup = createGroup("PUMP STATUS",
					SharedImage.CRADLE.getImage());
			deviceStatusWidget = new ControllerStatusWidget(pumpStageGroup, SWT.NONE);
			deviceStatusWidget
			.addDevice("/sample/mvp1/Control/SetPoint", "mvp setpoint", null, "")
			.addDevice("/sample/hplc1/pump/status", "hplc status", null, "")
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

			DeviceStatusWidget deviceStatusWidget;
			//		// Devices
			// Monitor Event Rate
			PGroup monitorGroup = createGroup("NEUTRON COUNTS",
					SharedImage.MONITOR.getImage());
			deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
			deviceStatusWidget
			.addDevice("/monitor/bm1_event_rate", "BM1 counts rate", null, "cts/s")
			.addDevice("/monitor/bm2_event_rate", "BM2 counts rate", null, "cts/s")
			;
			configureWidget(deviceStatusWidget);


			// Choppers
			PGroup chopperGroup = createGroup("CHOPPERS",
					SharedImage.POSITIONER.getImage());
			deviceStatusWidget = new DeviceStatusWidget(chopperGroup, SWT.NONE);
			deviceStatusWidget
			.addDevice("/instrument/chopper/c01/spee", "C01 speed", null, "rpm")
			.addDevice("/instrument/chopper/c01/phas", "C01 phase offset", null, "\u00b0")
			.addDevice("/instrument/chopper/c02/spee", "C02 speed", null, "rpm")
			.addDevice("/instrument/chopper/c02/phas", "C02 phase offset", null, "\u00b0")
			.addDevice("/instrument/chopper/c2b/spee", "C2b speed", null, "rpm")
			.addDevice("/instrument/chopper/c2b/phas", "C2b phase offset", null, "\u00b0")
			.addDevice("/instrument/chopper/c03/spee", "C03 speed", null, "rpm")
			.addDevice("/instrument/chopper/c03/phas", "C03 phase offset", null, "\u00b0")
			;
			configureWidget(deviceStatusWidget);

			// Slits Info
			PGroup slits1Group = createGroup("SLITS",
					SharedImage.ONE.getImage());
			deviceStatusWidget = new DeviceStatusWidget(slits1Group, SWT.NONE);
			deviceStatusWidget
			.addDevice("/instrument/slits/second/horizontal/gap", "s2 horizontal gap", null, "mm")
			.addDevice("/instrument/slits/third/horizontal/gap", "s3 horizontal gap", null, "mm")
			.addDevice("/instrument/slits/fourth/horizontal/gap", "s4 horizontal gap", null, "mm")
			.addDevice("/instrument/slits/second/vertical/gap", "s2 vertical gap", null, "mm")
			.addDevice("/instrument/slits/third/vertical/gap", "s3 vertical gap", null, "mm")
			.addDevice("/instrument/slits/fourth/vertical/gap", "s4 vertical gap", null, "mm")
			;
			configureWidget(deviceStatusWidget);

			// Monochromator
			PGroup sampleStageGroup = createGroup("SAMPLE STAGE",
					SharedImage.POSITIONER.getImage());
			deviceStatusWidget = new DeviceStatusWidget(sampleStageGroup, SWT.NONE);
			deviceStatusWidget
			.addDevice("/sample/som", "som", null, "deg", new DeviceStatusWidget.PrecisionConverter(3))
			.addDevice("/sample/sc", "sc", null, "mm", new DeviceStatusWidget.PrecisionConverter(3))
			.addDevice("/sample/stilt", "stilt", null, "mm", new DeviceStatusWidget.PrecisionConverter(3))
			.addDevice("/sample/sx", "sx", null, "mm", new DeviceStatusWidget.PrecisionConverter(3))
			.addDevice("/sample/sxtop", "sxtop", null, "mm", new DeviceStatusWidget.PrecisionConverter(3))
			.addDevice("/sample/sy", "sy", null, "mm", new DeviceStatusWidget.PrecisionConverter(3))
			.addDevice("/instrument/detector/detrot", "detrot", null, "deg", new DeviceStatusWidget.PrecisionConverter(3))
			;
			configureWidget(deviceStatusWidget);

			PGroup pumpStageGroup = createGroup("PUMP STATUS",
					SharedImage.CRADLE.getImage());
			deviceStatusWidget = new DeviceStatusWidget(pumpStageGroup, SWT.NONE);
			deviceStatusWidget
			.addDevice("/sample/mvp1/Control/SetPoint", "mvp setpoint", null, "")
			.addDevice("/sample/hplc1/pump/status", "hplc status", null, "")
			;
			configureWidget(deviceStatusWidget);

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
