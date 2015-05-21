/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Norman Xiong (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.emu.ui.internal;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class EmuStatusPerspective implements IPerspectiveFactory {

	public static final String EXPERIMENT_PERSPECTIVE_ID = "au.gov.ansto.bragg.emu.ui.StatusPerspective";
//	public static final String EXPERIMENT_PERSPECTIVE_THEME = "au.gov.ansto.bragg.pelican.ui.theme";
//	public static final String DEFAULT_PERSPECTIVE_THEME = "au.gov.ansto.bragg.nbi.ui.EmptyPerspective";
	public static final String EMU_HM_VIEW_ID = "au.gov.ansto.bragg.emu.ui.views.EmuHMView";

	public static final String ID_VIEW_ACTIVITY_MONITOR = "au.gov.ansto.bragg.nbi.ui.SicsRealtimeDataView";

	
	public void createInitialLayout(IPageLayout factory) {
		
		
		factory.addPerspectiveShortcut(EXPERIMENT_PERSPECTIVE_ID);


		factory.addStandaloneView(EMU_HM_VIEW_ID, false, 
				IPageLayout.LEFT, 0.6f, factory.getEditorArea());
		
		factory.addStandaloneView(ID_VIEW_ACTIVITY_MONITOR, false, 
				IPageLayout.BOTTOM, 0.60f, EMU_HM_VIEW_ID);


		factory.setEditorAreaVisible(false);
		
		
	}


}
