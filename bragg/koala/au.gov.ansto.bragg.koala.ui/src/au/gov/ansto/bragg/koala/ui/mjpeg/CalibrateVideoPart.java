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
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsInterruptException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.imp.DriveableController;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.scan.KoalaInterruptionException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public class CalibrateVideoPart extends Composite {

//	private static final String TEXT_SELECT_CENTRE = "2 Mark needle points on both videos";
	private static final String TEXT_PRESETUP = "Install the calibration cone";
	private static final String TEXT_DRIVE_ALL = "to default centre, " + Activator.PHI + " to 45\u00b0";
	private static final String TEXT_DRIVE_PHI = Activator.PHI + " to -45\u00b0";
	private static final String TEXT_DRIVE_X = "X to 5mm";
	private static final String TEXT_DRIVE_PHIBACK = Activator.PHI + " back to 45\u00b0";
	private static final String TEXT_CALCULATE_CENTRE = "to calculated beam centre";
	
	private static final String TEXT_MARK = "Mark needle points on both videos";
	
	private static final String SX_NAME = System.getProperty(ControlHelper.SX_PATH);
	private static final String SY_NAME = System.getProperty(ControlHelper.SY_PATH);
	private static final String SZ_NAME = System.getProperty(ControlHelper.SZ_PATH);
	private static final String SPHI_NAME = System.getProperty(ControlHelper.SAMPLE_PHI);
	
	private static final int STEP_PRE_SETUP = 0;
	private static final int STEP_DRIVE_ALL = 1;
	private static final int STEP_MARK_1 = 2;
	private static final int STEP_DRIVE_PHI = 3;
	private static final int STEP_MARK_2 = 4;
	private static final int STEP_DRIVE_X = 5;
	private static final int STEP_MARK_3 = 6;
	private static final int STEP_DRIVE_PHIBACK = 7;
	private static final int STEP_MARK_4 = 8;
	private static final int STEP_CALCULATE = 9;

	private static final int NUM_STEPS = STEP_CALCULATE + 1;

	private static final float DRIVE_GAP = 5;
	private static final float SPHI_ANGLE = 45;
	private static final float SPHI_ANGLE_CHANGE = -45;
	
	private static final Logger logger = LoggerFactory.getLogger(CalibrateVideoPart.class);

	private ControlHelper controlHelper;
	private MjpegViewer parentViewer;

	private float sxValue1;
	private float syValue1;
	private float scaleLeft;
	private float scaleRight;
	private int currentStepId;
	private Point markerLeft1;
	private Point markerLeft2;
	private Point markerLeft3;
	private Point markerLeft4;
	private Point markerRight1;
	private Point markerRight2;
	private Point markerRight3;
	private Point markerRight4;
	
//	private Button allZerosButton;
//	private Button pickBeamCentreButton;
//	private Button calPhiNEButton;
//	private Button pickLeftCentreButton;
//	private Button calSamYButton;
//	private Button pickRightCentreButton;
	
	private IStep[] steps;
	private boolean enabled;
	private float xGap;
	private float centreX;
	private float centreY;
	
	
	/**
	 * 
	 */
	public CalibrateVideoPart(Composite parent, int style, MjpegViewer viewer) {
		super(parent, style);
		parentViewer = viewer;
		controlHelper = ControlHelper.getInstance();
		steps = new IStep[NUM_STEPS];

		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(this);

		final Group beamCentreGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(beamCentreGroup);
		beamCentreGroup.setText("Redefine beam centre");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(beamCentreGroup);

		steps[STEP_PRE_SETUP] = new StepSetup(beamCentreGroup, STEP_PRE_SETUP, TEXT_PRESETUP);
		
		steps[STEP_DRIVE_ALL] = new StepDrive(beamCentreGroup, STEP_DRIVE_ALL, TEXT_DRIVE_ALL, new IDrivable() {
			
			@Override
			public void drive() {
				driveXYZPhi(STEP_DRIVE_ALL);
			}
		});
		
		steps[STEP_MARK_1] = new StepMark(beamCentreGroup, STEP_MARK_1, TEXT_MARK);
		
		steps[STEP_DRIVE_PHI] = new StepDrive(beamCentreGroup, STEP_DRIVE_PHI, TEXT_DRIVE_PHI, new IDrivable() {
			
			@Override
			public void drive() {
				driveSphi(SPHI_ANGLE_CHANGE, STEP_DRIVE_PHI);
			}
		});
		
		steps[STEP_MARK_2] = new StepMark(beamCentreGroup, STEP_MARK_2, TEXT_MARK);
		
		steps[STEP_DRIVE_X] = new StepDrive(beamCentreGroup, STEP_DRIVE_X, TEXT_DRIVE_X, new IDrivable() {
			
			@Override
			public void drive() {
				driveSx(STEP_DRIVE_X);
			}
		});
		

//		final Group leftVideoGroup = new Group(this, SWT.NONE);
//		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(leftVideoGroup);
//		leftVideoGroup.setText("Calibrate left video");
//		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(leftVideoGroup);

		steps[STEP_MARK_3] = new StepMark(beamCentreGroup, STEP_MARK_3, TEXT_MARK);
		
		steps[STEP_DRIVE_PHIBACK] = new StepDrive(beamCentreGroup, STEP_DRIVE_PHIBACK, TEXT_DRIVE_PHIBACK, new IDrivable() {
			
			@Override
			public void drive() {
				driveSphi(SPHI_ANGLE, STEP_DRIVE_PHIBACK);
			}
		});
		
		steps[STEP_MARK_4] = new StepMark(beamCentreGroup, STEP_MARK_4, TEXT_MARK);
		
		steps[STEP_CALCULATE] = new StepDrive(beamCentreGroup, STEP_CALCULATE, TEXT_CALCULATE_CENTRE, new IDrivable() {
			
			@Override
			public void drive() {
				driveXY(STEP_CALCULATE);
			}
		});
		
		parentViewer.getPanel1().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				if (enabled) {
					marker1Set();
				}
			}
			
			@Override
			public void centreSet() {
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
			}
		});

	}

	interface IStep {
		void setEnabled(boolean enabled);
		void reset();
		void finish();
		int getStepId();
	}
	
	interface IDrivable {
		void drive();
	}
	
	class StepDrive implements IStep {
		int stepId;
		Label label1;
		Button driveButton;
		Label label2;
		Button checkButton;
		IDrivable drivable;
		String text;
		
		public StepDrive(Group parent, final int id, final String text, final IDrivable drivable) {
			this.stepId = id;
			this.text = text;
			this.drivable = drivable;
			label1 = new Label(parent, SWT.NONE);
			label1.setText(String.format("%d. ", id + 1));
			label1.setFont(Activator.getMiddleFont());
			GridDataFactory.swtDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(label1);
			
			driveButton = new Button(parent, SWT.PUSH);
			driveButton.setText("Drive");
			driveButton.setFont(Activator.getMiddleFont());
			driveButton.setCursor(Activator.getHandCursor());
			GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(driveButton);
			driveButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					logger.info(text + " button clicked");
					parentViewer.getPanel1().hideMarker();
					parentViewer.getPanel2().hideMarker();
					drivable.drive();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			label2 = new Label(parent, SWT.NONE);
			label2.setText(text);
			label2.setFont(Activator.getMiddleFont());
			GridDataFactory.swtDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(label2);
			
			checkButton = new Button(parent, SWT.CHECK);
			checkButton.setFont(Activator.getMiddleFont());
//			checkButton.setEnabled(false);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(checkButton);
			
			checkButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (checkButton.getSelection()) {
						steps[id].finish();
						if (id < NUM_STEPS - 1) {
							moveToStep(id + 1);
						} else {
							finishAll();
						}
					} else {
						setEnabled(true);
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
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
//			checkButton.setEnabled(enabled);
		}
		
		@Override
		public void finish() {
			checkButton.setSelection(true);
		}

		@Override
		public int getStepId() {
			return stepId;
		}
	}
	
	class StepMark implements IStep {
		Label label1;
		Button checkButton;
		int stepId;
		public StepMark(Group parent, final int id, String text) {
			stepId = id;
			label1 = new Label(parent, SWT.NONE);
			label1.setText(String.format("%d. %s", id + 1, text));
			label1.setFont(Activator.getMiddleFont());
			GridDataFactory.swtDefaults().grab(false, false).span(3, 1).applyTo(label1);
			
			checkButton = new Button(parent, SWT.CHECK);
			checkButton.setFont(Activator.getMiddleFont());
//			checkButton.setEnabled(false);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(checkButton);
			
			checkButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (checkButton.getSelection()) {
						finish();
						if (stepId < NUM_STEPS - 1) {
							moveToStep(stepId + 1);
						} else {
							finishAll();
						}
					} else {
						setEnabled(true);
						if (stepId == STEP_MARK_1) {
							markerLeft1 = markerRight1 = null;
						} else if (stepId == STEP_MARK_2) {
							markerLeft2 = markerRight2 = null;
						} else if (stepId == STEP_MARK_3) {
							markerLeft3 = markerRight3 = null;
						} else if (stepId == STEP_MARK_4) {
							markerLeft4 = markerRight4 = null;
						}
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		
		@Override
		public void reset() {
			checkButton.setSelection(false);
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			currentStepId = stepId;
			label1.setEnabled(enabled);
			parentViewer.getPanel1().setMarkerFixed(!enabled);
			parentViewer.getPanel2().setMarkerFixed(!enabled);
//			checkButton.setEnabled(enabled);
		}

		@Override
		public void finish() {
			checkButton.setSelection(true);
			parentViewer.getPanel1().setMarkerFixed(true);
			parentViewer.getPanel2().setMarkerFixed(true);
		}

		@Override
		public int getStepId() {
			return stepId;
		}

	}
	
	class StepSetup implements IStep {
		Label label1;
		Button checkButton;
		int stepId;
		public StepSetup(Group parent, final int id, String text) {
			stepId = id;
			label1 = new Label(parent, SWT.NONE);
			label1.setText(String.format("%d. %s", id + 1, text));
			label1.setFont(Activator.getMiddleFont());
			GridDataFactory.swtDefaults().grab(false, false).span(3, 1).applyTo(label1);
			
			checkButton = new Button(parent, SWT.CHECK);
			checkButton.setFont(Activator.getMiddleFont());
//			checkButton.setEnabled(false);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(checkButton);
			
			checkButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (checkButton.getSelection()) {
						finish();
						if (stepId < NUM_STEPS - 1) {
							moveToStep(stepId + 1);
						} else {
							finishAll();
						}
					} else {
						setEnabled(true);
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		
		@Override
		public void reset() {
			checkButton.setSelection(false);
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			label1.setEnabled(enabled);
//			checkButton.setEnabled(enabled);
		}

		@Override
		public void finish() {
		}

		@Override
		public int getStepId() {
			return stepId;
		}

	}
	
	private void moveOn(boolean isFinished) {
		if (isFinished) {
			steps[currentStepId].finish();
			moveToStep(currentStepId + 1);	
		}
	}
	
	private void marker1Set() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				MjpegPanel p1 = parentViewer.getPanel1();
				Point marker = p1.getMarkerCoordinate();
				if (currentStepId == STEP_MARK_1) {
					markerLeft1 = marker;
					moveOn(markerLeft1 != null && markerRight1 != null);
				} else if (currentStepId == STEP_MARK_2) {
					markerLeft2 = marker;
					moveOn(markerLeft2 != null && markerRight2 != null);
				} else if (currentStepId == STEP_MARK_3) {
					markerLeft3 = marker;
					moveOn(markerLeft3 != null && markerRight3 != null);
				} else if (currentStepId == STEP_MARK_4) {
					markerLeft4 = marker;
					moveOn(markerLeft4 != null && markerRight4 != null);
				} 
//				double scale = ((double) xGap) / (marker.x - centre.x);
//				parentViewer.setMmPerPixelLeft(scale);

//				steps[4].finish();
//				moveToStep(5);
			}
		});
	}

	private void marker2Set() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				MjpegPanel p2 = parentViewer.getPanel2();
//				Point centre = p2.getBeamCentre();
				Point marker = p2.getMarkerCoordinate();
				if (currentStepId == STEP_MARK_1) {
					markerRight1 = marker;
					moveOn(markerLeft1 != null && markerRight1 != null);
				} else if (currentStepId == STEP_MARK_2) {
					markerRight2 = marker;
					moveOn(markerLeft2 != null && markerRight2 != null);
				} else if (currentStepId == STEP_MARK_3) {
					markerRight3 = marker;
					moveOn(markerLeft3 != null && markerRight3 != null);
				} else if (currentStepId == STEP_MARK_4) {
					markerRight4 = marker;
					moveOn(markerLeft4 != null && markerRight4 != null);
				} 
//				double scale = ((double) yGap) / (marker.x - centre.x);
//				parentViewer.setMmPerPixelRight(scale);

			}
		});
	}

