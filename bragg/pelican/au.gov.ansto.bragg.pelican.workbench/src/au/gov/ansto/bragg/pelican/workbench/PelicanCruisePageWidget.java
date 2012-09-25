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

		// Shutter Status
		PGroup shutterGroup = createGroup("SHUTTER STATUS",
				SharedImage.SHUTTER.getImage());
		ShutterStatusWidget shutterStatuswidget = new ShutterStatusWidget(
				shutterGroup, SWT.NONE);
		configureWidget(shutterStatuswidget);

		// Server Status
		PGroup sicsStatusGroup = createGroup("SERVER STATUS", 
				SharedImage.SERVER.getImage());
		SicsStatusGadget statusGadget = new SicsStatusGadget(sicsStatusGroup,
				SWT.NONE);
		ContextInjectionFactory.inject(statusGadget, getEclipseContext());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(statusGadget);

		// Monochromator
		PGroup monochromatorGroup = createGroup("MONOCROMATOR",
				SharedImage.MONOCHROMATOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monochromatorGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/instrument/crystal/wavelength", "wavelength", null, "")
				.addDevice("/instrument/crystal/mom", "mom", null, "")
				.addDevice("/instrument/crystal/mtth", "mtth", null, "")
				.render();

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("NEUTRON COUNTS",
				SharedImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/monitor/bm1_counts", "BM1 counts", null, "")
				.addDevice("/monitor/bm2_counts", "BM2 counts", null, "")
				.render();

		// Slits Info
		PGroup slitsGroup = createGroup("SLITS STATUS",
				SharedImage.SLITS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(slitsGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/instrument/aperture/sv1", "slit 1 top", null, "")
				.addDevice("/instrument/aperture/sh1", "slit 1 bottom", null, "")
				.addDevice("/instrument/aperture/sv2", "slit 2 top", null, "")
				.addDevice("/instrument/aperture/sh2", "slit 2 bottom", null, "")
				.render();

		// fermi chopper
		PGroup fermi1Group = createGroup("FERMI CHOPPER 1",
				SharedImage.SPIN.getImage());
		deviceStatusWidget = new DeviceStatusWidget(fermi1Group, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/instrument/fermi_chopper/ch1/frequency", "frequency", null, "")
				.addDevice("/instrument/fermi_chopper/ch1/ratio", "overlap ratio", null, "")
				.render();

		// fermi chopper
		PGroup fermi2Group = createGroup("FERMI CHOPPER 2",
				SharedImage.SPIN.getImage());
		deviceStatusWidget = new DeviceStatusWidget(fermi2Group, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/instrument/fermi_chopper/ch2/frequency", "frequency", null, "")
				.addDevice("/instrument/fermi_chopper/ch2/ratio", "overlap ratio", null, "")
				.render();

		// Other device
		PGroup otherGroup = createGroup("OTHER DEVICES",
				SharedImage.GEAR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(otherGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/instrument/crystal/FilterZ", "filter", null, "")
				.addDevice("/instrument/crystal/PolarizerZ", "polariser", null, "")
				.render();

		// Slits Info
		PGroup collimatorGroup = createGroup("RADIAL COLLIMATOR",
				SharedImage.CRADLE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(collimatorGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/instrument/collimator/vrcz", "in/out", null, "")
				.addDevice("/instrument/collimator/frequency", "frequency", null, "")
				.render();

		// Sample
		PGroup sampleGroup = createGroup("SAMPLE TANK",
				SharedImage.BEAKER.getImage());
		deviceStatusWidget = new DeviceStatusWidget(sampleGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/instrument/detector/stth", "angle", null, "")
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
		PGroup furnaceGroup = createGroup("FURNACE TEMP",
				SharedImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(furnaceGroup, SWT.NONE);
		configureWidget(deviceStatusWidget);
		deviceStatusWidget
				.addDevice("/sample/tempone/sensorA/value", "temperature")
				.addDevice("/sample/tempone/setpoint", "set point").render();


		// Temperature TC1 Control
		PGroup tempControlGroup = createGroup("TEMPERATURE CONTR",
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

