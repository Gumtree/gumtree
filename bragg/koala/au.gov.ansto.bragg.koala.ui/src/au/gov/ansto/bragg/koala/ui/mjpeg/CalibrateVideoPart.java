/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.mjpeg;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.scan.KoalaInterruptionException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaModelException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public class CalibrateVideoPart extends Composite {

//	private static final String TEXT_SELECT_CENTRE = "2 Mark needle points on both videos";
	private static final String TEXT_DRIVE_1 = "to default centre, " + Activator.PHI + " to -45\u00b0";
	private static final String TEXT_DRIVE_2 = "Phi to 45\u00b0 and X to 10mm";
	private static final String TEXT_DRIVE_3 = "X to zero and Y to 10mm";
	private static final String TEXT_MARK_1 = "Mark needle points on both videos";
	private static final String TEXT_MARK_2 = "Mark needle points on videos again";
	
	private static final String SX_NAME = System.getProperty(ControlHelper.SX_PATH);
	private static final String SY_NAME = System.getProperty(ControlHelper.SY_PATH);
	private static final String SZ_NAME = System.getProperty(ControlHelper.SZ_PATH);
	private static final String SPHI_NAME = System.getProperty(ControlHelper.SAMPLE_PHI);
	
	private static final float DRIVE_GAP = 5;
	private static final float SPHI_ANGLE = 45;
	
	private static final Logger logger = LoggerFactory.getLogger(CalibrateVideoPart.class);

	private ControlHelper controlHelper;
	private MjpegViewer parentViewer;

//	private Button allZerosButton;
//	private Button pickBeamCentreButton;
//	private Button calPhiNEButton;
//	private Button pickLeftCentreButton;
//	private Button calSamYButton;
//	private Button pickRightCentreButton;
	
	private IStep[] steps;
	private boolean enabled;
	private float xGap;
	private float yGap;
	
	
	/**
	 * 
	 */
	public CalibrateVideoPart(Composite parent, int style, MjpegViewer viewer) {
		super(parent, style);
		parentViewer = viewer;
		controlHelper = ControlHelper.getInstance();
		steps = new IStep[7];

		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(this);

		final Group beamCentreGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(beamCentreGroup);
		beamCentreGroup.setText("Redefine beam centre");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(beamCentreGroup);

		
		steps[0] = new StepDrive(beamCentreGroup, 1, TEXT_DRIVE_1, new IDrivable() {
			
			@Override
			public void drive() {
				driveXYZtoZero();
			}
		});
		steps[1] = new StepMark(beamCentreGroup, 2, TEXT_MARK_1);
		steps[2] = new StepMark(beamCentreGroup, 3, TEXT_MARK_2);
		
//		pickBeamCentreButton = new Button(beamCentreGroup, SWT.TOGGLE);
//		pickBeamCentreButton.setText("Next");
//		pickBeamCentreButton.setFont(Activator.getMiddleFont());
//		pickBeamCentreButton.setCursor(Activator.getHandCursor());
//		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(320, 48).applyTo(pickBeamCentreButton);
//		pickBeamCentreButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				logger.info("Select beam centre button clicked");
//				pickBeamCentre();
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});

		final Group leftVideoGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(leftVideoGroup);
		leftVideoGroup.setText("Calibrate left video");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(leftVideoGroup);

		steps[3] = new StepDrive(leftVideoGroup, 4, TEXT_DRIVE_2, new IDrivable() {
			
			@Override
			public void drive() {
				driveSphiAndX();
			}
		});
		steps[4] = new StepMark(leftVideoGroup, 5, TEXT_MARK_1);
		