//	private void centre2Set() {
//		Display.getDefault().asyncExec(new Runnable() {
//			
//			@Override
//			public void run() {
////				pickRightCentreButton.setText("Next");
////				pickRightCentreButton.setSelection(false);
//				parentViewer.saveBeamCentres();
//				steps[2].finish();
//				moveToStep(3);
//			}
//		});
//	}
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
	
	private void driveSphi(final float sphiAngle, final int stepId) {
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
						driveable.setTargetValue(sphiAngle);
						try {
							driveable.drive();
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									steps[stepId].finish();
									moveToStep(stepId + 1);
								}
							});
						} catch (SicsException e) {
							if (e instanceof SicsInterruptException) {
								ControlHelper.experimentModel.publishErrorMessage("user interrupted");
							} else {
								ControlHelper.experimentModel.publishErrorMessage("server error: " + e.getMessage());
							}
						}
					}
				});
			}
		}
	}
	
	private void driveXYZPhi(final int stepId) {
		if (controlHelper.isConnected()) {
			final Map<String, Number> devices = new HashMap<String, Number>();
			devices.put(SX_NAME, 0f);
			devices.put(SY_NAME, 0f);
			devices.put(SZ_NAME, Float.valueOf(System.getProperty(ControlHelper.SZ_ZERO)));
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
						ControlHelper.syncMultiDrive(devices);
						final ISicsController sxController = SicsManager.getSicsModel().findController(
								System.getProperty(ControlHelper.SX_PATH));
						sxValue1 = Float.valueOf(((DynamicController) sxController).getValue().toString());
						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								steps[stepId].finish();
								moveToStep(stepId + 1);
							}
						});
					} catch (final KoalaServerException e1) {
						ControlHelper.experimentModel.publishErrorMessage("server error: " + e1.getMessage());
					} catch (KoalaInterruptionException e) {
						ControlHelper.experimentModel.publishErrorMessage("user interrupted");
					} catch (NumberFormatException e) {
						ControlHelper.experimentModel.publishErrorMessage("invalid sx value");
					} catch (SicsModelException e) {
						ControlHelper.experimentModel.publishErrorMessage("invalid SICS connection");
					} 
				}
			});
		}
	}
	
	private void calculateBeamCentre() {
		scaleRight = Math.abs(xGap / (markerRight3.x - markerRight2.x));
		scaleLeft = Math.abs(xGap / (markerLeft1.x - markerLeft4.x));
		parentViewer.setMmPerPixelRight(scaleRight);
		parentViewer.setMmPerPixelLeft(scaleLeft);
		float dx = (markerLeft2.x - markerLeft1.x) * scaleLeft;
		float dy = (markerRight2.x - markerRight1.x) * scaleRight;
		centreX = (dx - dy) / 2;
		centreY = -(dy + dx) / 2;
		int centreZLeftInt = (markerLeft1.y + markerLeft2.y + markerLeft3.y + markerLeft4.y) / 4;
		int centreZRightInt = (markerRight1.y + markerRight2.y + markerRight3.y + markerRight4.y) / 4;
		int centreXint = markerLeft1.x + Float.valueOf((centreX - sxValue1) / scaleLeft).intValue();
		int centreYint = markerRight1.x - Float.valueOf((centreY - syValue1) / scaleRight).intValue();
		parentViewer.getPanel1().setBeamCentre(new Point(centreXint, centreZLeftInt));
		parentViewer.getPanel2().setBeamCentre(new Point(centreYint, centreZRightInt));
		Activator.setPreference(Activator.NAME_MJPEG_MMPERPIXEL_LEFT, String.valueOf(scaleLeft));
		Activator.setPreference(Activator.NAME_MJPEG_MMPERPIXEL_LEFT, String.valueOf(scaleRight));
		Activator.setPreference(Activator.BEAM_CENTRE_LEFT, String.format("%d,%d", centreXint, centreZLeftInt));
		Activator.setPreference(Activator.BEAM_CENTRE_RIGHT, String.format("%d,%d", centreYint, centreZRightInt));
	}
	
	private void driveXY(final int stepId) {
		calculateBeamCentre();
		if (controlHelper.isConnected()) {
			final Map<String, Number> devices = new HashMap<String, Number>();
			devices.put(SX_NAME, centreX);
			devices.put(SY_NAME, centreY);
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
								steps[stepId].finish();
								finishAll();
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
	
	private void driveSx(final int stepId) {
		if (controlHelper.isConnected()) {
			final ISicsController sxController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.SX_PATH));
			if (sxController instanceof DriveableController) {
				final DriveableController driveable = (DriveableController) sxController;
				JobRunner.run(new ILoopExitCondition() {
					
					@Override
					public boolean getExitCondition() {
						return true;
					}
				}, new Runnable() {
					
					@Override
					public void run() {
						driveable.setTargetValue(DRIVE_GAP);
						try {
							driveable.drive();
							float sxValue2 = Float.valueOf(driveable.getValue().toString());
							xGap = sxValue2 - sxValue1;
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									steps[stepId].finish();
									moveToStep(stepId + 1);
								}
							});
						} catch (final SicsException e1) {
							if (e1 instanceof SicsInterruptException) {
								ControlHelper.experimentModel.publishErrorMessage("user interrupted");
							} else {
								ControlHelper.experimentModel.publishErrorMessage("server error: " + e1.getMessage());
							}
						}
					}
				});
			}
		}
	}

	private void moveToStep(int id) {
		if (id > 0) {
			steps[id - 1].setEnabled(false);
		}
		steps[id].setEnabled(true);
//		for (int i = 0; i < steps.length; i ++) {
//			steps[i].setEnabled(id == i);
//			if (id == 1) {
//				parentViewer.getPanel1().setCentreFixed(false);
//			} else if (id == 2) {
//				parentViewer.getPanel2().setCentreFixed(false);
//			} else if (id == 4) {
//				parentViewer.getPanel1().setMarkerFixed(false);
//			} else if (id == 6) {
//				parentViewer.getPanel2().setMarkerFixed(false);
//			}
//		}
	}
	
	public void reset() {
		moveToStep(0);
		for (int i = 0; i < steps.length; i++) {
			steps[i].reset();
		}
		for (int i = 1; i < NUM_STEPS; i ++) {
			steps[i].setEnabled(false);
		}
		markerLeft1 = null;
		markerLeft2 = null;
		markerLeft3 = null;
		markerLeft4 = null;
		markerRight1 = null;
		markerRight2 = null;
		markerRight3 = null;
		markerRight4 = null;
	}
	
	public void finishAll() {
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
