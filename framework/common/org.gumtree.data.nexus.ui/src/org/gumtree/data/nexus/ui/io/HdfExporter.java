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
package org.gumtree.data.nexus.ui.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gumtree.data.impl.io.NcHdfWriter;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.vis.gdm.io.AbstractExporter;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.nexus.dataset.Hist2DNXDataset;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;

public class HdfExporter extends AbstractExporter {

	@Override
	public void export(File file, IDataset signal) throws IOException {
		if (signal instanceof IXYErrorDataset) {
			List<IXYErrorSeries> seriesList = ((IXYErrorDataset) signal).getSeries();
			if (seriesList.size() > 1) {
				int index = 0;
				for (IXYErrorSeries series : seriesList) {
					if (series instanceof NXDatasetSeries) {
						INXDataset nxDataset = ((NXDatasetSeries) series).getNxDataset();
						File subFile = new File(file.getAbsolutePath() + "/" + 
								nxDataset.getTitle() + "_" + (index++) + "." + getExtensionName());
						signalExport(subFile, nxDataset);
					}
				}				
			} else if (seriesList.size() > 0) {
				signalExport(file, ((NXDatasetSeries) seriesList.get(0)).getNxDataset());
			}
		} else if (signal instanceof Hist2DNXDataset) {
			signalExport(file, ((Hist2DNXDataset) signal).getNXDataset());
		}
		
	}
	
	/**
	 * Export any double array signal to target file in a certain format
	 * @throws IOException 
	 */
	public void signalExport(File file, INXDataset signal) throws IOException{
//		signal = ((Group) signal).getRootGroup().findGroup("entry1");
//		String filename = getFile(fileURI, ".hdf").getAbsolutePath();
//		String filename = getFile(fileURI, ".hdf").getAbsolutePath();
		
			NcHdfWriter hdfCreater = null;
			try{
				hdfCreater = new NcHdfWriter(file);    	
			}
			catch (Exception e){
				e.printStackTrace(); 
				throw new IOException("failed to create file " + file.getAbsolutePath());
			}
//			if (databag.isRootGroup()){
//			List<GroupData> entryList = databag.getGroups();
//			for (Iterator<GroupData> iter = entryList.iterator(); iter.hasNext();){
//			groupExport(hdfCreater, iter.next());
//			}
//			}else 
			groupExport(hdfCreater, signal.getRootGroup());
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
	public String toString() {
		return "Nexus HDF";
	}
	
	@Override
	public String getExtensionName() {
		return "hdf";
	}

}
