/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.parts.KoalaConstants.KoalaMode;

/**
 * @author nxi
 *
 */
public class HeaderPart extends Composite {

	private final static int NUM_BUTTONS = 5;
	private Label experimentSetupButton;
	private Label crystalAlignButton;
	private Label testImageButton;
	private Label fullExperimentButton;
	private Label adminPageButton;
	private Label[] buttons;
	private Label selectedButton;
	
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
		
	
		experimentSetupButton = new Label(this, SWT.PUSH);
//		experimentSetupButton.setImage(KoalaImage.CHEMISTRY64.getImage());
		experimentSetupButton.setText(" Experiment Setup  ");
		experimentSetupButton.setCursor(Activator.getHandCursor());
		experimentSetupButton.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(experimentSetupButton);
		experimentSetupButton.setToolTipText("Show Experiment Setup Panel");

		experimentSetupButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				buttonSelected(experimentSetupButton);
				getParentViewer().getMainPart().showProposalPanel();
			}
		});
		
		experimentSetupButton.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseEnter(MouseEvent e) {
				if (selectedButton != experimentSetupButton) {
					highlightButton(experimentSetupButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (selectedButton != experimentSetupButton) {
					highlightButton(experimentSetupButton, false);
				}
			}
		});
		
		crystalAlignButton = new Label(this, SWT.NONE);
		crystalAlignButton.setText(" Crystal Align  ");
		crystalAlignButton.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(crystalAlignButton);

		crystalAlignButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				buttonSelected(crystalAlignButton);
				getParentViewer().getMainPart().showCrystalPanel();
			}
		});
		
		crystalAlignButton.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseEnter(MouseEvent e) {
				if (selectedButton != crystalAlignButton) {
					highlightButton(crystalAlignButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (selectedButton != crystalAlignButton) {
					highlightButton(crystalAlignButton, false);
				}
			}
		});
		
		testImageButton = new Label(parent, SWT.NONE);
		testImageButton.setCursor(Activator.getHandCursor());
		testImageButton.setText(" Test Image  ");
		testImageButton.setFont(Activator.getLargeFont());
//		testImageButton.setImage(KoalaImage.WEATHER64.getImage());
	    GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(testImageButton);
	    testImageButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				
				buttonSelected(testImageButton);
				getParentViewer().getMainPart().showInitScanPanel();
				
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
				if (selectedButton != testImageButton) {
					highlightButton(testImageButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (selectedButton != testImageButton) {
					highlightButton(testImageButton, false);
				}
			}
		});
		
	    fullExperimentButton = new Label(this, SWT.NONE);
	    fullExperimentButton.setText(" Full Experiment  ");
	    fullExperimentButton.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(fullExperimentButton);

		fullExperimentButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				buttonSelected(fullExperimentButton);
				getParentViewer().getMainPart().showPhysicsPanel();
				if (getParentViewer().getMainPart().getInstrumentMode() == KoalaMode.CHEMISTRY) {
					getParentViewer().getMainPart().showChemistryPanel();
				} else {
					getParentViewer().getMainPart().showPhysicsPanel();
				}
			}
		});
		
		fullExperimentButton.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseEnter(MouseEvent e) {
				if (selectedButton != fullExperimentButton) {
					highlightButton(fullExperimentButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (selectedButton != fullExperimentButton) {
					highlightButton(fullExperimentButton, false);
				}
			}
		});
		
		adminPageButton = new Label(this, SWT.NONE);
		adminPageButton.setText(" Admin Page  ");
		adminPageButton.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(adminPageButton);

		adminPageButton.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(final MouseEvent e) {
				buttonSelected(adminPageButton);
				getParentViewer().getMainPart().showAdminPanel();
			}
		});
		
		adminPageButton.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseEnter(MouseEvent e) {
				if (selectedButton != adminPageButton) {
					highlightButton(adminPageButton, true);
				}
			}
			
			@Override
			public void mouseExit(MouseEvent e) {
				if (selectedButton != adminPageButton) {
					highlightButton(adminPageButton, false);
				}
			}
		});
		
		Label emptySpace = new Label(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(emptySpace);
		
		buttons = new Label[] {
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
//		if (mode == KoalaMode.PHYSICS) {
//			experimentSetupButton.setImage(KoalaImage.PHYSICS64.getImage());
//			experimentSetupButton.setToolTipText("Current mode: Physics. Click here to change the mode.");
//		} else {
//			experimentSetupButton.setImage(KoalaImage.CHEMISTRY64.getImage());
//			experimentSetupButton.setToolTipText("Current mode: Chemistry. Click here to change the mode.");
//		}
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
	}

	private void buttonSelected(final Label selectedButton) {
		this.selectedButton = selectedButton;
		for (int i = 0; i < buttons.length; i++) {
			Label button = buttons[i];
			setButtonFront(button, button == selectedButton);
		}
	}
	
	private void setButtonFront(Label button, boolean isFront) {
		if (isFront) {
			button.setBackground(getBackground());
			button.setForeground(Activator.getForgroundColor());
			button.setCursor(Activator.getDefaultCursor());
		} else {
			button.setBackground(Activator.getRunningForgroundColor());
			button.setForeground(Activator.getVeryLightForgroundColor());
			button.setCursor(Activator.getHandCursor());
		}
	}
	
	private void highlightButton(Label button, boolean isHighlighted) {
		if (isHighlighted) {
			button.setBackground(Activator.getHighlightBackgroundColor());
			button.setForeground(Activator.getLightForgroundColor());
		} else {
			button.setBackground(Activator.getRunningForgroundColor());
			button.setForeground(Activator.getVeryLightForgroundColor());
		}
	}
	
}
