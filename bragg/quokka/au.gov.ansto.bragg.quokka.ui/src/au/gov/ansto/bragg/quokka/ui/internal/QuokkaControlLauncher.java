/*****************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *****************************************************************************/

package au.gov.ansto.bragg.quokka.ui.internal;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.part.NullEditorInput;
import org.gumtree.core.service.ServiceUtils;
import org.gumtree.gumnix.sics.ui.SicsUIConstants;
import org.gumtree.ui.service.launcher.AbstractLauncher;
import org.gumtree.ui.service.launcher.LauncherException;
import org.gumtree.ui.service.multimonitor.IMultiMonitorManager;

public class QuokkaControlLauncher extends AbstractLauncher {

	public QuokkaControlLauncher() {
	}

	@Override
	public void launch() throws LauncherException {
		// Open control perspective on window 2
		IMultiMonitorManager mmManager = ServiceUtils.getService(IMultiMonitorManager.class);
		mmManager.openWorkbenchWindow("org.gumtree.gumnix.sics.ui.sicsPerspective", 2, true);
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new NullEditorInput(), SicsUIConstants.ID_EDITOR_SICS_CONTROL);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

}
