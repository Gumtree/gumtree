package au.gov.ansto.bragg.koala.ui.parts;

import java.util.Timer;
import java.util.TimerTask;

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
import org.eclipse.swt.widgets.ProgressBar;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsControllerAdapter;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.AbstractExpPanel.InstrumentPhase;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

public class ScanStatusPart {

	private ControlHelper controlHelper;
	private Label erasureButton;
	private Label expoButton;
	private Label readButton;
	private Button endButton;
	private Label proLabel;
	private ProgressBar proBar;

	private Timer statusTimer;
	private int totalTimeExp;
	private long finishTimeExp;
	private String scanStatus = "";


	public ScanStatusPart(Composite parent) {
		controlHelper = ControlHelper.getInstance();
		final Group phasePart = new Group(parent, SWT.NONE);
	    phasePart.setText("Instrument Phase");
	    GridLayoutFactory.fillDefaults().numColumns(3).margins(4, 4).applyTo(phasePart);
	    GridDataFactory.fillDefaults().grab(false, false).applyTo(phasePart);
	    
	    erasureButton = new Label(phasePart, SWT.BORDER);
	    erasureButton.setText(" Erasure");
	    erasureButton.setFont(Activator.getMiddleFont());
	    erasureButton.setBackground(Activator.getLightColor());
	    GridDataFactory.fillDefaults().grab(true, false).minSize(160, 32).applyTo(erasureButton);

	    expoButton = new Label(phasePart, SWT.BORDER);
	    expoButton.setText(" Exposure");
	    expoButton.setFont(Activator.getMiddleFont());
	    GridDataFactory.fillDefaults().grab(true, false).minSize(160, 32).applyTo(expoButton);

	    readButton = new Label(phasePart, SWT.BORDER);
	    readButton.setText(" Reading");
	    readButton.setFont(Activator.getMiddleFont());
	    GridDataFactory.fillDefaults().grab(true, false).minSize(160, 32).applyTo(readButton);

	    proLabel = new Label(phasePart, SWT.NONE);
//	    proLabel.setText("Running an Experiment");
	    proLabel.setForeground(Activator.getBusyColor());
	    proLabel.setFont(Activator.getMiddleFont());
	    GridDataFactory.fillDefaults().grab(false, false).span(3, 1).hint(360, SWT.DEFAULT).applyTo(proLabel);
	    
		proBar = new ProgressBar(phasePart, SWT.HORIZONTAL);
		proBar.setMaximum(100);
		proBar.setMinimum(0);
		proBar.setEnabled(false);
//		proBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(proBar);

		final Group controlPart = new Group(parent, SWT.NONE);
		controlPart.setText("Control");
	    GridLayoutFactory.fillDefaults().numColumns(2).margins(4, 4).applyTo(controlPart);
	    GridDataFactory.fillDefaults().grab(false, false).applyTo(controlPart);
	    
	    endButton = new Button(controlPart, SWT.PUSH);
	    endButton.setImage(KoalaImage.SKIP48.getImage());
	    endButton.setText("End Exposure");
	    endButton.setEnabled(false);
	    endButton.setFont(Activator.getMiddleFont());
	    endButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.FILL, SWT.CENTER)
			.hint(230, 80).applyTo(endButton);
		
		endButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				JobRunner.run(new ILoopExitCondition() {
					
					@Override
					public boolean getExitCondition() {
						return true;
					}
				}, new Runnable() {
					
					@Override
					public void run() {
						try {
							ControlHelper.asyncExec("collect stop");
						} catch (KoalaServerException e1) {
							e1.printStackTrace();
						}
					}
				});
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	    final Button abortButton = new Button(controlPart, SWT.PUSH);
	    abortButton.setImage(KoalaImage.STOP48.getImage());
	    abortButton.setText("Abort Experiment");
	    abortButton.setFont(Activator.getMiddleFont());
	    abortButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER)
			.hint(256, 80).applyTo(abortButton);

		abortButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				JobRunner.run(new ILoopExitCondition() {
					
					@Override
					public boolean getExitCondition() {
						return true;
					}
				}, new Runnable() {
					
					@Override
					public void run() {
						try {
							ControlHelper.interrupt();
						} catch (KoalaServerException e1) {
							e1.printStackTrace();
						}
					}
				});
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		new StatusControl();

		statusTimer = new Timer(true);
		statusTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				updateProgressBar();
			}
		}, 1000, 1000);

	}

	
	class StatusControl {
		
		ISicsController phiController;
		ISicsController stepController;
		ISicsController fnController;
		ISicsController phaseController;
		ISicsController gumtreeStatusController;
		ISicsController gumtreeTimeController;
		
		boolean initialised = false;
		
		public StatusControl() {
			
			if (controlHelper.isConnected()) {
				initControllers();
			}
			
			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
				
				@Override
				public void connect() {
					if (!initialised) {
						initControllers();
					}
				}
			};
			controlHelper.addProxyListener(proxyListener);
		}
		
		public void initControllers() {

			phaseController = SicsManager.getSicsModel().findControllerByPath(
					System.getProperty(ControlHelper.PHASE_PATH));
			gumtreeStatusController = SicsManager.getSicsModel().findControllerByPath(
					System.getProperty(ControlHelper.GUMTREE_STATUS_PATH));
			gumtreeTimeController = SicsManager.getSicsModel().findControllerByPath(
					System.getProperty(ControlHelper.GUMTREE_TIME_PATH));
			
			if (phaseController != null) {
				phaseController.addControllerListener(new PhaseListener());
			}
			if (gumtreeStatusController != null) {
				gumtreeStatusController.addControllerListener(new SicsControllerAdapter() {
					
					@Override
					public void updateValue(final Object oldValue, final Object newValue) {
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								scanStatus = String.valueOf(newValue);
								proLabel.setText(scanStatus);
							}
						});
						
					}

				});
			}
			if (gumtreeTimeController != null) {
				gumtreeTimeController.addControllerListener(new TimeExpectation());
			}
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						if (phaseController != null) {
							setPhase(String.valueOf(
									((DynamicController) phaseController).getValue()));
						}
						if (gumtreeStatusController != null) {
							scanStatus = String.valueOf(String.valueOf(
									((DynamicController) gumtreeStatusController).getValue()));
							proLabel.setText(scanStatus);
						} 
						if (gumtreeTimeController != null) {
							int totalTime = Integer.valueOf(
									((DynamicController) gumtreeStatusController).getValue().toString());
							if (totalTime >= 0) {
								totalTimeExp = totalTime;
								finishTimeExp = System.currentTimeMillis() + totalTime * 1000;
								proBar.setEnabled(true);
								updateProgressBar();
							} else {
								totalTimeExp = -1;
								proBar.setSelection(0);
								proBar.setEnabled(false);
							}	
						}
					} catch (Exception e) {
					}
				}
			});
			initialised = true;
		}
	}
	
	private void updateProgressBar() {
		if (totalTimeExp > 0) {
			long cur = System.currentTimeMillis();
			final long toGo = finishTimeExp - cur;
			if (toGo >= 0) {
				final int proVal = 100 - Double.valueOf(toGo / (totalTimeExp * 10.)).intValue();
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						String statusValue = String.format("%s remaining time = %d", scanStatus, toGo / 1000);
						proLabel.setText(statusValue);
						proBar.setSelection(proVal);
					}
				});
			}
		}
	}
	
	private void setPhase(String phase) {
		if (phase != null) {
			phase = phase.toUpperCase();
		}
		if (InstrumentPhase.ERASURE.name().equals(phase)) {
			erasureButton.setBackground(Activator.getRunningBackgoundColor());
			erasureButton.setForeground(Activator.getRunningForgroundColor());
			expoButton.setBackground(Activator.getBackgroundColor());
			expoButton.setForeground(Activator.getLightForgroundColor());
			readButton.setBackground(Activator.getBackgroundColor());
			readButton.setForeground(Activator.getLightForgroundColor());
			endButton.setEnabled(false);
		} else if (InstrumentPhase.EXPOSURE.name().equals(phase)) {
			erasureButton.setBackground(Activator.getBackgroundColor());
			erasureButton.setForeground(Activator.getLightForgroundColor());
			expoButton.setBackground(Activator.getRunningBackgoundColor());
			expoButton.setForeground(Activator.getRunningForgroundColor());
			readButton.setBackground(Activator.getBackgroundColor());
			readButton.setForeground(Activator.getLightForgroundColor());
			endButton.setEnabled(true);
		} else if (InstrumentPhase.READING.name().equals(phase)) {
			erasureButton.setBackground(Activator.getBackgroundColor());
			erasureButton.setForeground(Activator.getLightForgroundColor());
			expoButton.setBackground(Activator.getBackgroundColor());
			expoButton.setForeground(Activator.getLightForgroundColor());
			readButton.setBackground(Activator.getRunningBackgoundColor());
			readButton.setForeground(Activator.getRunningForgroundColor());
			endButton.setEnabled(false);
		} else {
			erasureButton.setBackground(Activator.getBackgroundColor());
			erasureButton.setForeground(Activator.getLightForgroundColor());
			expoButton.setBackground(Activator.getBackgroundColor());
			expoButton.setForeground(Activator.getLightForgroundColor());
			readButton.setBackground(Activator.getBackgroundColor());
			readButton.setForeground(Activator.getLightForgroundColor());
			endButton.setEnabled(false);
		}
	}
	

	class PhaseListener implements ISicsControllerListener {
		
		@Override
		public void updateState(ControllerState oldState, ControllerState newState) {
		}

		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					setPhase(String.valueOf(newValue));
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
	
	class TimeExpectation extends SicsControllerAdapter {
		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
//			super.updateValue(oldValue, newValue);
			final int totalTime = Integer.valueOf(newValue.toString());
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					if (totalTime > 0) {
						totalTimeExp = totalTime;
						finishTimeExp = System.currentTimeMillis() + totalTime * 1000;
						proBar.setEnabled(true);
						updateProgressBar();
					} else {
						totalTimeExp = -1;
						proBar.setSelection(0);
						proBar.setEnabled(false);
					}					
				}
			});
		}
	}

}
