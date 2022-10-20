package au.gov.ansto.bragg.koala.ui.parts;

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
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsControllerAdapter;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.RecurrentScheduler.IRecurrentTask;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.sics.CollectionHelper;
import au.gov.ansto.bragg.koala.ui.sics.CollectionHelper.ICollectionListener;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper.InstrumentPhase;

public class ScanStatusPart {

	private static final Logger logger = LoggerFactory.getLogger(ScanStatusPart.class);
	
	private ControlHelper controlHelper;
	private Label erasureButton;
	private Label expoButton;
	private Label readButton;
	private Button endButton;
	private Label proLabel;
	private ProgressBar proBar;

	private int totalTimeExp;
	private long finishTimeExp;
	private String scanStatus = "";

	protected MainPart mainPart;

	public ScanStatusPart(final Composite parent, final MainPart main) {
		mainPart = main;
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
				logger.info("End Exposure button clicked");
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
				logger.info("Abort Experiment button clicked");
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

//		statusTimer = new Timer(true);
//		statusTimer.schedule(new TimerTask() {
//			
//			@Override
//			public void run() {
//				if (!parent.isDisposed()) {
//					try {
//						updateProgressBar();
//					} catch (Exception e) {
//					}
//				} else {
//					this.cancel();
//				}
//			}
//		}, 1000, 1000);
		mainPart.getRecurrentScheduler().addTask(new IRecurrentTask() {
			
			@Override
			public void run() {
				final IRecurrentTask task = this;
				
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if (!parent.isDisposed()) {
							try {
								updateProgressBar();
							} catch (Exception e) {
							}
						} else {
							mainPart.getRecurrentScheduler().removeTask(task);
						}
					}
				});
			}
		});

	}

	
	class StatusControl {
		
		ISicsController phiController;
		ISicsController stepController;
		ISicsController fnController;
//		ISicsController phaseController;
		ISicsController gumtreeStatusController;
//		ISicsController gumtreeTimeController;
		
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

//			phaseController = SicsManager.getSicsModel().findController(
//					System.getProperty(ControlHelper.PHASE_PATH));
			gumtreeStatusController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.GUMTREE_STATUS_PATH));
//			gumtreeTimeController = SicsManager.getSicsModel().findController(
//					System.getProperty(ControlHelper.GUMTREE_TIME_PATH));
			
//			if (phaseController != null) {
//				phaseController.addControllerListener(new PhaseListener());
//			}
			CollectionHelper.getInstance().addCollectionListener(new PhaseListener());
			
			if (gumtreeStatusController != null) {
				gumtreeStatusController.addControllerListener(new SicsControllerAdapter() {
					
					@Override
					public void updateValue(final Object oldValue, final Object newValue) {
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								scanStatus = String.valueOf(newValue);
								setStatusText(proLabel, scanStatus);
							}
						});
						
					}

				});
			}
//			if (gumtreeTimeController != null) {
//				gumtreeTimeController.addControllerListener(new TimeExpectation());
//			}
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
//						if (phaseController != null) {
//							setPhase(String.valueOf(
//									((DynamicController) phaseController).getValue()));
//						}
						setPhase(CollectionHelper.getInstance().getPhase());
//						if (gumtreeStatusController != null) {
//							scanStatus = String.valueOf(String.valueOf(
//									((DynamicController) gumtreeStatusController).getValue()));
//							setStatusText(proLabel, scanStatus);
//						} 
						setStatusText(proLabel, scanStatus);
