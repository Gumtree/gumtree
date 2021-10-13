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
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;

/**
 * @author nxi
 *
 */
public class EnvironmentPanel extends AbstractPanel {

	private static final int WIDTH_HINT = 480;
	private static final int HEIGHT_HINT = 520;
	private MainPart mainPart;
	
	/**
	 * @param parent
	 * @param style
	 */
	public EnvironmentPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		GridLayoutFactory.fillDefaults().margins(8, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(480, 320).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		Label controllerLabel = new Label(this, SWT.NONE);
		controllerLabel.setText("Cobra Controller");
		controllerLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).applyTo(controllerLabel);
		Button controllerButton = new Button(this, SWT.TOGGLE);
		controllerButton.setImage(KoalaImage.COBRA.getImage());
		controllerButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.CENTER, SWT.CENTER).applyTo(controllerButton);
		
		final Composite statusPanel = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(8, 8).applyTo(statusPanel);
		GridDataFactory.swtDefaults().minSize(560, 240).align(SWT.CENTER, SWT.CENTER).applyTo(statusPanel);

		Label valueLabel = new Label(statusPanel, SWT.NONE);
		valueLabel.setText("Current value");
		valueLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 36).align(SWT.CENTER, SWT.CENTER).applyTo(valueLabel);
		Text valueText = new Text(statusPanel, SWT.READ_ONLY);
		valueText.setText("120.5 K");
		valueText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(240, 36).align(SWT.END, SWT.CENTER).applyTo(valueText);

		Label targetLabel = new Label(statusPanel, SWT.NONE);
		targetLabel.setText("Target value");
		targetLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 36).align(SWT.CENTER, SWT.CENTER).applyTo(targetLabel);
		Text targetText = new Text(statusPanel, SWT.BORDER);
		targetText.setText("");
		targetText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(240, 36).align(SWT.END, SWT.CENTER).applyTo(targetText);

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
		mainPart.setTitle("Sample Environment");
	}


}
