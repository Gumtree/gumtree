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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.gumtree.util.ILoopExitCondition;
import org.gumtree.util.LoopRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.scan.KoalaServerException;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public class JoeyPanel extends AbstractPanel {

	private final static Logger logger = LoggerFactory.getLogger(JoeyPanel.class);
	private static final int WIDTH_HINT = 600;
	private static final int HEIGHT_HINT = 680;
	private static final int DRUMDOWN_TIMEOUT = 600000;
	private MainPart mainPart;
	private Button activeButton;
	private Button advButton;
	private Label statusLabel;
	private Label infoLabel;
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
				if (MainPart.UNLOCK_TEXT.equals(value)) {
					logger.warn("password correctly input to Joey panel");
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

		Label titleLabel = new Label(this, SWT.NONE);
		titleLabel.setText("JOEY Mode");
		titleLabel.setForeground(Activator.getBusyColor());
		titleLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).minSize(360, 36).span(2, 1).applyTo(titleLabel);

		statusLabel = new Label(this, SWT.NONE);
		statusLabel.setText("DEACTIVATED");
		statusLabel.setEnabled(false);
		statusLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).minSize(360, 36).span(2, 1).applyTo(statusLabel);

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
				logger.warn("Activate/deactivate button clicked to set Joey mode to " + String.valueOf(isActivated));
				if (!isActivated) {
					activeButton.setEnabled(false);
					try {
						DrumDownHelper helper = new DrumDownHelper();
						helper.run();
					} catch (KoalaServerException e1) {
						ControlHelper.experimentModel.publishErrorMessage(e1.getMessage());
						activeButton.setEnabled(true);
						return;
					}
				}
				isActivated = !isActivated;
				passText.setText("");
				advButton.setVisible(false);
				hideAdvPanel();
				if (isActivated) {
					statusLabel.setText("ACTIVATED");
					statusLabel.setEnabled(true);
					statusLabel.setForeground(Activator.getHighlightColor());
					activeButton.setText("Deactivate");
				} else {
					statusLabel.setText("DEACTIVATED");
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

		infoLabel = new Label(this, SWT.NONE);
		infoLabel.setText("");
		infoLabel.setForeground(Activator.getWarningColor());
//		infoLabel.setEnabled(false);
		infoLabel.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.CENTER, SWT.CENTER).minSize(360, 24).span(2, 1).applyTo(infoLabel);

		boolean isJoeyMode = false;
		try {
			isJoeyMode = Boolean.valueOf(Activator.getPreference(Activator.NAME_JOEY_MODE));
		} catch (Exception e) {
		}
		if (isJoeyMode) {
			isActivated = true;
			statusLabel.setText("ACTIVATED");
			statusLabel.setEnabled(true);
			statusLabel.setForeground(Activator.getHighlightColor());
			activeButton.setText("Deactivate");
		}
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
		mainPart.setCurrentPanelName(PanelName.JOEY);
	}

	class DrumDownHelper {
		
		boolean isBusy;
		String errorMsg;
		
		public DrumDownHelper() {
			isBusy = false;
		}
		
		public void run() throws KoalaServerException {
			if (isBusy) {
				throw new KoalaServerException("System busy with dropping the drum.");
			}
			isBusy = true;
			errorMsg = null;
			infoLabel.setText("driving drum down ...");
			Thread runThread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						ControlHelper.syncDrive(System.getProperty(ControlHelper.DRUM_PATH), 
								Float.valueOf(System.getProperty(ControlHelper.DRUM_DOWN_VALUE)));
					} catch (Exception e1) {
						errorMsg = "Failed to drop the drum. " + e1.getMessage();
					} finally {
						isBusy = false;
					}
				}
			});
			runThread.start();

			LoopRunner.run(new ILoopExitCondition() {
				
				@Override
				public boolean getExitCondition() {
					return !isBusy;
				}
			}, DRUMDOWN_TIMEOUT);
			infoLabel.setText("");
			if (isBusy) {
				isBusy = false;
				if (runThread != null && runThread.isAlive()) {
					runThread.interrupt();
				}
				throw new KoalaServerException("Timeout in dropping the drum.");
			}
			if (errorMsg != null) {
				throw new KoalaServerException(errorMsg);
			}
		}
	}
}
