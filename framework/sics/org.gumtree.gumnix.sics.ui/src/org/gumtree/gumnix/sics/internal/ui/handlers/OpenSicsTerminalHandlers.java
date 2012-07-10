package org.gumtree.gumnix.sics.internal.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.gumtree.gumnix.sics.internal.ui.actions.SicsTerminalLaunchAction;

public class OpenSicsTerminalHandlers extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IAction action = new SicsTerminalLaunchAction();
		action.run();
		return null;
	}

}
