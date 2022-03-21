package org.gumtree.ui.service.multimonitor;

import org.eclipse.ui.IWorkbenchWindow;
import org.gumtree.core.service.IService;

public interface IMultiMonitorManager extends IService{

	// Manipulate on the current active window
	public void showPerspectiveOnActiveWindow(String perspectiveId, int monitorId, boolean maximized);
	
	public boolean openWorkbenchWindow(int monitorId, boolean maximized);
	
	// Open and manipulate a new window
	public boolean openWorkbenchWindow(String perspectiveId, int monitorId, boolean maximized);
	
	// Show perspective on an opened window on a particular monitor
	public void showPerspectiveOnWindow(String perspectiveId, IWorkbenchWindow window, int monitorId, boolean maximized);
	
	// Show perspective on an opened window on a particular monitor, where windowId is the
	// interal order of the window
	public void showPerspectiveOnOpenedWindow(String perspectiveId, int windowId, int monitorId, boolean maximized);
	
	// invalid monitor id is still ok, but will not be maximized
	public void manipulateWindow(IWorkbenchWindow window, int monitorId, boolean maximized);
	
	// Making a best guess on the display system
	// Currently either multi monitors or Matrox's triple-head-to-go
	// are considered to be multi monitor system.
	// TODO: Need better decision making on non-triple-head-to-go system
	public boolean isMultiMonitorSystem();
	
	public int getMonitorCounts();
	
	public int getMonitorWidth();
}
