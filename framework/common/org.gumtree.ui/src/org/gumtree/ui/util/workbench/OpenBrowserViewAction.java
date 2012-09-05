package org.gumtree.ui.util.workbench;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.gumtree.ui.internal.Activator;
import org.gumtree.widgets.swt.util.UIResourceUtils;

public abstract class OpenBrowserViewAction extends Action {
	
	public OpenBrowserViewAction() {
		this("Open Browser");
	}

	public OpenBrowserViewAction(String text) {
		this(text, UIResourceUtils.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/document.gif"));
	}

	public OpenBrowserViewAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public void run() {
		
		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		String urlString = getURL();
		try {
			IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR | IWorkbenchBrowserSupport.AS_VIEW | IWorkbenchBrowserSupport.STATUS | IWorkbenchBrowserSupport.PERSISTENT, toString(), getTitle(), urlString);
			browser.openURL(new URL(getURL()));
		} catch (PartInitException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Cannot open URL", "Cannot open " + urlString + "\nError: " + e.getMessage());
			e.printStackTrace();
		} catch (MalformedURLException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Cannot open URL", "Cannot open " + urlString + "\nError: " + e.getMessage());
			e.printStackTrace();
		}
		
//		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//		String secondaryId = Integer.toString(BrowserView.getAndIncreaseViewActivationCount());
//		try {
//			IViewPart part = page.showView(ISEEConstants.ID_VIEW_BROWSER, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
//			String url = getURL();
//			if(part != null && part instanceof IBrowserView && url != null) {
//				((IBrowserView)part).setURL(url);
//				((IBrowserView)part).setTitle(getTitle());
//			}
//		} catch (PartInitException e) {
//			e.printStackTrace();
//		}
	}

	public abstract String getURL();

	public abstract String getTitle();

}
