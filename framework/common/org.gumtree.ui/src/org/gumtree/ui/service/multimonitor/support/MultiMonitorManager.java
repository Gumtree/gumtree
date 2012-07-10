package org.gumtree.ui.service.multimonitor.support;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiMonitorManager implements IMultiMonitorManager {

	private static Logger logger = LoggerFactory.getLogger(MultiMonitorManager.class);
	
	// TODO: make this reconfigurable
	private static final int RESOLUTION_WIDTH = 1280;
	
	public MultiMonitorManager() {
		super();
	}
	
	public void showPerspectiveOnActiveWindow(String perspectiveId, int monitorId, boolean maximized) {
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		showPerspectiveOnWindow(perspectiveId, activeWindow, monitorId, maximized);
	}
	
	public void showPerspectiveOnWindow(String perspectiveId, IWorkbenchWindow window, int monitorId, boolean maximized) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		try {
			workbench.showPerspective(perspectiveId, window);
			manipulateWindow(window, monitorId, maximized);
		} catch (WorkbenchException e) {
			logger.error("Error occured in showPerspectiveOnWindow.", e);
		}
	}
	
	public void showPerspectiveOnOpenedWindow(String perspectiveId, int windowId, int monitorId, boolean maximized) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (windowId >= workbench.getWorkbenchWindowCount()) {
			// windowId is too high, do nothing
			return;
		} else {
			IWorkbenchWindow window = workbench.getWorkbenchWindows()[windowId];
			showPerspectiveOnWindow(perspectiveId, window, monitorId, maximized);
		}
	}
	
	public boolean openWorkbenchWindow(int monitorId, boolean maximized) {
		IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		return openWorkbenchWindow(registry.getDefaultPerspective(), monitorId, maximized);
	}
	
	// id start counting from zero
	// return true if it is opened
	// actual location is not guaranteed
	public boolean openWorkbenchWindow(String perspectiveId, int monitorId, boolean maximized) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWindow.getActivePage();
		try {
			IWorkbenchWindow window = workbench.openWorkbenchWindow(perspectiveId, activePage.getInput());
			manipulateWindow(window, monitorId, maximized);
		} catch (WorkbenchException e) {
			logger.error("Cannot open new window for perspective " + perspectiveId, e);
			// Try to recover by opening within the same window
			try {
				workbench.showPerspective(perspectiveId, activeWindow);
				activeWindow.getShell().setMaximized(maximized);
			} catch (WorkbenchException e1) {
				logger.error("Cannot open new perspective " + perspectiveId, e1);
				return false;
			}
		}
		return true;
	}
	
	public boolean isMultiMonitorSystem() {
		Monitor[] monitors = Display.getDefault().getMonitors();
		int firstMonitorWidth = Display.getDefault().getMonitors()[0].getBounds().width;
		return monitors.length > 1 || 
			(firstMonitorWidth  / RESOLUTION_WIDTH > 1 && firstMonitorWidth % RESOLUTION_WIDTH == 0);
	}
	
	public void manipulateWindow(IWorkbenchWindow window, int monitorId, boolean maximized) {
		Monitor[] monitors = Display.getDefault().getMonitors();
		boolean capped = false;
		if (monitors.length > monitorId) {
			// Multi-monitor system with correct monitorId
			Rectangle bounds = monitors[monitorId].getBounds();
			window.getShell().setLocation(bounds.x, bounds.y);
		} else {
			// use the last monitor
			Rectangle bounds = monitors[monitors.length - 1].getBounds();
			if (bounds.width / RESOLUTION_WIDTH > 1 &&
					bounds.width % RESOLUTION_WIDTH ==0) {
				// Special case --> Triple head to go
				int lastMonitor = (bounds.width / RESOLUTION_WIDTH ) - 1;
				if (monitorId > lastMonitor) {
					// cap to 3 monitor system
					monitorId = lastMonitor;
					capped = true;
				}
				window.getShell().setLocation(RESOLUTION_WIDTH * monitorId, bounds.y);
			} else {
				// Normal capped case
				window.getShell().setLocation(bounds.x, bounds.y);
				capped = true;
			}
		}
		window.getShell().setMaximized(maximized && !capped);
	}
	
	public static void main(String[] args) {
		int firstMonitorWidth = 1280;
		// false
		System.out.println((firstMonitorWidth  / RESOLUTION_WIDTH > 1 && firstMonitorWidth % RESOLUTION_WIDTH == 0));
		firstMonitorWidth = 1280 * 2;
		// true
		System.out.println((firstMonitorWidth  / RESOLUTION_WIDTH > 1 && firstMonitorWidth % RESOLUTION_WIDTH == 0));
		firstMonitorWidth = 1280 * 3;
		// true
		System.out.println((firstMonitorWidth  / RESOLUTION_WIDTH > 1 && firstMonitorWidth % RESOLUTION_WIDTH == 0));
		firstMonitorWidth = 1280 * 4;
		// true
		System.out.println((firstMonitorWidth  / RESOLUTION_WIDTH > 1 && firstMonitorWidth % RESOLUTION_WIDTH == 0));
		firstMonitorWidth = 1280 + 200;
		// false
		System.out.println((firstMonitorWidth  / RESOLUTION_WIDTH > 1 && firstMonitorWidth % RESOLUTION_WIDTH == 0));
	}
	
}
