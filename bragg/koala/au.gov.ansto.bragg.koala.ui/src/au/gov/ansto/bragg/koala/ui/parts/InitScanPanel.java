/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.control.core.ISicsController;
import org.gumtree.control.core.SicsManager;
import org.gumtree.control.events.ISicsControllerListener;
import org.gumtree.control.events.ISicsProxyListener;
import org.gumtree.control.events.SicsProxyListenerAdapter;
import org.gumtree.control.exception.SicsException;
import org.gumtree.control.imp.DynamicController;
import org.gumtree.control.model.PropertyConstants.ControllerState;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.KoalaConstants.KoalaMode;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.scan.ExperimentModel;
import au.gov.ansto.bragg.koala.ui.scan.ExperimentModelAdapter;
import au.gov.ansto.bragg.koala.ui.scan.IExperimentModelListener;
import au.gov.ansto.bragg.koala.ui.scan.KoalaInterruptionException;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.scan.SingleScan;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public class InitScanPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 1440;
	private static final int HEIGHT_HINT = 720;
	private static final Logger logger = LoggerFactory.getLogger(InitScanPanel.class);
	private MainPart mainPart;
	private SingleScan initScan;
	private Text phiText;
	private Text chiText;
	private Text tempText;
	private Text stepText;
	private Text nameText;
	private Text parentText;
	private Text fileText;
	private Text comText;
	private Text startText;
	private Text incText;
	private Text numText;
	private Text lastFileText;
	private boolean dirtyFlag = false;
	private String curSampleName;
	private String curComments;
	private ConditionControl control;
	private ControlHelper controlHelper;
	private IExperimentModelListener experimentModelListener;
	
	/**
	 * @param parent
	 * @param style
	 */
	public InitScanPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		controlHelper = ControlHelper.getInstance();
		mainPart = part;
		initScan = new SingleScan();
//		initScan = mainPart.getScanModel().getInitScan();
		mainPart.getChemistryModel().setInitScan(initScan);
		mainPart.getPhysicsModel().setInitScan(initScan);
		
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(720, 720).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
//		final Label titleLabel = new Label(this, SWT.NONE);
//		titleLabel.setText("Initial Scan");
//		titleLabel.setFont(Activator.getLargeFont());
//		GridDataFactory.fillDefaults().grab(true, false).minSize(320, 36).applyTo(titleLabel);
		
		final Group infoBlock = new Group(this, SWT.SHADOW_OUT);
		GridLayoutFactory.fillDefaults().numColumns(6).margins(8, 8).applyTo(infoBlock);
		GridDataFactory.fillDefaults().grab(true, false).minSize(SWT.DEFAULT, 64).span(2, 1).applyTo(infoBlock);
		
		final Label nameLabel = new Label(infoBlock, SWT.NONE);
		nameLabel.setText("Sample name");
		nameLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(320, 40).applyTo(nameLabel);
		
		nameText = new Text(infoBlock, SWT.BORDER);
		nameText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).minSize(700, 40).applyTo(nameText);
		
		nameText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = nameText.getText();
				if (text != null && !text.trim().equals(curSampleName)) {
					dirtyFlag = true;
				}
			}
		});
		
		final Label comLabel = new Label(infoBlock, SWT.NONE);
		comLabel.setText(" Comments");
		comLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).span(1, 2).align(SWT.BEGINNING, SWT.BEGINNING
				).minSize(320, 40).applyTo(comLabel);
		
		comText = new Text(infoBlock, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		comText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(1, 2).minSize(500, 88).hint(360, 88).applyTo(comText);
		
		comText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				String text = comText.getText();
				if (text != null && !text.trim().equals(curComments)) {
					dirtyFlag = true;
				}
			}
		});

		final Label fileLabel = new Label(infoBlock, SWT.NONE);
		fileLabel.setText("Image filename");
		fileLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, 
				SWT.CENTER).minSize(320, 40).applyTo(fileLabel);
		
		parentText = new Text(infoBlock, SWT.READ_ONLY);
		parentText.setFont(Activator.getMiddleFont());
//		parentText.setText(mainPart.getProposalFolder());
		parentText.setText(mainPart.getExperimentModel().getProposalFolder());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(parentText);
		
		fileText = new Text(infoBlock, SWT.BORDER);
		fileText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).hint(300, SWT.DEFAULT
				).applyTo(fileText);
