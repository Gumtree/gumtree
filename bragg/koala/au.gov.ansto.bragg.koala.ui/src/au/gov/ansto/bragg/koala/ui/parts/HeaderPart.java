/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.KoalaConstants.KoalaMode;
import au.gov.ansto.bragg.koala.ui.parts.MainPart.PanelName;

/**
 * @author nxi
 *
 */
public class HeaderPart extends Composite {

	private final static int NUM_BUTTONS = 5;
	private CLabel experimentSetupButton;
	private CLabel crystalAlignButton;
	private CLabel testImageButton;
	private CLabel fullExperimentButton;
	private CLabel adminPageButton;
	private CLabel[] buttons;
	private CLabel selectedButton;
	private boolean isEnabled = true;
	
	/**
	 * @param parent
	 * @param style
	 */
	public HeaderPart(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().spacing(1, SWT.DEFAULT).numColumns(NUM_BUTTONS + 1).applyTo(this);
		addDashboardButton(this);
	}

	private void addDashboardButton(Composite parent) {
		
	
		experimentSetupButton = new CLabel(this, SWT.NONE);
		experimentSetupButton.setImage(KoalaImage.HOME64.getImage());
		experimentSetupButton.setText(" Experiment Setup  ");
		experimentSetupButton.setCursor(Activator.getHandCursor());
		experimentSetupButton.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(experimentSetupButton);
		experimentSetupButton.setToolTipText("Show Experiment Setup Panel");

		experimentSetupButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				if (isEnabled) {
					buttonSelected(experimentSetupButton);
					getParentViewer().getMainPart().showProposalPanel();
				}
			}
		});
		
		experimentSetupButton.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseEnter(MouseEvent e) {
				if (isEnabled && selectedButton != experimentSetupButton) {
					highlightButton(experimentSetupButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (isEnabled && selectedButton != experimentSetupButton) {
					highlightButton(experimentSetupButton, false);
				}
			}
		});
		
		crystalAlignButton = new CLabel(this, SWT.NONE);
		crystalAlignButton.setText(" Crystal Align  ");
		crystalAlignButton.setFont(Activator.getLargeFont());
		crystalAlignButton.setToolTipText("Show crystal alignment panel.");
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(crystalAlignButton);

		crystalAlignButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				if (isEnabled) {
					buttonSelected(crystalAlignButton);
					getParentViewer().getMainPart().showCrystalPanel();
				}
			}
		});
		
		crystalAlignButton.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseEnter(MouseEvent e) {
				if (isEnabled && selectedButton != crystalAlignButton) {
					highlightButton(crystalAlignButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (isEnabled && selectedButton != crystalAlignButton) {
					highlightButton(crystalAlignButton, false);
				}
			}
		});
		
		testImageButton = new CLabel(parent, SWT.NONE);
		testImageButton.setCursor(Activator.getHandCursor());
		testImageButton.setText(" Test Image  ");
		testImageButton.setFont(Activator.getLargeFont());
		testImageButton.setToolTipText("Show the panel to test image collection.");
//		testImageButton.setImage(KoalaImage.WEATHER64.getImage());
	    GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(testImageButton);
	    testImageButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				
				if (isEnabled) {
					buttonSelected(testImageButton);
					getParentViewer().getMainPart().showInitScanPanel();
				}				
//			    Display.getCurrent().asyncExec(new Runnable() {
//					
//					@Override
//					public void run() {
//						try {
//							getParentViewer().getMainPart().showEnvironmentPanel();
//						} catch (Exception e2) {
//						}
//						
//					    final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//						
//						IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
//						for (IWorkbenchPage page : pages) {
//							try {
//								IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
//								for (IPerspectiveDescriptor perspective : perspectives) {
//									if (KoalaMainPerspective.ID_KOALA_MAIN_PERSPECTIVE.equals(perspective.getId())){
//										IWorkbenchPartReference myView = page.findViewReference(KoalaMainPerspective.ID_CRUISE_PANEL_VIEW);
//										if (dashboardButton.getSelection()) {
//											page.setPartState(myView, IWorkbenchPage.STATE_RESTORED);
//										} else {
//											page.setPartState(myView, IWorkbenchPage.STATE_MINIMIZED);
//										}
//									}
//								}
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//						}
//					}
//				});
			}
			
		});

	    testImageButton.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseEnter(MouseEvent e) {
				if (isEnabled && selectedButton != testImageButton) {
					highlightButton(testImageButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (isEnabled && selectedButton != testImageButton) {
					highlightButton(testImageButton, false);
				}
			}
		});
		
	    fullExperimentButton = new CLabel(this, SWT.NONE);
	    fullExperimentButton.setText(" Full Experiment  ");
	    fullExperimentButton.setImage(KoalaImage.CHEMISTRY64.getImage());
	    fullExperimentButton.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(fullExperimentButton);

		fullExperimentButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				if (isEnabled) {
					buttonSelected(fullExperimentButton);
					getParentViewer().getMainPart().showPhysicsPanel();
					if (getParentViewer().getMainPart().getInstrumentMode() == KoalaMode.CHEMISTRY) {
						getParentViewer().getMainPart().showChemistryPanel();
					} else {
						getParentViewer().getMainPart().showPhysicsPanel();
					}
				}
			}
		});
		
		fullExperimentButton.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseEnter(MouseEvent e) {
				if (isEnabled && selectedButton != fullExperimentButton) {
					highlightButton(fullExperimentButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (isEnabled && selectedButton != fullExperimentButton) {
					highlightButton(fullExperimentButton, false);
				}
			}
		});
		
		adminPageButton = new CLabel(this, SWT.NONE);
		adminPageButton.setText(" Admin Page  ");
		adminPageButton.setFont(Activator.getLargeFont());
		adminPageButton.setToolTipText("Show administration panel. You'll be prompted to login as instrument scientist.");
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(adminPageButton);

		adminPageButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				if (isEnabled) {
					if (getParentViewer().getMainPart().getCurrentPanelName() == PanelName.ADMIN) {
						return;
					}
					PasswordDialog dialog = new PasswordDialog(getShell());
					if (dialog.open() == Window.OK) {
//			            String user = dialog.getUser();
			            String pw = dialog.getPassword();
			            if (Activator.isPassDisabled() || MainPart.UNLOCK_TEXT.equals(pw)) {
			            	buttonSelected(adminPageButton);
			            	getParentViewer().getMainPart().showAdminPanel();
			            } else {
			            	MessageDialog.openWarning(getShell(), "Warning", "Invalid passcode");
			            }
			        }
				}
			}
		});
		
		adminPageButton.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseEnter(MouseEvent e) {
				if (isEnabled && selectedButton != adminPageButton) {
					highlightButton(adminPageButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (isEnabled && selectedButton != adminPageButton) {
					highlightButton(adminPageButton, false);
				}
			}
		});
		
		Label emptySpace = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(emptySpace);
		
		buttons = new CLabel[] {
			experimentSetupButton, 
			crystalAlignButton,
			testImageButton,
			fullExperimentButton,
			adminPageButton
		};
		
		buttonSelected(experimentSetupButton);
	}

