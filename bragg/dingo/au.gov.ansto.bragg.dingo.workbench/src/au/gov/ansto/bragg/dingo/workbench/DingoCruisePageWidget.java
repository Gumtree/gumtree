package au.gov.ansto.bragg.dingo.workbench;

import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.pgroup.ChevronsToggleRenderer;
import org.eclipse.nebula.widgets.pgroup.PGroup;
import org.eclipse.nebula.widgets.pgroup.SimpleGroupStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.gumtree.gumnix.sics.widgets.swt.DeviceStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.EnvironmentControlWidget;
import org.gumtree.gumnix.sics.widgets.swt.ShutterStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.SicsStatusWidget;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.util.messaging.ReducedDelayEventExecutor;

import au.gov.ansto.bragg.dingo.workbench.internal.InternalImage;
import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.workbench.ReactorStatusWidget;

@SuppressWarnings("restriction")
public class DingoCruisePageWidget extends AbstractCruisePageWidget {

	private IEclipseContext eclipseContext;

	private IDelayEventExecutor delayEventExecutor;

	@Inject
	private IDataAccessManager dataAccessManager;

	public DingoCruisePageWidget(Composite parent, int style) {
		super(parent, style);
	}

	@PostConstruct
	public DingoCruisePageWidget render() {
		GridLayoutFactory.swtDefaults().applyTo(this);
		getEclipseContext().set(IDelayEventExecutor.class,
				getDelayEventExecutor());
		
		// Reactor Source
//		PGroup sourceGroup = createGroup("REACTOR SOURCE",
//				SharedImage.REACTOR.getImage());
//		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(
//				sourceGroup, SWT.NONE);
//		deviceStatusWidget.addDevice("/instrument/source/power", "Power",
//				SharedImage.POWER.getImage(), null);
//		configureWidget(deviceStatusWidget);

		PGroup sourceGroup = createGroup("REACTOR SOURCE",
				SharedImage.REACTOR.getImage());
		ReactorStatusWidget reactorWidget = new ReactorStatusWidget(sourceGroup, SWT.NONE);
		reactorWidget.addDevice("reactorPower", "Power", "MW");
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

		// SICS status
		PGroup sicsStatusGroup = createGroup("SICS STATUS", null);
		SicsStatusWidget statusWidget = new SicsStatusWidget(sicsStatusGroup,
				SWT.NONE);
		ContextInjectionFactory.inject(statusWidget, getEclipseContext());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(statusWidget);

		// Pause Counter
//		PGroup pauseGroup = createGroup("PAUSE COUNTING",
//				SharedImage.SHUTTER.getImage());
//		HMVetoGadget pauseStatuswidget = new HMVetoGadget(
//				pauseGroup, SWT.NONE);
//		configureWidget(pauseStatuswidget);

		// Experiment info
		PGroup infoGroup = createGroup("EXPERIMENT INFO",
				InternalImage.EXPERIMENT_INFO.getImage());
		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(infoGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/experiment/title", "Proposal", null, "")
				.addDevice("/sample/name", "Sample", null, "")
				.addDevice("/user/name", "User", null, "");
		configureWidget(deviceStatusWidget);

		// Experiment Status
		PGroup statusGroup = createGroup("MOTOR STATUS",
				InternalImage.EXPERIMENT_STATUS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(statusGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/dy", "dy")
				.addDevice("/sample/stth", "stth")
				.addDevice("/sample/sx", "sx")
				.addDevice("/sample/sy", "sy")
				.addDevice("/sample/sz", "sz")
				.addDevice("/sample/dz", "dz")
//				.addSeparator()
//				.addDevice("/instrument/polarizer/z_translation", "analz")
//				.addDevice("/instrument/polarizer/rotation", "analtilt")
//				.addSeparator()
//				.addDevice("/instrument/collimator/translation", "c1ht")
//				.addDevice("/instrument/collimator/rotation", "m1ro")
//				.addSeparator()
//				.addDevice("/sample/translate_x", "sx")
//				.addDevice("/sample/translate_z", "sz")
//				.addDevice("/sample/sth", "sth")
//				.addDevice("/sample/sphi", "sphi")
//				.addSeparator()
//				.addDevice("/instrument/slits/first/horizontal/gap", "ss1hg")
//				.addDevice("/instrument/slits/first/vertical/gap", "ss1vg")
//				.addDevice("/instrument/slits/second/horizontal/gap", "ss2hg")
//				.addDevice("/instrument/slits/second/vertical/gap", "ss2vg")
//				.addDevice("/instrument/slits/third/horizontal/gap", "ss3hg")
//				.addDevice("/instrument/slits/third/vertical/gap", "ss3vg")
				;
		configureWidget(deviceStatusWidget);

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("CAMERA STATUS",
				InternalImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/monitor/mode", "CM1 Mode", null, "")
				.addDevice("/monitor/preset", "CM1 Preset", null, "")
				.addDevice("/monitor/cm1_counts", "CM1 Counts", null, "cts")
				.addDevice("/monitor/cm1_time", "CM1 Time", null, "s")
//				.addDevice("/instrument/detector/total_counts", "Total Detector", null, "")
				;
		configureWidget(deviceStatusWidget);

		// Furnace Temp
//		PGroup furnaceGroup = createGroup("FURNACE TEMPERATURE",
//				InternalImage.FURNACE.getImage());
//		deviceStatusWidget = new DeviceStatusWidget(furnaceGroup, SWT.NONE);
//		deviceStatusWidget
//				.addDevice("/sample/tempone/sensorA/value", "Temperature")
//				.addDevice("/sample/tempone/setpoint", "Setpoint");
//		configureWidget(deviceStatusWidget);

		// Temperature TC1 Control
//		PGroup tempControlGroup = createGroup("Temperature Controller 1",
//				InternalImage.FURNACE.getImage());
//		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup, SWT.NONE);
//		deviceStatusWidget
//				.addDevice("/sample/tc1/sensor/sensorValueA", "TC1A-T/C",
//						InternalImage.A.getImage(), null)
//				.addDevice("/sample/tc1/sensor/sensorValueB", "TC1B-T/C",
//						InternalImage.B.getImage(), null)
//				.addDevice("/sample/tc1/sensor/sensorValueC", "TC1C-T/C",
//						InternalImage.C.getImage(), null)
//				.addDevice("/sample/tc1/sensor/sensorValueD", "TC1D-T/C",
//						InternalImage.D.getImage(), null)
//				;
//		configureWidget(deviceStatusWidget);

//		PGroup tempControlGroup2 = createGroup("Temperature Controller 2",
//				InternalImage.FURNACE.getImage());
//		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup2, SWT.NONE);
//		deviceStatusWidget
//				.addDevice("/sample/tc2/sensor/sensorValueA", "TC2A-T/C",
//						InternalImage.A.getImage(), null)
//				.addDevice("/sample/tc2/sensor/sensorValueB", "TC2B-T/C",
//						InternalImage.B.getImage(), null)
//				.addDevice("/sample/tc2/sensor/sensorValueC", "TC2C-T/C",
//						InternalImage.C.getImage(), null)
//				.addDevice("/sample/tc2/sensor/sensorValueD", "TC2D-T/C",
//						InternalImage.D.getImage(), null)
//				;
//		configureWidget(deviceStatusWidget);
				
		// Environment Group
		PGroup environmentGroup = createGroup("ENVIRONMENT CONTROLLERS",
				InternalImage.FURNACE.getImage());
		EnvironmentControlWidget controlWidget = new EnvironmentControlWidget(environmentGroup, SWT.NONE);
		configureWidget(controlWidget);

		return this;
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

	public IDataAccessManager getDataAccessManager() {
		return dataAccessManager;
	}

	public void setDataAccessManager(IDataAccessManager dataAccessManager) {
		this.dataAccessManager = dataAccessManager;
	}

	public IEclipseContext getEclipseContext() {
		return eclipseContext;
	}

	@Inject
	public void setEclipseContext(IEclipseContext eclipseContext) {
		this.eclipseContext = eclipseContext;
	}

	/*************************************************************************
	 * Utilities
	 *************************************************************************/

	protected PGroup createGroup(String text, Image image) {
		PGroup group = new PGroup(this, SWT.NONE);
		group.setStrategy(new SimpleGroupStrategy());
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
				.margins(10, 0).applyTo(group);
		return group;
	}

	protected void configureWidget(Control widget) {
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(widget);
		if (getEclipseContext() != null) {
			ContextInjectionFactory.inject(widget, getEclipseContext());
		}
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

}
