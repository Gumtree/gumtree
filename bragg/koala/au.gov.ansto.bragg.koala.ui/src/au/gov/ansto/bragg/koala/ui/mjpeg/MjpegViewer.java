package au.gov.ansto.bragg.koala.ui.mjpeg;

import java.awt.Point;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.imp.DriveableController;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.MainPart;
import au.gov.ansto.bragg.koala.ui.parts.PasswordDialog;
import au.gov.ansto.bragg.koala.ui.scan.KoalaModelException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;
import au.gov.ansto.bragg.koala.ui.sics.SimpleControlSuite;

public class MjpegViewer extends Composite {

	private static final String CAM1_URL = "gumtree.koala.mjpeg1Url";
	private static final String CAM2_URL = "gumtree.koala.mjpeg2Url";
	private static final Logger logger = LoggerFactory.getLogger(MjpegViewer.class);
	public static final String BEAM_CENTRE_LEFT = "gumtree.koala.beamCentreLeft";
	public static final String BEAM_CENTRE_RIGHT = "gumtree.koala.beamCentreRight";
	private static final String TEXT_ALIGN_BUTTON = "Align sample in 5 steps";
	private static final String VALUE_SX_RANGE = "gumtree.koala.sxRange";
	private static final String VALUE_SY_RANGE = "gumtree.koala.syRange";
	
	public static final float DEFAULT_SZ_ZERO = Float.valueOf(String.valueOf(System.getProperty(ControlHelper.SZ_ZERO)));

//	private static final String CAM_SIZE = "gumtree.koala.camSize";

	private MjpegRunner runner1;
	private MjpegRunner runner2;
	private MjpegComposite mjpeg1;
	private MjpegComposite mjpeg2;
	private String cam1Url;
	private String cam2Url;
	private ScrolledComposite holder;
	private Composite manualComposite;
	private AlignVideoPart alignComposite;
	private CalibrateVideoPart calibComposite;
	private Button ledButton;
	private Button drumButton;
	private Button alignButton;
	private Button resetButton;
	private Button calibButton;
	private Button phiNWButton;
	private Button phiNEButton;
	private Button phiSWButton;
	private Button phiSEButton;
	private boolean isAddingMarker;
	private ControlHelper controlHelper;
	private IRunnerListener mjpegListener;
	private double mmPerPixelLeft = Double.NaN;
	private double mmPerPixelRight = Double.NaN;
//	private double mmPerPixelLeftZ = Double.NaN;
//	private double mmPerPixelRightZ = Double.NaN;
	
	public MjpegViewer(Composite parent, int style) {
		super(parent, style);
		controlHelper = ControlHelper.getInstance();
		cam1Url = System.getProperty(CAM1_URL);
		cam2Url = System.getProperty(CAM2_URL);

		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(this);
		
		Composite mainComposite = new Composite(this, SWT.NONE);
//		imageComposite.setLayout(new FillLayout());
		GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 0).spacing(1, 1).applyTo(mainComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);
		
