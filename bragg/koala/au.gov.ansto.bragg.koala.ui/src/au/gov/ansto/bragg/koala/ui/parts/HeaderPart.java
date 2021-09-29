/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import au.gov.ansto.bragg.koala.ui.Activator;
import au.gov.ansto.bragg.koala.ui.internal.KoalaImage;

/**
 * @author nxi
 *
 */
public class HeaderPart extends Composite {

	private Button physiButton;
	
	/**
	 * @param parent
	 * @param style
	 */
	public HeaderPart(Composite parent, int style) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);
		addDashboardButton(this);
	}

	private void addDashboardButton(Composite parent) {
		physiButton = new Button(this, SWT.PUSH);
		physiButton.setImage(KoalaImage.PHYSICS64.getImage());
		physiButton.setCursor(Activator.getHandCursor());
		GridDataFactory.fillDefaults().grab(false, true).align(SWT.BEGINNING, SWT.CENTER).applyTo(physiButton);
		physiButton.setToolTipText("Current mode: Physics. Click here to change the mode.");

		physiButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				getParentViewer().getMainPart().showProposalPanel();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		final Button dashboardButton = new Button(parent, SWT.TOGGLE);
		dashboardButton.setCursor(Activator.getHandCursor());
		dashboardButton.setImage(KoalaImage.WEATHER64.getImage());
	    GridDataFactory.fillDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(dashboardButton);
	    dashboardButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(final SelectionEvent e) {
			    Display.getCurrent().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						try {
							getParentViewer().getMainPart().showEnvironmentPanel();
						} catch (Exception e2) {
						}
						
					    final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						
						IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
						for (IWorkbenchPage page : pages) {
							try {
								IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
								for (IPerspectiveDescriptor perspective : perspectives) {
									if (KoalaMainPerspective.ID_KOALA_MAIN_PERSPECTIVE.equals(perspective.getId())){
										IWorkbenchPartReference myView = page.findViewReference(KoalaMainPerspective.ID_CRUISE_PANEL_VIEW);
										if (dashboardButton.getSelection()) {
											page.setPartState(myView, IWorkbenchPage.STATE_RESTORED);
										} else {
											page.setPartState(myView, IWorkbenchPage.STATE_MINIMIZED);
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	public void enablePhysicsButton() {
		physiButton.setVisible(true);
	}
	
	public void disablePhysicsButton() {
		physiButton.setVisible(false);
	}

	private KoalaMainViewer getParentViewer() {
		return (KoalaMainViewer) getParent();
	}
}
