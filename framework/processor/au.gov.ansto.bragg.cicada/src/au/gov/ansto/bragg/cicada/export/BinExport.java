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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;

import org.gumtree.data.interfaces.IGroup;

public class BinExport extends FormatedExport {

	public void resultExport(URI fileURI, IGroup signal) throws IOException{
		signalExport(fileURI, signal);
	}

	public void signalExport(URI fileURI, Object signal) throws IOException{
		//String wdirectory = "D:\\opaldra\\xml\\";
		//String filename = "HIPDDataset.dat";
		String filename = fileURI.getPath();
		File file = new File(filename);
		FileOutputStream fileStream = new FileOutputStream(file);
		ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
		objectStream.writeObject(signal);
		objectStream.close();
		fileStream.close();
	}

	public void signalExport(URI fileURI, Object signal, String title) throws IOException{
		//String wdirectory = "D:\\opaldra\\xml\\";
		//String filename = "HIPDDataset.dat";
		String filename = fileURI.getPath();
		File file = new File(filename);
		FileOutputStream fileStream = new FileOutputStream(file);
		ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
		objectStream.writeObject(signal);
		objectStream.close();
		fileStream.close();
	}

	@Override
	public void signalExport(URI fileURI, Object signal, boolean isTranspose)
			throws IOException {
		// TODO Auto-generated method stub
		signalExport(fileURI, signal);
	}

}