		Composite leftComposite = new Composite(mainComposite, SWT.BORDER);
		GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0).applyTo(leftComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(leftComposite);
		
		Composite controlComposite1 = new Composite(leftComposite, SWT.NONE);
//		controlComposite1.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.swtDefaults().numColumns(4).spacing(2, 0).margins(2, 0).applyTo(controlComposite1);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(controlComposite1);
		
		Label zoomLabel = new Label(controlComposite1, SWT.NONE);
		zoomLabel.setText(" Zoom");
//		zoomLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(zoomLabel);
//		zoomLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		Button zoomInButton = new Button(controlComposite1, SWT.PUSH);
		zoomInButton.setText("+");
//		zoomInButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).hint(40, 40).applyTo(zoomInButton);
		zoomInButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int zoomFactor = mjpeg1.getPanel().getZoomFactor();
				mjpeg1.getPanel().setZoomFactor(++zoomFactor);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Button zoomOutButton = new Button(controlComposite1, SWT.PUSH);
		zoomOutButton.setText("-");
//		zoomOutButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).hint(40, 40).applyTo(zoomOutButton);
		zoomOutButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int zoomFactor = mjpeg1.getPanel().getZoomFactor();
				if (zoomFactor > 1) {
					mjpeg1.getPanel().setZoomFactor(--zoomFactor);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Button zoomResetButton = new Button(controlComposite1, SWT.PUSH);
		zoomResetButton.setText("Reset");
//		zoomResetButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).hint(80, 40).applyTo(zoomResetButton);
		zoomResetButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int zoomFactor = mjpeg1.getPanel().getZoomFactor();
				if (zoomFactor > 1) {
					mjpeg1.getPanel().setZoomFactor(1);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Composite image1Composite = new Composite(leftComposite, SWT.NONE);
		FillLayout layout = new FillLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.spacing = 0;
		image1Composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(image1Composite);
		mjpeg1 = new MjpegComposite(image1Composite, SWT.NONE);
		
		Composite controlComposite = new Composite(mainComposite, SWT.NONE);
		controlComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.fillDefaults().numColumns(2).margins(4, 4).spacing(1, 2).applyTo(controlComposite);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.FILL).applyTo(controlComposite);

		Composite rightComposite = new Composite(mainComposite, SWT.BORDER);
		GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0).applyTo(rightComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(rightComposite);
		
		Composite controlComposite2 = new Composite(rightComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(4).spacing(2, 0).margins(2, 0).applyTo(controlComposite2);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(controlComposite2);
		
		zoomLabel = new Label(controlComposite2, SWT.NONE);
		zoomLabel.setText(" Zoom");
//		zoomLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(zoomLabel);
//		zoomLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		
		zoomInButton = new Button(controlComposite2, SWT.PUSH);
		zoomInButton.setText("+");
//		zoomInButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).hint(40, 40).applyTo(zoomInButton);
		zoomInButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int zoomFactor = mjpeg2.getPanel().getZoomFactor();
				mjpeg2.getPanel().setZoomFactor(++zoomFactor);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		zoomOutButton = new Button(controlComposite2, SWT.PUSH);
		zoomOutButton.setText("-");
//		zoomOutButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).hint(40, 40).applyTo(zoomOutButton);
		zoomOutButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int zoomFactor = mjpeg2.getPanel().getZoomFactor();
				if (zoomFactor > 1) {
					mjpeg2.getPanel().setZoomFactor(--zoomFactor);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		zoomResetButton = new Button(controlComposite2, SWT.PUSH);
		zoomResetButton.setText("Reset");
//		zoomResetButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).hint(80, 40).applyTo(zoomResetButton);
		zoomResetButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
//				int zoomFactor = mjpeg2.getPanel().getZoomFactor();
				mjpeg2.getPanel().resetZoomCentre();
//				if (zoomFactor > 1) {
				mjpeg2.getPanel().setZoomFactor(1);
//				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Composite image2Composite = new Composite(rightComposite, SWT.NONE);
		image2Composite.setLayout(new FillLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(image2Composite);
		mjpeg2 = new MjpegComposite(image2Composite, SWT.NONE);
		
		createControlComposite(controlComposite);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				mjpeg1.getPanel().dispose();
				mjpeg2.getPanel().dispose();
				stopRunner();
			}
		});
		
		loadBeamCentres();
		
		mjpeg1.getPanel().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				if (isAddingMarker) {
					checkMarkers();
				}
			}
			
			@Override
			public void centreSet() {
			}
		});

		mjpeg2.getPanel().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				if (isAddingMarker) {
					checkMarkers();
				}
			}
			
			@Override
			public void centreSet() {
			}
		});

		mjpegListener = new IRunnerListener() {
			
			@Override
			public void onError(Exception e) {
//				ControlHelper.experimentModel.publishErrorMessage("Errors on video camera, " + e.getMessage());
			}
		};
		
		startRunner();
		
		showPanel(manualComposite);
		loadPref();
		
		new SzHelper();
	}

	private void showPanel(final Composite composite) {
		holder.setContent(composite);
		composite.layout();
		holder.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		holder.getParent().layout();
	}
	
	private void checkMarkers() {
		if (mjpeg1.getPanel().getMarkerCoordinate() == null || mjpeg2.getPanel().getMarkerCoordinate() == null) {
//			syncSetText(markerButton, "adding ...");
		} else {
//			syncSetText(markerButton, "Remove Markers");
//			syncSetEnabled(alignButton, true);
		}
	}


	private void createControlComposite(Composite controlComposite) {
		
//		Composite controlComposite = new Composite(imageComposite, SWT.NONE);
//		controlComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
//		GridLayoutFactory.fillDefaults().numColumns(2).margins(4, 4).spacing(1, 2).applyTo(controlComposite);
//		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.FILL).applyTo(controlComposite);
		
//		createControls(controlComposite);
		alignButton = new Button(controlComposite, SWT.TOGGLE);
		alignButton.setText(TEXT_ALIGN_BUTTON);
		alignButton.setFont(Activator.getMiddleFont());
		alignButton.setImage(KoalaImage.TARGET48.getImage());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.CENTER).hint(SWT.DEFAULT, 64).applyTo(alignButton);
		alignButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (alignButton.getSelection()) {
					alignButton.setText("Cancel alignment");
					logger.info("alignment button clicked and enabling alignment");
					alignComposite.reset();
					alignComposite.setEnabled(true);
					showPanel(alignComposite);
					calibButton.setEnabled(false);
				} else {
					cancelAlignment();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		holder = new ScrolledComposite(controlComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(holder);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(holder);
		holder.setExpandHorizontal(true);
		holder.setExpandVertical(true);

		createManualComposite(holder);
		createAlignComposite(holder);
		createCalibComposite(holder);
		
		resetButton = new Button(controlComposite, SWT.PUSH);
		resetButton.setText("Reset video");
		resetButton.setFont(Activator.getMiddleFont());
		resetButton.setImage(KoalaImage.RELOAD48.getImage());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.BEGINNING, SWT.END).hint(SWT.DEFAULT, 64).applyTo(resetButton);
		resetButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Reset video button clicked");
				resetRunner();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}	
		});			


		calibButton = new Button(controlComposite, SWT.TOGGLE);
		calibButton.setText("Calibrate cameras");
		calibButton.setImage(KoalaImage.TOOLS48.getImage());
		calibButton.setFont(Activator.getMiddleFont());
		calibButton.setSelection(false);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.END, SWT.END).hint(SWT.DEFAULT, 64).applyTo(calibButton);
		calibButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (calibButton.getSelection()) {
					PasswordDialog dialog = new PasswordDialog(getShell());
					if (dialog.open() == Window.OK) {
			            String pw = dialog.getPassword();
			            if (Activator.isPassDisabled() || MainPart.UNLOCK_TEXT.equals(pw)) {
							calibButton.setText("Cancel calibration");
							logger.info("Calib button clicked and enabling calibration");
							calibComposite.reset();
							calibComposite.setEnabled(true);
							showPanel(calibComposite);
							alignButton.setEnabled(false);
			            } else {
			            	calibButton.setSelection(false);
			            	MessageDialog.openWarning(getShell(), "Warning", "Invalid passcode");
			            }
			        } else {
			        	calibButton.setSelection(false);
			        }
				} else {
//					calibButton.setText("Calibrate cameras");
//					logger.info("Calib button clicked and disabling calibration");
//					showPanel(manualComposite);
					cancelCalibration();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	}
	
	private void createManualComposite(Composite holder) {
		manualComposite = new Composite(holder, SWT.BORDER);
		manualComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(manualComposite);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(manualComposite);
		
		Composite axesControlComposite = new Composite(manualComposite, SWT.NONE);
//		axesControlComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		GridLayoutFactory.fillDefaults().margins(8, 8).numColumns(2).applyTo(axesControlComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).applyTo(axesControlComposite);
//		axesControlComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
		
		ledButton = new Button(axesControlComposite, SWT.CHECK);
		ledButton.setText("Light Source");
		ledButton.setFont(Activator.getMiddleFont());
		ledButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).hint(SWT.DEFAULT, 48).applyTo(ledButton);
		new LightSourceHelper();

		ledButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					final ISicsController ledController = SicsManager.getSicsModel().findController(
							System.getProperty(ControlHelper.LED_PATH));
					if (ledController == null) {
						throw new KoalaServerException("SICS server model not available");
					}
					if (ledButton.getSelection()) {
						((IDynamicController) ledController).setValue(1);
					} else {
						((IDynamicController) ledController).setValue(0);
					}
				} catch (Exception e1) {
					ControlHelper.experimentModel.publishErrorMessage(
							"failed to control LED light, " + e1.getMessage());
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		drumButton = new Button(axesControlComposite, SWT.PUSH);
		drumButton.setText("Move Drum Down");
		drumButton.setFont(Activator.getMiddleFont());
		drumButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.END, SWT.CENTER).hint(SWT.DEFAULT, 48).applyTo(drumButton);
		new DrumZHelper();

		drumButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Thread runThread = new Thread(new Runnable() {

					@Override
					public void run() {
//						try {
//							ControlHelper.syncDrive(System.getProperty(ControlHelper.DRUM_PATH), 
//									Float.valueOf(System.getProperty(ControlHelper.DRUM_DOWN_VALUE)));
//						} catch (Exception e1) {
//							ControlHelper.experimentModel.publishErrorMessage("failed to move the drum down, " + e1.getMessage());
//						}
						ControlHelper.concurrentDrive(System.getProperty(ControlHelper.DRUM_PATH), 
									Float.valueOf(System.getProperty(ControlHelper.DRUM_DOWN_VALUE)));
					}
				});
				runThread.start();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Group xGroup = new Group(axesControlComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).applyTo(xGroup);
		xGroup.setText("Sample X offset");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(xGroup);
		
		final Label curLabel = new Label(xGroup, SWT.NONE);
		curLabel.setText("Current");
		curLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curLabel);
		
		final Text curText = new Text(xGroup, SWT.READ_ONLY);
		curText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER).hint(100, 40).applyTo(curText);
		
		final Button driveButton = new Button(xGroup, SWT.PUSH);
		driveButton.setImage(KoalaImage.PLAY48.getImage());
		driveButton.setText("Drive");
		driveButton.setFont(Activator.getMiddleFont());
		driveButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).span(1, 3).hint(180, 64).applyTo(driveButton);

		final Label tarLabel = new Label(xGroup, SWT.NONE);
		tarLabel.setText("Target");
		tarLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(tarLabel);
		
		final Text tarText = new Text(xGroup, SWT.BORDER);
		tarText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER).hint(100, 40).applyTo(tarText);
		
		final Slider sliderX = new Slider(xGroup, SWT.HORIZONTAL);
