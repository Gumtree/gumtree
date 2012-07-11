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
package au.gov.ansto.bragg.quokka.exp.core.lib;

import static org.eclipse.core.resources.ResourcesPlugin.getWorkspace;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;


/**
 * @author nxi
 * Created on 27/06/2008
 */
public class Util {

	public static final String SCRIPTS_PROJECT = "ExperimentScript";
	public static final String LIBRARY_DIR = "Lib";

	public static URI getLibraryFolder(){
		final IWorkspace workspace = getWorkspace();
		final IProject project = workspace.getRoot().getProject( SCRIPTS_PROJECT );
		IFolder folder = project.getFolder(LIBRARY_DIR);
		return folder.getRawLocationURI();
	}
	
	public static URI getRawPath(String relativePath){
		final IWorkspace workspace = getWorkspace();
		final IProject project = workspace.getRoot().getProject( SCRIPTS_PROJECT );
		IFile file = project.getFile(relativePath);
		return file.getRawLocationURI();
	}
}
