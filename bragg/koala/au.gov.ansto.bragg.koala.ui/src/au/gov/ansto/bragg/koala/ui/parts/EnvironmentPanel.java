/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsControllerAdapter;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.imp.DriveableController;
import org.gumtree.control.imp.DynamicController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public class EnvironmentPanel extends AbstractPanel {

	private static final int WIDTH_HINT = 480;
	private static final int HEIGHT_HINT = 520;
	private MainPart mainPart;
	private ControlHelper controlHelper;
	private static Logger logger = LoggerFactory.getLogger(EnvironmentPanel.class);

	private Text targetText;
	private Text valueText;
	private Button runButton;
	private KeyListener targetKeyListner;
	private SelectionListener runButtonListener;
	/**
	 * @param parent
	 * @param style
	 */
	public EnvironmentPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		controlHelper = ControlHelper.getInstance();
		
		GridLayoutFactory.fillDefaults().margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(480, 320).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		Label controllerLabel = new Label(this, SWT.NONE);
		controllerLabel.setText("Temperature Controller");
		controllerLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(controllerLabel);
		Button controllerButton = new Button(this, SWT.PUSH);
		controllerButton.setImage(KoalaImage.COBRA.getImage());
//		controllerButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).applyTo(controllerButton);
		
		final Composite statusPanel = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(8, 8).applyTo(statusPanel);
		GridDataFactory.swtDefaults().minSize(560, 240).align(SWT.CENTER, SWT.CENTER).applyTo(statusPanel);

//		Label statusLabel = new Label(statusPanel, SWT.NONE);
//		statusLabel.setFont(Activator.getMiddleFont());
//		statusLabel.setForeground(Activator.getWarningColor());
//		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).align(SWT.BEGINNING, SWT.CENTER).minSize(360, 36).applyTo(statusLabel);

		Label valueLabel = new Label(statusPanel, SWT.NONE);
		valueLabel.setText("Current value (K)");
		valueLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(200, 36).align(SWT.CENTER, SWT.CENTER).applyTo(valueLabel);
		valueText = new Text(statusPanel, SWT.READ_ONLY);
		valueText.setText("");
		valueText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(160, 36).align(SWT.CENTER, SWT.CENTER).applyTo(valueText);

		runButton = new Button(statusPanel, SWT.PUSH);
		runButton.setImage(KoalaImage.PLAY48.getImage());
		runButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, true).minSize(80, 80
				).span(1, 2).align(SWT.END, SWT.CENTER).applyTo(runButton);
		
		Label targetLabel = new Label(statusPanel, SWT.NONE);
		targetLabel.setText("Target value (K)");
		targetLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(200, 36).align(SWT.CENTER, SWT.CENTER).applyTo(targetLabel);
		targetText = new Text(statusPanel, SWT.BORDER);
		targetText.setText("");
		targetText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(160, 36).align(SWT.CENTER, SWT.CENTER).applyTo(targetText);
		