//	    slider.setBounds(0, 0, 40, 200);
	    sliderX.setMaximum(100);
	    sliderX.setMinimum(0);
	    sliderX.setIncrement(1);
	    sliderX.setPageIncrement(5);
	    sliderX.setThumb(5);
	    sliderX.setSelection(50);
	    GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(sliderX);
	    
		Group yGroup = new Group(axesControlComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).applyTo(yGroup);
		yGroup.setText("Sample Y offset");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(yGroup);
		
		final Label curYLabel = new Label(yGroup, SWT.NONE);
		curYLabel.setText("Current");
		curYLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curYLabel);
		
		final Text curYText = new Text(yGroup, SWT.READ_ONLY);
		curYText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER).hint(100, 40).applyTo(curYText);
		
		final Button driveYButton = new Button(yGroup, SWT.PUSH);
		driveYButton.setImage(KoalaImage.PLAY48.getImage());
		driveYButton.setText("Drive");
		driveYButton.setFont(Activator.getMiddleFont());
		driveYButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).span(1, 3).hint(180, 64).applyTo(driveYButton);

		final Label tarYLabel = new Label(yGroup, SWT.NONE);
		tarYLabel.setText("Target");
		tarYLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(tarYLabel);
		
		final Text tarYText = new Text(yGroup, SWT.BORDER);
		tarYText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER).hint(100, 40).applyTo(tarYText);
		
		final Slider sliderY = new Slider(yGroup, SWT.HORIZONTAL);
