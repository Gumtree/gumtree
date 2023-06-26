/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

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
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;
import au.gov.ansto.bragg.koala.ui.sics.ControlHelper;

/**
 * @author nxi
 *
 */
public class FooterPart extends Composite {

//	private Composite controlPart;
	private Button browseFolderButton;
	private Button openImageButton;
	private Button temperatureButton;
	private Button samZButton;
	private Button drumDownButton;
	private Button joeyButton;
	
	/**
	 * @param parent
	 * @param style
	 */
	public FooterPart(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().numColumns(6).applyTo(this);
		
		browseFolderButton = new Button(this, SWT.PUSH);
		browseFolderButton.setText("Open Image Folder");
		browseFolderButton.setCursor(Activator.getHandCursor());
		browseFolderButton.setFont(Activator.getMiddleFont());
		browseFolderButton.setImage(KoalaImage.OPEN64.getImage());
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, true).minSize(240, 0).applyTo(browseFolderButton);
		
		browseFolderButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
//				getParentViewer().getMainPart().showBackPanel();
				String fn = ControlHelper.experimentModel.getLastFilename();
				File f;
				if (fn == null) {
					fn = ControlHelper.experimentModel.getProposalFolder();
					if (fn == null) {
						ControlHelper.experimentModel.publishErrorMessage("failed to locate proposal folder");
						return;
					} else {
						f = new File(fn);
					}
				} else {
					f = new File(fn).getParentFile();
				}
				if (f.exists()) {
					try {
						Desktop.getDesktop().open(f);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					ControlHelper.experimentModel.publishErrorMessage("failed to locate image folder, " + f.getAbsolutePath());
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		openImageButton = new Button(this, SWT.PUSH);
		openImageButton.setText("Show Last Image");
		openImageButton.setCursor(Activator.getHandCursor());
		openImageButton.setFont(Activator.getMiddleFont());
		openImageButton.setImage(KoalaImage.IMAGE64.getImage());
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, true).minSize(240, 0).applyTo(openImageButton);

		openImageButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
//				getParentViewer().getMainPart().showNextPanel();
				String fn = ControlHelper.experimentModel.getLastFilename();
				File f;
				if (fn == null) {
					ControlHelper.experimentModel.publishErrorMessage("failed to locate last image file");
					return;
				} 
				f = new File(fn);
				if (f.exists()) {
					try {
						Desktop.getDesktop().open(f);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else {
					ControlHelper.experimentModel.publishErrorMessage("failed to locate image folder, " + f.getAbsolutePath());
				}

			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		temperatureButton = new Button(this, SWT.PUSH);
		temperatureButton.setCursor(Activator.getHandCursor());
		temperatureButton.setImage(KoalaImage.WEATHER64.getImage());
		temperatureButton.setText("Environment Control");
		temperatureButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).minSize(240, 0).applyTo(temperatureButton);
		temperatureButton.setToolTipText("Click to unlock administrator page.");
		
		temperatureButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				getParentViewer().getMainPart().showEnvironmentPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		samZButton = new Button(this, SWT.PUSH);
		samZButton.setText("Move Sample Z Up");
		samZButton.setCursor(Activator.getHandCursor());
		samZButton.setFont(Activator.getMiddleFont());
		samZButton.setImage(KoalaImage.UP64.getImage());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).minSize(240, 0).applyTo(samZButton);
		
		samZButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				getParentViewer().getMainPart().showJoeyPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		drumDownButton = new Button(this, SWT.PUSH);
		drumDownButton.setText("Move Drum Down");
		drumDownButton.setCursor(Activator.getHandCursor());
		drumDownButton.setFont(Activator.getMiddleFont());
		drumDownButton.setImage(KoalaImage.DOWN64.getImage());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).minSize(240, 0).applyTo(drumDownButton);
		
		drumDownButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				getParentViewer().getMainPart().showJoeyPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		joeyButton = new Button(this, SWT.PUSH);
		joeyButton.setText("JOEY Mode ");
		joeyButton.setCursor(Activator.getHandCursor());
		joeyButton.setFont(Activator.getMiddleFont());
		joeyButton.setImage(KoalaImage.JOEY64.getImage());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.END, SWT.CENTER).minSize(240, 0).applyTo(joeyButton);
		joeyButton.setVisible(false);
		joeyButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				getParentViewer().getMainPart().showJoeyPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	public void setBackButtonEnabled(boolean isEnabled) {
//		browseFolderButton.setVisible(isEnabled);
	}
	
	public void setNextButtonEnabled(boolean isEnabled) {
//		openImageButton.setVisible(isEnabled);
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
		browseFolderButton.setEnabled(isEnabled);
		openImageButton.setEnabled(isEnabled);
		temperatureButton.setEnabled(isEnabled);
		samZButton.setEnabled(isEnabled);
		drumDownButton.setEnabled(isEnabled);
	}

	public void setJoeyPartVisible(boolean isVisible) {
		joeyButton.setVisible(isVisible);
	}
	
}