//		fileText.setText(SingleScan.DATA_FILENAME);

		final Button fileLocatorButton = new Button(infoBlock, SWT.PUSH);
		fileLocatorButton.setText(">>");
		fileLocatorButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(fileLocatorButton);
		fileLocatorButton.setToolTipText("Click to locate a directory.");
		fileLocatorButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				String propFolder = mainPart.getProposalFolder();
				propFolder = propFolder.substring(0, propFolder.length() - 1);
//				if (ScriptDataSourceViewer.fileDialogPath == null){
//					IWorkspace workspace= ResourcesPlugin.getWorkspace();
//					IWorkspaceRoot root = workspace.getRoot();
//					dialog.setFilterPath(root.getLocation().toOSString());
//				} else {
//					dialog.setFilterPath(ScriptDataSourceViewer.fileDialogPath);
//				}
				dialog.setFilterPath(propFolder);
				String ext = "*.tif,*.TIF";
				dialog.setFilterExtensions(ext.split(","));
				dialog.open();
				if (dialog.getFileName() == null || dialog.getFileName().trim().length() == 0) {
					return;
				}
				String filterPath = dialog.getFilterPath();
				if (!filterPath.startsWith(propFolder)) {
					mainPart.popupError("File path must be inside of the proposal folder: " + propFolder);
					return;
				}
				String text;
				if (filterPath.equals(propFolder)) {
					text = dialog.getFileName();
				} else {
					filterPath = filterPath.replace(propFolder + File.separator, "");
					text = filterPath + File.separator + dialog.getFileName();
				}