//		SimpleControlSuite controlSuite = new SimpleControlSuite(System.getProperty(ControlHelper.ENV_VALUE), 
//				valueText, System.getProperty(ControlHelper.ENV_SETPOINT), targetText, runButton, null, null, 0f);
		new TemperatureControllerHelper();
	}

	class TemperatureControllerHelper {
		
		public TemperatureControllerHelper() {
			if (controlHelper.isConnected()) {
				initialise();
			}
			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {

				@Override
				public void modelUpdated() {
					initialise();
				}
				
				@Override
				public void disconnect() {
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							valueText.setText("");
							targetText.setText("");
							if (targetKeyListner != null) {
								targetText.removeKeyListener(targetKeyListner);
							}
							if (runButtonListener != null) {
								runButton.removeSelectionListener(runButtonListener);
							}
						}
					});
				}
				
			};
			controlHelper.addProxyListener(proxyListener);
		}
		
		private void initialise() {
			String pathValues = System.getProperty(ControlHelper.ENV_VALUE);
			String[] paths = pathValues.split(",");
			ISicsController controller = null;
			for (int i = 0; i < paths.length; i++) {
				controller = SicsManager.getSicsModel().findController(paths[i]);
				if (controller != null) {
					break;
				}
			}
			final ISicsController valueController = controller;
			pathValues = System.getProperty(ControlHelper.ENV_SETPOINT);
			paths = pathValues.split(",");
			controller = null;
			for (int i = 0; i < paths.length; i++) {
				controller = SicsManager.getSicsModel().findController(paths[i]);
				if (controller != null) {
					break;
				}
			}
			final ISicsController targetController = controller;
			
			if (valueController != null && targetController != null) {
				if (valueController instanceof DynamicController && targetController instanceof DynamicController) {
					try {
						final String value = ((DynamicController) valueController).getValue().toString();
						final String target = ((DynamicController) targetController).getValue().toString();

						targetKeyListner = new KeyListener() {
							
							@Override
							public void keyReleased(KeyEvent e) {
								if (e.keyCode == SWT.LF || e.keyCode == SWT.CR || e.keyCode == 16777296) {
									logger.warn(String.format("ENTER-key pressed to drive %s to %s", targetController.getPath(), targetText.getText()));
									commitTarget(targetController);
								} 
							}
							
							@Override
							public void keyPressed(KeyEvent e) {
							}
						};
						
						runButtonListener = new SelectionListener() {
							
							@Override
							public void widgetSelected(SelectionEvent e) {
								logger.warn(String.format("run button pressed to drive %s to %s", targetController.getPath(), targetText.getText()));
								commitTarget(targetController);
							}
							
							@Override
							public void widgetDefaultSelected(SelectionEvent e) {
							}
						};

						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								valueText.setText(value);
								targetText.setText(target);
								targetText.addKeyListener(targetKeyListner);
								runButton.addSelectionListener(runButtonListener);
							}
						});
					} catch (SicsModelException e) {
					}

					valueController.addControllerListener(
							new SicsControllerAdapter() {
								
								@Override
								public void updateValue(final Object oldValue, final Object newValue) {
									Display.getDefault().asyncExec(new Runnable() {
										
										@Override
										public void run() {
											if (newValue != null) {
												valueText.setText(String.valueOf(newValue));
											} else {
												valueText.setText("");
											}
										}
									});
								}
								
							});
					targetController.addControllerListener(
							new SicsControllerAdapter() {
								
								@Override
								public void updateValue(final Object oldValue, final Object newValue) {
									Display.getDefault().asyncExec(new Runnable() {
										
										@Override
										public void run() {
											if (newValue != null) {
												targetText.setText(String.valueOf(newValue));
											} else {
												targetText.setText("");
											}
										}
									});
								}
								
							});
				}
			}
			
		}
	}
	
	private void commitTarget(final ISicsController setpointController) {
		if (controlHelper.isConnected()) {
			if (setpointController instanceof DynamicController) {
				final String text = targetText.getText();
				float value = 0;
				try {
					value = Float.valueOf(text);
				} catch (Exception e) {
					ControlHelper.experimentModel.publishErrorMessage("invalid target value, must be a number");
					return;
				}
				final float targetValue = value;
				Thread runThread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							if (setpointController instanceof DriveableController) {
								((DriveableController) setpointController).setTargetValue(targetValue);
								((DriveableController) setpointController).run();
							} else {
								((DynamicController) setpointController).setValue(targetValue);
							}
						} catch (final SicsException e1) {
							e1.printStackTrace();
							ControlHelper.experimentModel.publishErrorMessage(e1.getMessage());
						} 
					}
				});
				runThread.start();
			} else {
				ControlHelper.experimentModel.publishErrorMessage("failed to find temperature controller device");
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#next()
	 */
	@Override
	public void next() {
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#back()
	 */
	@Override
	public void back() {
		mainPart.showCurrentMainPanel();
	}

	@Override
	public void show() {
		mainPart.showPanel(this, WIDTH_HINT, HEIGHT_HINT);
		mainPart.enableBackButton();
		mainPart.disableNextButton();
		mainPart.setTitle("Sample Environment");
		mainPart.setCurrentPanelName(PanelName.ENVIRONMENT);
	}


}
