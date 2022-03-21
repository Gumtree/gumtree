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
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.vlcj.VlcjViewer;

/**
 * @author nxi
 *
 */
public class CrystalPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 1560;
	private static final int HEIGHT_HINT = 720;
	private MainPart mainPart;
	
	/**
	 * @param parent
	 * @param style
	 */
	public CrystalPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		GridLayoutFactory.fillDefaults().margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(WIDTH_HINT, HEIGHT_HINT).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
//		final Label titleLabel = new Label(this, SWT.NONE);
//		titleLabel.setText("Crystal Mounting");
//		titleLabel.setFont(Activator.getLargeFont());
//		GridDataFactory.fillDefaults().grab(true, false).minSize(320, 36).applyTo(titleLabel);
		
		final TabFolder tabFolder = new TabFolder(this, SWT.NONE);
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
		
		final Label curLabel = new Label(phiBlock, SWT.NONE);
		curLabel.setText("Current phi value");
		curLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(320, 40).applyTo(curLabel);
		
		final Text curText = new Text(phiBlock, SWT.BORDER);
		curText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(240, 40).applyTo(curText);
		
		final Button driveButton = new Button(phiBlock, SWT.PUSH);
		driveButton.setImage(KoalaImage.PLAY48.getImage());
		driveButton.setText("Drive");
		driveButton.setFont(Activator.getMiddleFont());
		driveButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).span(1, 3).hint(240, 64).applyTo(driveButton);

		final Label tarLabel = new Label(phiBlock, SWT.NONE);
		tarLabel.setText("Target phi value");
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
		
		final VlcjViewer vlcjBlock = new VlcjViewer(tabFolder, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(vlcjBlock);
		
		alignItem.setControl(vlcjBlock);
		
		final TabItem oriItem = new TabItem(tabFolder, SWT.NULL);
		oriItem.setText("Orientation  ");
		oriItem.setImage(KoalaImage.ORIENTATION64.getImage());
		
		final Composite oriBlock = new Composite(tabFolder, SWT.NULL);
		GridLayoutFactory.swtDefaults().applyTo(oriBlock);
		
		final Group innerBlock = new Group(oriBlock, SWT.NULL);
		innerBlock.setText("Chi position");
		innerBlock.setFont(Activator.getMiddleFont());
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).spacing(4, 20).applyTo(innerBlock);
		innerBlock.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));

		final Button zeroButton = new Button(innerBlock, SWT.RADIO);
		zeroButton.setText("Zero");
		zeroButton.setFont(Activator.getMiddleFont());
		zeroButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(240, 40).applyTo(zeroButton);
		
		final Button highButton = new Button(innerBlock, SWT.RADIO);
		highButton.setText("High");
		highButton.setFont(Activator.getMiddleFont());
		highButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).hint(240, 40).applyTo(highButton);
		
		final Button applyButton = new Button(innerBlock, SWT.PUSH);
		applyButton.setImage(KoalaImage.PLAY48.getImage());
		applyButton.setText("Drive");
		applyButton.setFont(Activator.getMiddleFont());
		applyButton.setCursor(Activator.getHandCursor());
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.CENTER, SWT.CENTER).span(2, 1).hint(480, 64).applyTo(applyButton);

		oriItem.setControl(oriBlock);
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

	@Override
	public void show() {
		mainPart.showPanel(this, WIDTH_HINT, HEIGHT_HINT);
		mainPart.enableBackButton();
		mainPart.enableNextButton();
		mainPart.setTitle("Crystal Mounting");
	}


}
