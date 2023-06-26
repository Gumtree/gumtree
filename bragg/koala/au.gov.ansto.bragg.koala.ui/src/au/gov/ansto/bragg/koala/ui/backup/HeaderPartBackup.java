/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.backup;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;
import au.gov.ansto.bragg.koala.ui.parts.KoalaConstants.KoalaMode;
import au.gov.ansto.bragg.koala.ui.parts.KoalaMainViewer;

/**
 * @author nxi
 *
 */
public class HeaderPartBackup extends Composite {

	private Button modeButton;
	private Label titleLabel;
	private Button dashboardButton;
	
	/**
	 * @param parent
	 * @param style
	 */
	public HeaderPartBackup(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(this);
		addDashboardButton(this);
	}

	private void addDashboardButton(Composite parent) {
		modeButton = new Button(this, SWT.PUSH);
		modeButton.setImage(KoalaImage.CHEMISTRY64.getImage());
		modeButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).applyTo(modeButton);
		modeButton.setToolTipText("Current mode: Chemistry. Click here to change the mode.");

		modeButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				getParentViewer().getMainPart().showProposalPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		titleLabel = new Label(this, SWT.NONE);
		titleLabel.setText("");
		titleLabel.setFont(Activator.getLargeFont());
		GridDataFactory.fillDefaults().grab(true, false).minSize(600, 36).align(SWT.BEGINNING, SWT.CENTER).applyTo(titleLabel);

		dashboardButton = new Button(parent, SWT.TOGGLE);
		dashboardButton.setCursor(Activator.getHandCursor());
		dashboardButton.setImage(KoalaImage.WEATHER64.getImage());
	    GridDataFactory.fillDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(dashboardButton);
	    dashboardButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
				
				getParentViewer().getMainPart().showEnvironmentPanel();
				
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
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
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
			modeButton.setImage(KoalaImage.PHYSICS64.getImage());
			modeButton.setToolTipText("Current mode: Physics. Click here to change the mode.");
		} else {
			modeButton.setImage(KoalaImage.CHEMISTRY64.getImage());
			modeButton.setToolTipText("Current mode: Chemistry. Click here to change the mode.");
		}
	}
	
	private KoalaMainViewer getParentViewer() {
		return (KoalaMainViewer) getParent();
	}
	
	public void setTitle(String title) {
		this.titleLabel.setText(title);
	}

	public void setButtonEnabled(boolean isEnabled) {
		modeButton.setEnabled(isEnabled);
		dashboardButton.setEnabled(isEnabled);
	}

}
