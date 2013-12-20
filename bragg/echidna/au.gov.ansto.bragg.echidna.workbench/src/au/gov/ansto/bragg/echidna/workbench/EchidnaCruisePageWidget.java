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
		deviceStatusWidget.addDevice("/experiment/title", "Proposal")
				.addDevice("/sample/name", "Sample")
				.addDevice("/user/name", "User");
		configureWidget(deviceStatusWidget);

		// Experiment Status
		PGroup statusGroup = createGroup("EXPERIMENT STATUS",
				InternalImage.EXPERIMENT_STATUS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(statusGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/sample/azimuthal_angle", "stth")
				.addDevice("/experiment/currpoint", "Currpoint");
		configureWidget(deviceStatusWidget);

		// Furnace Temp
		PGroup furnaceGroup = createGroup("FURNACE TEMPERATURE",
				InternalImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(furnaceGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/tempone/sensorA/value", "Temperature")
				.addDevice("/sample/tempone/setpoint", "Setpoint");
		configureWidget(deviceStatusWidget);

		// Robotic Changer
		PGroup robotGroup = createGroup("ROBOTIC CHANGER",
				InternalImage.ROBOT.getImage());
		deviceStatusWidget = new DeviceStatusWidget(robotGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/robby/Control/Pallet_Nam", "Pallet Name")
				.addDevice("/sample/robby/Control/Pallet_Idx",
						"Sample Position")
				.addDevice("/sample/robby/setpoint", "Sample")
				.addDevice("/sample/robby/status", "Robot Status");
		configureWidget(deviceStatusWidget);

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("BEAM MONITOR",
				InternalImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/monitor/bm1_event_rate", "BM1", null, "counts/sec")
				.addDevice("/monitor/bm2_event_rate", "BM2", null, "counts/sec")
				.addDevice("/monitor/bm3_event_rate", "BM3", null, "counts/sec");
		configureWidget(deviceStatusWidget);

		// Temperature TC1 Control
		PGroup tempControlGroup = createGroup("Temperature Controller 1",
				InternalImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/tc1/sensor/sensorValueA", "TC1A-T/C",
						InternalImage.A.getImage(), null)
				.addDevice("/sample/tc1/sensor/sensorValueB", "TC1B-T/C",
						InternalImage.B.getImage(), null)
				.addDevice("/sample/tc1/sensor/sensorValueC", "TC1C-T/C",
						InternalImage.C.getImage(), null)
				.addDevice("/sample/tc1/sensor/sensorValueD", "TC1D-T/C",
						InternalImage.D.getImage(), null)
				.addDevice("/sample/tc1/heater/heaterOutput_1", "TC1H1-R/O",
						InternalImage.ONE.getImage(), null)
				.addDevice("/sample/tc1/heater/heaterOutput_2", "TC1H2-R/O",
						InternalImage.TWO.getImage(), null);
		configureWidget(deviceStatusWidget);

		PGroup tempControlGroup2 = createGroup("Temperature Controller 2",
				InternalImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup2, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/tc2/sensor/sensorValueA", "TC2A-T/C",
						InternalImage.A.getImage(), null)
				.addDevice("/sample/tc2/sensor/sensorValueB", "TC2B-T/C",
						InternalImage.B.getImage(), null)
				.addDevice("/sample/tc2/sensor/sensorValueC", "TC2C-T/C",
						InternalImage.C.getImage(), null)
				.addDevice("/sample/tc2/sensor/sensorValueD", "TC2D-T/C",
						InternalImage.D.getImage(), null)
				.addDevice("/sample/tc2/heater/heaterOutput_1", "TC2H1-R/O",
						InternalImage.ONE.getImage(), null)
				.addDevice("/sample/tc2/heater/heaterOutput_2", "TC2H2-R/O",
						InternalImage.TWO.getImage(), null);
		configureWidget(deviceStatusWidget);
				
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
