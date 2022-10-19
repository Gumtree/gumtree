/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.mjpeg;

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
import org.gumtree.control.imp.DriveableController;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public class OldCalibrateVideoPart extends Composite {

	private static final String TEXT_SELECT_CENTRE = "Select needle points on both videos";
	private static final String TEXT_SELECT_LEFT = "Mark needle point on the left video";
	private static final String TEXT_SELECT_RIGHT = "Mark needle point on the right video";
	
	private static final Logger logger = LoggerFactory.getLogger(OldCalibrateVideoPart.class);

	private ControlHelper controlHelper;
	private MjpegViewer parentViewer;

	private Button allZerosButton;
	private Button pickBeamCentreButton;
	private Button calPhiNEButton;
	private Button pickLeftCentreButton;
	private Button calSamYButton;
	private Button pickRightCentreButton;
	
	private Button[] buttons;
	
	/**
	 * 
	 */
	public OldCalibrateVideoPart(Composite parent, int style, MjpegViewer viewer) {
		super(parent, style);
		parentViewer = viewer;
		controlHelper = ControlHelper.getInstance();

		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(this);

		final Group beamCentreGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(1).applyTo(beamCentreGroup);
		beamCentreGroup.setText("Redefine beam centre");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(beamCentreGroup);

		
		
		pickBeamCentreButton = new Button(beamCentreGroup, SWT.TOGGLE);
		pickBeamCentreButton.setText("Next");
		pickBeamCentreButton.setFont(Activator.getMiddleFont());
		pickBeamCentreButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(320, 48).applyTo(pickBeamCentreButton);
		pickBeamCentreButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Select beam centre button clicked");
				pickBeamCentre();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final Group leftVideoGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(1).applyTo(leftVideoGroup);
		leftVideoGroup.setText("Calibrate left video");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(leftVideoGroup);

		calPhiNEButton = new Button(leftVideoGroup, SWT.CHECK);
//		phiSButton.setImage(KoalaImage.PLAY48.getImage());
		calPhiNEButton.setText("2. Move Phi to 45\u00b0 and X to 10mm");
		calPhiNEButton.setFont(Activator.getMiddleFont());
		calPhiNEButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(350, 48).applyTo(calPhiNEButton);
		calPhiNEButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Phi 45 button clicked");
				driveSphiAndX();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		pickLeftCentreButton = new Button(leftVideoGroup, SWT.TOGGLE);
		pickLeftCentreButton.setText("Next");
		pickLeftCentreButton.setFont(Activator.getMiddleFont());
		pickLeftCentreButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(320, 48).applyTo(pickLeftCentreButton);
		pickLeftCentreButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Select left pointer button clicked");
				pickLeftPointer();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final Group rightVideoGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(1).applyTo(rightVideoGroup);
		rightVideoGroup.setText("Calibrate right video");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(rightVideoGroup);

		calSamYButton = new Button(rightVideoGroup, SWT.CHECK);
//		phiSButton.setImage(KoalaImage.PLAY48.getImage());
		calSamYButton.setText("3. Move X to zero and Y to 10mm");
		calSamYButton.setFont(Activator.getMiddleFont());
		calSamYButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(350, 48).applyTo(calSamYButton);
		calSamYButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Sample Y 10 button clicked");
				moveXY();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		pickRightCentreButton = new Button(rightVideoGroup, SWT.TOGGLE);
		pickRightCentreButton.setText("Next");
		pickRightCentreButton.setFont(Activator.getMiddleFont());
		pickRightCentreButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(320, 48).applyTo(pickRightCentreButton);
		pickRightCentreButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Select right pointer button clicked");
				pickRightPointer();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		parentViewer.getPanel1().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				marker1Set();
			}
			
			@Override
			public void centreSet() {
				checkCentreSet();
			}
		});

		parentViewer.getPanel2().addPanelListener(new IMjpegPanelListener() {
			
			@Override
			public void markerSet() {
				marker2Set();
			}
			
			@Override
			public void centreSet() {
				checkCentreSet();
			}
		});

		buttons = new Button[] {
				allZerosButton, 
				pickBeamCentreButton, 
				calPhiNEButton, 
				pickLeftCentreButton, 
				calSamYButton, 
				pickRightCentreButton
				};
		
	}

	class Step1 {
		Label label1;
		Button allZerosButton;
		Label label2;
		public Step1(Group parent) {
			label1 = new Label(parent, SWT.NONE);
			label1.setText("1. ");
			label1.setFont(Activator.getMiddleFont());
			GridDataFactory.swtDefaults().grab(false, false).applyTo(label1);
			
			allZerosButton = new Button(parent, SWT.PUSH);
			allZerosButton.setText("Drive");
			allZerosButton.setFont(Activator.getMiddleFont());
			allZerosButton.setCursor(Activator.getHandCursor());
			GridDataFactory.swtDefaults().grab(false, false).applyTo(allZerosButton);
			allZerosButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					logger.info("Sample XYZ zero button clicked");
					moveXYZtoZero();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			label2 = new Label(parent, SWT.NONE);
			label2.setText("2. ");
			label2.setFont(Activator.getMiddleFont());
			GridDataFactory.swtDefaults().grab(false, false).applyTo(label2);
			
		}
	}
	
	private void marker1Set() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				pickLeftCentreButton.setText("Next");
				pickLeftCentreButton.setSelection(false);
				moveToStep(4);
			}
		});
	}

	private void marker2Set() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				pickRightCentreButton.setText("Next");
				pickRightCentreButton.setSelection(false);
				finish();
			}
		});
	}

	private void checkCentreSet() {
		MjpegPanel panel1 = parentViewer.getPanel1();
		if (panel1.isCentreFixed()) {
			MjpegPanel panel2 = parentViewer.getPanel2();
			if (panel2.isCentreFixed()) {
				parentViewer.saveBeamCentres();
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						pickBeamCentreButton.setText("Next");
						pickBeamCentreButton.setSelection(false);
						moveToStep(2);
					}
				});
			}
		}
	}
	
	private void driveSphiAndX() {
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
						driveable.setTargetValue(45);
						try {
							driveable.drive();
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									moveToStep(3);
								}
							});
						} catch (final SicsException e1) {
//							e1.printStackTrace();
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									getParent().forceFocus();
								}
							});
						}
					}
				});
			}
		}
	}
	
	private void moveXYZtoZero() {
		if (controlHelper.isConnected()) {
			JobRunner.run(new ILoopExitCondition() {

				@Override
				public boolean getExitCondition() {
					return true;
				}
			}, new Runnable() {

				@Override
				public void run() {
//					try {
//						driveable.drive();
//					} catch (final SicsException e1) {
//						//							e1.printStackTrace();
//						Display.getDefault().asyncExec(new Runnable() {
//
//							@Override
//							public void run() {
//								getParent().forceFocus();
//							}
//						});
//					}
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
//							getParent().forceFocus();
							moveToStep(1);
						}
					});
				}
			});
		}
	}
	
	private void moveXY() {
		if (controlHelper.isConnected()) {
			JobRunner.run(new ILoopExitCondition() {

				@Override
				public boolean getExitCondition() {
					return true;
				}
			}, new Runnable() {

				@Override
				public void run() {
//					try {
//						driveable.drive();
//					} catch (final SicsException e1) {
//						//							e1.printStackTrace();
//						Display.getDefault().asyncExec(new Runnable() {
//
//							@Override
//							public void run() {
//								getParent().forceFocus();
//							}
//						});
//					}
					Display.getDefault().asyncExec(new Runnable() {

						@Override
						public void run() {
//							getParent().forceFocus();
							moveToStep(5);
						}
					});
				}
			});
		}
	}
	
	private void pickLeftPointer() {
		pickLeftCentreButton.setText(TEXT_SELECT_LEFT);
		pickLeftCentreButton.setSelection(true);
		parentViewer.getPanel1().setMarkerFixed(false);
		parentViewer.getPanel1().setMarkerFixed(false);
	}
	
	private void pickRightPointer() {
		pickRightCentreButton.setText(TEXT_SELECT_RIGHT);
		pickRightCentreButton.setSelection(true);
		parentViewer.getPanel2().setMarkerFixed(false);
		parentViewer.getPanel2().showMarker();
	}
	
	private void moveToStep(int id) {
		for (int i = 0; i < buttons.length; i ++) {
			if (id == i) {
				buttons[i].setEnabled(true);
			} else {
				buttons[i].setEnabled(false);
			}
		}
	}
	
	private void pickBeamCentre() {
		pickBeamCentreButton.setText(TEXT_SELECT_CENTRE);
		pickBeamCentreButton.setSelection(true);
		parentViewer.getPanel1().setCentreFixed(false);
		parentViewer.getPanel2().setCentreFixed(false);
	}
	
	public void reset() {
		moveToStep(0);
		allZerosButton.setSelection(false);
		pickBeamCentreButton.setSelection(false);
		calPhiNEButton.setSelection(false);
		pickLeftCentreButton.setSelection(false);
		calSamYButton.setSelection(false);
		pickRightCentreButton.setSelection(false);
	}
	
	public void finish() {
		reset();
		parentViewer.finishCalibration();
	}
}
