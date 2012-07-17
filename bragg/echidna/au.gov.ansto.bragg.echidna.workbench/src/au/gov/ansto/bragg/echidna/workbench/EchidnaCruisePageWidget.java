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
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;

import au.gov.ansto.bragg.echidna.workbench.internal.InternalImage;
import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.ui.widgets.DeviceStatusWidget;
import au.gov.ansto.bragg.nbi.ui.widgets.ShutterStatusWidget;

@SuppressWarnings("restriction")
public class EchidnaCruisePageWidget extends AbstractCruisePageWidget {

	private IEclipseContext eclipseContext;

	public EchidnaCruisePageWidget(Composite parent, int style) {
		super(parent, style);
	}

	public EchidnaCruisePageWidget render() {
		GridLayoutFactory.swtDefaults().applyTo(this);

		// Reactor Source
		PGroup sourceGroup = createGroup("REACTOR SOURCE",
				SharedImage.REACTOR.getImage());
		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(
				sourceGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/instrument/source/power", "Power",
				SharedImage.POWER.getImage(), null);
		configureWidget(deviceStatusWidget);

		// Shutter Status
		PGroup shutterGroup = createGroup("SHUTTER STATUS",
				SharedImage.SHUTTER.getImage());
		ShutterStatusWidget shutterStatuswidget = new ShutterStatusWidget(
				shutterGroup, SWT.NONE);
		configureWidget(shutterStatuswidget);
		shutterStatuswidget.render();

		// Experiment info
		PGroup infoGroup = createGroup("EXPERIMENT INFO",
				InternalImage.EXPERIMENT_INFO.getImage());
		deviceStatusWidget = new DeviceStatusWidget(infoGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.addDevice("/experiment/title", "Proposal")
				.addDevice("/experiment/title", "Sample")
				.addDevice("/user/name", "User").render();

		// Experiment Status
		PGroup statusGroup = createGroup("EXPERIMENT STATUS",
				InternalImage.EXPERIMENT_STATUS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(statusGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.addDevice("/sample/azimuthal_angle", "stth")
				.addDevice("/experiment/currpoint", "Currpoint").render();

		// Furnace Temp
		PGroup furnaceGroup = createGroup("FURNACE TEMPERATURE",
				InternalImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(furnaceGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/sample/tempone/sensorA/value", "Temperature")
				.addDevice("/sample/tempone/setpoint", "Setpoint").render();

		// Robotic Changer
		PGroup robotGroup = createGroup("ROBOTIC CHANGER",
				InternalImage.ROBOT.getImage());
		deviceStatusWidget = new DeviceStatusWidget(robotGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/sample/robby/Control/Pallet_Nam", "Pallet Name")
				.addDevice("/sample/robby/Control/Pallet_Idx",
						"Sample Position")
				.addDevice("/sample/robby/status", "Sample")
				.addDevice("/sample/robby/setpoint", "Robot Status").render();

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("BEAM MONITOR",
				InternalImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/monitor/bm1_event_rate", "BM1", null, "counts/sec")
				.addDevice("/monitor/bm2_event_rate", "BM2", null, "counts/sec")
				.render();

		// Temperature TC1 Control
		PGroup tempControlGroup = createGroup("Temperature Controller",
				InternalImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
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
						InternalImage.TWO.getImage(), null)
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
						InternalImage.TWO.getImage(), null)
				.render();

		return this;
	}

	@Override
	protected void disposeWidget() {
		eclipseContext = null;
	}

	/*************************************************************************
	 * Components
	 *************************************************************************/

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

}
