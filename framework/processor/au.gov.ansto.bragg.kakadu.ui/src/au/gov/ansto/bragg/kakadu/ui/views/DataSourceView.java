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
package au.gov.ansto.bragg.kakadu.ui.views;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import au.gov.ansto.bragg.kakadu.core.data.DataItem;
import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.instrument.SingleEntryDataSourceComposite;


/**
 * The view for Data Source files management. 
 * 
 * @author Danil Klimontov (dak)
 */
public class DataSourceView extends ViewPart {
	protected DataSourceComposite dataSourceComposite;

	/**
	 * The constructor.
	 */
	public DataSourceView() {
	}

	/**
	 * Creates all UI controls for the view.
	 */
	public void createPartControl(Composite parent) {
		ProjectManager.init();

		parent.setLayout(new FillLayout());
		dataSourceComposite = new DataSourceComposite(parent, SWT.NONE);
//		dataSourceComposite = new SingleEntryDataSourceComposite(parent, SWT.NONE);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(dataSourceComposite, "au.gov.ansto.bragg.kakadu.dataSourceView");
		
		contributeToActionBars();
	}

	protected void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		final IMenuManager menuManager = bars.getMenuManager();
		final IToolBarManager toolBarManager = bars.getToolBarManager();

		
		final List actionList = dataSourceComposite.getActionList();
		for (Iterator iterator = actionList.iterator(); iterator.hasNext();) {
			Object action = (Object) iterator.next();
			if (action instanceof Action) {
				Action a = (Action) action;
				menuManager.add(a);
				toolBarManager.add(a);
			} else if (action instanceof Separator) {
				Separator s = (Separator) action;
				menuManager.add(s);
				toolBarManager.add(s);
			}
		}
		
	}

	/**
	 * Adds data file to the list of source files. 
	 * @param filePath absolute file path to the file.
	 */
	public List<DataItem> addDataSourceFile(String filePath) {
		DataSourceFile sourceFile = dataSourceComposite.addFile(filePath);
		if (sourceFile == null)
			return new ArrayList<DataItem>();
		return sourceFile.getDataItems();
	}
	
	/**
	 * Removes all data files previously added to the view. 
	 */
	public void removeAllDataSourceFiles() {
		dataSourceComposite.removeAll();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		dataSourceComposite.setFocus();
	}
	
	public void setSelectionAll(boolean flag){
		dataSourceComposite.setSelectionAll(flag);
	}
	
	public void selectDataSourceItem(final URI fileUri, String  entryName){
		dataSourceComposite.selectDataSourceItem(fileUri, entryName);
	}
}