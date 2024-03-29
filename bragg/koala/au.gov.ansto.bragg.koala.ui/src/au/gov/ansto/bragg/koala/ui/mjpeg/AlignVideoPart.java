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
public class AlignVideoPart extends Composite {

//	private static final String TEXT_SELECT_CENTRE = "2 Mark needle points on both videos";
	private static final String TEXT_DRIVE_SZ_PHI = "sz to " + System.getProperty(ControlHelper.SZ_ZERO) + ", " + Activator.PHI + " to 45\u00b0";
//	private static final String TEXT_DRIVE_PHI = "Phi to 45\u00b0";
	private static final String TEXT_MARK_LEFT = "Mark sample centre on left video";
	private static final String TEXT_MARK_RIGHT = "Mark sample centre on right video";
	private static final String TEXT_DRIVE_ALIGN = "sample to beam centre";
	private static final String SX_NAME = System.getProperty(ControlHelper.SX_PATH);
	private static final String SY_NAME = System.getProperty(ControlHelper.SY_PATH);
	private static final String SZ_NAME = System.getProperty(ControlHelper.SZ_PATH);
	private static final String SPHI_NAME = System.getProperty(ControlHelper.SAMPLE_PHI);
	private static final float SPHI_ANGLE = 45;
	
	private static final int STEP_ID_DRIVEZPHI = 0;
//	private static final int STEP_ID_DRIVEPHI = 1;
	private static final int STEP_ID_MARKLEFT = 1;
	private static final int STEP_ID_MARKRIGHT = 2;
	private static final int STEP_ID_DRIVEALL = 3;
	
	public static final int NUM_STEPS = STEP_ID_DRIVEALL + 1;
	private static final Logger logger = LoggerFactory.getLogger(AlignVideoPart.class);

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
	
	/**
	 * 
	 */
	public AlignVideoPart(Composite parent, int style, MjpegViewer viewer) {
		super(parent, style);
		parentViewer = viewer;
		controlHelper = ControlHelper.getInstance();
		steps = new IStep[NUM_STEPS];

		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 2).applyTo(this);

		final Group phiGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(phiGroup);
		phiGroup.setText("Initialise sample position");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(phiGroup);

		steps[STEP_ID_DRIVEZPHI] = new StepDrive(phiGroup, STEP_ID_DRIVEZPHI, TEXT_DRIVE_SZ_PHI, new IDrivable() {
			
			@Override
			public void drive() {
				driveSzPhi(STEP_ID_DRIVEZPHI);
			}
		});

//		steps[STEP_ID_DRIVEPHI] = new StepDrive(phiGroup, STEP_ID_DRIVEPHI, TEXT_DRIVE_PHI, new IDrivable() {
//			
//			@Override
//			public void drive() {
//				drivePhi(STEP_ID_DRIVEPHI);
//			}
//		});

		final Group markerGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(markerGroup);
		markerGroup.setText("Add markers");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(markerGroup);

		steps[STEP_ID_MARKLEFT] = new StepMark(markerGroup, STEP_ID_MARKLEFT, TEXT_MARK_LEFT);
		steps[STEP_ID_MARKRIGHT] = new StepMark(markerGroup, STEP_ID_MARKRIGHT, TEXT_MARK_RIGHT);
		
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

		final Group alignGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(alignGroup);
		alignGroup.setText("Align sample");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(alignGroup);

		steps[STEP_ID_DRIVEALL] = new StepDrive(alignGroup, STEP_ID_DRIVEALL, TEXT_DRIVE_ALIGN, new IDrivable() {
			
			@Override
			public void drive() {
				driveAlign(STEP_ID_DRIVEALL);
			}
		});
		

		parentViewer.getPanel1().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				if (enabled) {
					marker1Set(STEP_ID_MARKLEFT);
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
					marker2Set(STEP_ID_MARKRIGHT);
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
			label1.setText(String.format("%d. ", id + 1));
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
			checkButton.setEnabled(enabled);
		}
		
		@Override
		public void finish() {
			checkButton.setSelection(true);
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
			checkButton.setEnabled(false);
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
			checkButton.setEnabled(enabled);
			if (stepId == STEP_ID_MARKLEFT) {
				parentViewer.getPanel1().setMarkerFixed(!enabled);
			} else if (stepId == STEP_ID_MARKRIGHT) {
				parentViewer.getPanel2().setMarkerFixed(!enabled);
			}
		}

		@Override
		public void finish() {
			checkButton.setSelection(true);
			if (stepId == STEP_ID_MARKLEFT) {
				parentViewer.getPanel1().setMarkerFixed(true);
			} else if (stepId == STEP_ID_MARKRIGHT) {
				parentViewer.getPanel2().setMarkerFixed(true);
			}
		}

	}
	
	
	private void marker1Set(final int stepId) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				steps[stepId].finish();
				moveToStep(stepId + 1);
			}
		});
	}

	private void marker2Set(final int stepId) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				steps[stepId].finish();
				moveToStep(stepId + 1);
			}
		});
	}

	
