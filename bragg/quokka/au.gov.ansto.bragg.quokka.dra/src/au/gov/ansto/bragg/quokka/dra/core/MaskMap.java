/******************************************************************************* 
* Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0 
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* 
* Contributors: 
*    Norman Xiong - ApplyRegion class prototype
*    Paul Hathaway - modified January 2009
*******************************************************************************/
package au.gov.ansto.bragg.quokka.dra.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.exception.PlotFactoryException;
import au.gov.ansto.bragg.datastructures.core.exception.StructureTypeException;
import au.gov.ansto.bragg.datastructures.core.plot.Axis;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.region.RectilinearRegion;
import au.gov.ansto.bragg.datastructures.core.region.Region;
import au.gov.ansto.bragg.datastructures.core.region.RegionFactory;
import au.gov.ansto.bragg.datastructures.core.region.RegionSet;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.datastructures.util.AxisRecord;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

public class MaskMap extends ConcreteProcessor {

	private static final String processClass = "MaskMap"; 
	private static final String processClassVersion = "1.0"; 
	private static final long processClassID = 2009013001; 

	private final static DataStructureType dataStructureType = DataStructureType.plot;
	private final static DataDimensionType dataDimensionType = DataDimensionType.map;
    private Boolean isDebugMode = true;

	private IGroup maskRoi;
	private Plot inPlot;
	private Plot outPlot;
	private Boolean doForceSkip = false;
	private Boolean doForceStop = false;

	private Integer top,btm,lft,rgt;

	private int[] plotShape;
	List<Axis> plotAxes;		
	List<Axis> dataAxes;
	
	public MaskMap() {
		this.setReprocessable(false);
	}
	
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {

    	this.outPlot = this.inPlot;
    	stampProcessLog();
    	
		if (!this.doForceSkip) {

			plotShape = outPlot.findSignalArray().getShape();
			plotAxes = outPlot.getAxisList();		
			dataAxes = new ArrayList<Axis>();
			dataAxes.add(plotAxes.get(plotAxes.size() - 2));
			dataAxes.add(plotAxes.get(plotAxes.size() - 1));

			applyMask();
			
			if (0<top+btm+lft+rgt) {
				if (!((top<0) || (btm<0) || (lft<0) || (rgt<0))) {
					maskTrim();
				}
			}
		} else {
			stampProcessSkip();
		}
		return doForceStop;
	}
	
