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

package au.gov.ansto.bragg.wombat.ui.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import au.gov.ansto.bragg.wombat.ui.WombatUIConstants;

public class DaqPerspectiveFactory implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
        defineActions(layout);
        defineLayout(layout);
	}

	 public void defineActions(IPageLayout layout) {
		 
	 }
	 
	 public void defineLayout(IPageLayout layout) {
		 String editorArea = layout.getEditorArea();
		 //Disable editor
		 layout.setEditorAreaVisible(false);
		 
		 layout.addStandaloneView(WombatUIConstants.ID_VIEW_WOMBAT_DAE, false,
					IPageLayout.TOP, 0.75f, editorArea);
		 
		 layout.addStandaloneView(WombatUIConstants.ID_VIEW_WOMBAT_CONTROL, false,
					IPageLayout.BOTTOM, 0.75f, editorArea);
		 
	 }
	 
}
