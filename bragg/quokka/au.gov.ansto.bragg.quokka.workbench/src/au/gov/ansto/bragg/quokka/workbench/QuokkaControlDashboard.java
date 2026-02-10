package au.gov.ansto.bragg.quokka.workbench;

import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;

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
import org.gumtree.control.ui.widgets.ServerStatusWidget;
import org.gumtree.control.ui.widgets.ShutterGroupWidget;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.util.messaging.ReducedDelayEventExecutor;

import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.workbench.ReactorStatusWidget;

@SuppressWarnings("restriction")
public class QuokkaControlDashboard extends AbstractCruisePageWidget {

	@Inject
	private IEclipseContext eclipseContext;

	private IDelayEventExecutor delayEventExecutor;

	@Inject
	private IDataAccessManager dataAccessManager;

	public QuokkaControlDashboard(Composite parent, int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		GridLayoutFactory.swtDefaults().spacing(1, 0)
				.applyTo(this);
		getEclipseContext().set(IDelayEventExecutor.class,
				getDelayEventExecutor());
		
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
//		PGroup pauseGroup = createGroup("PAUSE COUNTING",
//				SharedImage.SHUTTER.getImage());
//		HMVetoGadget pauseStatuswidget = new HMVetoGadget(
//				pauseGroup, SWT.NONE);
//		configureWidget(pauseStatuswidget);

		// Devices
		PGroup monochromatorGroup = createGroup("DEVICES",
				SharedImage.SPIN.getImage());
		ControllerStatusWidget deviceStatusWidget = new ControllerStatusWidget(monochromatorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/collimator/att", "att", null, null)
				.addDevice("/instrument/collimator/srce", "srce", null, null)
				.addDevice("/instrument/collimator/apx", "apx", null, null)
				.addDevice("/instrument/velocity_selector/wavelength_nominal", "wavelength", null, null)
				;
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();

		// Detector
		PGroup detectorGroup = createGroup("DETECTOR",
				SharedImage.POWER.getImage());
		deviceStatusWidget = new ControllerStatusWidget(detectorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/detector/total_counts", "total counts", null, "cts")
				.addDevice("/instrument/detector/detector_y", "detector_y", null, null)
				.addDevice("/instrument/detector/detector_x", "detector_x", null, null)
				.addDevice("/instrument/parameters/BeamStop", "beam stop", null, "")
				.addDevice("/instrument/detector/bsx", "bsx", null, null)
				.addDevice("/instrument/detector/bsz", "bsz", null, null)
				;
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("NEUTRON COUNTS",
				SharedImage.MONITOR.getImage());
		deviceStatusWidget = new ControllerStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/monitor/bm1_counts", "BM1 counts", null, "cts")
				.addDevice("/monitor/bm2_counts", "BM2 counts", null, "cts");
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();

		// Sample Info
		PGroup sampleGroup = createGroup("SAMPLE",
				SharedImage.BEAKER.getImage());
		deviceStatusWidget = new ControllerStatusWidget(sampleGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/samplenumber", "Number", null, "")
				.addDevice("/sample/name", "Name", null, "")
				;
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();

		// guide
		PGroup fermi1Group = createGroup("GUIDE",
				SharedImage.CRADLE.getImage());
		deviceStatusWidget = new ControllerStatusWidget(fermi1Group, SWT.NONE);
		deviceStatusWidget
				.addDevice("/commands/optics/guide/configuration", "configuration", null, "")
				;
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();

		PGroup tempControlGroup = createGroup("ENVIRONMENT CONTROLLERS",
				SharedImage.FURNACE.getImage());
		EnvironmentStatusWidget controlWidget = new EnvironmentStatusWidget(tempControlGroup, SWT.NONE);
		configureWidget(controlWidget);
		controlWidget.render();
		
		// Experiment info
//		PGroup infoGroup = createGroup("EXPERIMENT INFO",
//				InternalImage.EXPERIMENT_INFO.getImage());
//		deviceStatusWidget = new DeviceStatusWidget(infoGroup, SWT.NONE);
//		configureWidget(deviceStatusWidget);
//		deviceStatusWidget.addDevice("/experiment/title", "Proposal")
//				.addDevice("/experiment/title", "Sample")
//				.addDevice("/user/name", "User").render();

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

