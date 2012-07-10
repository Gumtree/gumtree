/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Danil Klimontov (Bragg Institute) - initial API and implementation
 *******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;

public class KakaduPerspective implements IPerspectiveFactory {

	public static final String KAKADU_PERSPECTIVE_ID = "au.gov.ansto.bragg.kakadu.ui.KakaduPerspective";
	public static final String PLOT_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.PlotView";
	public static final String MASK_PROPERTIES_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.MaskPropertiesView";
	public static final String ALGORITHM_LIST_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.AlgorithmListView";
	public static final String DATA_SOURCE_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.DataSourceView";
	public static final String OPERATION_PARAMETERS_VIEW_ID = "au.gov.ansto.bragg.kakadu.ui.views.OperationParametersView";
	
	private IFolderLayout bottom;
	private IFolderLayout top;
	private IFolderLayout left;
	private IFolderLayout left2;
	private IPlaceholderFolderLayout left3;
	private IFolderLayout right;

	public void createInitialLayout(IPageLayout factory) {
		
		factory.addShowViewShortcut(DATA_SOURCE_VIEW_ID);
		factory.addShowViewShortcut(ALGORITHM_LIST_VIEW_ID);
		factory.addShowViewShortcut(MASK_PROPERTIES_VIEW_ID);
		
		factory.addPerspectiveShortcut(KAKADU_PERSPECTIVE_ID);

//		bottom =
//			factory.createFolder(
//				"bottomRight", //NON-NLS-1
//				IPageLayout.BOTTOM,
//				0.80f,
//				factory.getEditorArea());
//		bottom.addPlaceholder("au.gov.ansto.bragg.kakadu.ui.views.TunerPropertiesView");
		
//		top =
//			factory.createFolder(
//				"top", //NON-NLS-1
//				IPageLayout.TOP,
//				0.10f,
//				factory.getEditorArea());
//		top.addView("au.gov.ansto.bragg.cicada.vi.eclipse.views.TopBar");
//		factory.getViewLayout("top").setMoveable(false);

		left = factory.createFolder(
				"left", //NON-NLS-1
				IPageLayout.LEFT,
				0.20f,
				factory.getEditorArea());
		left.addView(DATA_SOURCE_VIEW_ID);
		
		left2 = factory.createFolder(
				"left2", //NON-NLS-1
				IPageLayout.BOTTOM,
				0.50f,
				"left");
		left2.addView(ALGORITHM_LIST_VIEW_ID);
		
//		left3 =
//			factory.createFolder(
//				"left3", //NON-NLS-1
//				IPageLayout.BOTTOM,
//				0.50f,
//				"left2");
//		left3.addPlaceholder("au.gov.ansto.bragg.kakadu.ui.views.TunerPropertiesView");

//		right = factory.createFolder(
//		"right", //NON-NLS-1
//		IPageLayout.RIGHT,
//		0.1f,
//		factory.getEditorArea());
//right.addPlaceholder("au.gov.ansto.bragg.kakadu.ui.views.RegionView");

		IPlaceholderFolderLayout plotFolder = factory.createPlaceholderFolder(
		"plotFolder",
		IPageLayout.BOTTOM,
		0.5f,
		factory.getEditorArea());
		plotFolder.addPlaceholder(PLOT_VIEW_ID + ":*");

		factory.addPlaceholder(MASK_PROPERTIES_VIEW_ID, IPageLayout.RIGHT, 0.8f, factory.getEditorArea());
		factory.addPlaceholder(OPERATION_PARAMETERS_VIEW_ID, IPageLayout.RIGHT, 0.8f, factory.getEditorArea());
//		factory.addPlaceholder(PLOT_VIEW_ID
//				+ ":*"
//				,IPageLayout.BOTTOM, 0.5f,	factory.getEditorArea());
		
//		for (int i = 0; i < 4; i++) {
//			//locate first 4 plots independently under MASK_PROPERTIES_VIEW. Others will be added to the same folder
//		factory.addPlaceholder(
//				PLOT_VIEW_ID + ":" + i,
//				IPageLayout.BOTTOM, 0.5f,
//				MASK_PROPERTIES_VIEW_ID);
//		}
}

}
