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
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsInterruptException;
import org.gumtree.control.imp.DriveableController;
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
	private static final String TEXT_DRIVE_PHI = "Phi to 45\u00b0";
	private static final String TEXT_MARK_LEFT = "Mark sample centre on left video";
	private static final String TEXT_MARK_RIGHT = "Mark sample centre on right video";
	private static final String TEXT_DRIVE_ALIGN = "sample to beam centre";
	private static final String SX_NAME = System.getProperty(ControlHelper.SX_PATH);
	private static final String SY_NAME = System.getProperty(ControlHelper.SY_PATH);
	private static final float SPHI_ANGLE = 45;
	
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
		steps = new IStep[4];

		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(1, 2).applyTo(this);

		final Group phiGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(phiGroup);
		phiGroup.setText("Prepare sample Phi");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(phiGroup);

		
		steps[0] = new StepDrive(phiGroup, 1, TEXT_DRIVE_PHI, new IDrivable() {
			
			@Override
			public void drive() {
				drivePhi();
			}
		});

		final Group markerGroup = new Group(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).numColumns(4).applyTo(markerGroup);
		markerGroup.setText("Add markers");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(markerGroup);

		steps[1] = new StepMark(markerGroup, 2, TEXT_MARK_LEFT);
		steps[2] = new StepMark(markerGroup, 3, TEXT_MARK_RIGHT);
		
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

		steps[3] = new StepDrive(alignGroup, 4, TEXT_DRIVE_ALIGN, new IDrivable() {
			
			@Override
			public void drive() {
				driveAlign();
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
		
		public StepDrive(Group parent, int id, String text, IDrivable drivable) {
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
				steps[1].finish();
				moveToStep(2);
			}
		});
	}

	private void marker2Set() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				steps[2].finish();
				moveToStep(3);
			}
		});
	}

	
	private void drivePhi() {
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
						driveable.setTargetValue(SPHI_ANGLE);
						try {
							driveable.drive();
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									steps[0].finish();
									moveToStep(1);
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
	
	private void driveAlign() {
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
						
						float curX = Float.valueOf(sx.getValue().toString());
						MjpegPanel p1 = parentViewer.getPanel1();
						Point centre = p1.getBeamCentre();
						Point marker = p1.getMarkerCoordinate();
						double mmPerPixelX = parentViewer.getMmPerPixelX();
						float endX =  Double.valueOf(mmPerPixelX * (marker.x - centre.x) - curX).floatValue();
						
						float curY = Float.valueOf(sy.getValue().toString());
						MjpegPanel p2 = parentViewer.getPanel2();
						centre = p2.getBeamCentre();
						marker = p2.getMarkerCoordinate();
						double mmPerPixelY = parentViewer.getMmPerPixelY();
						float endY =  Double.valueOf(mmPerPixelY * (marker.x - centre.x) - curY).floatValue();
						
						final Map<String, Number> devices = new HashMap<String, Number>();
						devices.put(SX_NAME, endX);
						devices.put(SY_NAME, endY);

						ControlHelper.syncMultiDrive(devices);
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								steps[3].finish();
								finish();
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
			if (id == 1) {
				parentViewer.getPanel1().setMarkerFixed(false);
			} else if (id == 2) {
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