//	    slider.setBounds(0, 0, 40, 200);
		sliderY.setMaximum(100);
		sliderY.setMinimum(0);
		sliderY.setIncrement(1);
		sliderY.setPageIncrement(5);
		sliderY.setThumb(4);
		sliderY.setSelection(48);
	    GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(sliderY);
	    
		String sxPath = System.getProperty(ControlHelper.SX_PATH);		
		new SimpleControlSuite(sxPath, curText, sxPath, tarText, driveButton, null, 
				sliderX, Float.valueOf(System.getProperty(VALUE_SX_RANGE, "1")));

		String syPath = System.getProperty(ControlHelper.SY_PATH);
		new SimpleControlSuite(syPath, curYText, syPath, tarYText, driveYButton, null, 
				sliderY, Float.valueOf(System.getProperty(VALUE_SY_RANGE, "1")));

		Group phiGroup = new Group(axesControlComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(2).applyTo(phiGroup);
		phiGroup.setText(Activator.PHI + " positions");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.FILL, SWT.CENTER).applyTo(phiGroup);
		
		phiNWButton = new Button(phiGroup, SWT.CHECK);
//		phiSButton.setImage(KoalaImage.PLAY48.getImage());
		phiNWButton.setText(Activator.PHI + " = -45\u00b0");
		phiNWButton.setFont(Activator.getMiddleFont());
		phiNWButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.BEGINNING, SWT.CENTER).span(1, 3).hint(150, 48).applyTo(phiNWButton);
		phiNWButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Phi -45 button clicked");
				driveSphi(-45f);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		phiNEButton = new Button(phiGroup, SWT.CHECK);
//		phiNButton.setImage(KoalaImage.PLAY48.getImage());
		phiNEButton.setText(Activator.PHI + " = +45\u00b0");
		phiNEButton.setFont(Activator.getMiddleFont());
		phiNEButton.setCursor(Activator.getHandCursor());
		phiNEButton.setForeground(Activator.getHighlightColor());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).span(1, 3).hint(150, 48).applyTo(phiNEButton);
		phiNEButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Phi +45 button clicked");
				driveSphi(45f);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		phiSWButton = new Button(phiGroup, SWT.CHECK);
//		phiEButton.setImage(KoalaImage.PLAY48.getImage());
		phiSWButton.setText(Activator.PHI + " = -135\u00b0");
		phiSWButton.setFont(Activator.getMiddleFont());
		phiSWButton.setCursor(Activator.getHandCursor());
		phiSWButton.setForeground(Activator.getLightColor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).span(1, 3).hint(150, 48).applyTo(phiSWButton);
		phiSWButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Phi -135 button clicked");
				driveSphi(-135f);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		phiSEButton = new Button(phiGroup, SWT.CHECK);
