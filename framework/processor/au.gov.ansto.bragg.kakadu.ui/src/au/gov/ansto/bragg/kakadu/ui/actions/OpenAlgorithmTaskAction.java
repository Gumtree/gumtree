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
package au.gov.ansto.bragg.kakadu.ui.actions;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.cicada.core.Algorithm;
import au.gov.ansto.bragg.cicada.core.AlgorithmManager;
import au.gov.ansto.bragg.cicada.core.DRATask;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;
import au.gov.ansto.bragg.kakadu.core.UIAlgorithmManager;
import au.gov.ansto.bragg.kakadu.ui.util.KakaduDOM;
import au.gov.ansto.bragg.kakadu.ui.util.Util;

/**
 * @author nxi
 * Created on 06/06/2008
 */
public class OpenAlgorithmTaskAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		this.window = window;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction arg0) {
		// TODO Auto-generated method stub
		if (window == null) {
			return;
		}
		String filePath = Util.getFilenameFromShell(window.getShell(), "*.hdf", 
				"hdf task file");
		if (filePath == null)
			return;
		File file = new File(filePath);
		if (!file.exists())
			return;
		AlgorithmManager algorithmManager = UIAlgorithmManager.getAlgorithmManager();
		KakaduDOM kakadu = new KakaduDOM();
		DRATask task = null;
		URI fileUri = null;
		try {
			fileUri = ConverterLib.path2URI(filePath);
			task = algorithmManager.loadDRATask(fileUri);
			List<URI> filePathList = task.getDataSourceList();
			for (URI uri : filePathList){
				kakadu.addDataSourceFile(uri);
			}
			Algorithm algorithm = algorithmManager.findAlgorithm(task.getAlgorithmSetId(), 
					task.getAlgorithmName());
			IGroup configuration = task.getAlgorithmConfiguration();
			algorithm.setConfigurationGroup(configuration);
			kakadu.runAlgorithm(algorithm, fileUri);
//			ProjectManager.getCurrentAlgorithmTask().setFileUri(fileUri);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Util.handleException(window.getShell(), e);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction arg0, ISelection arg1) {
		// TODO Auto-generated method stub

	}

}
