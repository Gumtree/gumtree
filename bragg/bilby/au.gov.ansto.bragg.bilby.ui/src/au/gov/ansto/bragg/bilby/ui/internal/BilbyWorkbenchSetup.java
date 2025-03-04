package au.gov.ansto.bragg.bilby.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.gumtree.ui.service.multimonitor.support.MultiMonitorManager;
import org.gumtree.ui.util.SafeUIRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for launching the special Platypus workbench layout during
 * start up.
 * 
 * @author nxi
 *
 */
public class BilbyWorkbenchSetup implements IStartup {

	private static final String PROP_START_EXP_LAYOUT = "gumtree.startExperimentLayout";
	private static final String ID_PERSPECTIVE_SICS = "au.gov.ansto.bragg.nbi.ui.SICSExperimentPerspective";
	private static final String ID_PERSPECTIVE_SCRIPTING = "au.gov.ansto.bragg.nbi.ui.scripting.ScriptingPerspective";
	private static final String ID_PERSPECTIVE_ANALYSIS = "au.gov.ansto.bragg.nbi.ui.scripting.StandAloneScriptingPerspective";
	private static final String ID_PERSPECTIVE_CONTROL = "au.gov.ansto.bragg.nbi.ui.ControlExperimentPerspective";
	private static final String USE_NEW_PROXY = "gumtree.sics.useNewProxy";

	private boolean newProxy;
	private String sicsPID;
	
	private static Logger logger = LoggerFactory.getLogger(BilbyWorkbenchSetup.class);
	
	public BilbyWorkbenchSetup() {
		newProxy = Boolean.valueOf(System.getProperty(USE_NEW_PROXY, "false"));
		if (newProxy) {
			sicsPID = ID_PERSPECTIVE_CONTROL;
		} else {
			sicsPID = ID_PERSPECTIVE_SICS;
		}
	}
	public void earlyStartup() {
		String launchBilbyLayout = System.getProperty(PROP_START_EXP_LAYOUT, "false");
		// [GT-73] Launch Bilby layout if required
		if (Boolean.parseBoolean(launchBilbyLayout)) {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Bilby workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					hideMenus((WorkbenchWindow) activeWorkbenchWindow);
					if (activeWorkbenchWindow instanceof WorkbenchWindow) {
//						activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
						IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
						for (IWorkbenchPage page : pages) {
							try {
								IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
								for (IPerspectiveDescriptor perspective : perspectives) {
									if (!sicsPID.equals(perspective.getId())){
										activeWorkbenchWindow.getActivePage().closePerspective(perspective, false, true);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						activeWorkbenchWindow.addPerspectiveListener(new IPerspectiveListener() {
							
							@Override
							public void perspectiveChanged(IWorkbenchPage page,
									IPerspectiveDescriptor perspective, String changeId) {
								hideMenus((WorkbenchWindow) activeWorkbenchWindow);
							}
							
							@Override
							public void perspectiveActivated(IWorkbenchPage page,
									IPerspectiveDescriptor perspective) {
								hideMenus((WorkbenchWindow) activeWorkbenchWindow);
							}
						});
					}
					
					PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {
						
						@Override
						public void windowOpened(IWorkbenchWindow window) {
							hideMenus((WorkbenchWindow) window);
						}
						
						@Override
						public void windowDeactivated(IWorkbenchWindow window) {
						}
						
						@Override
						public void windowClosed(IWorkbenchWindow window) {
						}
						
						@Override
						public void windowActivated(IWorkbenchWindow window) {
							hideMenus((WorkbenchWindow) window);
						}
					});
					
					IMultiMonitorManager mmManager = new MultiMonitorManager();

					mmManager.showPerspectiveOnOpenedWindow(sicsPID, 0, 0, mmManager.isMultiMonitorSystem());
					
					if (PlatformUI.getWorkbench().getWorkbenchWindowCount() < 2) {
						// open new window as editor buffer
						mmManager.openWorkbenchWindow(BilbyWorkflowPerspective.ID_BILBY_WORKFLOW_PERSPECTIVE, 1, true);
					}
					
//					mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_ANALYSIS, 1, 1, mmManager.isMultiMonitorSystem());
				}			
			});
		} else {
			SafeUIRunner.asyncExec(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					logger.error("Failed to launch Bilby workbench layout during early startup.", exception);
				}
				public void run() throws Exception {
					final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (activeWorkbenchWindow instanceof WorkbenchWindow) {
//						activeWorkbenchWindow.getActivePage().closeAllPerspectives(true, false);
						IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
						for (IWorkbenchPage page : pages) {
							try {
								IPerspectiveDescriptor[] perspectives = page.getOpenPerspectives();
								for (IPerspectiveDescriptor perspective : perspectives) {
									activeWorkbenchWindow.getActivePage().closePerspective(perspective, false, true);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					IMultiMonitorManager mmManager = new MultiMonitorManager();

					mmManager.showPerspectiveOnOpenedWindow(ID_PERSPECTIVE_SCRIPTING, 0, 0, mmManager.isMultiMonitorSystem());
				}			
			});
		}
	}

	private void hideMenus(WorkbenchWindow window){
		WorkbenchWindow workbenchWin = (WorkbenchWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MenuManager menuManager = ((WorkbenchWindow) window).getMenuManager();
		IContributionItem[] items = menuManager.getItems();

		for(IContributionItem item : items) {
		  item.setVisible(false);
		}
		menuManager.setVisible(false);
		menuManager.setRemoveAllWhenShown(true);
	    
	    IContributionItem[] menubarItems = ((WorkbenchWindow) window).getMenuBarManager().getItems();
	    for (IContributionItem item : menubarItems) {
	    	item.setVisible(false);
	    }
	    ((WorkbenchWindow) window).getMenuBarManager().setVisible(false);
	    ((WorkbenchWindow) window).getMenuBarManager().setRemoveAllWhenShown(true);
	    
	}
	
}
