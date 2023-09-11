/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.imp.DriveableController;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
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
public class SampleChiPanel extends AbstractPanel {

	private static final int WIDTH_HINT = 480;
	private static final int HEIGHT_HINT = 520;
	private MainPart mainPart;
	private ControlHelper controlHelper;
	private static Logger logger = LoggerFactory.getLogger(SampleChiPanel.class);

	private Label chiStatusLabel;
	private Button chiZeroButton;
	private Button chiHighButton;
	private Button chiApplyButton;
	private ChiControllerListener chiListener;

	/**
	 * @param parent
	 * @param style
	 */
	public SampleChiPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		controlHelper = ControlHelper.getInstance();
		
		GridLayoutFactory.fillDefaults().margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(480, 320).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
			
		final Group innerBlock = new Group(this, SWT.NULL);
		innerBlock.setText(Activator.CHI + " position");
		innerBlock.setFont(Activator.getMiddleFont());
		GridLayoutFactory.fillDefaults().numColumns(2).margins(16, 16).spacing(4, 20).applyTo(innerBlock);
		innerBlock.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));

		chiStatusLabel = new Label(innerBlock, SWT.NONE);
		chiStatusLabel.setFont(Activator.getMiddleFont());
		chiStatusLabel.setForeground(Activator.getHighlightColor());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(chiStatusLabel);
		
		chiZeroButton = new Button(innerBlock, SWT.CHECK);
		chiZeroButton.setText("Zero");
		chiZeroButton.setFont(Activator.getMiddleFont());
		chiZeroButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(240, 40).applyTo(chiZeroButton);
		chiZeroButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Zero Chi button clicked");
				chiHighButton.setSelection(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		chiHighButton = new Button(innerBlock, SWT.CHECK);
		chiHighButton.setText("High");
		chiHighButton.setFont(Activator.getMiddleFont());
		chiHighButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(240, 40).applyTo(chiHighButton);
		chiHighButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("High Chi button clicked");
				chiZeroButton.setSelection(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		chiApplyButton = new Button(innerBlock, SWT.PUSH);
		chiApplyButton.setImage(KoalaImage.PLAY48.getImage());
		chiApplyButton.setText("Drive");
		chiApplyButton.setFont(Activator.getMiddleFont());
		chiApplyButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.CENTER, SWT.CENTER).span(2, 1).hint(480, 64).applyTo(chiApplyButton);
		chiApplyButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Drive Chi button clicked");
				if (chiZeroButton.getSelection()) {
					driveSchi(0);					
				} else if (chiHighButton.getSelection()) {
					driveSchi(90);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		new ChiControlSuite();
	}

	private void setChiStatusText(final Label statusLabel, final String text) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				statusLabel.setText(text);
			}
		});
	}

	private void driveSchi(final float value) {
		if (controlHelper.isConnected()) {
			final ISicsController schiController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.SAMPLE_CHI));
			if (schiController instanceof DriveableController) {
				setChiStatusText(chiStatusLabel, "");
				final DriveableController driveable = (DriveableController) schiController;
				JobRunner.run(new ILoopExitCondition() {
					
					@Override
					public boolean getExitCondition() {
						return true;
					}
				}, new Runnable() {
					
					@Override
					public void run() {
						driveable.setTargetValue(value);
						try {
							driveable.drive();
						} catch (SicsException e1) {
							e1.printStackTrace();
							chiListener.updateState(null, null);
							ControlHelper.experimentModel.publishErrorMessage(e1.getMessage());
//							setChiStatusText(chiStatusLabel, e1.getMessage());
//							mainPart.popupError(e1.getMessage());
						}
					}
				});
			} else {
				ControlHelper.experimentModel.publishErrorMessage("failed to find sample Chi motor.");
			}
		}
	}
	
	class ChiControlSuite {
		
		public ChiControlSuite() {
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
					chooseRange(null);
				}
				
			};
			controlHelper.addProxyListener(proxyListener);
		}
		
		private void initialise() {
			final ISicsController chiController = ControlHelper.getProxy().getSicsModel().findController(
					System.getProperty(ControlHelper.SAMPLE_CHI));
			if (chiController != null) {
				if (chiController instanceof DriveableController) {
					try {
						float value = (Float) ((DriveableController) chiController).getValue();
						double precision = ((DriveableController) chiController).getPrecision();
						if (Double.isNaN(precision)) {
							precision = 0;
						} else {
							precision = Math.abs(precision);
						}
						if (inRange(value, precision, 0)) {
							chooseRange(chiZeroButton);
						} else if (inRange(value, precision, 90)) {
							chooseRange(chiHighButton);
						} else {
							chooseRange(null);
						}
					} catch (SicsModelException e) {
					}
					chiListener = new ChiControllerListener((DriveableController) chiController);
					chiController.addControllerListener(chiListener);
				}
			}
		}
	}

	private boolean inRange(float value, double precision, float range) {
		if (value >= range - precision && value <= range + precision) {
			return true;
		} else {
			return false;
		}
	}
	
	private void chooseRange(final Button rangeButton) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				chiZeroButton.setSelection(false);
				chiHighButton.setSelection(false);
				if (rangeButton != null) {
					rangeButton.setSelection(true);
				}
			}
		});
	}

	class ChiControllerListener implements ISicsControllerListener {

		private DriveableController chiController;
		
		public ChiControllerListener(DriveableController controller) {
			chiController = controller;
		}
		
		@Override
		public void updateState(final ControllerState oldState, final ControllerState newState) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					if (newState == ControllerState.BUSY) {
						chiZeroButton.setEnabled(false);
						chiHighButton.setEnabled(false);
						chiApplyButton.setEnabled(false);
					} else {
						try {
							float value = (Float) chiController.getValue();
							double precision = chiController.getPrecision();
							if (Double.isNaN(precision)) {
								precision = 0;
							} else {
								precision = Math.abs(precision);
							}
							if (inRange(value, precision, 0)) {
								chooseRange(chiZeroButton);
							} else if (inRange(value, precision, 90)) {
								chooseRange(chiHighButton);
							} else {
								chooseRange(null);
							}						
						} catch (SicsModelException e) {
							e.printStackTrace();
						}
						chiZeroButton.setEnabled(true);
						chiHighButton.setEnabled(true);
						chiApplyButton.setEnabled(true);
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
		public void updateTarget(Object oldValue, Object newValue) {
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
//		mainPart.enableBackButton();
//		mainPart.disableNextButton();
		mainPart.setTitle("Sample Chi Position");
		mainPart.setCurrentPanelName(PanelName.ENVIRONMENT);
	}


}
