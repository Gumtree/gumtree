
/******************************************************************************* 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *	Based on XYSigma export by Norman Xiong
 *  Modified Paul Hathaway - April 2009
 *******************************************************************************/
package org.gumtree.data.nexus.ui.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.nexus.IAxis;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.IVariance;
import org.gumtree.data.nexus.utils.NexusUtils;
import org.gumtree.vis.gdm.io.AbstractExporter;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.nexus.dataset.Hist2DNXDataset;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;

public class SansExporter extends AbstractExporter {

    private Boolean isDebugMode = true;

	private final static String KEY_NOTES = "notes";
	
	private PrintWriter outFile;

	public SansExporter() {
	}

	private void comment(String message) {
		if(isDebugMode && (null!=message)) {
			System.out.println("> "+message);
		}
	}
	
	private void signalFailure(String msg) throws IOException {
		String output = "Failed to export data set: "+msg;
		comment(output);
		throw new IOException(output);		
	}
	
	public void resultExport(File file, IGroup signal) throws IOException {
		INXdata inPlot = null;
		IArray data;

		if (signal instanceof INXdata) { 
			inPlot = (INXdata) signal; 
		} else {
			signalFailure("Invalid signal type");
		}
		
		try {
			data = ((NcGroup) inPlot).getSignalArray();
		} catch (SignalNotAvailableException e1) {
			throw new IOException(e1);
		}
		
		int[] shape = data.getShape();
		int rank = data.getRank();
		String location = inPlot.getLocation();
		if (location == null) {
			location = inPlot.getRootGroup().getLocation();
		}
		if (location == null) {
			location = inPlot.getRootGroup().getDataset().getTitle();
		}
		String msgSource = "Source: " + location;  comment(msgSource);
		
		IAxis xAxis = inPlot.getAxisList().get(0);
//		AxisRecord xRec = AxisRecord.createRecord(xAxis,0,shape);		
		
		IVariance variance = inPlot.getVariance();
		IArray vData = variance.getData();
				
		IDataItem notes;
		List<IAttribute> alist = null;
		List<String> lines = new ArrayList<String>();
		IGroup root;
		try {
			root = inPlot.getRootGroup();
			//notes = root.getDataItem(KEY_NOTES);
			notes = inPlot.findDataItem(KEY_NOTES);
			if(null!=notes) { 
				alist = (List<IAttribute>) notes.getAttributeList();
				if(null!=alist) {
					for(int i=0;i<alist.size();i++) {
						String out = null;
						IAttribute att = alist.get(i);
						if(att.isString()) { 
							out = att.getName()+": "+att.getStringValue();
						} else {
							Number val=att.getNumericValue();
							if(null!=val) {
								out = att.getName()+": "+val.toString();
							}
						}
						if(null!=out) {
							lines.add(out);
						}
					}
				}
			}
			if(lines.isEmpty()) {
				comment("No processing notes"); 
			}
		} catch (Exception e) {
			comment("No processing notes");
		}		
		
		IArray abscissa = xAxis.getData();
		IArrayIterator aitr = abscissa.getIterator();
		IArrayIterator ditr = data.getIterator();
		IArrayIterator vitr = vData.getIterator();
				
		try{
			comment("Starting export of data to file");
			outFile = new PrintWriter(new FileWriter(file));
			
			outFile.format("# %-79s%n",msgSource);
			outFile.append("#\r\n");
			
			if(null!=lines) {
				ListIterator<String> litr = lines.listIterator();
				while(litr.hasNext()) {
					outFile.append("# "+litr.next()+"\r\n");
				}
				outFile.append("#\r\n");
			}
			
//			String processingLog = "# " + inPlot.getProcessingLog().replaceAll("\r\n", "\r\n# ") + "\r\n";
//			outFile.append(processingLog);
//			outFile.append("#\r\n#\r\n");
			
			String col1 = inPlot.getAxisList().get(inPlot.getAxisList().size() - 1).getShortName();
			String col2 = inPlot.getSignal().getShortName();
			String col3 = "sigma";
			String lineFormat = "#%1$12s %2$12s %3$12s %n"; 
			String lineHeader = String.format(lineFormat, col1,col2,col3);
			outFile.append(lineHeader);

			while(ditr.hasNext()) {
				outFile.format(" %12.5f %12.5f %12.5f%n",
						aitr.getDoubleNext(),
						ditr.getDoubleNext(),
						Math.sqrt(vitr.getDoubleNext()));
				}
			outFile.format("%n");  //one blank line between histograms - this is good for Gnuplot
			
		}catch (Exception e) {
			comment("Fail to complete export");
			e.printStackTrace();
		} finally {
			outFile.close();
		}
	}


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
						resultExport(subFile, data);
					}
				}				
			} else if (seriesList.size() > 0) {
				INXdata data = NexusUtils.getNXdata(((NXDatasetSeries) seriesList.get(0)).getNxDataset());
				resultExport(file, data);
			}
		} else if (signal instanceof Hist2DNXDataset) {
			INXdata data = NexusUtils.getNXdata(((Hist2DNXDataset) signal).getNXDataset());
			resultExport(file, data);
		}
		
	}
	
	
	public Boolean is1D() { return true; }

	public Boolean is2D() {	return false; }
	
	public Boolean isMultiD() { return false; }
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "SANS";
	}
	
	@Override
	public String getExtensionName() {
		return "sans.txt";
	}
}
