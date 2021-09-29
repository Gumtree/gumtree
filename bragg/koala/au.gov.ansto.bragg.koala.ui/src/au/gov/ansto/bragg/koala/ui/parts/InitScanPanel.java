/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;

/**
 * @author nxi
 *
 */
public class InitScanPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 1440;
	private static final int HEIGHT_HINT = 720;
	private MainPart mainPart;
	
	/**
	 * @param parent
	 * @param style
	 */
	public InitScanPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		GridLayoutFactory.fillDefaults().numColumns(3).margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(720, 720).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		final Label titleLabel = new Label(this, SWT.NONE);
		titleLabel.setText("Initial Scan");
		titleLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(320, 36).applyTo(titleLabel);
		
		final Group infoBlock = new Group(this, SWT.SHADOW_OUT);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(infoBlock);
		GridDataFactory.fillDefaults().grab(true, false).minSize(SWT.DEFAULT, 64).span(3, 1).applyTo(infoBlock);
		
		final Label nameLabel = new Label(infoBlock, SWT.NONE);
		nameLabel.setText("Sample name");
		nameLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(320, 40).applyTo(nameLabel);
		
		final Text nameText = new Text(infoBlock, SWT.BORDER);
		nameText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(600, 40).applyTo(nameText);
		
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
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(startText);
		
		final Label incLabel = new Label(phiGroup, SWT.NONE);
		incLabel.setText("Increment");
		incLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(incLabel);
		
		final Text incText = new Text(phiGroup, SWT.BORDER);
		incText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(incText);
		
		final Label numLabel = new Label(phiGroup, SWT.NONE);
		numLabel.setText("Number");
		numLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(numLabel);
		
		final Text numText = new Text(phiGroup, SWT.BORDER);
		numText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(240, 40).applyTo(numText);
		
		final Label finalLabel = new Label(phiGroup, SWT.NONE);
		finalLabel.setText("Final phi");
		finalLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 40).applyTo(finalLabel);
		
		final Text finText = new Text(phiGroup, SWT.BORDER);
		finText.setFont(Activator.getMiddleFont());
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
		mainPart.showFullExpPanel();
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
	}


}
