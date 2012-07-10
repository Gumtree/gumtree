
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
package au.gov.ansto.bragg.cicada.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.ListIterator;

import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.Variance;
import au.gov.ansto.bragg.datastructures.util.AxisRecord;

public class SansExport extends FormatedExport {

    private Boolean isDebugMode = true;

	private final static String KEY_NOTES = "notes";
	
	private PrintWriter outFile;

	public SansExport() {
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
	
	public void resultExport(URI fileURI, IGroup signal) throws IOException {
		Plot inPlot = null;
		IArray data;

		if (signal instanceof Plot) { 
			inPlot = (Plot) signal; 
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
		
		String msgSource = "Source: " + inPlot.getLocation();  comment(msgSource);
		
		Axis xAxis = inPlot.getAxis(0);
		AxisRecord xRec = AxisRecord.createRecord(xAxis,0,shape);		
		
		Variance variance = inPlot.getVariance();
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
		
		IArray abscissa = xRec.centres();
		IArrayIterator aitr = abscissa.getIterator();
		IArrayIterator ditr = data.getIterator();
		IArrayIterator vitr = vData.getIterator();
				
		try{
			File newFile = getFile(fileURI,".sans.txt");
			comment("Starting export of data to file");
			outFile = new PrintWriter(new FileWriter(newFile));
			
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
			
			String col1 = inPlot.getAxis(inPlot.getAxisArrayList().size() - 1).getTitle();
			String col2 = inPlot.findSingal().getTitle();
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

	private String getDeviceInfo(Plot inputdata, String deviceName, String numericFormat) {
		String result = "";
		if (deviceName != null){
			try {
				Object item = inputdata.getContainer(deviceName);
				IArray signal = null;
				String units = "";
				if (item instanceof IDataItem){
					signal = ((IDataItem) item).getData();
					units = ((IDataItem) item).getUnitsString();
				}
				else if (item instanceof IAttribute)
					signal = ((IAttribute) item).getValue();
				if (signal.getElementType() == Character.TYPE)
					result = deviceName + "=" + signal.toString();
				else{
					double signalMax = signal.getArrayMath().getMaximum();
					double signalMin = signal.getArrayMath().getMinimum();
					result = deviceName + "=";
					if (numericFormat == null)
						numericFormat = "%.5f";
					if (signalMax == signalMin)
						result += (new Formatter()).format(numericFormat, signalMax) + " " + units;
					else
						result += (new Formatter()).format(numericFormat, signal.getDouble(
								signal.getIndex().set(0))) + " " + units;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return result;
	}

	public void signalExport(URI fileURI, Object signal) throws IOException {
		if (signal instanceof IGroup) {
			resultExport(fileURI, (IGroup) signal);
		}
	}

	public void signalExport(URI fileURI, Object signal, String title)
		throws IOException {
		signalExport(fileURI, signal);
	}

	public void signalExport(URI fileURI, Object signal, boolean isTranspose)
		throws IOException {
		signalExport(fileURI, signal);
	}
	
	public Boolean is1D() { return true; }

	public Boolean is2D() {	return false; }
	
	public Boolean isMultiD() { return false; }
	
}
