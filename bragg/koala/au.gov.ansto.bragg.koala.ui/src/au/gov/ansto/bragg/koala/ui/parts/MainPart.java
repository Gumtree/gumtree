/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.gumtree.control.core.IDynamicController;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsModelException;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.parts.KoalaConstants.KoalaMode;
import au.gov.ansto.bragg.koala.ui.scan.ChemistryModel;
import au.gov.ansto.bragg.koala.ui.scan.ExperimentModel;
import au.gov.ansto.bragg.koala.ui.scan.ExperimentModelAdapter;
import au.gov.ansto.bragg.koala.ui.scan.IExperimentModelListener;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.scan.PhysicsModel;
import au.gov.ansto.bragg.koala.ui.scan.SingleScan;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;
import au.gov.ansto.bragg.koala.ui.widgets.DrumDoorWidget;

/**
 * @author nxi
 *
 */
public class MainPart extends Composite {

	public enum PanelName{
		ENVIRONMENT,
		JOEY,
		PROPOSAL,
		CRYSTAL,
		INITSCAN,
		EXPERIMENT,
		ADMIN
	}
	/**
	 * @param parent
	 * @param style
	 */
	public static final String UNLOCK_TEXT = "koala123";
	private static final String PROP_RECURRENT_PERIOD = "gumtree.koala.recurrentPeriodMS";
	private static Logger logger = LoggerFactory.getLogger(MainPart.class);


//	private String proposalFolder;
	private ScrolledComposite holder;
	private EnvironmentPanel environmentPanel;
	private JoeyPanel joeyPanel;
	private ProposalPanel proposalPanel;
	private CrystalPanel crystalPanel;
	private InitScanPanel initScanPanel;
	private ChemistryPanel chemExpPanel;
	private PhysicsPanel physicsPanel;
	private AdminPanel adminPanel;
	
	private AbstractControlPanel currentMainPanel;
	private AbstractPanel currentPanel;
//	private AbstractControlPanel nextPanel;
	private ChemistryModel chemModel;
	private PhysicsModel physModel;
	private KoalaMode instrumentMode = KoalaMode.CHEMISTRY;
	private ControlHelper control;
	
	private boolean isJoeyMode;
	private PanelName currentPanelName;
	private RecurrentScheduler scheduler;
	
	private ExperimentModel experimentModel;
	private IExperimentModelListener modelListener;
	
	public MainPart(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		long recPeriod;
		try {
			recPeriod = Long.valueOf(System.getProperty(PROP_RECURRENT_PERIOD));
		} finally {
			recPeriod = 1000;
		}
		scheduler = new RecurrentScheduler(recPeriod);
		control = new ControlHelper();
		chemModel = new ChemistryModel();
		physModel = new PhysicsModel();
		
		experimentModel = new ExperimentModel();
		ControlHelper.experimentModel = experimentModel;
		experimentModel.setChemistryModel(chemModel);
		experimentModel.setPhysicsModel(physModel);
		createPanels();
		if (experimentModel.getProposalFolder() == null) {
			popupError("No proposal folder found. Please add a proposal ID before commencing your experiment.");
		}
		modelListener = new ExperimentModelAdapter() {
			
			@Override
			public void onError(final String errorMessage) {
				popupError(errorMessage);
			}
			
			@Override
			public void onNotice(String noticeMessage) {
				popupNotice(noticeMessage);
			}
		};
		experimentModel.addExperimentModelListener(modelListener);
		
		addFileMonitor();
	}

	private void addFileMonitor() {
		
		if (control.isConnected()) {
			final IDynamicController fnController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
					System.getProperty(ControlHelper.FILENAME_PATH));
			if (fnController != null) {
				fnController.addControllerListener(new FilenameControllerListener());
			}
			
		} else {
			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {

				
				@Override
				public void modelUpdated() {
					final IDynamicController fnController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
							System.getProperty(ControlHelper.FILENAME_PATH));

					if (fnController != null) {
						fnController.addControllerListener(new FilenameControllerListener());
					}
					
					final IDynamicController versionController = (IDynamicController) SicsManager.getSicsModel().findControllerByPath(
							System.getProperty(ControlHelper.GUMTREE_VERSION_PATH));
					if (versionController != null) {
						String version = System.getProperty(ControlHelper.GUMTREE_VERSION_PROP);
						try {
							if (version != null) {
								versionController.setValue(version);
							} else {
								versionController.setValue("UNKNOWN");
							}
						} catch (Exception e) {
							logger.error("failed set gumtree version in SICS", e);
						}
					}
				}
			};

