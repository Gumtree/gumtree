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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.vis.gdm.io.AbstractExporter;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.nexus.dataset.Hist2DNXDataset;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;

public class XMLExporter extends AbstractExporter {

	private BufferedWriter bufferWriter;
	private String indent;

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
	public void signalExport(File file, INXDataset dataset) throws IOException{

//		String filename = fileURI.getPath();
//		if (signal instanceof IGroup){
//			IGroup group = (IGroup) signal;
//			IDataset dataset = null;
//			if (group.isRoot())
//				dataset = group.getDataset();
//			else{
//				dataset = Factory.createEmptyDatasetInstance();
//				dataset.getRootGroup().addSubgroup(group.clone());
//			}
			OutputStream os = new FileOutputStream(file);
//			dataset.writeNcML(os, fileURI.getPath());
			dataset.writeNcML(os, null);
			os.close();
//			if (!filename.endsWith(".xml")) filename = filename.concat(".xml");
//			File file = new File(filename);
//			FileWriter fileWriter = null;
//			try {
//				fileWriter = new FileWriter(file);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			bufferWriter = new BufferedWriter (fileWriter);
//			bufferWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ");
//			if (group.isRootGroup())
//				for (Object object : group.getAttributes())
//					attributeExport((Attribute) object);
//			bufferWriter.append("?>\n\n");
//			indent = "";
//			groupExport(group);    	
////			resultBufferExport(bufferWriter, signal);
//			bufferWriter.close();
//			fileWriter.close();
////			if (databag.isRootGroup()){
////			List<GroupData> entryList = databag.getGroups();
////			for (Iterator<GroupData> iter = entryList.iterator(); iter.hasNext();){
////			groupExport(hdfCreater, iter.next());
////			}
////			}else 
//
//		} else {
//			throw new IOException("create hdf file failed");
//		}
	}

	private void groupExport(IGroup group) throws IOException {
		// TODO Auto-generated method stub

		if (!group.isRoot()){
			bufferWriter.append(indent);
			bufferWriter.append("<" + group.getShortName());
			for (Object object : group.getAttributeList())
				attributeExport((IAttribute) object);
			bufferWriter.append(">\n");
			indent += "\t";
		}
		for (Object object : group.getDataItemList()){
			bufferWriter.append(indent);
			dataItemExport((IDataItem) object);
		}
		for (Object object : group.getGroupList())
			groupExport((IGroup) object);
		if (!group.isRoot()) indent = indent.substring(0, indent.length() - 1);
		if (! group.isRoot()){
			bufferWriter.append(indent);
			bufferWriter.append("</" + group.getShortName() + ">\n\n");
		}
	}

	private void dataItemExport(IDataItem dataItem) throws IOException {
		// TODO Auto-generated method stub
		boolean isComments = false;
		IAttribute commentsAttribute = dataItem.getAttribute("signal");
		if (commentsAttribute != null){
			String commentsValue = commentsAttribute.getStringValue();
			if (commentsValue.equals("comments"))
				isComments = true;
		}
		if (isComments) {
			bufferWriter.append("<!-- ");
			bufferWriter.append(dataItem.getData().toString());
			bufferWriter.append(" -->\n\n");
		}
		else{
			bufferWriter.append("<" + dataItem.getShortName());
			for (Object object : dataItem.getAttributeList())
				attributeExport((IAttribute) object);
			bufferWriter.append(">" + dataItem.getData().toString() + "</" + 
					dataItem.getShortName() + ">\n");

		}
	}

	private void attributeExport(IAttribute attribute) throws IOException {
		// TODO Auto-generated method stub
		if (attribute.getName().equals("signal") && attribute.getStringValue().matches("comments"))
			return;
		bufferWriter.append(" " + attribute.getName() + "=\"" + attribute.getStringValue() + "\"");
	}

	@Override
	public String toString() {
		return "Nexus XML";
	}
	
	@Override
	public String getExtensionName() {
		return "xml";
	}
}
