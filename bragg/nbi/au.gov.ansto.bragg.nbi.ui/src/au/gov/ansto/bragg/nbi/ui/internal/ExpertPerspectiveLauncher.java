package au.gov.ansto.bragg.nbi.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.internal.ui.actions.SicsControlLaunchAction;
import org.gumtree.gumnix.sics.internal.ui.actions.SicsTerminalLaunchAction;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;

public class ExpertPerspectiveLauncher extends AbstractLauncher {

	public ExpertPerspectiveLauncher() {
		super();
	}

	public void launch() throws LauncherException {
		IMultiMonitorManager mmManager = ServiceUtils.getService(IMultiMonitorManager.class);
		mmManager.openWorkbenchWindow("org.gumtree.gumnix.sics.ui.sicsPerspective", 1, true);
		
		// hack
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[1];
		
		// Open table tree
		Action action = new SicsControlLaunchAction(window);
		action.run();
		
		// Open terminal
		action = new SicsTerminalLaunchAction(window);
		action.run();
		
	}

}
