/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.KoalaConstants.KoalaMode;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.scan.ExperimentModel;
import au.gov.ansto.bragg.nbi.service.soap.CurrentProposalSOAPService;

/**
 * @author nxi
 *
 */
public class ProposalPanel extends AbstractControlPanel {
	
	private static final int WIDTH_HINT = 720;
	private static final int HEIGHT_HINT = 720;
	private static final String ID_PROP_CODE = "proposalCode";
	private static final String ID_PRCP_SCI = "principalSci";
	private static final String ID_LOC_SCI = "localSci";
	private static Logger logger = LoggerFactory.getLogger(ProposalPanel.class);
	
	private CurrentProposalSOAPService proposalService;
	private MainPart mainPart;
	private Text idText;
	private Text nameText;
	private Text isText;
	private Button changeButton;
	private Button resetButton;
	private boolean isWaiting;
	
	private String propIdValue;
	private String userNameValue;
	private String localSciValue;
	
	/**
	 * @param parent
	 * @param style
	 */
	public ProposalPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		proposalService = new CurrentProposalSOAPService("KOALA");
		
		mainPart = part;
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(720, 720).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		Button koalaButton = new Button(this, SWT.PUSH);
		koalaButton.setImage(KoalaImage.KOALA_V720.getImage());
		koalaButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).span(1, 2).applyTo(koalaButton);

		final ScrolledComposite propInputHolder = new ScrolledComposite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(propInputHolder);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(propInputHolder);
		propInputHolder.setExpandHorizontal(true);
		propInputHolder.setExpandVertical(true);

		final Composite currentPropPanel = new Composite(propInputHolder, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(currentPropPanel);
		GridDataFactory.swtDefaults().minSize(560, 240).align(SWT.CENTER, SWT.CENTER).applyTo(currentPropPanel);

		Label idLabel = new Label(currentPropPanel, SWT.NONE);
		idLabel.setText("Proposal ID");
		idLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 36).align(SWT.BEGINNING, SWT.CENTER).applyTo(idLabel);
		idText = new Text(currentPropPanel, SWT.BORDER);
		idText.setText("");
		idText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.END, SWT.CENTER).applyTo(idText);
		
		idText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = idText.getText();
				if (text != null && !text.trim().equals(propIdValue)) {
					resetButton.setEnabled(true);
					changeButton.setEnabled(true);
				}
			}
		});
		
		Label nameLabel = new Label(currentPropPanel, SWT.NONE);
		nameLabel.setText("User Name");
		nameLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 36).align(SWT.BEGINNING, SWT.CENTER).applyTo(nameLabel);
		nameText = new Text(currentPropPanel, SWT.BORDER);
		nameText.setText("");
		nameText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.END, SWT.CENTER).applyTo(nameText);

		nameText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = nameText.getText();
				if (text != null && !text.trim().equals(userNameValue)) {
					resetButton.setEnabled(true);
					changeButton.setEnabled(true);
				}
			}
		});
		
		Label isLabel = new Label(currentPropPanel, SWT.NONE);
		isLabel.setText("Local Contact");
		isLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 36).align(SWT.BEGINNING, SWT.CENTER).applyTo(isLabel);
		isText = new Text(currentPropPanel, SWT.BORDER);
		isText.setText("");
		isText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.END, SWT.CENTER).applyTo(isText);

		isText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = isText.getText();
				if (text != null && !text.trim().equals(localSciValue)) {
					resetButton.setEnabled(true);
					changeButton.setEnabled(true);
				}
			}
		});
		
		Composite buttonComposite = new Composite(currentPropPanel, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(buttonComposite);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(buttonComposite);
		
		Button fillButton = new Button(buttonComposite, SWT.PUSH);
		fillButton.setText("Auto fill");
		fillButton.setCursor(Activator.getHandCursor());
		fillButton.setToolTipText("Click here to retrieve proposal information from user portal");
		fillButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).minSize(240, 36).align(SWT.FILL, SWT.CENTER).applyTo(fillButton);
		
		fillButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				autoFill();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		resetButton = new Button(buttonComposite, SWT.PUSH);
		resetButton.setText("Reset");
		resetButton.setCursor(Activator.getHandCursor());
		resetButton.setToolTipText("Click here to reset and discard all changes.");
		resetButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(240, 36).align(SWT.CENTER, SWT.CENTER).applyTo(resetButton);
		
		resetButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadModel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		changeButton = new Button(buttonComposite, SWT.PUSH);
		changeButton.setText("Apply change");
		changeButton.setCursor(Activator.getHandCursor());
		changeButton.setToolTipText("Click here to apply change.");
		changeButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(240, 36).align(SWT.CENTER, SWT.CENTER).applyTo(changeButton);
		
		changeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				applyChange();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		propInputHolder.setContent(currentPropPanel);
		
		final Composite selectPanel = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(selectPanel);
