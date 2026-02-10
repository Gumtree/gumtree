package au.gov.ansto.bragg.koala.workbench;

import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.gumtree.control.ui.widgets.ControllerStatusWidget;
import org.gumtree.control.ui.widgets.ServerStatusWidget;
import org.gumtree.service.dataaccess.IDataAccessManager;
import org.gumtree.ui.cruise.support.AbstractCruisePageWidget;
import org.gumtree.util.messaging.IDelayEventExecutor;
import org.gumtree.util.messaging.ReducedDelayEventExecutor;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.widgets.DrumDoorWidget;
import au.gov.ansto.bragg.nbi.ui.core.SharedImage;
import au.gov.ansto.bragg.nbi.workbench.ReactorStatusWidget;

public class KoalaCruisePageWidget extends AbstractCruisePageWidget {

	private static final String PROP_CF2_PATH = "gumtree.koala.cf2Sensor";
	private static final String PROP_COBRA_PATH = "gumtree.koala.cobraSensor";
	
	@Inject
	private IEclipseContext eclipseContext;

	private IDelayEventExecutor delayEventExecutor;

	@Inject
	private IDataAccessManager dataAccessManager;

	public KoalaCruisePageWidget(Composite parent, int style) {
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
		ReactorStatusWidget reactorWidget = new ReactorStatusWidget(sourceGroup, SWT.NONE);
		reactorWidget.addDevice("reactorPower", "Power", "MW");
		reactorWidget.createWidgetArea();
		configureWidget(reactorWidget);
		sourceGroup.setExpanded(false);
		reactorWidget.setExpandingEnabled(true);

		// Shutter Status
//		PGroup shutterGroup = createGroup("SHUTTER STATUS",
//				SharedImage.SHUTTER.getImage());
//		ShutterGroupWidget shutterStatuswidget = new ShutterGroupWidget(
//				shutterGroup, SWT.NONE);
//		configureWidget(shutterStatuswidget);
//		shutterGroup.setExpanded(true);
//		shutterStatuswidget.render();
		
		// Drum door status
		PGroup drumDoorGroup = createGroup("DRUM DOOR STATUS",
				SharedImage.SHUTTER.getImage());
		DrumDoorWidget drumDoorWidget = new DrumDoorWidget(
				drumDoorGroup, SWT.NONE);
		configureWidget(drumDoorWidget);
		drumDoorGroup.setExpanded(true);
		drumDoorWidget.render();

		// Server Status
		PGroup sicsStatusGroup = createGroup("SERVER STATUS", 
				SharedImage.SERVER.getImage());
		ServerStatusWidget statusWidget = new ServerStatusWidget(sicsStatusGroup,
				SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(statusWidget);
		configureWidget(statusWidget);
		statusWidget.render();

		// Pause Counter
//		PGroup pauseGroup = createGroup("PAUSE COUNTING",
//				SharedImage.SHUTTER.getImage());
//		PauseStatusWidget pauseStatuswidget = new PauseStatusWidget(
//				pauseGroup, SWT.NONE);
//		configureWidget(pauseStatuswidget);
//		pauseStatuswidget.render();

		ControllerStatusWidget deviceStatusWidget;

		// Beam monitor
		PGroup monitorGroup = createGroup("BEAM MONITOR",
				SharedImage.MONITOR.getImage());
		deviceStatusWidget = new ControllerStatusWidget(monitorGroup, SWT.NONE);
		deviceStatusWidget
//				.addDevice("/instrument/dummy_motor", "Dummy Motor", null, "mm")		
//				.addDevice("/instrument/dcz", "Detector Height", null, "mm")
//				.addDevice("/instrument/crystal/reading_head", "Reading Head", null, "mm")
				.addDevice("/monitor/bm1_counts", "BM1 Counts", null, "")
				.addDevice("/monitor/bm1_time", "BM1 Time", null, "s", new ControllerStatusWidget.PrecisionConverter(0))
				;
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();

		// Sample
		PGroup sampleStageGroup = createGroup("SAMPLE",
				SharedImage.POSITIONER.getImage());
		deviceStatusWidget = new ControllerStatusWidget(sampleStageGroup, SWT.NONE);
		deviceStatusWidget
//				.addDevice("/sample/sz", "Sample Height", null, "mm", new ControllerStatusWidget.PrecisionConverter(3))
				.addDevice("/sample/sr", "Sample " + Activator.PHI, null, "\u00b0", new ControllerStatusWidget.PrecisionConverter(2))
				.addDevice("/sample/schi", "Sample " + Activator.CHI, null, "\u00b0", new ControllerStatusWidget.PrecisionConverter(2))
				.addDevice("/sample/sx", "sx", null, "mm", new ControllerStatusWidget.PrecisionConverter(2))
				.addDevice("/sample/sy", "sy", null, "mm", new ControllerStatusWidget.PrecisionConverter(2))
				.addDevice("/sample/sz", "sz", null, "mm", new ControllerStatusWidget.PrecisionConverter(2))
				;
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();

		// Choppers
		PGroup chopperGroup = createGroup("INSTRUMENT",
				SharedImage.POSITIONER.getImage());
		deviceStatusWidget = new ControllerStatusWidget(chopperGroup, SWT.NONE);
		deviceStatusWidget
//				.addDevice("/instrument/dummy_motor", "Dummy Motor", null, "mm")		
//				.addDevice("/instrument/dcz", "Detector Height", null, "mm")
//				.addDevice("/instrument/crystal/reading_head", "Reading Head", null, "mm")
				.addDevice("/instrument/dcz", "Drum Position", null, "mm")
				;
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();

		// Choppers
		PGroup experimentGroup = createGroup("EXPERIMENT",
				SharedImage.EXPERIMENT_INFO.getImage());
		deviceStatusWidget = new ControllerStatusWidget(experimentGroup, SWT.NONE);
		deviceStatusWidget
				.addDevice("/experiment/currpoint", "Frame ID", null, "")
				.addDevice("/instrument/image/state", "Phase", null, "", 
						new ControllerStatusWidget.LabelConverter() {
					
					@Override
					public String convertValue(Object obj) {
						// TODO Auto-generated method stub
						String text = obj.toString();
//						if (text != null && text.contains("_")) {
//							text = text.substring(0, text.indexOf("_"));
//						}
						if (text != null && text.length() > 23) {
							return text.substring(0, 23);
						} else {
							return text;
						}
					}
				}, false, new ControllerStatusWidget.ColorConverter() {
					
					@Override
					public Color convertColor(Object value) {
						if (value != null && value.toString().trim().length() > 0) {
							try {
								if ("IDLE".equalsIgnoreCase(value.toString())) {
									return Activator.getIdleColor();
								} else if (value.toString().toUpperCase().contains("ERROR")) {
									return Activator.getWarningColor();
								} else {
									return Activator.getBusyForgroundColor();
								}
							} catch (Exception e) {
								return Activator.getWarningColor();
							}
						} else {
							return Activator.getWarningColor();
						}
					}
				})
				.addDevice("/instrument/image/galil_api/STATUS", "Galil Status", null, "", new ControllerStatusWidget.LabelConverter() {
					
					@Override
					public String convertValue(Object obj) {
						if (obj == null) {
							return "--";
						}
						int value = 0;
						try {
							value = Float.valueOf(obj.toString()).intValue();
						} catch (Exception e) {
							return String.valueOf(obj);
						}
						if (value == 0) {
							return "OK";
						}
						if (value < 0) {
							switch (value) {
							case -1:
								return "motion disabled";
							case -2:
								return "timeout error";
							case -3:
								return "sz out";
							case -4:
								return "vibration warning";
							case -11:
								return "drum rotation fault";
							case -12:
								return "drum Z fault";
							case -13:
								return "sz fault";
							case -19:
								return "drum rotation error";
							case -21:
								return "drum Z limit";

							default:
								return String.valueOf(obj);
							}
						} else {
							switch (value) {
							case 1:
								return "motion disabled";
							case 2:
								return "B forward limit";
							case 4:
								return "B Reverse limit";
							case 7:
								return "7:EMO/Trip/ISO/Doors";
							case 8:
								return "sz out of beam";
							case 256:
								return "HOME";
							case 512:
								return "ERASE";
							case 1024:
								return "EXPOSE";
							case 2048:
								return "CLOSE SHUTTER";
							case 4096:
								return "READ";
							default:
								return String.valueOf(obj);
							}
						}
					}
				}, false, new ControllerStatusWidget.ColorConverter() {
					
					@Override
					public Color convertColor(Object value) {
						if (value != null && value.toString().trim().length() > 0) {
							try {
								int i = Float.valueOf(value.toString()).intValue();
								if (i < 0) {
									return Activator.getWarningColor();
								} else if (i >= 1 && i <= 8) {
									return Activator.getWarningColor();
								} else {
									return Activator.getRunningForgroundColor();
								}
							} catch (Exception e) {
								return Activator.getWarningColor();
							}
						} else {
							return Activator.getWarningColor();
						}
					}
				})
				.addDevice("/instrument/image/error_msg", "Last Message", null, "", new ControllerStatusWidget.LabelConverter() {
					
					@Override
					public String convertValue(Object obj) {
						if (obj != null) {
							String text = obj.toString();
							if (text.trim().length() == 0 || text.equalsIgnoreCase("OK")) {
								return text;
							} else {
								if (text.length() > 24) {
									return text.substring(0, 24);
								} else {
									return text;
 								}
							}
						} else {
							return "";
						}
					}
				}, false, new ControllerStatusWidget.ColorConverter() {
					
					@Override
					public Color convertColor(Object value) {
						if (value != null && value.toString().trim().length() > 0 && !"OK".equalsIgnoreCase(value.toString())) {
							return Activator.getWarningColor();
						} else {
							return Activator.getRunningForgroundColor();
						}
					}
				})
				.addDevice(System.getProperty(PROP_COBRA_PATH), "Cobra Controller", null, "K")
				.addDevice(System.getProperty(PROP_CF2_PATH), "CF-2 Lakeshore", null, "K")
				;
		configureWidget(deviceStatusWidget);
		deviceStatusWidget.render();


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
