/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.KoalaConstants.KoalaMode;

/**
 * @author nxi
 *
 */
public class ProposalPanel extends AbstractControlPanel {

	private static final int WIDTH_HINT = 720;
	private static final int HEIGHT_HINT = 720;
	private MainPart mainPart;
	
	/**
	 * @param parent
	 * @param style
	 */
	public ProposalPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
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
		Text idText = new Text(currentPropPanel, SWT.BORDER);
		idText.setText("1234");
		idText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.END, SWT.CENTER).applyTo(idText);
		
		Label nameLabel = new Label(currentPropPanel, SWT.NONE);
		nameLabel.setText("User Name");
		nameLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 36).align(SWT.BEGINNING, SWT.CENTER).applyTo(nameLabel);
		Text nameText = new Text(currentPropPanel, SWT.BORDER);
		nameText.setText("Test User");
		nameText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.END, SWT.CENTER).applyTo(nameText);

		Label isLabel = new Label(currentPropPanel, SWT.NONE);
		isLabel.setText("Local Contact");
		isLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).minSize(240, 36).align(SWT.BEGINNING, SWT.CENTER).applyTo(isLabel);
		Text isText = new Text(currentPropPanel, SWT.BORDER);
		isText.setText("Instrument Scientist");
		isText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.END, SWT.CENTER).applyTo(isText);

		Button changeButton = new Button(currentPropPanel, SWT.PUSH);
		changeButton.setText("Change");
		changeButton.setCursor(Activator.getHandCursor());
//		changeButton.setEnabled(false);
		changeButton.setToolTipText("Click here to apply change.");
		changeButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.CENTER, SWT.CENTER).span(2, 1).applyTo(changeButton);
		
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
				mainPart.setMode(KoalaMode.PHYSICS);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		if (mainPart.getInstrumentMode() == KoalaMode.CHEMISTRY) {
			chemButton.setSelection(true);
		} else if (mainPart.getInstrumentMode() == KoalaMode.PHYSICS) {
			physiButton.setSelection(true);
		}
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
	}


}
