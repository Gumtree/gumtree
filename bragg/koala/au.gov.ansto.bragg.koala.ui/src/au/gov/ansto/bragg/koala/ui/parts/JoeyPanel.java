/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.koala.ui.Activator;

/**
 * @author nxi
 *
 */
public class JoeyPanel extends AbstractPanel {

	private static final int WIDTH_HINT = 480;
	private static final int HEIGHT_HINT = 520;
	private MainPart mainPart;
	
	/**
	 * @param parent
	 * @param style
	 */
	public JoeyPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(480, 320).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		Label passLabel = new Label(this, SWT.NONE);
		passLabel.setText("Password");
		passLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).applyTo(passLabel);
		Text passText = new Text(this, SWT.PASSWORD | SWT.BORDER);
		passText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(240, 36).align(SWT.END, SWT.CENTER).applyTo(passText);

		Label statusLabel = new Label(this, SWT.NONE);
		statusLabel.setText("Not Active");
		statusLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).span(2, 1).applyTo(statusLabel);

		Button activeButton = new Button(this, SWT.TOGGLE);
		activeButton.setCursor(Activator.getHandCursor());
		activeButton.setText("ACTIVATE");
		activeButton.setEnabled(false);
		activeButton.setToolTipText("Click here to activate/deactivate Joey Mode");
		activeButton.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.CENTER, SWT.CENTER).span(2, 1).applyTo(activeButton);
		
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
		mainPart.enableBackButton();
		mainPart.disableNextButton();
		mainPart.setTitle("Joey Mode Control");
	}


}
