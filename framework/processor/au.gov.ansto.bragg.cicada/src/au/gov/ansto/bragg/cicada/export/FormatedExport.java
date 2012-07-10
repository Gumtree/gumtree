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
package au.gov.ansto.bragg.cicada.export;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.gumtree.data.interfaces.IGroup;

public abstract class FormatedExport {

	/**
	 * Export result signal in the instrument signal object in a certain format
	 */
	public abstract void resultExport(URI fileURI, IGroup signal) throws IOException;
	
	/**
	 * Export any double array signal to target file in a certain format
	 */
	public abstract void signalExport(URI fileURI, Object signal) throws IOException;
	
	public abstract void signalExport(URI fileURI, Object signal, String title) throws IOException;
	
	public abstract void signalExport(URI fileURI, Object signal, boolean isTranspose) throws IOException;
	
	protected File getFile(URI uri, String extensionName){
		if (uri == null || uri.getPath().isEmpty())
			return null;
		String filename = uri.getPath();
		if (!filename.endsWith(extensionName))
			if (extensionName.startsWith("."))
				filename = filename + extensionName;
			else 
				filename = filename + "." + extensionName;
		return new File(filename);
	}
}
