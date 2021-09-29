/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import au.gov.ansto.bragg.koala.ui.Activator;

/**
 * @author nxi
 *
 */
public class FullExpPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 1440;
	private static final int HEIGHT_HINT = 720;
	private MainPart mainPart;
	
	/**
	 * @param parent
	 * @param style
	 */
	public FullExpPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		GridLayoutFactory.fillDefaults().margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(720, 720).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		final Label titleLabel = new Label(this, SWT.NONE);
		titleLabel.setText("Full Experiment");
		titleLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(320, 36).applyTo(titleLabel);
		
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
		mainPart.showInitScanPanel();
	}

	@Override
	public void show() {
		mainPart.showPanel(this, WIDTH_HINT, HEIGHT_HINT);
		mainPart.enableBackButton();
		mainPart.disableNextButton();
	}


}
