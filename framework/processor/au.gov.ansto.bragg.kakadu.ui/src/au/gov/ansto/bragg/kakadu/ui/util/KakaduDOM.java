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
package au.gov.ansto.bragg.kakadu.ui.util;

import java.io.File;
import java.net.URI;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.exception.LoadAlgorithmFileFailedException;
import au.gov.ansto.bragg.cicada.core.exception.NoneAlgorithmException;
import au.gov.ansto.bragg.kakadu.core.DataSourceManager;
import au.gov.ansto.bragg.kakadu.ui.KakaduPerspective;
import au.gov.ansto.bragg.kakadu.ui.ProjectManager;
import au.gov.ansto.bragg.kakadu.ui.views.DataSourceView;
import au.gov.ansto.bragg.process.exception.NullSignalException;

/**
 * Implementation of MonkeyDOM functionality.
 * The methods going to be used as open API for scripting applications.
 * 
 * @author Danil Klimontov (dak)
 */
public class KakaduDOM {

	public static IWorkbenchPage activePage;
	private boolean isConfirm;
	public static File currentDir = new File (".");
	DataSourceView dataSourceView;
	
	public KakaduDOM() {
	}

	/**
	 * Opens Analysis (Kakadu) perspective on active WorkbenchPage 
	 * or create a new window if the are no active page available. 
	 */
	public void openKakaduPersective() {
		IWorkbench workbench;
		IWorkbenchWindow workbenchWindow;
		if (activePage == null){
			workbench = PlatformUI.getWorkbench();
//		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
			IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
//				windows[i].
			}
			workbenchWindow = workbench
				.getActiveWorkbenchWindow();
			activePage = workbenchWindow.getActivePage();
		}else{
			workbenchWindow = activePage.getWorkbenchWindow();
			workbench = workbenchWindow.getWorkbench();
		}
		final IPerspectiveDescriptor kakaduPerspectiveDescriptor = workbench
				.getPerspectiveRegistry().findPerspectiveWithId(
						KakaduPerspective.KAKADU_PERSPECTIVE_ID);
		if (kakaduPerspectiveDescriptor == null) {
			MessageDialog
					.openError(
							workbenchWindow.getShell(),
							"Error opening Analysis perspective",
							"Analysis perspective did not installed properly. " +
							"Try to reinstall Kakadu plug-in.");
			return;
		}