//		phiNButton.setImage(KoalaImage.PLAY48.getImage());
		phiSEButton.setText(Activator.PHI + " = +135\u00b0");
		phiSEButton.setFont(Activator.getMiddleFont());
		phiSEButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.END, SWT.CENTER).span(1, 3).hint(150, 48).applyTo(phiSEButton);
		phiSEButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Phi +135 button clicked");
				driveSphi(135f);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		new PhiControlSuite();
		
	}

	private void createAlignComposite(Composite holder) {
		alignComposite = new AlignVideoPart(holder, SWT.BORDER, this);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(alignComposite);
	}

	private void createCalibComposite(Composite holder) {
//		calibComposite = new Composite(holder, SWT.BORDER);
		calibComposite = new CalibrateVideoPart(holder, SWT.BORDER, this);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(calibComposite);
		
//		Composite beamCentreComposite = new Composite(calibComposite, SWT.NONE);
//		GridLayoutFactory.fillDefaults().margins(8, 8).applyTo(beamCentreComposite);
//		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).applyTo(beamCentreComposite);


//		final Button phiNButton = new Button(beamCentreGroup, SWT.TOGGLE);
////		phiNButton.setImage(KoalaImage.PLAY48.getImage());
//		phiNButton.setText("Phi +90\u00b0");
//		phiNButton.setFont(Activator.getMiddleFont());
//		phiNButton.setCursor(Activator.getHandCursor());
//		phiNButton.setForeground(Activator.getHighlightColor());
//		GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).span(1, 3).hint(150, 48).applyTo(phiNButton);
//		phiNButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				logger.info("Phi 90 button clicked");
//				driveSphi(90f);
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
	}
		
    private void loadPref() {
		String perPixelLeft = Activator.getPreference(Activator.NAME_MJPEG_MMPERPIXEL_LEFT);
		if (perPixelLeft != null) {
			try {
				mmPerPixelLeft = Double.valueOf(perPixelLeft);
			} catch (Exception e) {
			}
		}
		String perPixelRight = Activator.getPreference(Activator.NAME_MJPEG_MMPERPIXEL_RIGHT);
		if (perPixelRight != null) {
			try {
				mmPerPixelRight = Double.valueOf(perPixelRight);
			} catch (Exception e) {
			}
		}
//		String perPixelLeftZ = Activator.getPreference(Activator.NAME_MJPEG_MMPERPIXEL_LEFTZ);
//		if (perPixelLeftZ != null) {
//			try {
//				mmPerPixelLeftZ = Double.valueOf(perPixelLeftZ);
//			} catch (Exception e) {
//			}
//		}
//		String perPixelRightZ = Activator.getPreference(Activator.NAME_MJPEG_MMPERPIXEL_RIGHTZ);
//		if (perPixelRightZ != null) {
//			try {
//				mmPerPixelRightZ = Double.valueOf(perPixelRightZ);
//			} catch (Exception e) {
//			}
//		}
	}

    public double getMmPerPixelLeft() throws KoalaModelException {
    	if (Double.isNaN(mmPerPixelLeft)) {
    		throw new KoalaModelException("Camera has not been calibrated. Please click on the Calibration button.");
    	}
		return Math.abs(mmPerPixelLeft);
	}
    
    public double getMmPerPixelRight() throws KoalaModelException {
    	if (Double.isNaN(mmPerPixelRight)) {
    		throw new KoalaModelException("Camera has not been calibrated. Please click on the Calibration button.");
    	}
		return Math.abs(mmPerPixelRight);
	}
    
//    public double getMmPerPixelLeftZ() throws KoalaModelException {
//    	if (Double.isNaN(mmPerPixelLeftZ)) {
//    		throw new KoalaModelException("Camera has not been calibrated. Please click on the Calibration button.");
//    	}
//		return mmPerPixelLeftZ;
//	}
//    
//    public double getMmPerPixelRightZ() throws KoalaModelException {
//    	if (Double.isNaN(mmPerPixelRightZ)) {
//    		throw new KoalaModelException("Camera has not been calibrated. Please click on the Calibration button.");
//    	}
//		return mmPerPixelRightZ;
//	}
    
    public void setMmPerPixelLeft(double mmPerPixel) {
		this.mmPerPixelLeft = Math.abs(mmPerPixel);
		Activator.setPreference(Activator.NAME_MJPEG_MMPERPIXEL_LEFT, String.valueOf(mmPerPixel));
	}
    
    public void setMmPerPixelRight(double mmPerPixel) {
		this.mmPerPixelRight = Math.abs(mmPerPixel);
		Activator.setPreference(Activator.NAME_MJPEG_MMPERPIXEL_RIGHT, String.valueOf(mmPerPixel));
	}