//				String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
				if (text != null) {
					fileText.setText(text);
					fileText.setToolTipText(text);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Composite leftMain = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(leftMain);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(leftMain);
		
		final Group phiGroup = new Group(leftMain, SWT.SHADOW_ETCHED_OUT);
		phiGroup.setText(Activator.PHI + " Setup");
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(phiGroup);
		GridDataFactory.fillDefaults().grab(true, true).minSize(480, SWT.DEFAULT).applyTo(phiGroup);
		
		final Label startLabel = new Label(phiGroup, SWT.NONE);
		startLabel.setText("Start");
		startLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(startLabel);
		
		startText = new Text(phiGroup, SWT.BORDER);
		startText.setFont(Activator.getMiddleFont());
		startText.setText(String.valueOf(initScan.getStart()));
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(startText);
		
		final Label incLabel = new Label(phiGroup, SWT.NONE);
		incLabel.setText("Increment");
		incLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(incLabel);
		
		incText = new Text(phiGroup, SWT.BORDER);
		incText.setText(String.valueOf(initScan.getInc()));
		incText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(incText);
		
		final Label numLabel = new Label(phiGroup, SWT.NONE);
		numLabel.setText("Number");
		numLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(numLabel);
		
		numText = new Text(phiGroup, SWT.BORDER);
		numText.setFont(Activator.getMiddleFont());
		numText.setText(String.valueOf(initScan.getNumber()));
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(numText);

		final Label finalLabel = new Label(phiGroup, SWT.NONE);
		finalLabel.setText("Final " + Activator.PHI);
		finalLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(finalLabel);
		
		final Text finText = new Text(phiGroup, SWT.BORDER);
		finText.setFont(Activator.getMiddleFont());
		finText.setText(String.valueOf(initScan.getEnd()));
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(finText);

		final Group duriGroup = new Group(leftMain, SWT.SHADOW_ETCHED_IN);
		duriGroup.setText("Scan Duration");
		GridLayoutFactory.fillDefaults().numColumns(3).margins(8, 8).applyTo(duriGroup);
		GridDataFactory.fillDefaults().grab(true, true).minSize(480, SWT.DEFAULT).applyTo(duriGroup);
		
		final Label expLabel = new Label(duriGroup, SWT.NONE);
		expLabel.setText("Exposure");
		expLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(expLabel);
		
		final Text expText = new Text(duriGroup, SWT.BORDER);
		expText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(180, 40).applyTo(expText);
		
		final Label unitsLabel1 = new Label(duriGroup, SWT.NONE);
		unitsLabel1.setText("sec");
		unitsLabel1.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(60, 40).applyTo(unitsLabel1);

		final Label eraLabel = new Label(duriGroup, SWT.NONE);
		eraLabel.setText("Erasure");
		eraLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(eraLabel);
		
		final Text eraText = new Text(duriGroup, SWT.BORDER);
		eraText.setEditable(false);
		eraText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(180, 40).applyTo(eraText);
		
//		eraText.addModifyListener(new ModifyListener() {
//			
//			@Override
//			public void modifyText(ModifyEvent e) {
//				try {
//					int era = Integer.valueOf(eraText.getText());
//					ControlHelper.ERASURE_TIME = era;
//				} catch (Exception e2) {
//					handleError("invalid erasure time");
//				}
//			}
//		});
		
		final Label unitsLabel2 = new Label(duriGroup, SWT.NONE);
		unitsLabel2.setText("sec");
		unitsLabel2.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(60, 40).applyTo(unitsLabel2);
		
		Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()), new Runnable() {
			public void run() {
				DataBindingContext bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(startText, SWT.Modify),
						BeansObservables.observeValue(initScan, "start"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(incText, SWT.Modify),
						BeansObservables.observeValue(initScan, "inc"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(numText, SWT.Modify),
						BeansObservables.observeValue(initScan, "number"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(finText, SWT.Modify),
						BeansObservables.observeValue(initScan, "end"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(expText, SWT.Modify),
						BeansObservables.observeValue(initScan, "exposure"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(eraText, SWT.Modify),
						BeansObservables.observeValue(initScan, "erasure"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(comText, SWT.Modify),
						BeansObservables.observeValue(initScan, "comments"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
				bindingContext = new DataBindingContext();
				bindingContext.bindValue(SWTObservables.observeText(fileText, SWT.Modify),
						BeansObservables.observeValue(initScan, "filename"),
						new UpdateValueStrategy(), new UpdateValueStrategy());
			}
		});

		final Group runBlock = new Group(leftMain, SWT.SHADOW_OUT);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(4, 4).applyTo(runBlock);
		GridDataFactory.fillDefaults().grab(true, false).minSize(SWT.DEFAULT, 64).span(3, 1).applyTo(runBlock);
		runBlock.setText("Run");
		
		final Button runButton = new Button(runBlock, SWT.PUSH);
		runButton.setImage(KoalaImage.PLAY48.getImage());
		runButton.setText("Start the scan");
		runButton.setFont(Activator.getMiddleFont());
		runButton.setCursor(Activator.getHandCursor());
//		runButton.setSize(240, 64);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.BEGINNING)
				.hint(240, 80).applyTo(runButton);
		
		runButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.info("Start the scan button clicked");
				runPhiScan();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Composite rightMain = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(rightMain);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(rightMain);
		
		final Group condGroup = new Group(rightMain, SWT.SHADOW_OUT);
		condGroup.setText("Current Condition");
		GridLayoutFactory.fillDefaults().numColumns(5).margins(8, 8).applyTo(condGroup);
		GridDataFactory.fillDefaults().grab(true, true).minSize(480, SWT.DEFAULT).applyTo(condGroup);
		
		final Label phiLabel = new Label(condGroup, SWT.NONE);
		phiLabel.setText(Activator.PHI + " (\u00b0)");
		phiLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(phiLabel);
		
		phiText = new Text(condGroup, SWT.BORDER);
		phiText.setFont(Activator.getMiddleFont());
		phiText.setEditable(false);
		GridDataFactory.fillDefaults().grab(false, false).minSize(300, 40).hint(300, SWT.DEFAULT).applyTo(phiText);
		
		final Label chiLabel = new Label(condGroup, SWT.NONE);
		chiLabel.setText(Activator.CHI + " (\u00b0)");
		chiLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(chiLabel);
		
		chiText = new Text(condGroup, SWT.BORDER);
		chiText.setEditable(false);
		chiText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).minSize(180, 40).applyTo(chiText);
		
		final Label tempLabel = new Label(condGroup, SWT.NONE);
		tempLabel.setText("Temperature (K)");
		tempLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(tempLabel);
		
		tempText = new Text(condGroup, SWT.BORDER);
		tempText.setEditable(false);
		tempText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(4, 1).minSize(240, 40).applyTo(tempText);
		
		final Label stepLabel = new Label(condGroup, SWT.NONE);
		stepLabel.setText("Step number");
		stepLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(numLabel);
		
		stepText = new Text(condGroup, SWT.BORDER);
		stepText.setFont(Activator.getMiddleFont());
		stepText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 1).minSize(240, 40).applyTo(stepText);

		final Label lastFileLabel = new Label(condGroup, SWT.NONE);
		lastFileLabel.setText("Last file");
		lastFileLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(180, 40).applyTo(fileLabel);
		
		lastFileText = new Text(condGroup, SWT.BORDER);
		lastFileText.setFont(Activator.getMiddleFont());
		lastFileText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).span(3, SWT.DEFAULT).minSize(240, 40).applyTo(lastFileText);

		final Button openButton = new Button(condGroup, SWT.PUSH);
	    openButton.setImage(KoalaImage.IMAGE32.getImage());
//	    openButton.setFont(Activator.getMiddleFont());
	    openButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER)
			.hint(40, 40).applyTo(openButton);

		openButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String fn = lastFileText.getText();
				File f = new File(fn);
				if (f.exists()) {
					try {
						Desktop.getDesktop().open(f);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					MessageDialog.openWarning(getShell(), "Warning", "File not found: " + fn);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
//		ProgressBar proBar = new ProgressBar(runBlock, SWT.HORIZONTAL);
//		proBar.setMaximum(100);
//		proBar.setMinimum(0);
//		proBar.setSelection(10);
//		proBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
//		Composite statusComposite = new Composite(rightMain, SWT.NONE);
//	    GridLayoutFactory.fillDefaults().applyTo(statusComposite);
//	    GridDataFactory.fillDefaults().grab(true, true).align(
//	    		SWT.FILL, SWT.BEGINNING).applyTo(statusComposite);
		new ScanStatusPart(rightMain, mainPart);
		
		control = new ConditionControl();
		loadPreference();
		
		final ExperimentModel model = mainPart.getExperimentModel();
		experimentModelListener = new ExperimentModelAdapter() {
			
			@Override
			public void proposalIdChanged(final String newId) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if (!parentText.isDisposed()) {
							parentText.setText(model.getProposalFolder());
						}
					}
				});
			}
			
			@Override
			public void updateLastFilename(final String filename) {
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						lastFileText.setText(filename);
						File f = new File(filename);
						if (f.exists()) {
							try {
								Desktop.getDesktop().open(f);
							} catch (IOException e1) {
								handleError("Failed to open file in image viewer: " + filename);
							}
						} else {
							handleError("File not found: " + filename);
						}
					}
				});
			}
		};
		model.addExperimentModelListener(experimentModelListener);
	}

	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#next()
	 */
	@Override
	public void next() {
		applySampleInfo();
		if (mainPart.getInstrumentMode() == KoalaMode.CHEMISTRY) {
			mainPart.showChemistryPanel();
		} else if (mainPart.getInstrumentMode() == KoalaMode.PHYSICS) {
			mainPart.showPhysicsPanel();
		}
	}

	private void runPhiScan() {
//		Display.getDefault().asyncExec(new Runnable() {
//			
//			@Override
//			public void run() {
//				float start, inc;
//				int numStep;
//				try {
//					start = Float.valueOf(startText.getText());
//				} catch (Exception e) {
//					handleError("invalid start value");
//				}
//				try {
//					inc = Float.valueOf(incText.getText());
//				} catch (Exception e) {
//					handleError("invalid increment value");
//				}
//				try {
//					numStep = Integer.valueOf(numText.getText());
//				} catch (Exception e) {
//					handleError("invalid number of steps value");
//				}
//				try {
//					initScan.run();
//				} catch (KoalaInterruptionException ei) {
//					handleError("user interrupted");
//				} catch (KoalaServerException e) {
//					handleError(e.getMessage());;
//				}
//			}
//		});
		
		applySampleInfo();
		JobRunner.run(new ILoopExitCondition() {
			
			@Override
			public boolean getExitCondition() {
				return true;
			}
		}, new Runnable() {
			
			@Override
			public void run() {
				try {
					if (initScan.needToRun()) {
						logger.warn("test scan started");
						initScan.run();
					} else {
						mainPart.popupError("The scan is empty. Please review the scan configuration.");
					}
				} catch (KoalaInterruptionException ei) {
					handleError("user interrupted");
				} catch (KoalaServerException e) {
					handleError("server error: " + e.getMessage());;
				} catch (Exception e) {
					handleError("error: " + e.getMessage());
				} finally {
					logger.warn("test scan finished");
				}
			}
		});
	}
	
	private void handleError(String errorText) {
		mainPart.popupError(errorText);
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#back()
	 */
	@Override
	public void back() {
		applySampleInfo();
		mainPart.showCrystalPanel();
	}

	private void loadPreference() {
//		String sampleName = Activator.getPreference(Activator.NAME_SAMPLE_NAME);
//		if (sampleName != null) {
//			nameText.setText(sampleName);
//		} 
//		String comments = Activator.getPreference(Activator.NAME_COMMENTS);
//		if (comments != null) {
//			comText.setText(comments);
//		} 
		String filename = Activator.getPreference(Activator.NAME_PROP_FOLDER);
		if (filename != null) {
//			fileText.setText(filename);
		} 
	}
	
	
	private void savePreference() {
//		Activator.setPreference(Activator.NAME_SAMPLE_NAME, nameText.getText());
//		Activator.setPreference(Activator.NAME_COMMENTS, comText.getText());
//		Activator.setPreference(Activator.NAME_FILENAME, fileText.getText());
		Activator.flushPreferenceStore();
	}
	
	private void applySampleInfo() {
		if (dirtyFlag) {
			try {
				logger.info("samplename and/or comments changed, apply the change to the server");
				control.applyChange();
			} catch (SicsException e) {
				mainPart.popupError("failed to save sample information");
			}
			savePreference();
		}
	}
	
	@Override
	public void show() {
		mainPart.showPanel(this, WIDTH_HINT, HEIGHT_HINT);
		mainPart.enableBackButton();
		mainPart.enableNextButton();
		mainPart.setTitle("Test Scan");
		mainPart.setCurrentPanelName(PanelName.INITSCAN);
	}

	class ConditionControl {
		
		private ISicsController sampleController;
		private ISicsController commentsController;
		private ISicsController phiController;
		private ISicsController chiController;
		private ISicsController tempController;
		private ISicsController stepController;
		
		public ConditionControl() {
			ISicsProxyListener proxyListener = new SicsProxyListenerAdapter() {
				
				@Override
				public void connect() {
					init();
				}
			};
			if (controlHelper.isConnected()) {
				init();
			}
			controlHelper.addProxyListener(proxyListener);
		}
		
		private void init() {
			phiController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.SAMPLE_PHI));
			chiController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.SAMPLE_CHI));
			tempController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.ENV_VALUE));
			stepController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.STEP_PATH));
			sampleController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.GUMTREE_SAMPLE_NAME));
			commentsController = SicsManager.getSicsModel().findController(
					System.getProperty(ControlHelper.GUMTREE_COMMENTS));
			if (phiController != null) {
				phiController.addControllerListener(
						new ControllerListener(phiText));
			}
			if (chiController != null) {
				chiController.addControllerListener(
						new ControllerListener(chiText));
			}
			if (tempController != null) {
				tempController.addControllerListener(
						new ControllerListener(tempText));
			}
			if (stepController != null) {
				stepController.addControllerListener(
						new ControllerListener(stepText));
			}
			if (sampleController != null) {
				sampleController.addControllerListener(
						new ControllerListener(nameText));
			}
			if (commentsController != null) {
				commentsController.addControllerListener(
						new ControllerListener(comText));
			}
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						if (phiController != null) {
							phiText.setText(String.valueOf(
									((DynamicController) phiController).getValue()));
						}
						if (chiController != null) {
							chiText.setText(String.valueOf(
									((DynamicController) chiController).getValue()));
						}
						if (tempController != null) {
							tempText.setText(String.valueOf(
									((DynamicController) tempController).getValue()));
						}
						if (stepController != null) {
							stepText.setText(String.valueOf(
									((DynamicController) stepController).getValue()));
						}
						if (sampleController != null) {
							curSampleName = String.valueOf(
									((DynamicController) sampleController).getValue());
							nameText.setText(curSampleName);
						}
						if (commentsController != null) {
							curComments = String.valueOf(
									((DynamicController) commentsController).getValue());
							comText.setText(curComments);
						}
					} catch (Exception e) {
					}
				}
			});

		}
		
		public void applyChange() throws SicsException {
			curSampleName = nameText.getText();
			curComments = comText.getText();
			((DynamicController) sampleController).setValue(curSampleName);
			((DynamicController) commentsController).setValue(curComments);
		}
	}
	
	class ControllerListener implements ISicsControllerListener {
		
		private Text widget;
//		private DynamicController controller;
		
		public ControllerListener(Text widget) {
			this.widget = widget;
//			this.controller = controller;
		}
		
		@Override
		public void updateState(final ControllerState oldState, final ControllerState newState) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (newState == ControllerState.BUSY) {
						widget.setForeground(Activator.getBusyColor());
					} else {
						widget.setForeground(Activator.getIdleColor());
					}
				}
			});
		}

		@Override
		public void updateValue(final Object oldValue, final Object newValue) {
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					widget.setText(String.valueOf(newValue));
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
	
	public SingleScan getInitScan() {
		return initScan;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		mainPart.getExperimentModel().removeExperimentModelListener(
				experimentModelListener);
	}
}