		if (workbenchWindow != null && activePage != null) {
			activePage.getWorkbenchWindow().getWorkbench().
			getDisplay().syncExec(new Runnable(){

				public void run() {
					// TODO Auto-generated method stub
					activePage.setPerspective(
							kakaduPerspectiveDescriptor);

					final IViewReference viewReference = activePage.findViewReference(KakaduPerspective.DATA_SOURCE_VIEW_ID);
					try {
						activePage.hideView(viewReference);
						activePage.showView(KakaduPerspective.DATA_SOURCE_VIEW_ID);
					} catch (Exception e) {
						showErrorMessage(e.getMessage());
					}

				}

			});
		} else {
			try {
				workbench.openWorkbenchWindow(
						KakaduPerspective.KAKADU_PERSPECTIVE_ID,
						activePage.getInput());
			} catch (WorkbenchException e) {
				e.printStackTrace();
				MessageDialog.openError(workbenchWindow.getShell(),
						"Error opening Kakadu perspective", e.getMessage());
			}
		}
	}

	public void addDataSourceFile(final URI fileUri) {
		addDataSourceFile(fileUri.getPath());
	}
	
	public void selectDataSourceItem(final URI fileUri, String  entryName){
		
		if (dataSourceView == null)
			createDataSourceView();
		activePage.getWorkbenchWindow().getWorkbench().getDisplay().syncExec(new Runnable(){

			public void run() {
				// TODO Auto-generated method stub
				try {
//					DataSourceView dataSourceView = null;
					dataSourceView.setSelectionAll(false);
					
				} catch (Exception e) {
					showErrorMessage(e.getMessage());
				}						
			}
			
		});
	}
	private void createDataSourceView() {
		// TODO Auto-generated method stub
		IWorkbench workbench;
		IWorkbenchWindow workbenchWindow;
		if (activePage == null){
			workbench = PlatformUI.getWorkbench();
//		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
			workbenchWindow = workbench
				.getActiveWorkbenchWindow();
			activePage = workbenchWindow.getActivePage();
		}else{
			workbenchWindow = activePage.getWorkbenchWindow();
			workbench = workbenchWindow.getWorkbench();
		} 
		final IViewReference dataSourceViewReference = activePage.findViewReference(KakaduPerspective.DATA_SOURCE_VIEW_ID);

		//check is DataSourceView active at the moment. 
		//If yes then update UI controls as well. 
		//If no then update only DataSourceManager
		final IViewReference[] activeViewReferences = activePage.getViewReferences();
//		for (IViewReference viewReference : activeViewReferences) {
//			if (dataSourceViewReference == viewReference) {
				workbench.getDisplay().syncExec(new Runnable(){

					public void run() {
						// TODO Auto-generated method stub
						try {
//							DataSourceView dataSourceView = null;
							dataSourceView = (DataSourceView)activePage.showView(KakaduPerspective.DATA_SOURCE_VIEW_ID, null, 
									IWorkbenchPage.VIEW_VISIBLE);
						} catch (PartInitException e) {
							showErrorMessage(e.getMessage());
						}						
					}
					
				});
				return;
	}

	/**
	 * Adds data source file to the list of available data files in the DataSourceView.
	 * @param filePath an absolute path to the file
	 */
	public void addDataSourceFile(final String filePath) {
//		final IWorkbench workbench = PlatformUI.getWorkbench();
//		final IWorkbenchWindow workbenchWindow = workbench
//				.getActiveWorkbenchWindow();
//		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
		IWorkbench workbench;
		IWorkbenchWindow workbenchWindow;
		if (activePage == null){
			workbench = PlatformUI.getWorkbench();
//		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
			workbenchWindow = workbench
				.getActiveWorkbenchWindow();
			activePage = workbenchWindow.getActivePage();
		}else{
			workbenchWindow = activePage.getWorkbenchWindow();
			workbench = workbenchWindow.getWorkbench();
		} 
		final IViewReference dataSourceViewReference = activePage.findViewReference(KakaduPerspective.DATA_SOURCE_VIEW_ID);

		//check is DataSourceView active at the moment. 
		//If yes then update UI controls as well. 
		//If no then update only DataSourceManager
		final IViewReference[] activeViewReferences = activePage.getViewReferences();
//		for (IViewReference viewReference : activeViewReferences) {
//			if (dataSourceViewReference == viewReference) {
				workbench.getDisplay().syncExec(new Runnable(){

					public void run() {
						// TODO Auto-generated method stub
						try {
//							DataSourceView dataSourceView = null;
							dataSourceView = (DataSourceView)activePage.showView(KakaduPerspective.DATA_SOURCE_VIEW_ID, null, 
									IWorkbenchPage.VIEW_VISIBLE);
							dataSourceView.addDataSourceFile(filePath);
						} catch (PartInitException e) {
							showErrorMessage(e.getMessage());
						}						
					}
					
				});
				return;
//			}
//		}
//
//		//process DataSourceManager if DataSourceView is not active
//		final DataSourceManager dataSourceManager = DataSourceManager.getInstance();
//		try {
//			dataSourceManager.addFile(filePath);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * Removes all data files from the list of available files in DataSourceView.
	 */
	public void removeAllDataSourceFiles() {
		IWorkbench workbench;
		IWorkbenchWindow workbenchWindow;
		if (activePage == null){
			workbench = PlatformUI.getWorkbench();
//		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
			workbenchWindow = workbench
				.getActiveWorkbenchWindow();
			activePage = workbenchWindow.getActivePage();
		}else{
			workbenchWindow = activePage.getWorkbenchWindow();
			workbench = workbenchWindow.getWorkbench();
		}
		final IViewReference dataSourceViewReference = activePage.findViewReference(KakaduPerspective.DATA_SOURCE_VIEW_ID);

		//check is DataSourceView active at the moment. 
		//If yes then update UI controls as well. 
		//If no then update only DataSourceManager
		final IViewReference[] activeViewReferences = activePage.getViewReferences();
		for (IViewReference viewReference : activeViewReferences) {
			if (dataSourceViewReference == viewReference) {
				DataSourceView dataSourceView = null;
				try {
					dataSourceView = (DataSourceView)activePage.showView(KakaduPerspective.DATA_SOURCE_VIEW_ID);
				} catch (PartInitException e) {
					showErrorMessage(e.getMessage());
				}
				dataSourceView.removeAllDataSourceFiles();
				return;
			}
		}

		//process DataSourceManager if DataSourceView is not active
		final DataSourceManager dataSourceManager = DataSourceManager.getInstance();
		dataSourceManager.removeAll();
	}

	/**
	 * Runs the algorithm with the list of added data source files.
	 * @param algorithm an Algorithm.
	 */
	public void runAlgorithm(Algorithm algorithm) {
		try {
			ProjectManager.runAlgorithm(algorithm);
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage(e.getMessage());
		}
		
	}

	public void runAlgorithm(Algorithm algorithm, URI fileUri){
		try {
			ProjectManager.runAlgorithm(algorithm, fileUri);
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage(e.getMessage());
		} 
		
	}
	/**
	 * Runs the algorithm for the data file.
	 * Opens Analysis (Kakadu) perspective, adds the file to DataSourceView and then run an algorithm.
	 * @param filePath an data source file path.
	 * @param algorithm an algorithm to be run.
	 */
	public void run(String filePath, Algorithm algorithm) {
		openKakaduPersective();
		removeAllDataSourceFiles();
		addDataSourceFile(filePath);
		runAlgorithm(algorithm);
	}

	/**
	 * Shows error message in Message dialog.
	 * @param message a message to be shown.
	 */
	private void showErrorMessage(String message) {
		MessageDialog.openError(
			Display.getDefault().getActiveShell(),
			"KakaduDOM",
			message);
	}
	
	/**
	 * Open a confirm dialog window and receive boolean value.
	 * @param messageText String text
	 * @return boolean type
	 * Created on 07/03/2008
	 */
	public boolean confirm(final String messageText){
		final String title = "Confirm";
//		final IWorkbench workbench = PlatformUI.getWorkbench();
//		final IWorkbenchWindow workbenchWindow = workbench
//				.getActiveWorkbenchWindow();
		activePage.getWorkbenchWindow().getWorkbench().getDisplay().syncExec(new Runnable(){

			public void run() {
				// TODO Auto-generated method stub
				isConfirm = MessageDialog.openConfirm(activePage.getWorkbenchWindow().getShell(), 
						title, messageText);
			}
			
		});
		return isConfirm;
	}

	public void setWorkbenchPage(IWorkbenchPage workbenchPage){
		activePage = workbenchPage;
	}
	
	public static File getFilenameFromShell(String extensionName, String fileDescription){
		String fileFilterPath = null;
		try{
			fileFilterPath = currentDir.getCanonicalPath();// + "\\..";
		}    catch(Exception e) {
			e.printStackTrace();
		}
		FileDialog fileDialog = new FileDialog(activePage.getWorkbenchWindow().getShell(), SWT.MULTI);
		fileDialog.setFilterPath(fileFilterPath);
		fileDialog.setFilterExtensions(new String[]{extensionName, "*.*"});
		fileDialog.setFilterNames(new String[]{ fileDescription, "Any"});
	
		String firstFile = fileDialog.open();
		String filename = null;
		if(firstFile != null) {
			final File file = new File(firstFile);
			if (file.exists()) {
				currentDir = file.getParentFile();
				filename = fileDialog.getFilterPath() + "\\" + fileDialog.getFileName();
				return file;
			}
		}
		return null;
	}

}
