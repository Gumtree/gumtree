package au.gov.ansto.bragg.nbi.ui.internal;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;

public class KakaduLauncher extends AbstractLauncher {

	public KakaduLauncher() {
		super();
	}

	public void launch() throws LauncherException {
		IPerspectiveDescriptor perspective = PlatformUI.getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(
						"au.gov.ansto.bragg.kakadu.ui.KakaduPerspective");
		if (perspective != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().setPerspective(perspective);
		}
		// IMultiMonitorManager mmManager =
		// GTPlatformUI.getMultiMonitorManager();
		// mmManager.openWorkbenchWindow(
		// "au.gov.ansto.bragg.kakadu.ui.KakaduPerspective", 1, true);
	}

}
