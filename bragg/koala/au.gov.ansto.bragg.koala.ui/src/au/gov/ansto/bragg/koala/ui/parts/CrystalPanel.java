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
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
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
import au.gov.ansto.bragg.koala.ui.mjpeg.MjpegViewer;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;
import au.gov.ansto.bragg.koala.ui.sics.SimpleControlSuite;

/**
 * @author nxi
 *
 */
public class CrystalPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 2200;
	private static final int HEIGHT_HINT = 1080;
	private static final int WIDTH_HINT_SMALL = 1560;
	private static final int HEIGHT_HINT_SMALL = 720;
	private static Logger logger = LoggerFactory.getLogger(CrystalPanel.class);
	private MainPart mainPart;
	private int panelWidth;
	private int panelHeight;
	private TabFolder tabFolder;
	private MjpegViewer mjpegViewer;
	private Label chiStatusLabel;
	private Button chiZeroButton;
	private Button chiHighButton;
	private Button chiApplyButton;
	private ControlHelper controlHelper;
	
	/**
	 * @param parent
	 * @param style
	 */
	public CrystalPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		
		if (Activator.getMonitorWidth() < 2500) {
			panelWidth = WIDTH_HINT_SMALL;
			panelHeight = HEIGHT_HINT_SMALL;
		} else {
			panelWidth = WIDTH_HINT;
			panelHeight = HEIGHT_HINT;			
		}

		controlHelper = ControlHelper.getInstance();
		GridLayoutFactory.fillDefaults().margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(panelWidth, panelHeight).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
//		final Label titleLabel = new Label(this, SWT.NONE);
//		titleLabel.setText("Crystal Mounting");
//		titleLabel.setFont(Activator.getLargeFont());
//		GridDataFactory.fillDefaults().grab(true, false).minSize(320, 36).applyTo(titleLabel);
		
		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 64).applyTo(tabFolder);
		
		final TabItem envItem = new TabItem(tabFolder, SWT.NULL);
		envItem.setText("Environment  ");
		envItem.setImage(KoalaImage.TEMPERATURE64.getImage());
	    
		final EnvironmentPanel envPanel = new EnvironmentPanel(tabFolder, SWT.NONE, null);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(envPanel);
		envItem.setControl(envPanel);
		
		final TabItem phiItem = new TabItem(tabFolder, SWT.NULL);
		phiItem.setText("Phi Setup    ");
		phiItem.setImage(KoalaImage.PHI64.getImage());
		
		final Composite interBlock = new Composite(tabFolder, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(interBlock);
		
		final Group phiBlock = new Group(interBlock, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(8, 8).applyTo(phiBlock);
//		GridDataFactory.swtDefaults().grab(true, true).hint(600, 480).align(SWT.CENTER, SWT.CENTER).applyTo(phiBlock);
		phiBlock.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
		
		final Label phiStatusLabel = new Label(phiBlock, SWT.NONE);
		phiStatusLabel.setFont(Activator.getMiddleFont());
		phiStatusLabel.setForeground(Activator.getHighlightColor());
		GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(phiStatusLabel);
		
		final Label curLabel = new Label(phiBlock, SWT.NONE);
		curLabel.setText("Current phi value (\u00b0)");
		curLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(320, 40).applyTo(curLabel);
		
		final Text curText = new Text(phiBlock, SWT.READ_ONLY);
		curText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(240, 40).applyTo(curText);
		
		final Button driveButton = new Button(phiBlock, SWT.PUSH);
		driveButton.setImage(KoalaImage.PLAY48.getImage());
		driveButton.setText("Drive");
		driveButton.setFont(Activator.getMiddleFont());
		driveButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).span(1, 3).hint(240, 64).applyTo(driveButton);

		final Label tarLabel = new Label(phiBlock, SWT.NONE);
		tarLabel.setText("Target phi value (\u00b0)");
		tarLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(320, 40).applyTo(tarLabel);
		
		final Text tarText = new Text(phiBlock, SWT.BORDER);
		tarText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(240, 40).applyTo(tarText);
		
		final Slider slider = new Slider(phiBlock, SWT.HORIZONTAL);
//	    slider.setBounds(0, 0, 40, 200);
	    slider.setMaximum(100);
	    slider.setMinimum(0);
	    slider.setIncrement(1);
	    slider.setPageIncrement(5);
	    slider.setThumb(4);
	    slider.setSelection(48);
	    GridDataFactory.fillDefaults().grab(false, false).span(2, 1).applyTo(slider);
	    
	    phiItem.setControl(interBlock);
		
		final TabItem alignItem = new TabItem(tabFolder, SWT.NULL);
		alignItem.setText("Alignment    ");
		alignItem.setImage(KoalaImage.ALIGNED64.getImage());
		
//		final VlcjViewer vlcjBlock = new VlcjViewer(tabFolder, SWT.HORIZONTAL);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(vlcjBlock);
		mjpegViewer = new MjpegViewer(tabFolder, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(mjpegViewer);
		
		alignItem.setControl(mjpegViewer);
		
		final TabItem oriItem = new TabItem(tabFolder, SWT.NULL);
		oriItem.setText("Orientation  ");
		oriItem.setImage(KoalaImage.ORIENTATION64.getImage());
		
		final Composite oriBlock = new Composite(tabFolder, SWT.NULL);
		GridLayoutFactory.swtDefaults().applyTo(oriBlock);
		
		final Group innerBlock = new Group(oriBlock, SWT.NULL);
		innerBlock.setText("Chi position");
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

		oriItem.setControl(oriBlock);
		
		tabFolder.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tabFolder.getSelectionIndex() == 2) {
					mjpegViewer.setRunnerPaused(false);
				} else {
					mjpegViewer.setRunnerPaused(true);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		String samplePhiPath = System.getProperty(ControlHelper.SAMPLE_PHI);
		new SimpleControlSuite(samplePhiPath, 
				curText, samplePhiPath, tarText, driveButton, phiStatusLabel);
		
		new ChiControlSuite();
	}

	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#next()
	 */
	@Override
	public void next() {
		mainPart.showInitScanPanel();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#back()
	 */
	@Override
	public void back() {
		mainPart.showProposalPanel();
	}

	public void pauseVideo() {
		mjpegViewer.setRunnerPaused(true);
	}
	
	@Override
	public void show() {
		mainPart.showPanel(this, panelWidth, panelHeight);
		mainPart.enableBackButton();
		mainPart.enableNextButton();
		mainPart.setTitle("Crystal Mounting");
		if (tabFolder.getSelectionIndex() == 2) {
			mjpegViewer.setRunnerPaused(false);
		}
		mainPart.setCurrentPanelName(PanelName.CRYSTAL);
	}

	private void driveSchi(final float value) {
		if (controlHelper.isConnected()) {
			final ISicsController schiController = SicsManager.getSicsModel().findControllerByPath(
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
							setChiStatusText(chiStatusLabel, e1.getMessage());
//							mainPart.popupError(e1.getMessage());
						}
					}
				});
			}
		}
	}
	
	private void setChiStatusText(final Label statusLabel, final String text) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				statusLabel.setText(text);
			}
		});
	}
	
	class ChiControlSuite {
		
		public ChiControlSuite() {
			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
				
				@Override
				public void connect() {
					final ISicsController chiController = SicsManager.getSicsModel().findControllerByPath(
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
							
							chiController.addControllerListener(
									new ChiControllerListener((DriveableController) chiController));
						}
					}

				}
			};
			controlHelper.addProxyListener(proxyListener);
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
}