//		GridDataFactory.fillDefaults().minSize(320, 560).grab(true, false).align(SWT.CENTER, SWT.CENTER).applyTo(selectPanel);
//		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(selectPanel);
		GridDataFactory.swtDefaults().minSize(560, 240).grab(false, true).align(SWT.CENTER, SWT.CENTER).applyTo(selectPanel);
		
		Label modeLabel = new Label(selectPanel, SWT.NONE);
		modeLabel.setText("Operation Mode");
		modeLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).align(SWT.BEGINNING, SWT.CENTER).applyTo(modeLabel);

		Button chemButton = new Button(selectPanel, SWT.RADIO);
		chemButton.setCursor(Activator.getHandCursor());
		chemButton.setImage(KoalaImage.CHEMISTRY64.getImage());
		chemButton.setText("Chemistry");
		chemButton.setFont(Activator.getMiddleFont());
		chemButton.setGrayed(false);
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 64).applyTo(chemButton);
		
		chemButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Chemistry button clicked");
				mainPart.setMode(KoalaMode.CHEMISTRY);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Button physiButton = new Button(selectPanel, SWT.RADIO);
		physiButton.setCursor(Activator.getHandCursor());
		physiButton.setImage(KoalaImage.PHYSICS64.getImage());
		physiButton.setText("Physics");
		physiButton.setFont(Activator.getMiddleFont());
		physiButton.setGrayed(true);
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 64).applyTo(physiButton);
		
		physiButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Physics button clicked");
				mainPart.setMode(KoalaMode.PHYSICS);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		String mode = Activator.getPreference(Activator.NAME_OP_MODE);
		if (mode != null) {
			try {
				KoalaMode instrumentMode = KoalaMode.valueOf(mode);
				if (instrumentMode == KoalaMode.CHEMISTRY) {
					chemButton.setSelection(true);
				} else if (instrumentMode == KoalaMode.PHYSICS) {
					physiButton.setSelection(true);
				}
			} catch (Exception e) {
			}
		}

		proposalService.addListener(new CurrentProposalSOAPService.IServiceListener() {
			
			@Override
			public void onLoaded(final Map<String, String> response) {
				if (response != null) {
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							isWaiting = false;
							boolean isChanged = false;
							if (response.containsKey(ID_PROP_CODE)) {
								idText.setText(response.get(ID_PROP_CODE));
								isChanged = true;
							}
							if (response.containsKey(ID_PRCP_SCI)) {
								nameText.setText(response.get(ID_PRCP_SCI));
								isChanged = true;
							}
							if (response.containsKey(ID_LOC_SCI)) {
								isText.setText(response.get(ID_LOC_SCI));
								isChanged = true;
							}
							if (isChanged) {
								changeButton.setEnabled(true);
								resetButton.setEnabled(true);
							}
							getShell().setCursor(null);
						}
					});
				}
			}

		});

		loadModel();
	}

	private void autoFill() {
		isWaiting = true;
		getShell().setCursor(Activator.getBusyCursor());
		proposalService.load();
		logger.info("Auto fill button clicked");
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if (isWaiting) {
					Display.getDefault().asyncExec(new Runnable() {
						
						@Override
						public void run() {
							logger.error("failed to communicate with portal server");
							idText.setText("Timeout connecting to server");
						}
					});
				}
			}
		}, 10000);
	}
	
	private void applyChange() {
		logger.info("Apply change button clicked");
		String propId = idText.getText();
		if (propId == null || propId.trim().length() == 0) {
			mainPart.popupError("Proposal ID can't be empty.");
			return;
		}
		String userName = nameText.getText();
		if (userName == null || userName.trim().length() == 0) {
			mainPart.popupError("User name can't be empty.");
			return;
		}
		ExperimentModel model = mainPart.getExperimentModel();
		try {
			model.setProposalInfo(idText.getText(), nameText.getText(), isText.getText());
		} catch (Exception e) {
			mainPart.popupError("Failed to apply change, " + e.getMessage());
			return;
		}
		changeButton.setEnabled(false);
		resetButton.setEnabled(false);
		logger.info(String.format("proposalId = %s, username = %s, localContact = %s", 
				idText.getText(), nameText.getText(), isText.getText()));
	}
	
	private void loadModel() {
		ExperimentModel model = mainPart.getExperimentModel();
		String propId =  model.getProposalId();
		if (propId != null) {
			propIdValue = propId;
			idText.setText(propId);
		} else {
			idText.setText("");
		}
		String userName = model.getUsername();
		if (userName != null) {
			userNameValue = userName;
			nameText.setText(userName);
		} else {
			nameText.setText("");
		}
		String localSci = model.getLocalContact();
		if (localSci != null) {
			localSciValue = localSci;
			isText.setText(localSci);
		} else {
			isText.setText("");
		}
		changeButton.setEnabled(false);
		resetButton.setEnabled(false);
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#next()
	 */
	@Override
	public void next() {
		mainPart.showCrystalPanel();
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#back()
	 */
	@Override
	public void back() {
	}

	@Override
	public void show() {
		mainPart.showPanel(this, WIDTH_HINT, HEIGHT_HINT);
		mainPart.disableBackButton();
		mainPart.enableNextButton();
		mainPart.setTitle("Experiment Setup");
		mainPart.setCurrentPanelName(PanelName.PROPOSAL);
	}


}
