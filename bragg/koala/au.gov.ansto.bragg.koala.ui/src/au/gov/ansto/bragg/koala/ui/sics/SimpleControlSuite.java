/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.sics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
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
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;

/**
 * @author nxi
 *
 */
public class SimpleControlSuite {

	private final static Color BUSY_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_GREEN);
	private final static Color IDLE_COLOR = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);

	private String currentPath;
	private String setpointPath;
	private Text currentControl;
	private Text setpointControl;
	private Button runButton;
	private ControlHelper controlHelper;
	/**
	 * 
	 */
	public SimpleControlSuite() {
		controlHelper = ControlHelper.getInstance();
	}

	public SimpleControlSuite(final String curPath, final Text current, 
			final String targetPath, final Text target, final Button run) {
		this();
		this.currentPath = curPath;
		this.setpointPath = targetPath;
		currentControl = current;
		setpointControl = target;
		runButton = run;
		
		ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
			
			@Override
			public void connect() {
				final ISicsController currentController = SicsManager.getSicsModel().findControllerByPath(currentPath);
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
				
				final ISicsController setpointController = SicsManager.getSicsModel().findControllerByPath(targetPath);
				if (setpointController != null) {
					if (setpointController instanceof DynamicController) {
						try {
							final Object value = ((DynamicController) setpointController).getValue();
							Display.getDefault().asyncExec(new Runnable() {
								
								@Override
								public void run() {
									setpointControl.setText(value.toString());
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
				commitTarget();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	private void commitTarget() {
		if (controlHelper.isConnected()) {
			final ISicsController setpointController = SicsManager.getSicsModel().findControllerByPath(setpointPath);
			if (setpointController instanceof DynamicController) {
				final String value = setpointControl.getText();
				JobRunner.run(new ILoopExitCondition() {
					
					@Override
					public boolean getExitCondition() {
						return true;
					}
				}, new Runnable() {
					
					@Override
					public void run() {
						((DynamicController) setpointController).setTargetValue(value);
						try {
							if (setpointController instanceof DriveableController) {
								((DriveableController) setpointController).drive();
							} else {
								((DynamicController) setpointController).commitTargetValue();
							}
						} catch (SicsException e1) {
							e1.printStackTrace();
						}
					}
				});
			}
		}
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
		public void updateState(ControllerState oldState, ControllerState newState) {
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
		public void updateValue(Object oldValue, Object newValue) {
			if (newValue != null) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						setpointControl.setText(String.valueOf(newValue));
					}
				});
			}
		}

		@Override
		public void updateEnabled(boolean isEnabled) {
		}

		@Override
		public void updateTarget(Object oldValue, Object newValue) {
		}
		
	}
	
}
