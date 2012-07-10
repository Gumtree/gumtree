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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.gumtree.data.Factory;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

public class XMLExport extends FormatedExport{

	private BufferedWriter bufferWriter;
	private String indent;
	/**
	 * Export result signal in the instrument signal object in a certain format
	 */
	public void resultExport(URI fileURI, IGroup signal) throws IOException{};

	/**
	 * Export any double array signal to target file in a certain format
	 * @throws IOException 
	 */
	public void signalExport(URI fileURI, Object signal) throws IOException{

//		String filename = fileURI.getPath();
		if (signal instanceof IGroup){
			IGroup group = (IGroup) signal;
			IDataset dataset = null;
			if (group.isRoot())
				dataset = group.getDataset();
			else{
				dataset = Factory.createEmptyDatasetInstance();
				dataset.getRootGroup().addSubgroup(group.clone());
			}
			OutputStream os = new FileOutputStream(getFile(fileURI, ".xml"));
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
		}
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

	/**
	 * Export any double array signal to target file in a certain format
	 * @throws IOException 
	 */
	public void signalExport(URI fileURI, Object signal, String title) throws IOException{
		signalExport(fileURI, signal);
	}

	@Override
	public void signalExport(URI fileURI, Object signal, boolean isTranspose)
			throws IOException {
		// TODO Auto-generated method stub
		signalExport(fileURI, signal);
	}
}
