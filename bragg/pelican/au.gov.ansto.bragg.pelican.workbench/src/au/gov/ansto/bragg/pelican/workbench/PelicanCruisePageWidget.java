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
import org.gumtree.gumnix.sics.ui.widgets.SicsStatusGadget;
import org.gumtree.gumnix.sics.widgets.swt.DeviceStatusWidget;
import org.gumtree.gumnix.sics.widgets.swt.DeviceStatusWidget.LabelConverter;
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
		sourceGroup.setExpanded(false);
		deviceStatusWidget.setExpandingEnabled(false);

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

		// Hist Counter
//		PGroup histGroup = createGroup("HISTOGRAM",
//				SharedImage.SHUTTER.getImage());
//		HMImageDisplayWidget hmWidget = new HMImageDisplayWidget(
//				histGroup, SWT.NONE);
//		String path = "http://localhost:60030/admin/openimageinformat.egi&#63;type=HISTOPERIOD_XYT&#38;open_format=DISLIN_PNG&#38;open_colour_table=RAIN&#38;open_plot_zero_pixels=AUTO&#38;open_annotations=ENABLE";
////		String path = "http://localhost:60030/admin/openimageinformat.egi?type=HISTOPERIOD_XYT&open_format=DISLIN_PNG&open_colour_table=RAIN&open_plot_zero_pixels=AUTO&open_annotations=ENABLE";
//		hmWidget.setDataURI(path);
//		configureWidget(hmWidget);

		// Monochromator
		PGroup monochromatorGroup = createGroup("MONOCHROMATOR",
				SharedImage.MONOCHROMATOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monochromatorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/crystal/wavelength", "wavelength", null, "\u212B")
				.addDevice("/instrument/crystal/mom", "mom", null, "")
				.addDevice("/instrument/crystal/mtth", "mtth", null, "deg")
				.addDevice("/instrument/crystal/moma", "moma", null, "deg")
				.addDevice("/instrument/crystal/momb", "momb", null, "deg")
				.addDevice("/instrument/crystal/momc", "momc", null, "deg")
				.addDevice("/instrument/crystal/mra", "mra", null, "deg")
				.addDevice("/instrument/crystal/mrb", "mrb", null, "deg")
				.addDevice("/instrument/crystal/mrc", "mrc", null, "deg")
				;
		configureWidget(deviceStatusWidget);

		// Monitor Event Rate
		PGroup monitorGroup = createGroup("NEUTRON COUNTS",
				SharedImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/monitor/bm1_counts", "BM1 counts", null, "")
				.addDevice("/monitor/bm2_counts", "BM2 counts", null, "")
				.addDevice("/instrument/detector/total_counts", "Detector counts", null, "")
				.addDevice("/instrument/detector/time", "Time of counting", null, "s")
				;
		configureWidget(deviceStatusWidget);

		// Slits Info
		PGroup slitsGroup = createGroup("SLITS STATUS",
				SharedImage.SLITS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(slitsGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/aperture/sv1", "slit 1 vertical", null, "")
				.addDevice("/instrument/aperture/sh1", "slit 1 horizontal", null, "")
				.addDevice("/instrument/aperture/sv2", "slit 2 vertical", null, "")
				.addDevice("/instrument/aperture/sh2", "slit 2 horizontal", null, "");
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
		
		LabelConverter converter = new LabelConverter() {
			
			@Override
			public String convertValue(Object obj) {
				try {
					String data = String.valueOf(obj);
					double value = Math.round(Double.valueOf(data));
					if (value == 1) {
						return "graphite";
					} else if (value == 2) {
						return "none";
					} else {
						return "Be";
					}
				} catch (Exception e) {
					return "none";
				}
			}
		};
		deviceStatusWidget.addDevice("/instrument/crystal/FilterZ", "filter", null, "", converter);
		converter = new LabelConverter() {
			
			@Override
			public String convertValue(Object obj) {
				try {
					String data = String.valueOf(obj);
					double value = Math.round(Double.valueOf(data));
					if (value == 1) {
						return "collimator";
					} else if (value == 2) {
						return "none";
					} else if (value == 3){
						return "polariser";
					} else {
						return "none";
					}
				} catch (Exception e) {
					return "none";
				}
			}
		};
		deviceStatusWidget.addDevice("/instrument/crystal/PolarizerZ", "polariser", null, "", converter);
		configureWidget(deviceStatusWidget);

		// Slits Info
		PGroup collimatorGroup = createGroup("RADIAL COLLIMATOR",
				SharedImage.CRADLE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(collimatorGroup, SWT.NONE);
		converter = new LabelConverter() {
			
			@Override
			public String convertValue(Object obj) {
				try {
					String data = String.valueOf(obj);
					double value = Double.valueOf(data);
					if (Math.round(value) == 1) {
						return "OUT";
					} else {
						return "IN";
					}
				} catch (Exception e) {
					return "OUT";
				}
			}
		};
		
		deviceStatusWidget
				.addDevice("/instrument/collimator/RCollZ", "in/out", null, "", converter);
//				.addDevice("/instrument/collimator/rcz", "frequency", null, "");
		deviceStatusWidget.setExpandingEnabled(false);
		collimatorGroup.setExpanded(false);
		configureWidget(deviceStatusWidget);

		// Sample
		PGroup sampleGroup = createGroup("SAMPLE TANK",
				SharedImage.BEAKER.getImage());
		deviceStatusWidget = new DeviceStatusWidget(sampleGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/instrument/detector/stth", "angle", null, "");
		sampleGroup.setExpanded(false);
		deviceStatusWidget.setExpandingEnabled(false);
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

