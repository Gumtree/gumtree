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
	private Button sampleChiButton;
	private Button passButton;
	private Button joeyButton;
	private boolean isEnabled;
	
	/**
	 * @param parent
	 * @param style
	 */
	public FooterPart(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().numColumns(8).applyTo(this);
		
		browseFolderButton = new Button(this, SWT.PUSH);
		browseFolderButton.setText("Open Image Folder ");
		browseFolderButton.setCursor(Activator.getHandCursor());
		browseFolderButton.setFont(Activator.getMiddleFont());
		browseFolderButton.setImage(KoalaImage.OPEN32.getImage());
		browseFolderButton.setToolTipText("Click to open the current image folder.");
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, true).minSize(240, 64).applyTo(browseFolderButton);
		
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
		openImageButton.setText("Show Last Image ");
		openImageButton.setCursor(Activator.getHandCursor());
		openImageButton.setFont(Activator.getMiddleFont());
		openImageButton.setImage(KoalaImage.IMAGE32.getImage());
		openImageButton.setToolTipText("Click to show the most recent image.");
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, true).minSize(240, 64).applyTo(openImageButton);

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
		temperatureButton.setImage(KoalaImage.WEATHER32.getImage());
		temperatureButton.setText("Environment Control ");
		temperatureButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).minSize(240, 64).applyTo(temperatureButton);
		temperatureButton.setToolTipText("Click to show temperature controller page.");
		
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
		samZButton.setText(ControlHelper.SampleZHelper.MOVE_Z_UP);
		samZButton.setCursor(Activator.getHandCursor());
		samZButton.setFont(Activator.getMiddleFont());
		samZButton.setImage(KoalaImage.UP32.getImage());
		samZButton.setToolTipText("Click to drive the sample Z (sz) to top position.");
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).minSize(240, 64).applyTo(samZButton);
		
		new ControlHelper.SampleZHelper(samZButton);
		
		drumDownButton = new Button(this, SWT.PUSH);
		drumDownButton.setText("Drum Down ");
		drumDownButton.setCursor(Activator.getHandCursor());
		drumDownButton.setFont(Activator.getMiddleFont());
		drumDownButton.setImage(KoalaImage.DRUM32.getImage());
		drumDownButton.setToolTipText("Click to drive the drum Z to buttom position.");
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).minSize(240, 64).applyTo(drumDownButton);
		
		new ControlHelper.DrumZHelper(drumDownButton);
		
		sampleChiButton = new Button(this, SWT.PUSH);
		sampleChiButton.setCursor(Activator.getHandCursor());
		sampleChiButton.setImage(KoalaImage.ORIENTATION32.getImage());
		sampleChiButton.setText(" " + Activator.CHI + " Position ");
		sampleChiButton.setFont(Activator.getMiddleFont());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).minSize(240, 64).applyTo(sampleChiButton);
		sampleChiButton.setToolTipText("Click to show sample Chi position panel.");
		
		sampleChiButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				getParentViewer().getMainPart().showSampleChiPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		joeyButton = new Button(this, SWT.PUSH);
		joeyButton.setText("JOEY Mode ");
		joeyButton.setCursor(Activator.getHandCursor());
		joeyButton.setFont(Activator.getMiddleFont());
		joeyButton.setImage(KoalaImage.JOEY32.getImage());
		joeyButton.setToolTipText("Click to show the Joey mode control panel.");
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.END, SWT.CENTER).minSize(240, 64).applyTo(joeyButton);
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

		passButton = new Button(this, SWT.PUSH);
		passButton.setText("Passcode ");
		passButton.setCursor(Activator.getHandCursor());
		passButton.setFont(Activator.getMiddleFont());
		passButton.setImage(KoalaImage.PASS32.getImage());
		passButton.setToolTipText("Click to change administrator passcode.");
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.END, SWT.CENTER).minSize(240, 64).applyTo(passButton);
		passButton.setVisible(false);
		passButton.addSelectionListener(new SelectionListener() {
			
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
		this.isEnabled = isEnabled;
		joeyButton.setEnabled(isEnabled);
		browseFolderButton.setEnabled(isEnabled);
		openImageButton.setEnabled(isEnabled);
		temperatureButton.setEnabled(isEnabled);
		samZButton.setEnabled(isEnabled);
		drumDownButton.setEnabled(isEnabled);
		if (isEnabled) {
			samZButton.setVisible(true);
			drumDownButton.setVisible(true);
		} else {
			samZButton.setVisible(false);
			drumDownButton.setVisible(false);
		}
	}

	public void setJoeyPartVisible(boolean isVisible) {
		passButton.setVisible(isVisible);
		joeyButton.setVisible(isVisible);
	}
	
	
}