//						if (gumtreeTimeController != null) {
//							int totalTime = Integer.valueOf(
//									((DynamicController) gumtreeStatusController).getValue().toString());
//							if (totalTime > 0) {
//								totalTimeExp = totalTime;
//								finishTimeExp = System.currentTimeMillis() + totalTime * 1000;
//								proBar.setEnabled(true);
//								updateProgressBar();
//							} else {
//								totalTimeExp = -1;
//								proBar.setSelection(0);
//								proBar.setEnabled(false);
//							}	
//						}
					} catch (Exception e) {
					}
				}
			});
			initialised = true;
		}
	}
	
	private void setStatusText(Label statusLabel, String text) {
		if (text.equalsIgnoreCase("interrupted") || text.equalsIgnoreCase("error")) {
			statusLabel.setForeground(Activator.getHighlightColor());
		} else if (text.equalsIgnoreCase("idle")) {
			statusLabel.setForeground(Activator.getIdleColor());
		} else {
			statusLabel.setForeground(Activator.getBusyColor());
		}
		statusLabel.setText(text);
	}
	
	private void updateProgressBar() {
		if (totalTimeExp > 0) {
			long cur = System.currentTimeMillis();
			final long toGo = finishTimeExp - cur;
			if (toGo >= 0) {
				final int proVal = 100 - Double.valueOf(toGo / (totalTimeExp * 10.)).intValue();
//				Display.getDefault().asyncExec(new Runnable() {
//
//					@Override
//					public void run() {
//						String statusValue = String.format("%s remaining time = %s", scanStatus, 
//								getTimeString(toGo / 1000));
//						setStatusText(proLabel, statusValue);
//						proBar.setSelection(proVal);
//					}
//				});
				String statusValue = String.format("%s remaining time = %s", scanStatus, 
						getTimeString(toGo / 1000));
				setStatusText(proLabel, statusValue);
				proBar.setSelection(proVal);
			}
		} else {
			setStatusText(proLabel, scanStatus);
		}
	}
	
	private void setPhase(InstrumentPhase phase) {
		scanStatus = phase.name();
		if (InstrumentPhase.ERASE.equals(phase)) {
			erasureButton.setBackground(Activator.getRunningBackgoundColor());
			erasureButton.setForeground(Activator.getRunningForgroundColor());
			expoButton.setBackground(Activator.getBackgroundColor());
			expoButton.setForeground(Activator.getLightForgroundColor());
			readButton.setBackground(Activator.getBackgroundColor());
			readButton.setForeground(Activator.getLightForgroundColor());
			endButton.setEnabled(false);
		} else if (InstrumentPhase.EXPOSE.equals(phase)) {
			erasureButton.setBackground(Activator.getBackgroundColor());
			erasureButton.setForeground(Activator.getLightForgroundColor());
			expoButton.setBackground(Activator.getRunningBackgoundColor());
			expoButton.setForeground(Activator.getRunningForgroundColor());
			readButton.setBackground(Activator.getBackgroundColor());
			readButton.setForeground(Activator.getLightForgroundColor());
			endButton.setEnabled(true);
		} else if (InstrumentPhase.READ.equals(phase)) {
			erasureButton.setBackground(Activator.getBackgroundColor());
			erasureButton.setForeground(Activator.getLightForgroundColor());
			expoButton.setBackground(Activator.getBackgroundColor());
			expoButton.setForeground(Activator.getLightForgroundColor());
			readButton.setBackground(Activator.getRunningBackgoundColor());
			readButton.setForeground(Activator.getRunningForgroundColor());
			endButton.setEnabled(false);
		} else if (InstrumentPhase.ERROR.equals(phase)) {
			erasureButton.setBackground(Activator.getBackgroundColor());
			erasureButton.setForeground(Activator.getLightForgroundColor());
			expoButton.setBackground(Activator.getBackgroundColor());
			expoButton.setForeground(Activator.getLightForgroundColor());
			readButton.setBackground(Activator.getBackgroundColor());
			readButton.setForeground(Activator.getLightForgroundColor());
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
	

	class PhaseListener implements ICollectionListener {

		@Override
		public void phaseChanged(final InstrumentPhase newPhase, final int timeCost) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					setPhase(newPhase);
					if (timeCost > 0) {
						totalTimeExp = timeCost;
						finishTimeExp = System.currentTimeMillis() + timeCost * 1000;
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

		@Override
		public void collectionStarted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void collectionFinished() {
			// TODO Auto-generated method stub
			
		}
		
		
	}
	
//	class TimeExpectation extends SicsControllerAdapter {
//		@Override
//		public void updateValue(final Object oldValue, final Object newValue) {
////			super.updateValue(oldValue, newValue);
//			final int totalTime = Integer.valueOf(newValue.toString());
//			Display.getDefault().asyncExec(new Runnable() {
//				
//				@Override
//				public void run() {
//					if (totalTime > 0) {
//						totalTimeExp = totalTime;
//						finishTimeExp = System.currentTimeMillis() + totalTime * 1000;
//						proBar.setEnabled(true);
//						updateProgressBar();
//					} else {
//						totalTimeExp = -1;
//						proBar.setSelection(0);
//						proBar.setEnabled(false);
//					}					
//				}
//			});
//		}
//	}
	
	public static String getTimeString(long seconds) {
		if (seconds > 3600) {
			return String.valueOf(seconds / 3600) + "h " + getTimeString(seconds % 3600);
		} else if (seconds > 60) {
			return String.valueOf(seconds / 60) + "m " + getTimeString(seconds % 60);
		} else {
			return String.valueOf(seconds) + "s";
		}
	}

}
