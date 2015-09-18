package au.gov.ansto.bragg.kowari.workbench;

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
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;

import au.gov.ansto.bragg.kowari.workbench.internal.InternalImage;
import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.workbench.ReactorStatusWidget;

@SuppressWarnings("restriction")
public class KowariCruisePageWidget extends AbstractCruisePageWidget {

	private IEclipseContext eclipseContext;

	public KowariCruisePageWidget(Composite parent, int style) {
		super(parent, style);
	}

	public KowariCruisePageWidget render() {
		GridLayoutFactory.swtDefaults().spacing(1, 0).applyTo(this);

		// Reactor Source
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

		// Server Status
		PGroup sicsStatusGroup = createGroup("SERVER STATUS",
				InternalImage.SERVER.getImage());
		SicsStatusWidget statusWidget = new SicsStatusWidget(sicsStatusGroup,
				SWT.NONE);
		configureWidget(statusWidget);

		// Pause Counter
		PGroup pauseGroup = createGroup("PAUSE COUNTING",
				SharedImage.SHUTTER.getImage());
		HMVetoGadget pauseStatuswidget = new HMVetoGadget(
				pauseGroup, SWT.NONE);
		configureWidget(pauseStatuswidget);

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("NEUTRON COUNTS",
				InternalImage.MONITOR.getImage());
		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/monitor/bm1_counts", "Monitor Counts", null, "")
							.addDevice("/instrument/detector/total_counts", "Detector Counts", null, "");
		configureWidget(deviceStatusWidget);

		// Slits Info
		PGroup slitsGroup = createGroup("SLITS STATUS",
				InternalImage.SLITS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(slitsGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/slits/primary_psho", "psho", null, null)
				.addDevice("/instrument/slits/primary_psp", "psp", null, null)
				.addDevice("/instrument/slits/primary_psw", "psw", null, null)
				.addDevice("/instrument/slits/secondary_ssho", "ssho", null, null)
				.addDevice("/instrument/slits/secondary_ssp", "ssp", null, null)
				.addDevice("/instrument/slits/secondary_ssw", "ssw", null, null);
		configureWidget(deviceStatusWidget);

		// Positioner Group
		PGroup positionerGroup = createGroup("POSITIONER STATUS",
				InternalImage.POSITIONER.getImage());
		deviceStatusWidget = new DeviceStatusWidget(positionerGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/sample/sx", "sx", null, null, null, true)
				.addDevice("/sample/sy", "sy", null, null, null, true)
				.addDevice("/sample/sz", "sz", null, null, null, true)
				.addDevice("/sample/som", "som", null, null, null, true)
				.addDevice("/sample/stth", "stth", null, null);
		configureWidget(deviceStatusWidget);

		// Monochromator
		PGroup monochromatorGroup = createGroup("MONOCHROMATOR",
				InternalImage.MONOCHROMATOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monochromatorGroup,
				SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/crystal/mom", "mom", null, null)
				.addDevice("/instrument/crystal/mtth", "mtth", null, null)
				.addDevice("/instrument/monochromator/focus/mf1", "mf1", null,
						"")
				.addDevice("/instrument/monochromator/focus/mf2", "mf2", null,
						"");
		configureWidget(deviceStatusWidget);

		// Euler Cradle
		PGroup cradleGroup = createGroup("EULER CRADLE",
				InternalImage.CRADLE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(cradleGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/sample/eom", "EOM", null, "")
				.addDevice("/sample/echi", "EChi", null, "")
				.addDevice("/sample/ephi", "EPhi", null, "");
		configureWidget(deviceStatusWidget);

		// Experiment info
		// PGroup infoGroup = createGroup("EXPERIMENT INFO",
		// InternalImage.EXPERIMENT_INFO.getImage());
		// deviceStatusWidget = new DeviceStatusWidget(infoGroup, SWT.NONE);
		// configureWidget(deviceStatusWidget);
		// deviceStatusWidget.addDevice("/experiment/title", "Proposal")
		// .addDevice("/experiment/title", "Sample")
		// .addDevice("/user/name", "User").render();

		// Furnace Temp
//		PGroup furnaceGroup = createGroup("FURNACE TEMPERATURE",
//				InternalImage.FURNACE.getImage());
//		deviceStatusWidget = new DeviceStatusWidget(furnaceGroup, SWT.NONE);
//		deviceStatusWidget.addDevice("/sample/tempone/sensorA/value",
//				"Temperature")
//				.addDevice("/sample/tempone/setpoint", "Setpoint");
//		configureWidget(deviceStatusWidget);

		// Temperature TC1 Control
//		PGroup tempControlGroup = createGroup("TEMPERATURE CONTROLLER",
//				InternalImage.FURNACE.getImage());
//		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup, SWT.NONE);
//		deviceStatusWidget
//				.addDevice("/sample/tc1/sensor/sensorValueA", "TC1A",
//						InternalImage.A.getImage(), null)
//				.addDevice("/sample/tc1/sensor/sensorValueB", "TC1B",
//						InternalImage.B.getImage(), null)
//				.addDevice("/sample/tc1/sensor/sensorValueC", "TC1C",
//						InternalImage.C.getImage(), null)
//				.addDevice("/sample/tc1/sensor/sensorValueD", "TC1D",
//						InternalImage.D.getImage(), null);
		// .addDevice("/sample/tc1/heater/heaterOutput_1", "TC1H1-R/O",
		// InternalImage.ONE.getImage(), null)
		// .addDevice("/sample/tc1/heater/heaterOutput_2", "TC1H2-R/O",
		// InternalImage.TWO.getImage(), null)
		// .addDevice("/sample/tc2/sensor/sensorValueA", "TC2A-T/C",
		// InternalImage.A.getImage(), null)
		// .addDevice("/sample/tc2/sensor/sensorValueB", "TC2B-T/C",
		// InternalImage.B.getImage(), null)
		// .addDevice("/sample/tc2/sensor/sensorValueC", "TC2C-T/C",
		// InternalImage.C.getImage(), null)
		// .addDevice("/sample/tc2/sensor/sensorValueD", "TC2D-T/C",
		// InternalImage.D.getImage(), null)
		// .addDevice("/sample/tc2/heater/heaterOutput_1", "TC2H1-R/O",
		// InternalImage.ONE.getImage(), null)
		// .addDevice("/sample/tc2/heater/heaterOutput_2", "TC2H2-R/O",
		// InternalImage.TWO.getImage(), null)
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
		GridLayoutFactory.swtDefaults().numColumns(1).spacing(1, 0)
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