//    public void setMmPerPixelLeftZ(double mmPerPixelZ) {
//		this.mmPerPixelLeftZ = mmPerPixelZ;
//		Activator.setPreference(Activator.NAME_MJPEG_MMPERPIXEL_LEFTZ, String.valueOf(mmPerPixelZ));
//	}
//
//    public void setMmPerPixelRightZ(double mmPerPixelZ) {
//		this.mmPerPixelRightZ = mmPerPixelZ;
//		Activator.setPreference(Activator.NAME_MJPEG_MMPERPIXEL_RIGHTZ, String.valueOf(mmPerPixelZ));
//	}

	private void driveSphi(final float value) {
		if (controlHelper.isConnected()) {
			final ISicsController sphiController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.SAMPLE_PHI));
			if (sphiController instanceof DriveableController) {
				final DriveableController driveable = (DriveableController) sphiController;
				JobRunner.run(new ILoopExitCondition() {
					
					@Override
					public boolean getExitCondition() {
						return true;
					}
				}, new Runnable() {
					
					@Override
					public void run() {
						driveable.setTargetValue(value);
						try {
							driveable.drive();
						} catch (final SicsException e1) {
//							e1.printStackTrace();
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									getParent().forceFocus();
									updatePhiButtons();
									ControlHelper.experimentModel.publishErrorMessage(e1.getMessage());
								}
							});
						}
					}
				});
			}
		}
	}
	
	
	public void startRunner() {
		try {
			runner1 = new MjpegRunner(mjpeg1.getPanel(), 
					new URL(cam1Url));
			runner1.addRunnerListener(mjpegListener);
			new Thread(runner1).start();
			runner2 = new MjpegRunner(mjpeg2.getPanel(), 
					new URL(cam2Url));
			runner2.addRunnerListener(mjpegListener);
			new Thread(runner2).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void stopRunner() {
		try {
			if (runner1 != null) {
				runner1.stop();
				runner1.removeRunnerListener(mjpegListener);
			}
			if (runner2 != null) {
				runner2.stop();
				runner2.removeRunnerListener(mjpegListener);
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void setRunnerPaused(boolean isPaused) {
		try {
			if (runner1 != null) {
				runner1.setPaused(isPaused);
			}
			if (runner2 != null) {
				runner2.setPaused(isPaused);
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public void resetRunner() {
		stopRunner();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				startRunner();
			}
		}, 1000);
	}
	
//	private void createControls(Composite controlComposite) {
//		Button startButton = new Button(controlComposite, SWT.PUSH);
//		startButton.setText("Start Video");
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(startButton);
//		startButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				startRunner();
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
//		
//		Button stopButton = new Button(controlComposite, SWT.PUSH);
//		stopButton.setText("Stop Video");
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(stopButton);
//		stopButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				stopRunner();
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
//		
//		final Button pauseButton = new Button(controlComposite, SWT.TOGGLE);
//		pauseButton.setText("Pause Video");
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(pauseButton);
//		pauseButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				boolean isPaused = pauseButton.getSelection();
//				setRunnerPaused(isPaused);
//				if (isPaused) {
//					pauseButton.setText("Unpause Video");
//				} else {
//					pauseButton.setText("Pause Video");
//				}
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
//				
//	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (runner1 != null) {
			runner1.removeRunnerListener(mjpegListener);
		}
		if (runner2 != null) {
			runner2.removeRunnerListener(mjpegListener);
		}
	}
	
	public static void main(String[] args) {
		System.setProperty(CAM1_URL, "http://192.168.0.9:2323/mimg");
		System.setProperty(CAM2_URL, "http://192.168.0.10:2323/mimg");
		System.setProperty(MjpegPanel.MARKER_SIZE, "16");
		System.setProperty(MjpegPanel.BEAM_CENTRE, "1296,968");
		System.setProperty(MjpegPanel.CAM_SIZE, "2592x1936");
	    final Display display = new Display();
	    final Shell shell = new Shell(display);
	    shell.setLayout(new FillLayout());

		MjpegViewer viewer = new MjpegViewer(shell, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer);

		shell.open();

//		viewer.startRunner();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
	class LightSourceHelper {
		
		public LightSourceHelper() {
			if (controlHelper.isConnected()) {
				final ISicsController ledController = SicsManager.getSicsModel().findController(
						System.getProperty(ControlHelper.LED_PATH));	
				ledController.addControllerListener(
						new LedControllerListener());
			}
			ISicsProxyListener proxyLedListener = new SicsProxyListenerAdapter() {

				@Override
				public void connect() {
					final ISicsController ledController = SicsManager.getSicsModel().findController(
							System.getProperty(ControlHelper.LED_PATH));	
					if (ledController != null) {
						if (ledController instanceof DynamicController) {
							try {
								final float value = Float.valueOf(((DynamicController) ledController).getValue().toString());
								Display.getDefault().asyncExec(new Runnable() {
									
									@Override
									public void run() {
										if (value == 1) {
											ledButton.setSelection(true);
										} else {
											ledButton.setSelection(false);
										}
									}
								});
							} catch (SicsModelException e) {
							}

							ledController.addControllerListener(
									new LedControllerListener());
						}
					}

				}
			};
			controlHelper.addProxyListener(proxyLedListener);
		}
	}
	
	class LedControllerListener implements ISicsControllerListener {

		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
		}

		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (Float.valueOf(String.valueOf(newValue)) == 1) {
						ledButton.setSelection(true);
					} else {
						ledButton.setSelection(false);
					}
				}
				
			});
		}

		@Override
		public void updateEnabled(boolean isEnabled) {
		}

		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
	}
	
	public void setZOffset(float zPosition) throws KoalaModelException {
		double xMmPerPixel = getMmPerPixelLeft();
		double yMmPerPixel = getMmPerPixelRight();
		float gap = DEFAULT_SZ_ZERO - zPosition;
		getPanel1().setCentreYOffset(Double.valueOf(gap / xMmPerPixel).intValue());
		getPanel2().setCentreYOffset(Double.valueOf(gap / yMmPerPixel).intValue());
	}
	
	class SzHelper {
		public SzHelper() {
			if (controlHelper.isConnected()) {
				final ISicsController szController = SicsManager.getSicsModel().findController(
						System.getProperty(ControlHelper.SZ_PATH));	
				szController.addControllerListener(
						new DrumZControllerListener());
			}
			ISicsProxyListener proxySzListener = new SicsProxyListenerAdapter() {

				@Override
				public void connect() {
					final ISicsController szController = SicsManager.getSicsModel().findController(
							System.getProperty(ControlHelper.SZ_PATH));	
					if (szController != null) {
						if (szController instanceof DynamicController) {
							try {
								final float value = Float.valueOf(((DynamicController) szController
										).getValue().toString());
								setZOffset(value);
							} catch (SicsModelException e) {
							} catch (KoalaModelException e) {
								e.printStackTrace();
							}

							szController.addControllerListener(new SzControllerListener());
						}
					}

				}
			};
			controlHelper.addProxyListener(proxySzListener);
		}
	}
	
	class SzControllerListener implements ISicsControllerListener {

		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
		}

		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					Float value = Float.valueOf(String.valueOf(newValue));
					try {
						setZOffset(value);
					} catch (KoalaModelException e) {
						e.printStackTrace();
					}
				}
				
			});
		}

		@Override
		public void updateEnabled(boolean isEnabled) {
		}

		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
	}
	
	class DrumZHelper {
		
		public DrumZHelper() {
			if (controlHelper.isConnected()) {
				final ISicsController drumController = SicsManager.getSicsModel().findController(
						System.getProperty(ControlHelper.DRUM_PATH));	
				drumController.addControllerListener(
						new DrumZControllerListener());
			}
			ISicsProxyListener proxyDrumZListener = new SicsProxyListenerAdapter() {

				@Override
				public void connect() {
					final ISicsController drumZController = SicsManager.getSicsModel().findController(
							System.getProperty(ControlHelper.DRUM_PATH));	
					if (drumZController != null) {
						if (drumZController instanceof DynamicController) {
							try {
								final float value = Float.valueOf(((DynamicController) drumZController).getValue().toString());
								final float limit = Float.valueOf(System.getProperty(ControlHelper.DRUM_DOWN_VALUE));
								Display.getDefault().asyncExec(new Runnable() {
									
									@Override
									public void run() {
										if (value > limit + 1) {
											drumButton.setEnabled(true);
										} else {
											drumButton.setEnabled(false);
										}
									}
								});
							} catch (SicsModelException e) {
							}

							drumZController.addControllerListener(
									new LedControllerListener());
						}
					}

				}
			};
			controlHelper.addProxyListener(proxyDrumZListener);
		}
	}
	
	class DrumZControllerListener implements ISicsControllerListener {

		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
		}

		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			final float limit = Float.valueOf(System.getProperty(ControlHelper.DRUM_DOWN_VALUE));
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (Float.valueOf(String.valueOf(newValue)) > limit + 1) {
						drumButton.setEnabled(true);
					} else {
						drumButton.setEnabled(false);
					}
				}
				
			});
		}

		@Override
		public void updateEnabled(boolean isEnabled) {
		}

		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
	}
	
	class PhiControlSuite {
		
		public PhiControlSuite() {
			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
				
				@Override
				public void connect() {
					final ISicsController phiController = SicsManager.getSicsModel().findController(
							System.getProperty(ControlHelper.SAMPLE_PHI));
					if (phiController != null) {
						if (phiController instanceof DriveableController) {
							try {
								float value = (Float) ((DriveableController) phiController).getValue();
								double precision = ((DriveableController) phiController).getPrecision();
								if (Double.isNaN(precision)) {
									precision = 0;
								} else {
									precision = Math.abs(precision);
								}
								if (inRange(value, precision, -45)) {
									chooseRange(phiNWButton);
								} else if (inRange(value, precision, 45)) {
									chooseRange(phiNEButton);
								} else if (inRange(value, precision, -135)) {
									chooseRange(phiSWButton);
								} else if (inRange(value, precision, 135)) {
									chooseRange(phiSEButton);
								} else {
									chooseRange(null);
								}
							} catch (SicsModelException e) {
							}
							
							phiController.addControllerListener(
									new PhiControllerListener((DriveableController) phiController));
						}
					}

				}
			};
			controlHelper.addProxyListener(proxyListener);
		}
	}

	private boolean inRange(float value, double precision, float range) {
		if (value >= range - precision && value <= range + precision) {
			return true;
		} else {
			return false;
		}
	}
	
	private void chooseRange(final Button rangeButton) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				phiNEButton.setSelection(false);
				phiNWButton.setSelection(false);
				phiSEButton.setSelection(false);
				phiSWButton.setSelection(false);
				if (rangeButton != null) {
					rangeButton.setSelection(true);
				}
			}
		});
	}

	private void updatePhiButtons() {
		try {
			final ISicsController phiController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.SAMPLE_PHI));
			float value = (Float) ((DriveableController) phiController).getValue();
			double precision = ((DriveableController) phiController).getPrecision();
			if (Double.isNaN(precision)) {
				precision = 0;
			} else {
				precision = Math.abs(precision);
			}
			if (inRange(value, precision, -45)) {
				chooseRange(phiNWButton);
			} else if (inRange(value, precision, 45)) {
				chooseRange(phiNEButton);
			} else if (inRange(value, precision, -135)) {
				chooseRange(phiSWButton);
			} else if (inRange(value, precision, 135)) {
				chooseRange(phiSEButton);
			} else {
				chooseRange(null);
			}					
		} catch (SicsModelException e) {
			e.printStackTrace();
		}
		phiNEButton.setEnabled(true);
		phiNWButton.setEnabled(true);
		phiSWButton.setEnabled(true);
		phiSEButton.setEnabled(true);
	}
	
	class PhiControllerListener implements ISicsControllerListener {

		private DriveableController phiController;
		
		public PhiControllerListener(DriveableController controller) {
			phiController = controller;
		}
		
		@Override
		public void updateState(final ControllerState oldState, final ControllerState newState) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					if (newState == ControllerState.BUSY) {
						phiNEButton.setEnabled(false);
						phiNWButton.setEnabled(false);
						phiSWButton.setEnabled(false);
						phiSEButton.setEnabled(false);
					} else {
						updatePhiButtons();
					}
				}
			});
		}

		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
		}

		@Override
		public void updateEnabled(boolean isEnabled) {
		}

		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
	}
	
	public MjpegPanel getPanel1() {
		return mjpeg1.getPanel();
	}
	
	public MjpegPanel getPanel2() {
		return mjpeg2.getPanel();
	}

	public void saveBeamCentres() {
		Point centre1 = getPanel1().getBeamCentre();
		Point centre2 = getPanel2().getBeamCentre();
		Activator.setPreference(BEAM_CENTRE_LEFT, String.format("%d,%d", centre1.x, centre1.y));
		Activator.setPreference(BEAM_CENTRE_RIGHT, String.format("%d,%d", centre2.x, centre2.y));
		Activator.flushPreferenceStore();
	}
	
	private void loadBeamCentres() {
		String bc = Activator.getPreference(BEAM_CENTRE_LEFT);
		if (bc.length() > 0) {
			String[] bcPair = bc.split(",");
			Point beamCentre = new Point(Integer.valueOf(bcPair[0]), Integer.valueOf(bcPair[1]));
			getPanel1().setBeamCentre(beamCentre);
		}
		bc = Activator.getPreference(BEAM_CENTRE_RIGHT);
		if (bc.length() > 0) {
			String[] bcPair = bc.split(",");
			Point beamCentre = new Point(Integer.valueOf(bcPair[0]), Integer.valueOf(bcPair[1]));
			getPanel2().setBeamCentre(beamCentre);
		}
	}
	
	public void finishCalibration() {
		alignButton.setEnabled(true);
		calibButton.setSelection(false);
		calibButton.setText("Calibrate cameras");
		logger.info("Calibration finished");
		calibComposite.setEnabled(false);
		showPanel(manualComposite);
		getPanel1().hideMarker();
		getPanel2().hideMarker();
	}
	
	public void finishAlignment() {
		calibButton.setEnabled(true);
		alignButton.setSelection(false);
		alignButton.setText(TEXT_ALIGN_BUTTON);
		logger.info("Alignment finished");
		alignComposite.setEnabled(false);
		showPanel(manualComposite);
		getPanel1().hideMarker();
		getPanel2().hideMarker();
	}
	
	public void cancelCalibration() {
		alignButton.setEnabled(true);
		calibButton.setSelection(false);
		calibButton.setText("Calibrate cameras");
		logger.info("Calibration cancelled");
		calibComposite.setEnabled(false);
		showPanel(manualComposite);
		getPanel1().hideMarker();
		getPanel2().hideMarker();
		getPanel1().setCentreFixed(true);
		getPanel2().setCentreFixed(true);
		loadBeamCentres();
	}
	
	public void cancelAlignment() {
		calibButton.setEnabled(true);
		alignButton.setSelection(false);
		alignButton.setText(TEXT_ALIGN_BUTTON);
		logger.info("alignment cancelled");
		alignComposite.setEnabled(false);
		showPanel(manualComposite);
		getPanel1().hideMarker();
		getPanel2().hideMarker();
	}
	
}
