package org.gumtree.app.workbench.support;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.actions.NewWizardShortcutAction;
import org.eclipse.ui.wizards.IWizardDescriptor;

public class NewWizardHandler extends AbstractHandler {

	private static final String PARAM_NEW_WIZARD_ID = "newWizardId";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			String newWizardId = event.getParameter(PARAM_NEW_WIZARD_ID);
			IWizardDescriptor desc = WorkbenchPlugin.getDefault()
					.getNewWizardRegistry().findWizard(newWizardId);
			if (desc != null) {
				IAction action = new NewWizardShortcutAction(PlatformUI
						.getWorkbench().getActiveWorkbenchWindow(), desc);
				action.run();
			}
		} catch (Exception e) {
			throw new ExecutionException("Failed to execute new wizard command", e);
		}
		
		return null;
	}

}
