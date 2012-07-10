/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
*******************************************************************************/
package au.gov.ansto.bragg.kakadu.ui.instrument;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import au.gov.ansto.bragg.kakadu.core.data.DataSourceFile;
import au.gov.ansto.bragg.kakadu.ui.util.DisplayManager;
import au.gov.ansto.bragg.kakadu.ui.util.Util;
import au.gov.ansto.bragg.kakadu.ui.widget.CheckboxTableTreeViewer;

/**
 * @author nxi
 * Created on 13/07/2009
 */
public class SingleEntryDataSourceComposite extends InstrumentDataSourceComposite {

	/**
	 * @param parent
	 * @param style
	 */
	public SingleEntryDataSourceComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected CheckboxTableTreeViewer createTreeViewer() {
		// TODO Auto-generated method stub
		return new CheckboxTableTreeViewer(this, SWT.FULL_SELECTION | SWT.CHECK);
	}
	
	@Override
	protected void initialise() {
		super.initialise();
//		String defaultFolderName = null;
//		defaultFolderName = System.getProperty("sics.data.path");
//		if (defaultFolderName != null){
//			File defaultFolder = new File(defaultFolderName);
//			if (defaultFolder.exists()){
//				addDirectory(defaultFolder.getAbsolutePath());
//			}
//		}
	}

	/**
	 * Adds data files from the directory to the list of source files.
	 * @param directoryName path to the directory with data files.
	 */
	public void addDirectory(String directoryName, int maxNumber) {
		File directory = new File(directoryName);
		if (directory.isDirectory()) {
			File[] directoryFiles = directory.listFiles(new FileFilter() {
				public boolean accept(File file) {
					//filter for only known formats
					String name = file.getName();
					int lastDotPosition = name.lastIndexOf(".");
					String extention = name.substring(lastDotPosition + 1, name.length()).toLowerCase();
					return Util.getSupportedFileExtentions().contains(extention);
				}
			
			});
			
			Arrays.sort(directoryFiles, new Comparator<File>(){
				public int compare(File file1, File file2) {
				
//					if (file1.lastModified() > file2.lastModified()) {
//						return -1;
//					} else if (file1.lastModified() < file2.lastModified()) {
//						return +1;
//					} else {
//						return 0;
//					}
					return Collator.getInstance().compare(file2.getName(), file1.getName());
				}

			}); 

			for (int i = 0; i < (maxNumber < directoryFiles.length ? maxNumber : directoryFiles.length
					); i ++)
				addFile(directoryFiles[i].getAbsolutePath(), false);
			
//			File lastFile = directoryFiles[directoryFiles.length - 1];
//			addFile(lastFile.getAbsolutePath());

			DisplayManager.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					adjustColumnSize();
				}
			});
			
		} else {
			new IllegalArgumentException("The path '" +directoryName+"' is not a directory.");
		}
	}
	
	/**
	 * Adds data file to the list of source files. 
	 * @param filePath absolute file path to the file.
	 */
	@Override
	public DataSourceFile addFile(String filePath) {
		
		//check duplications
//		DataSourceFile dataSourceFile = DataSourceManager.getInstance().getDataSourceFile(filePath);
//		if (dataSourceFile != null) {
//			if (confirmFileReplace(filePath)) {
//				DataSourceManager.getInstance().removeFile(dataSourceFile);
//
//				//update UI
//				removeFileNode(dataSourceFile);
//			} else {
//				return dataSourceFile;
//			}
//		}
//		
//		//add file to DataSourceManager
//		try {
//			dataSourceFile = DataSourceManager.getInstance().addFile(filePath);
//		} catch (IOException e) {
//			Util.handleException(getShell(), e);
//			return null;
//		} catch (Exception e) {
//			Util.handleException(getShell(), e);
//			return null;
//		}
//
//		//create DataItem nodes
//		DefaultMutableTreeNode fileNode = createNode(dataSourceFile);
//		
//		//add nodes to view
//		addFileNode(fileNode);
//
//		//update expanded/checked state
//		fileTableTreeViewer.setExpandedState(fileNode, false);
//		fileTableTreeViewer.setSubtreeChecked(fileNode, true);
//		return dataSourceFile;
		return super.addFile(filePath);
	}

}
