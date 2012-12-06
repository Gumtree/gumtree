package au.gov.ansto.bragg.echidna.workbench.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;

public class OpenCustomisedTemperatureExperimentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IPerspectiveDescriptor persp = PlatformUI
				.getWorkbench()
				.getPerspectiveRegistry()
				.findPerspectiveWithId(
						"au.gov.ansto.bragg.echidna.ui.internal.TCLRunnerPerspective");
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.setPerspective(persp);
		return null;
	}

}