//	public void enablePhysicsButton() {
//		modeButton.setVisible(true);
//	}
//	
//	public void disablePhysicsButton() {
//		modeButton.setVisible(false);
//	}

	public void setMode(KoalaMode mode) {
		if (mode == KoalaMode.PHYSICS) {
			if (selectedButton == fullExperimentButton) {
				fullExperimentButton.setImage(KoalaImage.PHYSICS64.getImage());
			} else {
				fullExperimentButton.setImage(KoalaImage.PHYSICS_GRAY64.getImage());
			}
			fullExperimentButton.setToolTipText("Current mode: Physics.");
		} else {
			if (selectedButton == fullExperimentButton) {
				fullExperimentButton.setImage(KoalaImage.CHEMISTRY64.getImage());
			} else {
				fullExperimentButton.setImage(KoalaImage.CHEMISTRY_GRAY64.getImage());
			}
			fullExperimentButton.setToolTipText("Current mode: Chemistry.");
		}
	}
	
	private KoalaMainViewer getParentViewer() {
		return (KoalaMainViewer) getParent();
	}
	
	public void setTitle(String title) {
//		this.crystalAlignButton.setText(title);
	}

	public void setButtonEnabled(boolean isEnabled) {
//		experimentSetupButton.setEnabled(isEnabled);
//		testImageButton.setEnabled(isEnabled);
		this.isEnabled = isEnabled;
		if (isEnabled) {
			buttonSelected(selectedButton);
		} else {
			for (int i = 0; i < buttons.length; i++) {
				CLabel button = buttons[i];
				button.setBackground(Activator.getRunningForgroundColor());
				button.setForeground(Activator.getVeryLightForgroundColor());
				button.setCursor(Activator.getDefaultCursor());
			}
		}
	}

	public void buttonSelected(final CLabel selectedButton) {
		this.selectedButton = selectedButton;
		for (int i = 0; i < buttons.length; i++) {
			CLabel button = buttons[i];
			setButtonFront(button, button == selectedButton);
		}
	}
	
	private void setButtonFront(CLabel button, boolean isFront) {
		if (isFront) {
			button.setBackground(getBackground());
			button.setForeground(Activator.getForgroundColor());
			button.setCursor(Activator.getDefaultCursor());
			if (button == experimentSetupButton) {
				experimentSetupButton.setImage(KoalaImage.HOME64.getImage());
			} else if (button == fullExperimentButton) {
				if (getParentViewer().getMainPart().getInstrumentMode() == KoalaMode.CHEMISTRY) {
					fullExperimentButton.setImage(KoalaImage.CHEMISTRY64.getImage());
				} else {
					fullExperimentButton.setImage(KoalaImage.PHYSICS64.getImage());
				}
			}
		} else {
			button.setBackground(Activator.getRunningForgroundColor());
			button.setForeground(Activator.getVeryLightForgroundColor());
			button.setCursor(Activator.getHandCursor());
			if (button == experimentSetupButton) {
				experimentSetupButton.setImage(KoalaImage.HOME_GRAY64.getImage());
			} else if (button == fullExperimentButton) {
				if (getParentViewer().getMainPart() != null) {
					if (getParentViewer().getMainPart().getInstrumentMode() == KoalaMode.CHEMISTRY) {
						fullExperimentButton.setImage(KoalaImage.CHEMISTRY_GRAY64.getImage());
					} else {
						fullExperimentButton.setImage(KoalaImage.PHYSICS_GRAY64.getImage());
					}
				} else {
					fullExperimentButton.setImage(KoalaImage.CHEMISTRY_GRAY64.getImage());
				}
			}
		}
	}
	
	private void highlightButton(CLabel button, boolean isHighlighted) {
		if (isHighlighted) {
			button.setBackground(Activator.getHighlightBackgroundColor());
			button.setForeground(Activator.getLightForgroundColor());
		} else {
			button.setBackground(Activator.getRunningForgroundColor());
			button.setForeground(Activator.getVeryLightForgroundColor());
		}
	}
	
}
