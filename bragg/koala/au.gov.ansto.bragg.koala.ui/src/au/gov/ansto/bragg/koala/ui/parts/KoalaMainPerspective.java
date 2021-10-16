/**
 * 
 */
package au.gov.ansto.bragg.koala.ui.parts;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

import au.gov.ansto.bragg.koala.ui.KoalaWorkbenchSetup;

/**
 * @author nxi
 *
 */
public class KoalaMainPerspective implements IPerspectiveFactory {

	public static final String ID_KOALA_MAIN_PERSPECTIVE = "au.gov.ansto.bragg.koala.ui.KoalaMainPerspective";
	public static final String ID_KOALA_CONTROL_VIEW = "au.gov.ansto.bragg.koala.ui.KoalaMainView";
	public static final String ID_CRUISE_PANEL_VIEW = "org.gumtree.ui.cruise.CruisePanelView";

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.addStandaloneView(ID_KOALA_CONTROL_VIEW, false, 
				IPageLayout.LEFT, 1.f, layout.getEditorArea());
		layout.setFixed(true);

//		IFolderLayout folder = layout.createFolder("stack_folder", IPageLayout.RIGHT, 0.85f, ID_KOALA_CONTROL_VIEW);
//		folder.addView(ID_CRUISE_PANEL_VIEW);
//		layout.getViewLayout(ID_CRUISE_PANEL_VIEW).setCloseable(false);
		
//		folder.addView(PROJECT_EXPLORER_VIEW_ID);
//		factory.addView(PLOT_VIEW_ID, IPageLayout.RIGHT, 0.5f, SCRIPT_CONSOLE_VIEW_ID);

//		factory.addStandaloneViewPlaceholder(PLOT1_VIEW_ID, IPageLayout.RIGHT, 0.50f, factory.getEditorArea(), false);

		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		workbenchWindow.addPerspectiveListener(new PerspectiveAdapter() {
			@Override
			public void perspectiveActivated(final IWorkbenchPage page,
					IPerspectiveDescriptor perspective) {
				super.perspectiveOpened(page, perspective);
				final PerspectiveAdapter adapter = this;
				Display.getDefault().asyncExec(new Runnable(){

					public void run() {
						try{
							workbenchWindow.getActivePage().setEditorAreaVisible(false);
								try {
									IWorkbenchPartReference myView = page.findViewReference(ID_CRUISE_PANEL_VIEW);
									page.setPartState(myView, IWorkbenchPage.STATE_MINIMIZED);
//									KoalaWorkbenchSetup.hideMenus((WorkbenchWindow) workbenchWindow);
								} catch (Exception e) {
								}
							workbenchWindow.removePerspectiveListener(adapter);
							workbenchWindow.getShell().setMaximized(true);
						}catch (Exception e) {
						}
					}});
			}
			
			public void perspectiveChanged(IWorkbenchPage page,
					IPerspectiveDescriptor perspective, String changeId) {
				if (perspective.getId().equals(ID_KOALA_MAIN_PERSPECTIVE)) {
					workbenchWindow.getActivePage().setEditorAreaVisible(false);
				}
			}
			
		});

	}

}
