package au.gov.ansto.bragg.koala.ui.mjpeg;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.imp.DriveableController;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;
import au.gov.ansto.bragg.koala.ui.sics.SimpleControlSuite;
import au.gov.ansto.bragg.nbi.ui.internal.InternalImage;

public class MjpegViewer extends Composite {

	private static final String CAM1_URL = "gumtree.koala.mjpeg1Url";
	private static final String CAM2_URL = "gumtree.koala.mjpeg2Url";
	
//	private static final String CAM_SIZE = "gumtree.koala.camSize";

	private MjpegRunner runner1;
	private MjpegRunner runner2;
	private MjpegComposite mjpeg1;
	private MjpegComposite mjpeg2;
	private String cam1Url;
	private String cam2Url;
	private Button markerButton;
	private Button reloadButton;
	private Button alignButton;
	private boolean isAddingMarker;
	private Button phiSButton;
	private Button phiNButton;
	private Button phiEButton;
	private Button phiWButton;
	private ControlHelper controlHelper;
	
	public MjpegViewer(Composite parent, int style) {
		super(parent, style);
		controlHelper = ControlHelper.getInstance();
		cam1Url = System.getProperty(CAM1_URL);
		cam2Url = System.getProperty(CAM2_URL);

		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(this);
		
		Composite controlComposite = new Composite(this, SWT.NONE);
//		controlComposite.setLayout(new FillLayout());
		GridLayoutFactory.fillDefaults().numColumns(3).margins(1, 1).spacing(0, 0).applyTo(controlComposite);
		GridDataFactory.fillDefaults().applyTo(controlComposite);
		
		createControls(controlComposite);

		Composite mainComposite = new Composite(this, SWT.NONE);
//		imageComposite.setLayout(new FillLayout());
		GridLayoutFactory.fillDefaults().numColumns(3).spacing(1, 1).applyTo(mainComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);
		
		Composite image1Composite = new Composite(mainComposite, SWT.NONE);
		image1Composite.setLayout(new FillLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(image1Composite);
		mjpeg1 = new MjpegComposite(image1Composite, SWT.BORDER);
		
		createControlComposite(mainComposite);
		
		Composite image2Composite = new Composite(mainComposite, SWT.NONE);
		image2Composite.setLayout(new FillLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(image2Composite);
		mjpeg2 = new MjpegComposite(image2Composite, SWT.BORDER);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				mjpeg1.getPanel().dispose();
				mjpeg2.getPanel().dispose();
				stopRunner();
			}
		});
		