//		calPhiNEButton = new Button(leftVideoGroup, SWT.CHECK);
////		phiSButton.setImage(KoalaImage.PLAY48.getImage());
//		calPhiNEButton.setText("2. Move ");
//		calPhiNEButton.setFont(Activator.getMiddleFont());
//		calPhiNEButton.setCursor(Activator.getHandCursor());
//		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(350, 48).applyTo(calPhiNEButton);
//		calPhiNEButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				logger.info("Phi 45 button clicked");
//				driveSphiAndX();
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
//		
//		pickLeftCentreButton = new Button(leftVideoGroup, SWT.TOGGLE);
//		pickLeftCentreButton.setText("Next");
//		pickLeftCentreButton.setFont(Activator.getMiddleFont());
//		pickLeftCentreButton.setCursor(Activator.getHandCursor());
//		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(320, 48).applyTo(pickLeftCentreButton);
//		pickLeftCentreButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				logger.info("Select left pointer button clicked");
//				pickLeftPointer();
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});

		final Group rightVideoGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(rightVideoGroup);
		rightVideoGroup.setText("Calibrate right video");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(rightVideoGroup);

		steps[5] = new StepDrive(rightVideoGroup, 6, TEXT_DRIVE_3, new IDrivable() {
			
			@Override
			public void drive() {
				driveXY();
			}
		});
		steps[6] = new StepMark(rightVideoGroup, 7, TEXT_MARK_2);

//		calSamYButton = new Button(rightVideoGroup, SWT.CHECK);
////		phiSButton.setImage(KoalaImage.PLAY48.getImage());
//		calSamYButton.setText("3. Move ");
//		calSamYButton.setFont(Activator.getMiddleFont());
//		calSamYButton.setCursor(Activator.getHandCursor());
//		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(350, 48).applyTo(calSamYButton);
//		calSamYButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				logger.info("Sample Y 10 button clicked");
//				moveXY();
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
//		
//		pickRightCentreButton = new Button(rightVideoGroup, SWT.TOGGLE);
//		pickRightCentreButton.setText("Next");
//		pickRightCentreButton.setFont(Activator.getMiddleFont());
//		pickRightCentreButton.setCursor(Activator.getHandCursor());
//		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(320, 48).applyTo(pickRightCentreButton);
//		pickRightCentreButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				logger.info("Select right pointer button clicked");
//				pickRightPointer();
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//			}
//		});
		
		parentViewer.getPanel1().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				if (enabled) {
					marker1Set();
				}
			}
			
			@Override
			public void centreSet() {
				if (enabled) {
					centre1Set();
				}
			}
		});

		parentViewer.getPanel2().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				if (enabled) {
					marker2Set();
				}
			}
			
			@Override
			public void centreSet() {
				if (enabled) {
					centre2Set();
				}
			}
		});

	}

	interface IStep {
		void setEnabled(boolean enabled);
		void reset();
		void finish();
	}
	
	interface IDrivable {
		void drive();
	}
	
	class StepDrive implements IStep {
		Label label1;
		Button driveButton;
		Label label2;
		Button checkButton;
		IDrivable drivable;
		String text;
		
		public StepDrive(Group parent, final int id, final String text, final IDrivable drivable) {
			this.text = text;
			this.drivable = drivable;
			label1 = new Label(parent, SWT.NONE);
			label1.setText(String.format("%d. ", id));
			label1.setFont(Activator.getMiddleFont());
			GridDataFactory.swtDefaults().grab(false, false).applyTo(label1);
			
			driveButton = new Button(parent, SWT.PUSH);
			driveButton.setText("Drive");
			driveButton.setFont(Activator.getMiddleFont());
			driveButton.setCursor(Activator.getHandCursor());
			GridDataFactory.swtDefaults().grab(false, false).applyTo(driveButton);
			driveButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					logger.info(text + " button clicked");
					drivable.drive();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			label2 = new Label(parent, SWT.NONE);
			label2.setText(text);
			label2.setFont(Activator.getMiddleFont());
			GridDataFactory.swtDefaults().grab(false, false).applyTo(label2);
			
			checkButton = new Button(parent, SWT.CHECK);
			checkButton.setFont(Activator.getMiddleFont());
			checkButton.setEnabled(false);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(checkButton);
		}
		
		@Override
		public void reset() {
			checkButton.setSelection(false);
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			label1.setEnabled(enabled);
			label2.setEnabled(enabled);
			driveButton.setEnabled(enabled);
		}
		
		@Override
		public void finish() {
			checkButton.setSelection(true);
		}
	}
	
	class StepMark implements IStep {
		Label label1;
		Button checkButton;
		public StepMark(Group parent, int id, String text) {
			label1 = new Label(parent, SWT.NONE);
			label1.setText(String.format("%d. %s", id, text));
			label1.setFont(Activator.getMiddleFont());
			GridDataFactory.swtDefaults().grab(false, false).span(3, 1).applyTo(label1);
			
			checkButton = new Button(parent, SWT.CHECK);
			checkButton.setFont(Activator.getMiddleFont());
			checkButton.setEnabled(false);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(checkButton);
		}
		
		@Override
		public void reset() {
			checkButton.setSelection(false);
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			label1.setEnabled(enabled);
		}

		@Override
		public void finish() {
			checkButton.setSelection(true);
		}

	}
	
	
	private void marker1Set() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				MjpegPanel p1 = parentViewer.getPanel1();
				Point centre = p1.getBeamCentre();
				Point marker = p1.getMarkerCoordinate();
				double scale = ((double) xGap) / (marker.x - centre.x);
				parentViewer.setMmPerPixelLeft(scale);

				steps[4].finish();
				moveToStep(5);
			}
		});
	}

	private void marker2Set() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				MjpegPanel p2 = parentViewer.getPanel2();
				Point centre = p2.getBeamCentre();
				Point marker = p2.getMarkerCoordinate();
				double scale = ((double) yGap) / (marker.x - centre.x);
				parentViewer.setMmPerPixelRight(scale);

				steps[6].finish();
				finish();
			}
		});
	}

	private void centre1Set() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
