/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import au.gov.ansto.bragg.koala.ui.Activator;

/**
 * @author nxi
 *
 */
public class JoeyPanel extends AbstractPanel {

	private static final String UNLOCK_TEXT = "koala123";
	private static final int WIDTH_HINT = 600;
	private static final int HEIGHT_HINT = 680;
	private MainPart mainPart;
	private Button activeButton;
	private Button advButton;
	private Label statusLabel;
	private boolean isActivated;
	private ScrolledComposite advHolder;
	private Group passPart;

	/**
	 * @param parent
	 * @param style
	 */
	public JoeyPanel(Composite parent, int style, MainPart part) {
		super(parent, style);
		mainPart = part;
		GridLayoutFactory.fillDefaults().numColumns(2).margins(20, 8).applyTo(this);
		GridDataFactory.swtDefaults().minSize(480, 480).align(SWT.CENTER, SWT.CENTER).applyTo(this);
		
		Label passLabel = new Label(this, SWT.NONE);
		passLabel.setText("Password");
		passLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).applyTo(passLabel);
		final Text passText = new Text(this, SWT.PASSWORD | SWT.BORDER);
		passText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.END, SWT.CENTER).applyTo(passText);

		passText.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				String value = passText.getText();
				if (UNLOCK_TEXT.equals(value)) {
					activeButton.setEnabled(true);
					advButton.setVisible(true);
				} else {
					activeButton.setEnabled(false);
					advButton.setVisible(false);
					advButton.setSelection(false);
					hideAdvPanel();
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
//				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF || e.keyCode == 16777296) {
//					if (parameter.getDirtyFlag()){
//						String command = parameter.getCommand();
//						if (command != null) {
//							runIndependentCommand(command);
//						}
//						parameter.resetDirtyFlag();
//					}
//				} 
			}
		});
		Label emptyLabel = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(emptyLabel);
		
		advButton = new Button(this, SWT.TOGGLE);
		advButton.setCursor(Activator.getHandCursor());
		advButton.setText("Advanced control");
		advButton.setVisible(false);
		advButton.setToolTipText("Click here to change password");
		advButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(320, 36).align(SWT.END, SWT.BEGINNING).applyTo(advButton);

		advHolder = new ScrolledComposite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(4, 4).applyTo(advHolder);
		GridDataFactory.fillDefaults().grab(true, false).minSize(400, 180).span(2, 1).applyTo(advHolder);
		advHolder.setExpandHorizontal(true);
		advHolder.setExpandVertical(true);
		
		passPart = new Group(advHolder, SWT.BORDER);
		passPart.setBackground(Activator.getLightColor());
		passPart.setText("Change password");
		GridLayoutFactory.fillDefaults().margins(8, 12).numColumns(2).applyTo(passPart);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(passPart);
		
		final Label newLabel = new Label(passPart, SWT.NONE);
		newLabel.setText("New password");
		newLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).applyTo(newLabel);
		final Text newText = new Text(passPart, SWT.PASSWORD | SWT.BORDER);
		newText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(240, 36).align(SWT.END, SWT.CENTER).applyTo(newText);

		final Label reLabel = new Label(passPart, SWT.NONE);
		reLabel.setText("Repeat new password");
		reLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.CENTER, SWT.CENTER).applyTo(reLabel);
		final Text reText = new Text(passPart, SWT.PASSWORD | SWT.BORDER);
		reText.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(240, 36).align(SWT.END, SWT.CENTER).applyTo(reText);

		Button applyButton = new Button(passPart, SWT.PUSH);
		applyButton.setCursor(Activator.getHandCursor());
		applyButton.setText("Apply new password");
		applyButton.setEnabled(false);
		applyButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.CENTER, SWT.CENTER).span(2, 1).applyTo(applyButton);

		advButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (advButton.getSelection()) {
					advHolder.setContent(passPart);
					passPart.layout();
					advHolder.setMinSize(passPart.computeSize(400, 80));
					layout();
				} else {
					advHolder.setContent(null);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		statusLabel = new Label(this, SWT.NONE);
		statusLabel.setText("Inactive");
		statusLabel.setEnabled(false);
		statusLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).span(2, 1).applyTo(statusLabel);

		activeButton = new Button(this, SWT.PUSH);
		activeButton.setCursor(Activator.getHandCursor());
		activeButton.setText("Activate");
		activeButton.setEnabled(false);
		activeButton.setToolTipText("Click here to activate/deactivate Joey Mode");
		activeButton.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, true).minSize(320, 36).align(SWT.CENTER, SWT.CENTER).span(2, 1).applyTo(activeButton);
		
		activeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				isActivated = !isActivated;
				passText.setText("");
				advButton.setVisible(false);
				hideAdvPanel();
				if (isActivated) {
					statusLabel.setText("ACTIVE");
					statusLabel.setEnabled(true);
					statusLabel.setForeground(Activator.getHighlightColor());
					activeButton.setText("Deactivate");
				} else {
					statusLabel.setText("Inactive");
					statusLabel.setEnabled(false);
					statusLabel.setForeground(Activator.getLightColor());
					activeButton.setText("Activate");
				}
				mainPart.setJoeyMode(isActivated);
				passText.setFocus();
				activeButton.setEnabled(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	private void hideAdvPanel() {
		advHolder.setContent(null);
		advHolder.layout();
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