		mjpeg1.getPanel().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				if (isAddingMarker) {
					checkMarkers();
				}
			}
		});

		mjpeg2.getPanel().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				if (isAddingMarker) {
					checkMarkers();
				}
			}
		});

		startRunner();
	}

	private void createControls(Composite controlComposite) {
		markerButton = new Button(controlComposite, SWT.TOGGLE);
		markerButton.setText("Add Markers");
		markerButton.setImage(InternalImage.ADD_ITEM.getImage());
		markerButton.setSelection(false);
		GridDataFactory.fillDefaults().grab(true, false).minSize(0, 32).hint(100, 32).applyTo(markerButton);
		markerButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				isAddingMarker = markerButton.getSelection();
				if (isAddingMarker) {
					mjpeg1.getPanel().unfixMarker();
					mjpeg1.getPanel().showMarker();
					mjpeg2.getPanel().unfixMarker();
					mjpeg2.getPanel().showMarker();
					checkMarkers();
				} else {
					mjpeg1.getPanel().resetMarkerCoordinate();
					mjpeg2.getPanel().resetMarkerCoordinate();
					markerButton.setText("Add Markers");
					alignButton.setEnabled(false);
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		alignButton = new Button(controlComposite, SWT.PUSH);
		alignButton.setEnabled(false);
		alignButton.setText("Align Sample");
		alignButton.setImage(InternalImage.PLAY_16.getImage());
		GridDataFactory.fillDefaults().grab(true, false).minSize(0, 32).hint(100, 32).applyTo(alignButton);
		alignButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				alignButton.setEnabled(false);
				mjpeg1.getPanel().resetMarkerCoordinate();
				mjpeg2.getPanel().resetMarkerCoordinate();
				markerButton.setText("Add Markers");
				markerButton.setSelection(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		reloadButton = new Button(controlComposite, SWT.PUSH);
		reloadButton.setText("Reset Video");
		reloadButton.setImage(InternalImage.PLAY_16.getImage());
		GridDataFactory.fillDefaults().grab(true, false).minSize(0, 32).hint(100, 32).applyTo(reloadButton);
		reloadButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				resetRunner();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}	
		});			
	}

	private void checkMarkers() {
		if (mjpeg1.getPanel().getMarkerCoordinate() == null || mjpeg2.getPanel().getMarkerCoordinate() == null) {
			syncSetText(markerButton, "adding ...");
		} else {
			syncSetText(markerButton, "Remove Markers");
			syncSetEnabled(alignButton, true);
		}
	}

	private void syncSetText(final Button button, final String text) {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				button.setText(text);
			}
		});
	}

	private void syncSetImage(final Button button, final Image image) {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				button.setImage(image);
			}
		});
	}

	private void syncSetEnabled(final Button button, final boolean isEnabled) {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				button.setEnabled(isEnabled);
			}
		});
	}

	private void createControlComposite(Composite imageComposite) {
		Composite wrapComposite = new Composite(imageComposite, SWT.BORDER);
		wrapComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(wrapComposite);
		GridDataFactory.fillDefaults().grab(false, true).applyTo(wrapComposite);
		
		Composite axesControlComposite = new Composite(wrapComposite, SWT.NONE);
//		axesControlComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		GridLayoutFactory.fillDefaults().margins(8, 8).numColumns(2).applyTo(axesControlComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER).applyTo(axesControlComposite);
//		axesControlComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 1));
		
		phiSButton = new Button(axesControlComposite, SWT.RADIO);
//		phiSButton.setImage(KoalaImage.PLAY48.getImage());
		phiSButton.setText("Phi -90\u00b0");
		phiSButton.setFont(Activator.getMiddleFont());
		phiSButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).span(1, 3).hint(150, 48).applyTo(phiSButton);
		phiSButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				driveSphi(-90);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		phiNButton = new Button(axesControlComposite, SWT.RADIO);
//		phiNButton.setImage(KoalaImage.PLAY48.getImage());
		phiNButton.setText("Phi +90\u00b0");
		phiNButton.setFont(Activator.getMiddleFont());
		phiNButton.setCursor(Activator.getHandCursor());
		phiNButton.setForeground(Activator.getHighlightColor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.END, SWT.CENTER).span(1, 3).hint(150, 48).applyTo(phiNButton);
		phiNButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				driveSphi(90);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});


		Group xGroup = new Group(axesControlComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).applyTo(xGroup);
		xGroup.setText("Sample X offset");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.CENTER, SWT.CENTER).applyTo(xGroup);
		
		final Label curLabel = new Label(xGroup, SWT.NONE);
		curLabel.setText("Current");
		curLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curLabel);
		
		final Text curText = new Text(xGroup, SWT.READ_ONLY);
		curText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curText);
		
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
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(tarText);
		
		final Slider slider = new Slider(xGroup, SWT.HORIZONTAL);
//	    slider.setBounds(0, 0, 40, 200);
	    slider.setMaximum(100);
	    slider.setMinimum(0);
	    slider.setIncrement(1);
	    slider.setPageIncrement(5);
	    slider.setThumb(4);
	    slider.setSelection(48);
	    GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(slider);
	    
		String sxPath = System.getProperty(ControlHelper.SX_PATH);
		SimpleControlSuite sxSuite = new SimpleControlSuite(sxPath, curText, sxPath, tarText, driveButton);
		
		Group yGroup = new Group(axesControlComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(3).applyTo(yGroup);
		yGroup.setText("Sample Y offset");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.CENTER, SWT.CENTER).applyTo(yGroup);
		
		final Label curYLabel = new Label(yGroup, SWT.NONE);
		curYLabel.setText("Current");
		curYLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curYLabel);
		
		final Text curYText = new Text(yGroup, SWT.READ_ONLY);
		curYText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(curYText);
		
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
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(100, 40).applyTo(tarYText);
		
		final Slider sliderY = new Slider(yGroup, SWT.HORIZONTAL);
