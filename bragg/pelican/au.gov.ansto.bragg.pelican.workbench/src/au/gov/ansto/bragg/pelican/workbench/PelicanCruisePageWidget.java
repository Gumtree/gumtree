package au.gov.ansto.bragg.pelican.workbench;

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
import org.gumtree.gumnix.sics.ui.widgets.SicsInterruptGadget;
import org.gumtree.gumnix.sics.ui.widgets.SicsStatusGadget;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;

import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.ui.widgets.DeviceStatusWidget;
import au.gov.ansto.bragg.nbi.ui.widgets.ShutterStatusWidget;

@SuppressWarnings("restriction")
public class PelicanCruisePageWidget extends AbstractCruisePageWidget {

	private IEclipseContext eclipseContext;

	public PelicanCruisePageWidget(Composite parent, int style) {
		super(parent, style);
	}

	public PelicanCruisePageWidget render() {
		GridLayoutFactory.swtDefaults().applyTo(this);

		// Reactor Source
		PGroup sourceGroup = createGroup("REACTOR SOURCE",
				SharedImage.REACTOR.getImage());
		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(
				sourceGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/instrument/source/power", "Power",
				SharedImage.POWER.getImage(), null);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();

		// Shutter Status
		PGroup shutterGroup = createGroup("SHUTTER STATUS",
				SharedImage.SHUTTER.getImage());
		ShutterStatusWidget shutterStatuswidget = new ShutterStatusWidget(
				shutterGroup, SWT.NONE);
		configureWidget(shutterStatuswidget);
		shutterStatuswidget.render();

		// Server Status
		PGroup sicsStatusGroup = createGroup("SERVER STATUS", 
				SharedImage.SERVER.getImage());
		SicsStatusGadget statusGadget = new SicsStatusGadget(sicsStatusGroup,
				SWT.NONE);
		ContextInjectionFactory.inject(statusGadget, getEclipseContext());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(statusGadget);

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("Neutron Counts",
				SharedImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/monitor/bm1_counts", "Monitor Counts", null, "")
				.addDevice("/instrument/detector/total_counts", "Detector Counts", null, "")
				.render();

		// Slits Info
		PGroup slitsGroup = createGroup("Slits Status",
				SharedImage.SLITS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(slitsGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/instrument/slits/primary_psho", "psho", null, "")
				.addDevice("/instrument/slits/primary_psp", "psp", null, "")
				.addDevice("/instrument/slits/primary_psw", "psw", null, "")
				.addDevice("/instrument/slits/secondary_ssho", "ssho", null, "")
				.addDevice("/instrument/slits/secondary_ssp", "ssp", null, "")
				.addDevice("/instrument/slits/secondary_ssw", "ssw", null, "")
				.render();

		// Positioner Group
		PGroup positionerGroup = createGroup("Positioner Status",
				SharedImage.POSITIONER.getImage());
		deviceStatusWidget = new DeviceStatusWidget(positionerGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/sample/sx", "sx", null, "")
				.addDevice("/sample/sy", "sy", null, "")
				.addDevice("/sample/sz", "sz", null, "")
				.addDevice("/sample/som", "som", null, "")
				.addDevice("/sample/stth", "stth", null, "")
				.render();
		
		// Monochromator
		PGroup monochromatorGroup = createGroup("Monochromator",
				SharedImage.MONOCHROMATOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monochromatorGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/instrument/crystal/mom", "mom", null, "")
				.addDevice("/instrument/crystal/mtth", "mtth", null, "")
				.addDevice("/instrument/monochromator/focus/mf1", "mf1", null, "")
				.addDevice("/instrument/monochromator/focus/mf2", "mf2", null, "")
				.render();

		// Euler Cradle
		PGroup cradleGroup = createGroup("Euler Cradle",
				SharedImage.CRADLE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(cradleGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/sample/eom", "EOM", null, "")
				.addDevice("/sample/echi", "EChi", null, "")
				.addDevice("/sample/ephi", "EPhi", null, "")
				.render();

		// Experiment info
//		PGroup infoGroup = createGroup("EXPERIMENT INFO",
//				InternalImage.EXPERIMENT_INFO.getImage());
//		deviceStatusWidget = new DeviceStatusWidget(infoGroup, SWT.NONE);
//		configureWidget(deviceStatusWidget);
//		deviceStatusWidget.addDevice("/experiment/title", "Proposal")
//				.addDevice("/experiment/title", "Sample")
//				.addDevice("/user/name", "User").render();

		// Furnace Temp
		PGroup furnaceGroup = createGroup("FURNACE TEMPERATURE",
				SharedImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(furnaceGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/sample/tempone/sensorA/value", "Temperature")
				.addDevice("/sample/tempone/setpoint", "Setpoint").render();


		// Temperature TC1 Control
		PGroup tempControlGroup = createGroup("Temperature Controller",
				SharedImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/sample/tc1/sensor/sensorValueA", "TC1A",
						SharedImage.A.getImage(), null)
				.addDevice("/sample/tc1/sensor/sensorValueB", "TC1B",
						SharedImage.B.getImage(), null)
				.addDevice("/sample/tc1/sensor/sensorValueC", "TC1C",
						SharedImage.C.getImage(), null)
				.addDevice("/sample/tc1/sensor/sensorValueD", "TC1D",
						SharedImage.D.getImage(), null)
//				.addDevice("/sample/tc1/heater/heaterOutput_1", "TC1H1-R/O",
//						InternalImage.ONE.getImage(), null)
//				.addDevice("/sample/tc1/heater/heaterOutput_2", "TC1H2-R/O",
//						InternalImage.TWO.getImage(), null)
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
//						InternalImage.TWO.getImage(), null)
				.render();

		// Interrupt
		PGroup interruptGroup = createGroup("INTERRUPT", null);
		SicsInterruptGadget interruptGadget = new SicsInterruptGadget(
				interruptGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).applyTo(interruptGadget);
		interruptGadget.afterParametersSet();

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

