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

import org.gumtree.data.impl.io.NcHdfWriter;
import org.gumtree.data.interfaces.IGroup;

public class HdfExport extends FormatedExport {
	/**
	 * Export result signal in the instrument signal object in a certain format
	 */
	public void resultExport(URI fileURI, IGroup signal) throws IOException{};

	/**
	 * Export any double array signal to target file in a certain format
	 * @throws IOException 
	 */
	public void signalExport(URI fileURI, Object signal) throws IOException{
//		signal = ((Group) signal).getRootGroup().findGroup("entry1");
		String filename = getFile(fileURI, ".hdf").getAbsolutePath();
//		String filename = getFile(fileURI, ".hdf").getAbsolutePath();
		if (signal instanceof IGroup){
			IGroup databag = (IGroup) signal;
			NcHdfWriter hdfCreater = null;
			try{
				hdfCreater = new NcHdfWriter(new File(filename));    	
			}
			catch (Exception e){
				e.printStackTrace(); 
				throw new IOException("failed to create file " + filename);
			}
//			if (databag.isRootGroup()){
//			List<GroupData> entryList = databag.getGroups();
//			for (Iterator<GroupData> iter = entryList.iterator(); iter.hasNext();){
//			groupExport(hdfCreater, iter.next());
//			}
//			}else 
			groupExport(hdfCreater, databag);
		} else {
			throw new IOException("create hdf file failed");
		}
	}

	public void groupExport(NcHdfWriter hdfCreater, IGroup signal) throws IOException{
		try{
			hdfCreater.open();
			hdfCreater.writeToRoot(signal);
			hdfCreater.close();
		}
		catch (Exception e){
			e.printStackTrace(); 
			throw new IOException("write file failed");
		}
	}

	@Override
	public void signalExport(URI fileURI, Object signal, String title)
			throws IOException {
		// TODO Auto-generated method stub
		signalExport(fileURI, signal);
	}

	@Override
	public void signalExport(URI fileURI, Object signal, boolean isTranspose)
			throws IOException {
		// TODO Auto-generated method stub
		if (!isTranspose)
			signalExport(fileURI, signal);
		else
			signalExport(fileURI, signal);
	}

}
