/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import java.io.File;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.KoalaConstants.KoalaMode;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.scan.SingleScan;
import au.gov.ansto.bragg.nbi.ui.scripting.parts.ScriptDataSourceViewer;

/**
 * @author nxi
 *
 */
public class InitScanPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 1440;
	private static final int HEIGHT_HINT = 720;
	private MainPart mainPart;
	private SingleScan initScan;
	
	/**
	 * @param parent
	 * @param style
	 */
	public InitScanPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		initScan = new SingleScan();
//		initScan = mainPart.getScanModel().getInitScan();
		mainPart.getChemistryModel().setInitScan(initScan);
		mainPart.getPhysicsModel().setInitScan(initScan);
		
		GridLayoutFactory.fillDefaults().numColumns(3).margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(720, 720).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
//		final Label titleLabel = new Label(this, SWT.NONE);
//		titleLabel.setText("Initial Scan");
//		titleLabel.setFont(Activator.getLargeFont());
//		GridDataFactory.fillDefaults().grab(true, false).minSize(320, 36).applyTo(titleLabel);
		
		final Group infoBlock = new Group(this, SWT.SHADOW_OUT);
		GridLayoutFactory.fillDefaults().numColumns(5).margins(8, 8).applyTo(infoBlock);
		GridDataFactory.fillDefaults().grab(true, false).minSize(SWT.DEFAULT, 64).span(3, 1).applyTo(infoBlock);
		
		final Label nameLabel = new Label(infoBlock, SWT.NONE);
		nameLabel.setText("Sample name");
		nameLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(320, 40).applyTo(nameLabel);
		
		final Text nameText = new Text(infoBlock, SWT.BORDER);
		nameText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).minSize(600, 40).applyTo(nameText);
		
		final Label comLabel = new Label(infoBlock, SWT.NONE);
		comLabel.setText(" Comments");
		comLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).span(1, 2).align(SWT.BEGINNING, SWT.BEGINNING
				).minSize(320, 40).applyTo(comLabel);
		
		final Text comText = new Text(infoBlock, SWT.WRAP | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		comText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).span(1, 2).minSize(600, 88).applyTo(comText);
		
		final Label fileLabel = new Label(infoBlock, SWT.NONE);
		fileLabel.setText("Image filename");
		fileLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(320, 40).applyTo(fileLabel);
		
		final Text fileText = new Text(infoBlock, SWT.BORDER);
		fileText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(fileText);

		final Button fileLocatorButton = new Button(infoBlock, SWT.PUSH);
		fileLocatorButton.setText(">>");
		fileLocatorButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(false, false).applyTo(fileLocatorButton);
		fileLocatorButton.setToolTipText("Click to locate a directory.");
		fileLocatorButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				if (ScriptDataSourceViewer.fileDialogPath == null){
					IWorkspace workspace= ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();
					dialog.setFilterPath(root.getLocation().toOSString());
				} else {
					dialog.setFilterPath(ScriptDataSourceViewer.fileDialogPath);
				}
				String ext = "*.tif,*.TIF";
				dialog.setFilterExtensions(ext.split(","));
				dialog.open();
				if (dialog.getFileName() == null || dialog.getFileName().trim().length() == 0) {
					return;
				}
				String filePath = dialog.getFilterPath() + File.separator + dialog.getFileName();
				if (filePath != null) {
					fileText.setText(filePath);
					fileText.setToolTipText(filePath);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		final Group condGroup = new Group(this, SWT.SHADOW_OUT);
		condGroup.setText("Current Condition");
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(condGroup);
		GridDataFactory.fillDefaults().grab(true, true).minSize(480, SWT.DEFAULT).applyTo(condGroup);
		
		final Label phiLabel = new Label(condGroup, SWT.NONE);
		phiLabel.setText("Phi");
		phiLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(phiLabel);
		
		final Text phiText = new Text(condGroup, SWT.BORDER);
		phiText.setFont(Activator.getMiddleFont());
		phiText.setEditable(false);
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(phiText);
		
		final Label chiLabel = new Label(condGroup, SWT.NONE);
		chiLabel.setText("Chi");
		chiLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(chiLabel);
		
		final Text chiText = new Text(condGroup, SWT.BORDER);
		chiText.setEditable(false);
		chiText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(chiText);
		
		final Label tempLabel = new Label(condGroup, SWT.NONE);
		tempLabel.setText("Temperature");
		tempLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(tempLabel);
		
		final Text tempText = new Text(condGroup, SWT.BORDER);
		tempText.setEditable(false);
		tempText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(tempText);
		
		final Group phiGroup = new Group(this, SWT.SHADOW_ETCHED_OUT);
		phiGroup.setText("Phi Setup");
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(phiGroup);
		GridDataFactory.fillDefaults().grab(true, true).minSize(480, SWT.DEFAULT).applyTo(phiGroup);
		
		final Label startLabel = new Label(phiGroup, SWT.NONE);
		startLabel.setText("Start");
		startLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(startLabel);
		
		final Text startText = new Text(phiGroup, SWT.BORDER);
		startText.setFont(Activator.getMiddleFont());
		startText.setText(String.valueOf(initScan.getStart()));
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(startText);
		
		final Label incLabel = new Label(phiGroup, SWT.NONE);
		incLabel.setText("Increment");
		incLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(incLabel);
		
		final Text incText = new Text(phiGroup, SWT.BORDER);
		incText.setText(String.valueOf(initScan.getInc()));
		incText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(incText);
		
		final Label numLabel = new Label(phiGroup, SWT.NONE);
		numLabel.setText("Number");
		numLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(numLabel);
		
		final Text numText = new Text(phiGroup, SWT.BORDER);
		numText.setFont(Activator.getMiddleFont());
		numText.setText(String.valueOf(initScan.getNumber()));
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(numText);

		final Label finalLabel = new Label(phiGroup, SWT.NONE);
		finalLabel.setText("Final phi");
		finalLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(finalLabel);
		
		final Text finText = new Text(phiGroup, SWT.BORDER);
		finText.setFont(Activator.getMiddleFont());
		finText.setText(String.valueOf(initScan.getEnd()));
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(finText);

		final Group duriGroup = new Group(this, SWT.SHADOW_ETCHED_IN);
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
		eraText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(180, 40).applyTo(eraText);
		
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

		final Group runBlock = new Group(this, SWT.SHADOW_OUT);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(runBlock);
		GridDataFactory.fillDefaults().grab(true, false).minSize(SWT.DEFAULT, 64).span(3, 1).applyTo(runBlock);
		
		final Button runButton = new Button(runBlock, SWT.PUSH);
		runButton.setImage(KoalaImage.PLAY48.getImage());
		runButton.setText("Run");
		runButton.setFont(Activator.getMiddleFont());
		runButton.setCursor(Activator.getHandCursor());
//		runButton.setSize(240, 64);
		GridDataFactory.fillDefaults().grab(false, false).hint(240, 64).applyTo(runButton);
		
		ProgressBar proBar = new ProgressBar(runBlock, SWT.HORIZONTAL);
		proBar.setMaximum(100);
		proBar.setMinimum(0);
		proBar.setSelection(10);
		proBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
	}

	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#next()
	 */
	@Override
	public void next() {
		if (mainPart.getInstrumentMode() == KoalaMode.CHEMISTRY) {
			mainPart.showChemistryPanel();
		} else if (mainPart.getInstrumentMode() == KoalaMode.PHYSICS) {
			mainPart.showPhysicsPanel();
		}
	}

	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.koala.ui.parts.AbstractControlPart#back()
	 */
	@Override
	public void back() {
		mainPart.showCrystalPanel();
	}

	@Override
	public void show() {
		mainPart.showPanel(this, WIDTH_HINT, HEIGHT_HINT);
		mainPart.enableBackButton();
		mainPart.enableNextButton();
		mainPart.setTitle("Initial Scan");
		mainPart.setCurrentPanelName(PanelName.INITSCAN);
	}

}