//				pickLeftCentreButton.setText("Next");
//				pickLeftCentreButton.setSelection(false);
				parentViewer.saveBeamCentres();
				steps[1].finish();
				moveToStep(2);
			}
		});
	}

	private void centre2Set() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
//				pickRightCentreButton.setText("Next");
//				pickRightCentreButton.setSelection(false);
				parentViewer.saveBeamCentres();
				steps[2].finish();
				moveToStep(3);
			}
		});
	}
//	private void checkCentreSet() {
//		MjpegPanel panel1 = parentViewer.getPanel1();
//		if (panel1.isCentreFixed()) {
//			MjpegPanel panel2 = parentViewer.getPanel2();
//			if (panel2.isCentreFixed()) {
//				parentViewer.saveBeamCentres();
//				Display.getDefault().asyncExec(new Runnable() {
//					
//					@Override
//					public void run() {
//						pickBeamCentreButton.setText("Next");
//						pickBeamCentreButton.setSelection(false);
//						moveToStep(2);
//					}
//				});
//			}
//		}
//	}
	
	private void driveSphiAndX() {
		if (controlHelper.isConnected()) {
			final IDynamicController sx = (IDynamicController) 
					SicsManager.getSicsModel().findController(SX_NAME);
			final Map<String, Number> devices = new HashMap<String, Number>();
			devices.put(SX_NAME, DRIVE_GAP);
			devices.put(SPHI_NAME, SPHI_ANGLE);
			JobRunner.run(new ILoopExitCondition() {
				
				@Override
				public boolean getExitCondition() {
					return true;
				}
			}, new Runnable() {
				
				@Override
				public void run() {
					try {
						float startX = 0;
						try {
							startX = Float.valueOf(sx.getValue().toString());
						} catch (Exception e2) {
							throw new KoalaModelException("invalid sx value");
						}
						ControlHelper.syncMultiDrive(devices);
						try {
							float endX = Float.valueOf(sx.getValue().toString());
							xGap = endX - startX;
						} catch (SicsModelException e2) {
							throw new KoalaModelException("invalid sx value");
						}
						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								steps[3].finish();
								moveToStep(4);
							}
						});
					} catch (final KoalaServerException e1) {
						ControlHelper.experimentModel.publishErrorMessage("server error: " + e1.getMessage());
					} catch (KoalaInterruptionException e) {
						ControlHelper.experimentModel.publishErrorMessage("user interrupted");
					} catch (KoalaModelException e) {
						ControlHelper.experimentModel.publishErrorMessage("model error: " + e.getMessage());
					}
				}
			});
		}
	}
	
	private void driveXYZtoZero() {
		if (controlHelper.isConnected()) {
			final Map<String, Number> devices = new HashMap<String, Number>();
			devices.put(SX_NAME, 0f);
			devices.put(SY_NAME, 0f);
			devices.put(SZ_NAME, 0f);
			devices.put(SPHI_NAME, -45);
			JobRunner.run(new ILoopExitCondition() {

				@Override
				public boolean getExitCondition() {
					return true;
				}
			}, new Runnable() {

				@Override
				public void run() {
					try {
						ControlHelper.syncMultiDrive(devices);
						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								steps[0].finish();
								moveToStep(1);
							}
						});
					} catch (final KoalaServerException e1) {
						ControlHelper.experimentModel.publishErrorMessage("server error: " + e1.getMessage());
					} catch (KoalaInterruptionException e) {
						ControlHelper.experimentModel.publishErrorMessage("user interrupted");
					} 
				}
			});
		}
	}
	
	private void driveXY() {
		if (controlHelper.isConnected()) {
			final IDynamicController sy = (IDynamicController) 
					SicsManager.getSicsModel().findController(SY_NAME);
			final Map<String, Number> devices = new HashMap<String, Number>();
			devices.put(SX_NAME, 0f);
			devices.put(SY_NAME, DRIVE_GAP);
			JobRunner.run(new ILoopExitCondition() {

				@Override
				public boolean getExitCondition() {
					return true;
				}
			}, new Runnable() {

				@Override
				public void run() {
					try {
						float startY = 0;
						try {
							startY = Float.valueOf(sy.getValue().toString());
						} catch (Exception e2) {
							throw new KoalaModelException("invalid sy value");
						}
						ControlHelper.syncMultiDrive(devices);
						try {
							float endY = Float.valueOf(sy.getValue().toString());
							yGap = endY - startY;
						} catch (SicsModelException e2) {
							throw new KoalaModelException("invalid sy value");
						}
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								steps[5].finish();
								moveToStep(6);
							}
						});
					} catch (final KoalaServerException e1) {
						ControlHelper.experimentModel.publishErrorMessage("server error: " + e1.getMessage());
					} catch (KoalaInterruptionException e) {
						ControlHelper.experimentModel.publishErrorMessage("user interrupted");
					} catch (KoalaModelException e) {
						ControlHelper.experimentModel.publishErrorMessage("model error: " + e.getMessage());
					}
				}
			});
		}
	}
	
	private void moveToStep(int id) {
		for (int i = 0; i < steps.length; i ++) {
			steps[i].setEnabled(id == i);
			if (id == 1) {
				parentViewer.getPanel1().setCentreFixed(false);
			} else if (id == 2) {
				parentViewer.getPanel2().setCentreFixed(false);
			} else if (id == 4) {
				parentViewer.getPanel1().setMarkerFixed(false);
			} else if (id == 6) {
				parentViewer.getPanel2().setMarkerFixed(false);
			}
		}
	}
	
	public void reset() {
		moveToStep(0);
		for (int i = 0; i < steps.length; i++) {
			steps[i].reset();
		}
	}
	
	public void finish() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				parentViewer.forceFocus();
				MessageDialog.openInformation(getShell(), "Camera calibration", "The caliabration process has been finished");
				reset();
				parentViewer.finishCalibration();
			}
		});
	}
	
	public void setEnabled(boolean isEnabled) {
		enabled = isEnabled;
	}
}
