/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.sics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.imp.DriveableController;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nxi
 *
 */
public class SimpleControlSuite {

	private final static Color BUSY_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private final static Color IDLE_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	private static Logger logger = LoggerFactory.getLogger(SimpleControlSuite.class);

	private String currentPath;
	private String setpointPath;
	private Text currentControl;
	private Text setpointControl;
	private float targetValue = Float.NaN;
	private Button runButton;
	private Slider slider;
	private float sliderRange;
	private Label statusLabel;
	private boolean targetChanged;
	private ControlHelper controlHelper;
	/**
	 * 
	 */
	public SimpleControlSuite() {
		controlHelper = ControlHelper.getInstance();
	}

	public SimpleControlSuite(final String curPath, final Text current, 
			final String targetPath, final Text target, final Button run, final Label status, 
			final Slider slider, final float sliderRange) {
		this();
		this.currentPath = curPath;
		this.setpointPath = targetPath;
		currentControl = current;
		setpointControl = target;
		runButton = run;
		statusLabel = status;
		this.slider = slider;
		this.sliderRange = sliderRange;
		
		ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
			
			@Override
			public void connect() {
				final ISicsController currentController = SicsManager.getSicsModel().findController(currentPath);
				if (currentController != null) {
					if (currentController instanceof DynamicController) {
						try {
							final Object value = ((DynamicController) currentController).getValue();
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									currentControl.setText(value.toString());
								}
							});
						} catch (Exception e) {
						}
					}
					currentController.addControllerListener(new CurrentControllerListener());
				}
				
				final ISicsController setpointController = SicsManager.getSicsModel().findController(targetPath);
				if (setpointController != null) {
					if (setpointController instanceof DynamicController) {
						try {
							final Object value = ((DynamicController) setpointController).getTargetValue().getSicsString();
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									try {
										setpointControl.setText(value.toString());
										targetValue = Float.valueOf(value.toString());
									} catch (Exception e) {
										targetValue = Float.NaN;
									}
								}
							});
						} catch (Exception e) {
						}
					}
					setpointController.addControllerListener(new TargetControllerListener());
				}
			}
		};
		
		controlHelper.addProxyListener(proxyListener);
		
		setpointControl.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.LF || e.keyCode == SWT.CR || e.keyCode == 16777296) {
					logger.info(String.format("ENTER-key pressed to drive %s to %s", currentPath, setpointControl.getText()));
					commitTarget();
				} 
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		
		runButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.warn(String.format("button clicked to drive %s to %s", currentPath, setpointControl.getText()));
				commitTarget();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		if (slider != null) {
			slider.setDragDetect(false);
			slider.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (!Float.isNaN(targetValue)) {
						targetChanged = true;
						setpointControl.setText(String.valueOf(targetValue + (slider.getSelection() - 50) * sliderRange / 100));
//						commitTarget();
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			slider.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseUp(MouseEvent e) {
					if (targetChanged) {
						targetChanged = false;
						commitTarget();
					}
				}
				
				@Override
				public void mouseDown(MouseEvent e) {
				}
				
				@Override
				public void mouseDoubleClick(MouseEvent e) {
				}
			});
		}
	}
	
	private void commitTarget() {
		if (controlHelper.isConnected()) {
			slider.setEnabled(false);
			final ISicsController setpointController = 
					SicsManager.getSicsModel().findController(setpointPath);
			if (setpointController instanceof DynamicController) {
				final String value = setpointControl.getText();
				try {
					targetValue = Float.valueOf(value);
				} catch (Exception e) {
					setStatusText(statusLabel, "invalid target value, must be a number");
				}
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
//						((DynamicController) setpointController).setTargetValue(value);
//						try {
//							if (setpointController instanceof DriveableController) {
//								((DriveableController) setpointController).drive();
//							} else {
//								((DynamicController) setpointController).commitTargetValue();
//							}
//						} catch (final SicsException e1) {
//							Display.getDefault().asyncExec(new Runnable() {
//								
//								@Override
//								public void run() {
//									statusLabel.setText(e1.getMessage());
//								}
//							});
//						}
//					}
//				});
				if (statusLabel != null) {
					setStatusText(statusLabel, "");
				}
				Thread runThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						((DynamicController) setpointController).setTargetValue(value);
						try {
							if (setpointController instanceof DriveableController) {
								((DriveableController) setpointController).drive();
							} else {
								((DynamicController) setpointController).commitTargetValue();
							}
						} catch (final SicsException e1) {
							e1.printStackTrace();
							setStatusText(statusLabel, e1.getMessage());
						} finally {
							resetSlider();
						}
					}
				});
				runThread.start();
			} else {
				setStatusText(statusLabel, "failed to find device: " + setpointPath);
			}
		}
	}
	
	private void resetSlider() {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				slider.setSelection(50);
				slider.setEnabled(true);
			}
		});
	}
	
	private void setStatusText(final Label statusLabel, final String text) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				if (statusLabel != null) {
					statusLabel.setText(text);
				} else {
					ControlHelper.experimentModel.publishErrorMessage(text);
				}
//				statusLabel.forceFocus();
//				statusLabel.getParent().forceFocus();
//				statusLabel.update();
//				statusLabel.setRedraw(true);
				if (runButton != null) {
					runButton.forceFocus();
				}
			}
		});
	}
	
	class CurrentControllerListener implements ISicsControllerListener {

		private Object currentValueObj;
		
		@Override
		public void updateState(final ControllerState oldState, final ControllerState newState) {
		}
		
		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			if (newValue != null && !newValue.toString().equals(currentValueObj)) {
				currentValueObj = newValue.toString();
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						currentControl.setText(String.valueOf(newValue));
					}
				});
			}
		}
		@Override
		public void updateEnabled(boolean isEnabled) {
		}

		@Override
		public void updateTarget(final Object oldValue, final Object newValue) {
		}
	}
	
	class TargetControllerListener implements ISicsControllerListener {

		@Override
		public void updateState(final ControllerState oldState, final ControllerState newState) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					if (newState == ControllerState.BUSY) {
						currentControl.setForeground(BUSY_COLOR);
						runButton.setEnabled(false);
					} else {
						currentControl.setForeground(IDLE_COLOR);
						runButton.setEnabled(true);
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
		public void updateTarget(final Object oldValue, final Object newValue) {
			if (newValue != null) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						try {
							setpointControl.setText(newValue.toString());
							targetValue = Float.valueOf(newValue.toString());
						} catch (Exception e) {
							targetValue = Float.NaN;
						}
					}
				});
			}
		}
		
	}
	
}
