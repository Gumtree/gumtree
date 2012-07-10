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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.NoSuchElementException;

import org.gumtree.data.Factory;
import org.gumtree.data.exception.InvalidRangeException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IArrayIterator;
import org.gumtree.data.interfaces.ISliceIterator;
import org.gumtree.data.nexus.INXDataset;
import org.gumtree.data.nexus.INXdata;
import org.gumtree.data.nexus.utils.NexusUtils;
import org.gumtree.vis.gdm.io.AbstractExporter;
import org.gumtree.vis.interfaces.IDataset;
import org.gumtree.vis.interfaces.IXYErrorDataset;
import org.gumtree.vis.interfaces.IXYErrorSeries;
import org.gumtree.vis.nexus.dataset.Hist2DNXDataset;
import org.gumtree.vis.nexus.dataset.NXDatasetSeries;

/**
 * @author jxh
 * 
 * Based on XYSigma export module by jxh,nxi.
 * 
 * Some especially brain-dead applications require that every line of an input
 * file have XY data, that is, no header comments and no trailing blank lines!
 */
public class NakedXYSigmaExporter extends AbstractExporter {

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
	
	public void resultExport(File file, INXdata inputdata) throws IOException {
		IArray outputnumbers;
		try {
			outputnumbers = ((NcGroup) inputdata).getSignalArray();
		} catch (SignalNotAvailableException e1) {
			throw new IOException(e1);
		}
		int[] dataShape = outputnumbers.getShape();
		int dataRank = outputnumbers.getRank();
		// For each dimension in the data, write a file
		IArray tth_points = inputdata.getAxisList().get(dataRank-1).getData();
		int tth_rank = tth_points.getRank();
		IArray err_array = null;
		try{
			err_array = inputdata.getVariance().getData();
		}catch (Exception e) {
			err_array = outputnumbers;
		}
		if(tth_points.getShape()[tth_rank-1]==dataShape[dataRank-1]+1) {      //do bin boundary processing
			try{
				tth_points = toCentres(tth_points);
			}catch (Exception e) {
			}
		}
		// Do some double-checking of two theta lengths...
		if(tth_points.getShape()[tth_rank-1]!=dataShape[dataRank-1]) {
			throw new IOException(String.format("Horizontal coordinate length %d does not match data length %d",
					tth_points.getShape()[tth_rank-1],dataShape[dataRank-1]));
		}
		//Note that tth may be 2 dimensional but may also be single-dimensional
		PrintWriter outputfile = null;
		try{
			ISliceIterator tth_iter = tth_points.getSliceIterator(1);
			ISliceIterator out_iter = outputnumbers.getSliceIterator(1);
			ISliceIterator err_iter = err_array.getSliceIterator(1);
			int frame_ct = 0;                                                      //be sure that file is unique
//			String processInfo = getProcessInfo(signal);
//			if (processInfo.contains("i") || processInfo.contains("f"))
//				extensionName = ".xid";
			
			System.out.println("Now outputting 2theta-intensity file named " + file.getAbsolutePath());
			outputfile = new PrintWriter(new FileWriter(file));
			while(out_iter.hasNext()) {
				IArray tth_row = null;
				try {
					tth_row = tth_iter.getArrayNext();
				} catch(NoSuchElementException e) {  //This might happen if horizontal coordinate is fixed for all frames
					tth_iter = tth_points.getSliceIterator(1);   //re-initialise
					tth_row = tth_iter.getArrayNext();
				}
				IArray data_row = out_iter.getArrayNext();
				IArray err_row = err_iter.getArrayNext();
				IArrayIterator row_iter = tth_row.getIterator();
				IArrayIterator data_iter = data_row.getIterator();
				IArrayIterator error_iter = err_row.getIterator();
				frame_ct++;
				while(row_iter.hasNext()) {
					outputfile.format("%10.5f %15.5f %15.5f%n",row_iter.getDoubleNext(),data_iter.getDoubleNext(),
							Math.sqrt(error_iter.getDoubleNext()));
				}
				if(out_iter.hasNext()) outputfile.format("%n");  //one blank line between histograms - this is good for Gnuplot
			}
			outputfile.close();
		}catch (Exception e) {
			if (outputfile != null)
				outputfile.close();
			e.printStackTrace();
		}
	}

	private IArray toCentres(IArray inarray) throws InvalidRangeException {
		int[] centre_shape = inarray.getShape();  //final shape
		int centre_rank = inarray.getRank();      //final rank
		centre_shape[centre_rank-1]--;
		IArray centre_array = Factory.createArray(double.class, centre_shape);
		//Create an iterator over the higher dimensions
		int[] range_list = new int[centre_rank];
		for(int i=0;i<centre_rank-1;i++) range_list[i] = centre_shape[i];
		range_list[centre_rank-1]=1;
		IArray high_dim_array = inarray.getArrayUtils().sectionNoReduce(new int[centre_rank],range_list, null).getArray();
		//Now repurpose the range_list for the internal array
		for(int i=0;i<centre_rank-1;i++) range_list[i] = 1;
		range_list[centre_rank-1] = centre_shape[centre_rank-1]+1;
		IArrayIterator high_dim_iter = high_dim_array.getIterator();
		while(high_dim_iter.hasNext()) {
			high_dim_iter.next();
			//Iterate over the lowest dimensional array
			int[] origin_list = high_dim_iter.getCounter();
			IArray this_scan = inarray.getArrayUtils().section(origin_list,range_list,null).getArray();
			range_list[centre_rank-1]--;
			IArray c_scan = centre_array.getArrayUtils().section(origin_list,range_list,null).getArray();
			range_list[centre_rank-1]++;  //for next time
			IArrayIterator o_iter = this_scan.getIterator();
			IArrayIterator c_iter = c_scan.getIterator();
			double bin_lowedge = o_iter.getDoubleNext();    //We get o_iter one extra time
			//note we rely on canonical behaviour, that is, that the iterator will loop over the fastest index first
			double bin_highedge = 0;
			while(c_iter.hasNext()) {
				bin_highedge = o_iter.getDoubleNext();
				c_iter.next().setDoubleCurrent(bin_lowedge + (bin_highedge-bin_lowedge)/2.0);
				bin_lowedge = bin_highedge;                 //set ready for next time through the loop
			}
		}
		return centre_array;
	}

	@Override
	public String toString() {
		return "XYSigma Headerless";
	}
	
	@Override
	public String getExtensionName() {
		return "xys";
	}

}