	private void stampProcessLog() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("ProcessClass/Version/ID: ["
				+processClass+";"
				+processClassVersion+";"
				+processClassID+"]");		
	}
	
	private void stampProcessSkip() {
		((NcGroup) outPlot).setLocation(inPlot.getLocation());
		outPlot.addProcessingLog("["+processClassID+"]:"+"SKIP process");		
	}
	
	private void applyMask() throws StructureTypeException, IOException, PlotFactoryException, InvalidArrayTypeException 
	{
		applyMask(this.maskRoi);		
	}
	
	private void applyMask(IGroup mask) throws StructureTypeException, IOException, PlotFactoryException, InvalidArrayTypeException 
	{
		if (null!=mask) {
			outPlot = (Plot) RegionUtils.applyRegionToGroup(inPlot,mask);
	
			if (outPlot instanceof Plot){
				String regionString = "";
				if (mask instanceof RegionSet){
					for (Region region : ((RegionSet) mask).getRegionList()){
						if (region instanceof RectilinearRegion){
							RectilinearRegion recRegion = (RectilinearRegion) region;
							int rank = recRegion.getRank();
							double[] section;
							if (rank >= 3){
								section = recRegion.getPhysicalSection(rank - 3);
								regionString += "z in [" + section[0] + "," + section[1] + "]; ";
							}
							section = recRegion.getPhysicalSection(rank - 2);
							regionString += "y in [" + section[0] + "," + section[1] + "]; ";
							section = recRegion.getPhysicalSection(rank - 1);
							regionString += "x in [" + section[0] + "," + section[1] + "]; ";						
						}
					}
				}
				((Plot) outPlot).addProcessingLog("apply mask : " + regionString);
			}
		}
	}
	
	private void maskTrim() throws StructureTypeException, IOException, PlotFactoryException, InvalidArrayTypeException 
	{
		int topIdx,btmIdx,lftIdx,rgtIdx;
		
		int[] dataShape = new int[]{
				plotShape[plotAxes.size()-2],
				plotShape[plotAxes.size()-1]};
		
		AxisRecord vAxis = AxisRecord.createRecord(dataAxes.get(0),0,dataShape);
		AxisRecord hAxis = AxisRecord.createRecord(dataAxes.get(1),1,dataShape);
		
		/**
		 *  determine directional sense of axes for correct orientation of mask 
		 **/
		if (vAxis.centre(0) < vAxis.centre((int)(vAxis.length()-1))) {
			btmIdx = btm;
			topIdx = (int)vAxis.length() - 1 - top;
		} else {
			btmIdx = (int)vAxis.length() - 1 - btm;
			topIdx = top;
		}
		if (hAxis.centre(0) < hAxis.centre((int)(hAxis.length()-1))) {
			if (isDebugMode) {
	    		System.out.println("*> hc(0), hc(L-1): "+hAxis.centre(0)+", "+hAxis.centre((int)(hAxis.length()-1)));
			}
			lftIdx = lft;
			rgtIdx = (int)hAxis.length() - 1 - rgt;
		} else {
	    	lftIdx = (int)hAxis.length() - 1 - lft;
			rgtIdx = rgt;
			if (isDebugMode) {
	    		System.out.println("*> (lftIdx,rgtIdx): "+lftIdx+", "+rgtIdx);
			}
		}

		double vRef = vAxis.centre(btmIdx);
		double hRef = hAxis.centre(lftIdx);
		double vRange = Math.abs(vAxis.centre(topIdx) - vRef);
		double hRange = Math.abs(hAxis.centre(rgtIdx) - hRef);

		Region mask = (Region) RegionFactory.createRectilinearRegion(
				(IGroup) outPlot, 
				"trim", 
				new double[]{vRef,hRef}, 
				new double[]{vRange,hRange},
				new String[]{"mm","mm"},
				true);

		if (isDebugMode) {
    		System.out.println("*> vRef, vRange: "+vRef+", "+vRange);
    		System.out.println("*> hRef, hRange: "+hRef+", "+hRange);
    	}

		applyMask((IGroup) mask);
	}
		
	/* Client Support methods -----------------------------------------*/
	
	public static DataStructureType getDataStructureType() {
		return dataStructureType;
	}

	public static DataDimensionType getDataDimensionType() {
		return dataDimensionType;
	}

	public void setIsDebugMode(Boolean isDebugMode) {
		this.isDebugMode = isDebugMode;
	}

	public Boolean getIsDebugMode() {
		return isDebugMode;
	}	
	
	/* Port get/set methods -----------------------------------------*/
    
    /* In-Ports */

	/**
	 * @param inPlot the plot Group entering the processor
	 */
	public void setInPlot(Plot inPlot) {
		this.inPlot = inPlot;
	}

    /* Out-Ports */

	/**
	 * @return the output Plot
	 */
	public Plot getOutPlot() {
		return outPlot;
	}

	/* Var-Ports (options) */

	/**
	 * @param doSkip indicates whether to skip this process
	 */
	public void setSkip(Boolean doSkip) {
		this.doForceSkip = doSkip;
	}
	/**
	 * @param doStop indicates whether to stop chain after processing
	 */
	public void setStop(Boolean doStop) {
		this.doForceStop = doStop;
	}
	public Boolean getSkip() {
		return doForceSkip;
	}

	public Boolean getStop() {
		return doForceStop;
	}

    /* Var-Ports (tuners) */

	/**
	 * @param maskRoi the mask region of interest to set
	 */
	public void setMaskRoi(IGroup maskRoi) {
		this.maskRoi = maskRoi;
	}
	
	public void setTop(Integer top) {
		this.top = top;
	}

	public void setBtm(Integer btm) {
		this.btm = btm;
	}

	public void setLft(Integer lft) {
		this.lft = lft;
	}

	public void setRgt(Integer rgt) {
		this.rgt = rgt;
	}

}
