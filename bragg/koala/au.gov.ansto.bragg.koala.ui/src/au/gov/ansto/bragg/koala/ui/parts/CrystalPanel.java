/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;

/**
 * @author nxi
 *
 */
public class CrystalPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 1440;
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
		GridDataFactory.swtDefaults().minSize(720, 720).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		final Label titleLabel = new Label(this, SWT.NONE);
		titleLabel.setText("Crystal Mounting");
		titleLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(320, 36).applyTo(titleLabel);
		
		final TabFolder tabFolder = new TabFolder(this, SWT.BORDER);
		tabFolder.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 64).applyTo(tabFolder);
		
		final TabItem envItem = new TabItem(tabFolder, SWT.NULL);
		envItem.setText("Environment  ");
		envItem.setImage(KoalaImage.TEMPERATURE64.getImage());
	    
		final TabItem phiItem = new TabItem(tabFolder, SWT.NULL);
		phiItem.setText("Phi Setup    ");
		phiItem.setImage(KoalaImage.PHI64.getImage());
	    
		final TabItem alignItem = new TabItem(tabFolder, SWT.NULL);
		alignItem.setText("Alignment    ");
		alignItem.setImage(KoalaImage.ALIGNED64.getImage());
		
		final TabItem oriItem = new TabItem(tabFolder, SWT.NULL);
		oriItem.setText("Orientation  ");
		oriItem.setImage(KoalaImage.ORIENTATION64.getImage());
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
	}


}
