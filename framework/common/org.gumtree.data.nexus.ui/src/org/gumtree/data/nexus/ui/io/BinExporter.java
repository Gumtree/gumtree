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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.utils.NexusUtils;
import org.gumtree.vis.gdm.io.AbstractExporter;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.nexus.dataset.Hist2DNXDataset;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;

public class BinExporter extends AbstractExporter {

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
						INXdata data = NexusUtils.getNXdata(nxDataset);
						signalExport(subFile, data);
					}
				}				
			} else if (seriesList.size() > 0) {
				INXdata data = NexusUtils.getNXdata(((NXDatasetSeries) seriesList.get(0)).getNxDataset());
				signalExport(file, data);
			}
		} else if (signal instanceof Hist2DNXDataset) {
			INXdata data = NexusUtils.getNXdata(((Hist2DNXDataset) signal).getNXDataset());
			signalExport(file, data);
		}
		
	}
	
	public void signalExport(File file, INXdata signal) throws IOException{
		FileOutputStream fileStream = new FileOutputStream(file);
		ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
		objectStream.writeObject(signal);
		objectStream.close();
		fileStream.close();
	}

	@Override
	public String toString() {
		return "Java Binary";
	}
	
	@Override
	public String getExtensionName() {
		return "bin";
	}
}