//	    slider.setBounds(0, 0, 40, 200);
		sliderY.setMaximum(100);
		sliderY.setMinimum(0);
		sliderY.setIncrement(1);
		sliderY.setPageIncrement(5);
		sliderY.setThumb(4);
		sliderY.setSelection(48);
	    GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(sliderY);
	    
		String syPath = System.getProperty(ControlHelper.SY_PATH);
		SimpleControlSuite sySuite = new SimpleControlSuite(syPath, curYText, syPath, tarYText, driveYButton);

		phiEButton = new Button(axesControlComposite, SWT.RADIO);
//		phiEButton.setImage(KoalaImage.PLAY48.getImage());
		phiEButton.setText("Phi 0\u00b0");
		phiEButton.setFont(Activator.getMiddleFont());
		phiEButton.setCursor(Activator.getHandCursor());
		phiEButton.setForeground(Activator.getLightColor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).span(1, 3).hint(150, 48).applyTo(phiEButton);
		phiEButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				driveSphi(0);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		phiWButton = new Button(axesControlComposite, SWT.RADIO);
//		phiNButton.setImage(KoalaImage.PLAY48.getImage());
		phiWButton.setText("Phi -180\u00b0");
		phiWButton.setFont(Activator.getMiddleFont());
		phiWButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.END, SWT.CENTER).span(1, 3).hint(150, 48).applyTo(phiWButton);
		phiWButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				driveSphi(180);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		PhiControlSuite phiSuite = new PhiControlSuite();
	}

	private void driveSphi(final float value) {
		if (controlHelper.isConnected()) {
			final ISicsController sphiController = SicsManager.getSicsModel().findControllerByPath(
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
						} catch (SicsException e1) {
							e1.printStackTrace();
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
			new Thread(runner1).start();
			runner2 = new MjpegRunner(mjpeg2.getPanel(), 
					new URL(cam2Url));
			new Thread(runner2).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	public void stopRunner() {
		try {
			if (runner1 != null) {
				runner1.stop();
			}
			if (runner2 != null) {
				runner2.stop();
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
	
	class PhiControlSuite {
		
		public PhiControlSuite() {
			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
				
				@Override
				public void connect() {
					final ISicsController phiController = SicsManager.getSicsModel().findControllerByPath(
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
								if (inRange(value, precision, 90)) {
									chooseRange(phiNButton);
								} else if (inRange(value, precision, -90)) {
									chooseRange(phiSButton);
								} else if (inRange(value, precision, 0)) {
									chooseRange(phiEButton);
								} else if (inRange(value, precision, 180)) {
									chooseRange(phiWButton);
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
				phiNButton.setSelection(false);
				phiSButton.setSelection(false);
				phiWButton.setSelection(false);
				phiEButton.setSelection(false);
				if (rangeButton != null) {
					rangeButton.setSelection(true);
				}
			}
		});
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
						phiNButton.setEnabled(false);
						phiSButton.setEnabled(false);
						phiEButton.setEnabled(false);
						phiWButton.setEnabled(false);
					} else {
						try {
							float value = (Float) phiController.getValue();
							double precision = phiController.getPrecision();
							if (Double.isNaN(precision)) {
								precision = 0;
							} else {
								precision = Math.abs(precision);
							}
							if (inRange(value, precision, 90)) {
								chooseRange(phiNButton);
							} else if (inRange(value, precision, -90)) {
								chooseRange(phiSButton);
							} else if (inRange(value, precision, 0)) {
								chooseRange(phiEButton);
							} else if (inRange(value, precision, 180)) {
								chooseRange(phiWButton);
							} else {
								chooseRange(null);
							}						
						} catch (SicsModelException e) {
							e.printStackTrace();
						}
						phiNButton.setEnabled(true);
						phiSButton.setEnabled(true);
						phiEButton.setEnabled(true);
						phiWButton.setEnabled(true);
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
}