			control.addProxyListener(proxyListener);
		}
	
	}


	private void createPanels() {
		holder = new ScrolledComposite(this, SWT.NONE);
//		holder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayoutFactory.fillDefaults().applyTo(holder);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(holder);
//		cmpMain.setBackground(getBackground());
		holder.setExpandHorizontal(true);
		holder.setExpandVertical(true);

		environmentPanel = new EnvironmentPanel(holder, SWT.BORDER, this);
		joeyPanel = new JoeyPanel(holder, SWT.BORDER, this);
		proposalPanel = new ProposalPanel(holder, SWT.BORDER, this);
		crystalPanel = new CrystalPanel(holder, SWT.BORDER, this);
		initScanPanel = new InitScanPanel(holder, SWT.BORDER, this);
		chemExpPanel = new ChemistryPanel(holder, SWT.BORDER, this);
		physicsPanel = new PhysicsPanel(holder, SWT.BORDER, this);
		adminPanel = new AdminPanel(holder, SWT.BORDER, this);
	}
	
	public void setCurrentPanelName(PanelName name) {
		currentPanelName = name;
	}
	
	public PanelName getCurrentPanelName() {
		return currentPanelName;
	}
	
	public void showEnvironmentPanel() {
		environmentPanel.show();
	}

	public void showJoeyPanel() {
		joeyPanel.show();
	}

	public void showAdminPanel() {
		adminPanel.show();
	}

	public void showProposalPanel() {
		proposalPanel.show();
	}
	
	public void showCrystalPanel() {
		crystalPanel.show();
	}
	
	public void showInitScanPanel() {
		initScanPanel.show();
	}
	
	public void showChemistryPanel() {
		chemExpPanel.show();
	}

	public void showPhysicsPanel() {
		physicsPanel.show();
	}

	public void showPanel(AbstractPanel panel, int xHint, int yHint) {
		holder.setContent(panel);
		panel.layout();
		holder.setMinSize(panel.computeSize(xHint, yHint));
		holder.getParent().layout();
		currentPanel = panel;

		if (panel instanceof AbstractControlPanel) {
			currentMainPanel = (AbstractControlPanel) panel;
		}
		if (!(panel instanceof CrystalPanel)) {
			crystalPanel.pauseVideo();
		}
		getParentViewer().getFooterPart().setJoeyPartVisible(panel instanceof AdminPanel);
	}
	
	public void showCurrentMainPanel() {
		if (currentMainPanel != null) {
			currentMainPanel.show();
		}
	}
	
	public void showBackPanel() {
		if (currentPanel != null) {
			currentPanel.back();
		}
	}

	public void showNextPanel() {
		if (currentPanel != null) {
			currentPanel.next();;
		}
	}

	public KoalaMainViewer getParentViewer() {
		return (KoalaMainViewer) getParent();
	}
	
	public void enableBackButton() {
		getParentViewer().getFooterPart().setBackButtonEnabled(true);
	}

	public void disableBackButton() {
		getParentViewer().getFooterPart().setBackButtonEnabled(false);
	}

	public void enableNextButton() {
		getParentViewer().getFooterPart().setNextButtonEnabled(true);
	}

	public void disableNextButton() {
		getParentViewer().getFooterPart().setNextButtonEnabled(false);
	}

	public EnvironmentPanel getEnvironmentPanel() {
		return environmentPanel;
	}
	
	public JoeyPanel getJoeyPanel() {
		return joeyPanel;
	}
	
	public ProposalPanel getProposalPanel() {
		return proposalPanel;
	}
	
	public CrystalPanel getCrystalPanel() {
		return crystalPanel;
	}
	
	public PhysicsPanel getPhysicsPanel() {
		return physicsPanel;
	}
	
	public void applyMode() {
		getParentViewer().getHeaderPart().setMode(instrumentMode);
//		switch (instrumentMode) {
//		case CHEMISTRY:
//			getParentViewer().getFooterPart().enableChemistryButton();
//			getParentViewer().getHeaderPart().disablePhysicsButton();
//			break;
//		case PHYSICS:
//			getParentViewer().getHeaderPart().enablePhysicsButton();
//			getParentViewer().getFooterPart().disableChemistryButton();
//			break;
//		default:
//			getParentViewer().getFooterPart().disableChemistryButton();
//			getParentViewer().getHeaderPart().disablePhysicsButton();
//			break;
//		}
	}

	public void setMode(KoalaMode mode) {
		instrumentMode = mode;
		applyMode();
		Activator.setPreference(Activator.NAME_OP_MODE, instrumentMode.name());
		Activator.flushPreferenceStore();
	}
	
	public ChemistryModel getChemistryModel() {
		return chemModel;
	}

	public PhysicsModel getPhysicsModel() {
		return physModel;
	}

	public KoalaMode getInstrumentMode() {
		return instrumentMode;
	}
	
	public void setTitle(String title) {
		getParentViewer().getHeaderPart().setTitle(title);
	}
	
	public ControlHelper getControl() {
		return control;
	}
	
	public void setJoeyMode(boolean isEnabled) {
		isJoeyMode = isEnabled;
		getParentViewer().getHeaderPart().setButtonEnabled(!isEnabled);
		getParentViewer().getFooterPart().setButtonEnabled(!isEnabled);
		Activator.setPreference(Activator.NAME_JOEY_MODE, String.valueOf(isEnabled));
	}
	
	public RecurrentScheduler getRecurrentScheduler() {
		return scheduler;
	}
	
	public void popupError(final String errorText) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				String text = errorText;
				if (!DrumDoorWidget.isClosed()) {
					text += "\n** The drum door may not be closed well, please check the door status.";
				} else {
					text += "\n** " + ControlHelper.getMCError();
				}
				MessageDialog.openError(getShell(), "Error", text);
				logger.error(errorText);
			}
		});
	}
	
	public void popupNotice(final String noticeText) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog.openInformation(getShell(), "System Message", noticeText);
				logger.warn(noticeText);
			}
		});
	}
	
	public String getProposalFolder() {
		return getExperimentModel().getProposalFolder();
	}
	
	public ExperimentModel getExperimentModel() {
		return experimentModel;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		experimentModel.removeExperimentModelListener(modelListener);
	}
	
	public SingleScan getInitScan() {
		return initScanPanel.getInitScan();
	}
	
	class FilenameControllerListener implements ISicsControllerListener {

		private String currentValue;
		
		@Override
		public void updateState(final ControllerState oldState, final ControllerState newState) {
		}
		
		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			if (newValue != null && !newValue.toString().equals(currentValue)) {
				currentValue = newValue.toString();
				try {
					if (currentValue != null && currentValue.trim().length() > 0) {
						experimentModel.copyFile(currentValue);
					}
				} catch (KoalaServerException e) {
					popupError(e.getMessage());
				}
			}
		}
		
		@Override
		public void updateEnabled(boolean isEnabled) {
		}

		@Override
		public void updateTarget(final Object oldValue, final Object newValue) {
		}
	}

}
