package au.gov.ansto.bragg.echidna.workbench;

import javax.inject.Inject;

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
import org.gumtree.gumnix.sics.ui.widgets.HMVetoGadget;
import org.gumtree.gumnix.sics.widgets.swt.DeviceStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.EnvironmentControlWidget;
import org.gumtree.gumnix.sics.widgets.swt.ShutterStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.SicsStatusWidget;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.util.messaging.ReducedDelayEventExecutor;

import au.gov.ansto.bragg.echidna.workbench.internal.InternalImage;
import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.workbench.ReactorStatusWidget;

@SuppressWarnings("restriction")
public class EchidnaCruisePageWidget extends AbstractCruisePageWidget {

	private IEclipseContext eclipseContext;

	private IDelayEventExecutor delayEventExecutor;

	@Inject
	private IDataAccessManager dataAccessManager;

	public EchidnaCruisePageWidget(Composite parent, int style) {
		super(parent, style);
	}

	public EchidnaCruisePageWidget render() {
		GridLayoutFactory.swtDefaults().applyTo(this);
		getEclipseContext().set(IDelayEventExecutor.class,
				getDelayEventExecutor());
		
		PGroup sourceGroup = createGroup("REACTOR SOURCE",
				SharedImage.REACTOR.getImage());
		ReactorStatusWidget reactorWidget = new ReactorStatusWidget(sourceGroup, SWT.NONE);
		reactorWidget.addDevice("reactorPower", "Power", "MW");
		reactorWidget.createWidgetArea();
//		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(
//				sourceGroup, SWT.NONE);
//		deviceStatusWidget.addDevice("/instrument/source/power", "Power",
//				SharedImage.POWER.getImage(), null);
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
		PGroup sicsStatusGroup = createGroup("SERVER STATUS", null);
		SicsStatusWidget statusWidget = new SicsStatusWidget(sicsStatusGroup,
				SWT.NONE);
		ContextInjectionFactory.inject(statusWidget, getEclipseContext());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(statusWidget);

		// Pause Counter
		PGroup pauseGroup = createGroup("PAUSE COUNTING",
				SharedImage.SHUTTER.getImage());
		HMVetoGadget pauseStatuswidget = new HMVetoGadget(
				pauseGroup, SWT.NONE);
		configureWidget(pauseStatuswidget);
//		pauseStatuswidget.render();

		// Experiment info
		PGroup infoGroup = createGroup("EXPERIMENT INFO",
				InternalImage.EXPERIMENT_INFO.getImage());
		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(infoGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/experiment/title", "Proposal", null, "")
				.addDevice("/sample/name", "Sample", null, "")
				.addDevice("/user/name", "User", null, "");
		configureWidget(deviceStatusWidget);

		// Experiment Status
		PGroup statusGroup = createGroup("EXPERIMENT STATUS",
				InternalImage.EXPERIMENT_STATUS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(statusGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/sample/azimuthal_angle", "stth", null, null, new DeviceStatusWidget.PrecisionConverter(3))
				.addDevice("/experiment/currpoint", "Currpoint", null, "");
		configureWidget(deviceStatusWidget);

		// Robotic Changer
		PGroup robotGroup = createGroup("ROBOTIC CHANGER",
				InternalImage.ROBOT.getImage());
		deviceStatusWidget = new DeviceStatusWidget(robotGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/robby/Control/Pallet_Nam", "Pallet Name", null, "")
				.addDevice("/sample/robby/Control/Pallet_Idx",
						"Sample Position", null, "")
				.addDevice("/sample/robby/setpoint", "Sample", null, "")
				.addDevice("/sample/robby/status", "Robot Status", null, "");
		configureWidget(deviceStatusWidget);

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("BEAM MONITOR",
				InternalImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/monitor/bm1_event_rate", "BM1", null, "cts/s")
				.addDevice("/monitor/bm2_event_rate", "BM2", null, "cts/s")
				.addDevice("/monitor/bm3_event_rate", "BM3", null, "cts/s");
		configureWidget(deviceStatusWidget);

		// Temperature TC1 Control
		PGroup tempControlGroup = createGroup("FURNACE",
				InternalImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/tc1/sensor", "sensor",
						null, "K")
				.addDevice("/sample/tc1/setpoint", "setpoint",
						null, "K");
		configureWidget(deviceStatusWidget);

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
//				.addDevice("/sample/tc2/heater/heaterOutput_1", "TC2H1-R/O",
//						InternalImage.ONE.getImage(), null)
//				.addDevice("/sample/tc2/heater/heaterOutput_2", "TC2H2-R/O",
//						InternalImage.TWO.getImage(), null);
//		configureWidget(deviceStatusWidget);
//
//		PGroup tc1Group = createGroup("TC1 Controller",
//				InternalImage.FURNACE.getImage());
//		deviceStatusWidget = new DeviceStatusWidget(tc1Group, SWT.NONE);
//		deviceStatusWidget
//				.addDevice("/sample/tc1/Loop1/sensor", "TC1_Loop1_Sensor",
//						InternalImage.A.getImage(), null)
//				.addDevice("/sample/tc1/Loop1/setpoint", "TC1_Loop1_Setpoint",
//						InternalImage.A.getImage(), null)
//				.addDevice("/sample/tc1/Loop2/sensor", "TC1_Loop2_Sensor",
//						InternalImage.B.getImage(), null)
//				.addDevice("/sample/tc1/Loop2/setpoint", "TC1_Loop2_Setpoint",
//						InternalImage.A.getImage(), null)
//				.addDevice("/sample/tc1/Loop3/sensor", "TC1_Loop3_Sensor",
//						InternalImage.C.getImage(), null)
//				.addDevice("/sample/tc1/Loop3/setpoint", "TC1_Loop3_Setpoint",
//						InternalImage.A.getImage(), null)
//				.addDevice("/sample/tc1/Loop4/sensor", "TC1_Loop4_Sensor",
//						InternalImage.D.getImage(), null)
//				.addDevice("/sample/tc1/Loop4/setpoint", "TC1_Loop4_Setpoint",
//						InternalImage.A.getImage(), null);
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
