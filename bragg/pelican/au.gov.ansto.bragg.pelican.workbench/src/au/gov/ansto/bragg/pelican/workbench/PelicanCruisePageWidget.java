package au.gov.ansto.bragg.pelican.workbench;

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
import org.gumtree.gumnix.sics.ui.widgets.HMVetoGadget;
import org.gumtree.gumnix.sics.ui.widgets.SicsInterruptGadget;
import org.gumtree.gumnix.sics.ui.widgets.SicsStatusGadget;
import org.gumtree.gumnix.sics.widgets.swt.DeviceStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.ShutterStatusWidget;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.util.messaging.ReducedDelayEventExecutor;

import au.gov.ansto.bragg.nbi.ui.core.SharedImage;

@SuppressWarnings("restriction")
public class PelicanCruisePageWidget extends AbstractCruisePageWidget {

	@Inject
	private IEclipseContext eclipseContext;

	private IDelayEventExecutor delayEventExecutor;

	@Inject
	private IDataAccessManager dataAccessManager;

	public PelicanCruisePageWidget(Composite parent, int style) {
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
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(statusGadget);
		configureWidget(statusGadget);

		// Pause Counter
		PGroup pauseGroup = createGroup("PAUSE COUNTING",
				SharedImage.SHUTTER.getImage());
		HMVetoGadget pauseStatuswidget = new HMVetoGadget(
				pauseGroup, SWT.NONE);
		configureWidget(pauseStatuswidget);

		// Monochromator
		PGroup monochromatorGroup = createGroup("MONOCROMATOR",
				SharedImage.MONOCHROMATOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monochromatorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/crystal/wavelength", "wavelength", null, "")
				.addDevice("/instrument/crystal/mom", "mom", null, "")
				.addDevice("/instrument/crystal/mtth", "mtth", null, "");
		configureWidget(deviceStatusWidget);

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("NEUTRON COUNTS",
				SharedImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/monitor/bm1_counts", "BM1 counts", null, "")
				.addDevice("/monitor/bm2_counts", "BM2 counts", null, "");
		configureWidget(deviceStatusWidget);

		// Slits Info
		PGroup slitsGroup = createGroup("SLITS STATUS",
				SharedImage.SLITS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(slitsGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/aperture/sv1", "slit 1 top", null, "")
				.addDevice("/instrument/aperture/sh1", "slit 1 bottom", null, "")
				.addDevice("/instrument/aperture/sv2", "slit 2 top", null, "")
				.addDevice("/instrument/aperture/sh2", "slit 2 bottom", null, "");
		configureWidget(deviceStatusWidget);

		// fermi chopper
		PGroup fermi1Group = createGroup("FERMI CHOPPER",
				SharedImage.SPIN.getImage());
		deviceStatusWidget = new DeviceStatusWidget(fermi1Group, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/fermi_chopper/mchs", "master chopper", null, "rpm")
				.addDevice("/instrument/fermi_chopper/schs", "slave chopper", null, "rpm");
		configureWidget(deviceStatusWidget);

		// Other device
		PGroup otherGroup = createGroup("OTHER DEVICES",
				SharedImage.GEAR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(otherGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/crystal/FilterZ", "filter", null, "")
				.addDevice("/instrument/crystal/PolarizerZ", "polariser", null, "");
		configureWidget(deviceStatusWidget);

		// Slits Info
		PGroup collimatorGroup = createGroup("RADIAL COLLIMATOR",
				SharedImage.CRADLE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(collimatorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/collimator/RCollZ", "in/out", null, "")
				.addDevice("/instrument/collimator/rcz", "frequency", null, "");
		configureWidget(deviceStatusWidget);

		// Sample
		PGroup sampleGroup = createGroup("SAMPLE TANK",
				SharedImage.BEAKER.getImage());
		deviceStatusWidget = new DeviceStatusWidget(sampleGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/detector/stth", "angle", null, "");
		configureWidget(deviceStatusWidget);
		
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
		deviceStatusWidget
				.addDevice("/sample/tempone/sensorA/value", "temperature")
				.addDevice("/sample/tempone/setpoint", "set point");
		configureWidget(deviceStatusWidget);


		// Temperature TC1 Control
		PGroup tempControlGroup = createGroup("TEMPERATURE CONTR",
				SharedImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(tempControlGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/sample/tc1/sensor/sensorValueA", "TC1A",
						SharedImage.A.getImage(), null)
				.addDevice("/sample/tc1/sensor/sensorValueB", "TC1B",
						SharedImage.B.getImage(), null)
				.addDevice("/sample/tc1/sensor/sensorValueC", "TC1C",
						SharedImage.C.getImage(), null)
				.addDevice("/sample/tc1/sensor/sensorValueD", "TC1D",
						SharedImage.D.getImage(), null);
		configureWidget(deviceStatusWidget);

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

