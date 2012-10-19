package au.gov.ansto.bragg.taipan.workbench;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
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
import org.gumtree.gumnix.sics.ui.widgets.SicsInterruptGadget;
import org.gumtree.gumnix.sics.ui.widgets.SicsStatusGadget;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.util.messaging.ReducedDelayEventExecutor;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.ui.widgets.DeviceStatusWidget;
import au.gov.ansto.bragg.taipan.ui.widgets.BeamMonitorDialWidget;

@SuppressWarnings("restriction")
public class TaipanCruisePageWidget extends AbstractCruisePageWidget {

	private static final Logger logger = LoggerFactory
			.getLogger(TaipanCruisePageWidget.class);

	private IDelayEventExecutor delayEventExecutor;

	@Inject
	private IDataAccessManager dataAccessManager;

	@Inject
	private IEclipseContext eclipseContext;

	public TaipanCruisePageWidget(Composite parent, int style) {
		super(parent, style);
	}

	@PostConstruct
	public void render() {
		GridLayoutFactory.swtDefaults().applyTo(this);
		getEclipseContext().set(IDelayEventExecutor.class,
				getDelayEventExecutor());

		// SICS status
		PGroup statusGroup = createGroup("SERVER STATUS", null);
		SicsStatusGadget statusGadget = new SicsStatusGadget(statusGroup,
				SWT.NONE);
		ContextInjectionFactory.inject(statusGadget, getEclipseContext());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(statusGadget);

		// Beam monitor 1
		PGroup bm1Group = createGroup("BEAM MONITOR 1", null);

		BeamMonitorDialWidget bm1DialWidget = new BeamMonitorDialWidget(
				bm1Group, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).hint(200, 200).applyTo(bm1DialWidget);
		IEclipseContext context = getEclipseContext().createChild();
		context.set("devicePath", "/monitor/bm1_counts");
		context.set("unit", "counts/sec");
		ContextInjectionFactory.inject(bm1DialWidget, context);

		File bgImageFile = getDataAccessManager().get(
				SharedImage.SPIN.getURI(), File.class);
		try {
			bm1DialWidget.getChart().setBackgroundImage(
					ImageIO.read(bgImageFile));
		} catch (IOException e) {
			logger.error("Failed to load image", e);
		}
		bm1DialWidget.getChart().setBackgroundPaint(Color.DARK_GRAY);

		DeviceStatusWidget deviceStatusWidget = new DeviceStatusWidget(
				bm1Group, SWT.NONE);
		deviceStatusWidget.addDevice("/monitor/bm1_counts", "BM1", null,
				"counts/sec");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(deviceStatusWidget);
		ContextInjectionFactory.inject(deviceStatusWidget, getEclipseContext());
//
//		// Beam monitor 2
//		PGroup bm2Group = createGroup("BEAM MONITOR 2", null);
//
//		BeamMonitorDialWidget bm2DialWidget = new BeamMonitorDialWidget(
//				bm2Group, SWT.NONE);
//		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
//				.grab(true, false).hint(200, 200).applyTo(bm2DialWidget);
//		context = getEclipseContext().createChild();
//		context.set("devicePath", "/monitor/bm2_counts");
//		context.set("unit", "counts/sec");
//		ContextInjectionFactory.inject(bm2DialWidget, context);
//
//		try {
//			bm2DialWidget.getChart().setBackgroundImage(
//					ImageIO.read(bgImageFile));
//		} catch (IOException e) {
//			logger.error("Failed to load image", e);
//		}
//		bm2DialWidget.getChart().setBackgroundPaint(Color.DARK_GRAY);
//
//		deviceStatusWidget = new DeviceStatusWidget(bm2Group, SWT.NONE);
//
//		deviceStatusWidget.addDevice("/monitor/bm2_counts", "BM2", null,
//				"counts/sec");
//		ContextInjectionFactory.inject(deviceStatusWidget, context);
//		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
//				.grab(true, false).applyTo(deviceStatusWidget);

		// INSTRUMENT
		PGroup instrumentGroup = createGroup("INSTRUMENT",
				SharedImage.MONOCHROMATOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(instrumentGroup, SWT.NONE);
		deviceStatusWidget
		.addDevice("/instrument/crystal/m1", "m1", null, "")
		.addDevice("/instrument/crystal/m2", "m2", null, "")
		.addDevice("/sample/s1", "s1", null, "")
		.addDevice("/sample/s2", "s2", null, "")
		.addDevice("/instrument/crystal/a1", "a1", null, "")
		.addDevice("/instrument/detector/a2", "a2", null, "");
		configureWidget(deviceStatusWidget);
		
		// Virtual
		PGroup virtualParaGroup = createGroup("VIRTUAL PARAMETERS",
				SharedImage.CRADLE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(virtualParaGroup, SWT.NONE);
		deviceStatusWidget
		.addDevice("/sample/ei", "ei", null, "")
		.addDevice("/sample/ef", "ef", null, "")
		.addDevice("/sample/en", "en", null, "")
		.addDevice("/sample/qh", "qh", null, "")
		.addDevice("/sample/qk", "qk", null, "")
		.addDevice("/sample/ql", "ql", null, "");
		configureWidget(deviceStatusWidget);
		
		// Monitor Event Rate
		PGroup monitorGroup = createGroup("BEAM MONITOR",
				SharedImage.MONITOR.getImage());
		deviceStatusWidget = new DeviceStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/monitor/bm1_counts", "BM1", null, "counts")
				.addDevice("/monitor/bm2_counts", "BM2", null, "counts");
		configureWidget(deviceStatusWidget);
		
		// Furnace Temp
		PGroup furnaceGroup = createGroup("FURNACE TEMPERATURE",
				SharedImage.FURNACE.getImage());
		deviceStatusWidget = new DeviceStatusWidget(furnaceGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/sample/tempone/sensorA/value",
				"Temperature")
				.addDevice("/sample/tempone/setpoint", "Setpoint");
		configureWidget(deviceStatusWidget);
				
		// Magnet field
		PGroup magnetGroup = createGroup("MAGNET SENSOR",
				SharedImage.POSITIONER.getImage());
		deviceStatusWidget = new DeviceStatusWidget(magnetGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/magnet/sensor/value", "Magnetic")
				.addDevice("/magnet/setpoint", "Setpoint");
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

		// Experiment info
//		PGroup infoGroup = createGroup("EXPERIMENT INFO",
//				SharedImage.EXPERIMENT_INFO.getImage());
//		deviceStatusWidget = new DeviceStatusWidget(infoGroup, SWT.NONE);
//		deviceStatusWidget.addDevice("/experiment/title", "Proposal")
//				.addDevice("/experiment/title", "Sample")
//				.addDevice("/experiment/file_name", "Filename");
//		configureWidget(deviceStatusWidget);
		
		// Scan status
		PGroup scanGroup = createGroup("SCAN STATUS",
				SharedImage.EXPERIMENT_STATUS.getImage());
		deviceStatusWidget = new DeviceStatusWidget(scanGroup, SWT.NONE);
		deviceStatusWidget.addDevice("/commands/scan/bmonscan/scan_variable", "variable")
				.addDevice("/commands/scan/bmonscan/feedback/scan_variable_value", "value")
				.addDevice("/experiment/currpoint", "scanpoint")
				.addDevice("/experiment/file_name", "filename");
		configureWidget(deviceStatusWidget);
		
		// Interrupt
		PGroup interruptGroup = createGroup("INTERRUPT", null);
		SicsInterruptGadget interruptGadget = new SicsInterruptGadget(
				interruptGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER)
				.grab(true, false).applyTo(interruptGadget);
		interruptGadget.afterParametersSet();
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