//	private void drivePhi(final int stepId) {
//		if (controlHelper.isConnected()) {
//			final ISicsController sphiController = SicsManager.getSicsModel().findController(
//					System.getProperty(ControlHelper.SAMPLE_PHI));
//			if (sphiController instanceof DriveableController) {
//				final DriveableController driveable = (DriveableController) sphiController;
//				JobRunner.run(new ILoopExitCondition() {
//					
//					@Override
//					public boolean getExitCondition() {
//						return true;
//					}
//				}, new Runnable() {
//					
//					@Override
//					public void run() {
//						driveable.setTargetValue(SPHI_ANGLE);
//						try {
//							driveable.drive();
//							Display.getDefault().asyncExec(new Runnable() {
//								
//								@Override
//								public void run() {
//									steps[stepId].finish();
//									moveToStep(stepId + 1);
//								}
//							});
//						} catch (final SicsException e1) {
//							if (e1 instanceof SicsInterruptException) {
//								ControlHelper.experimentModel.publishErrorMessage("user interrupted");
//							} else {
//								ControlHelper.experimentModel.publishErrorMessage("server error: " + e1.getMessage());
//							}
//						}
//					}
//				});
//			}
//		}
//	}

	private void driveSzPhi(final int stepId) {
		if (controlHelper.isConnected()) {
			final Map<String, Number> devices = new HashMap<String, Number>();
			devices.put(SZ_NAME, Float.valueOf(String.valueOf(System.getProperty(ControlHelper.SZ_ZERO))));
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
					}
				}
			});
		}
	}

	private void driveAlign(final int stepId) {
		if (controlHelper.isConnected()) {
			JobRunner.run(new ILoopExitCondition() {

				@Override
				public boolean getExitCondition() {
					return true;
				}
			}, new Runnable() {

				@Override
				public void run() {
					try {
						final IDynamicController sx = (IDynamicController) 
								SicsManager.getSicsModel().findController(SX_NAME);
						final IDynamicController sy = (IDynamicController) 
								SicsManager.getSicsModel().findController(SY_NAME);
						final IDynamicController sz = (IDynamicController) 
								SicsManager.getSicsModel().findController(SZ_NAME);
						
						float curX = Float.valueOf(sx.getValue().toString());
						MjpegPanel p1 = parentViewer.getPanel1();
						Point centre1 = p1.getBeamCentre();
						Point marker1 = p1.getMarkerCoordinate();
						double mmPerPixelLeft = parentViewer.getMmPerPixelLeft();
						float endX =  Double.valueOf(curX - mmPerPixelLeft * (marker1.x - centre1.x)).floatValue();
						
						float curY = Float.valueOf(sy.getValue().toString());
						MjpegPanel p2 = parentViewer.getPanel2();
						Point centre2 = p2.getBeamCentre();
						Point marker2 = p2.getMarkerCoordinate();
						double mmPerPixelRight = parentViewer.getMmPerPixelRight();
						float endY =  Double.valueOf(curY + mmPerPixelRight * (marker2.x - centre2.x)).floatValue();
						
						float curZ = Float.valueOf(sz.getValue().toString());
//						double mmPerPixelLeftZ = parentViewer.getMmPerPixelLeftZ();
//						double mmPerPixelRightZ = parentViewer.getMmPerPixelRightZ();
						float endLeftZ =  Double.valueOf(mmPerPixelLeft * (marker1.y - centre1.y) + curZ).floatValue();
						float endRightZ =  Double.valueOf(mmPerPixelRight * (marker2.y - centre2.y) + curZ).floatValue();
						float endZ = (endLeftZ + endRightZ) / 2;
						
						final Map<String, Number> devices = new HashMap<String, Number>();
						devices.put(SX_NAME, endX);
						devices.put(SY_NAME, endY);
						devices.put(SZ_NAME, endZ);

						Activator.setPreference(Activator.NAME_SX_ALIGN, String.valueOf(endX));
						Activator.setPreference(Activator.NAME_SY_ALIGN, String.valueOf(endY));
						Activator.setPreference(Activator.NAME_SZ_ALIGN, String.valueOf(endZ));
						
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
					} catch (Exception e) {
						ControlHelper.experimentModel.publishErrorMessage("error in alignment: " + e.getMessage());
					}
				}
			});
		}
	}
	
	private void moveToStep(int id) {
		for (int i = 0; i < steps.length; i ++) {
			steps[i].setEnabled(id == i);
		}
	}
	
	public void reset() {
		moveToStep(0);
		for (int i = 0; i < steps.length; i++) {
			steps[i].reset();
		}
	}
	
	public void finishAll() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				parentViewer.forceFocus();
				MessageDialog.openInformation(getShell(), "Sample alignment", "The alignment process has been finished");
				reset();
				parentViewer.finishAlignment();
			}
		});
	}
	
	public void setEnabled(boolean isEnabled) {
		enabled = isEnabled;
	}
}
