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

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import au.gov.ansto.bragg.quokka.ui.QuokkaUIConstants;

public class QuokkaScanPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		defineActions(layout);
        defineLayout(layout);
	}

	private void defineActions(IPageLayout layout) {
	}
	
	private void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.addStandaloneView(QuokkaUIConstants.ID_VIEW_QUOKKA_SCAN, false, IPageLayout.LEFT, 1.0f, editorArea);
		layout.getViewLayout(QuokkaUIConstants.ID_VIEW_QUOKKA_SCAN).setCloseable(false);
		layout.getViewLayout(QuokkaUIConstants.ID_VIEW_QUOKKA_SCAN).setMoveable(false);
	}
	
}
