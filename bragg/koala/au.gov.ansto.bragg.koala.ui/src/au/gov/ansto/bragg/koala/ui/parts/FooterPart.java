/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;

/**
 * @author nxi
 *
 */
public class FooterPart extends Composite {

	private Composite controlPart;
	private Button backButton;
	private Button nextButton;
	private Button adminButton;
	private Button joeyButton;
	
	/**
	 * @param parent
	 * @param style
	 */
	public FooterPart(Composite parent, int style) {
		super(parent, style);
		// TODO Auto-generated constructor stub
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(this);
		joeyButton = new Button(this, SWT.PUSH);
		joeyButton.setText("JOEY Mode ");
		joeyButton.setCursor(Activator.getHandCursor());
		joeyButton.setFont(Activator.getMiddleFont());
		joeyButton.setImage(KoalaImage.JOEY64.getImage());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).applyTo(joeyButton);
		
		joeyButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				getParentViewer().getMainPart().showJoeyPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		controlPart = new Composite(this, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(controlPart);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).minSize(720, 0).applyTo(controlPart);
		
		backButton = new Button(controlPart, SWT.PUSH);
		backButton.setText("BACK");
		backButton.setCursor(Activator.getHandCursor());
		backButton.setFont(Activator.getMiddleFont());
		backButton.setImage(KoalaImage.BACK64.getImage());
		backButton.setVisible(false);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).minSize(240, 0).applyTo(backButton);
		
		backButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				getParentViewer().getMainPart().showBackPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		nextButton = new Button(controlPart, SWT.PUSH | SWT.RIGHT_TO_LEFT);
		nextButton.setText("NEXT");
		nextButton.setCursor(Activator.getHandCursor());
		nextButton.setFont(Activator.getMiddleFont());
		nextButton.setImage(KoalaImage.NEXT64.getImage());
		nextButton.setVisible(false);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).minSize(240, 0).applyTo(nextButton);

		nextButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				getParentViewer().getMainPart().showNextPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		adminButton = new Button(this, SWT.PUSH);
		adminButton.setCursor(Activator.getHandCursor());
		adminButton.setImage(KoalaImage.LOCK64.getImage());
		adminButton.setText("Admin Page");
		adminButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.END, SWT.CENTER).applyTo(adminButton);
		adminButton.setToolTipText("Click to unlock administrator page.");
		
		adminButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
//				getParentViewer().getMainPart().showProposalPanel();
				PasswordDialog dialog = new PasswordDialog(getShell());
				if (dialog.open() == Window.OK) {
		            String user = dialog.getUser();
		            String pw = dialog.getPassword();
		            if (MainPart.UNLOCK_TEXT.equals(pw)) {
		            	getParentViewer().getMainPart().showAdminPanel();
		            }
		        }
//				MessageDialog.openQuestion(getParentViewer(), "Administrator login", "");
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	public void setBackButtonEnabled(boolean isEnabled) {
		backButton.setVisible(isEnabled);
	}
	
	public void setNextButtonEnabled(boolean isEnabled) {
		nextButton.setVisible(isEnabled);
	}
	
//	public void enableChemistryButton() {
//		chemButton.setVisible(true);
//	}
//	
//	public void disableChemistryButton() {
//		chemButton.setVisible(false);
//	}
	
	private KoalaMainViewer getParentViewer() {
		return (KoalaMainViewer) getParent();
	}
	
	public void setButtonEnabled(boolean isEnabled) {
		joeyButton.setEnabled(isEnabled);
		backButton.setEnabled(isEnabled);
		nextButton.setEnabled(isEnabled);
		adminButton.setEnabled(isEnabled);
	}

}
