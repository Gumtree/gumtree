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
package au.gov.ansto.bragg.nbi.dra.source;

import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.region.RectilinearRegion;
import au.gov.ansto.bragg.datastructures.core.region.Region;
import au.gov.ansto.bragg.datastructures.core.region.RegionFactory;
import au.gov.ansto.bragg.datastructures.core.region.RegionSet;
import au.gov.ansto.bragg.datastructures.core.region.RegionUtils;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/**
 * @author nxi
 * Created on 24/09/2008
 */
public class ApplyRegion extends ConcreteProcessor {

	private IGroup applyRegion_region;
	private IGroup applyRegion_inputGroup;
	private IGroup applyRegion_outputGroup;
	private IGroup applyRegion_sourceGroup;
	private Boolean ignoreXLimits = false;
	private Boolean ignoreYLimits = false;
	private Boolean applyRegion_skip = false;
	private Boolean applyRegion_stop = false;
	/* (non-Javadoc)
	 * @see au.gov.ansto.bragg.nbi.dra.core.ConcreteProcessor#process()
	 */
	public Boolean process() throws Exception {
		applyRegion_sourceGroup = applyRegion_inputGroup;
		if (applyRegion_skip || applyRegion_region == null || 
				applyRegion_inputGroup.getShortName().matches("emptyData")){
			applyRegion_outputGroup = applyRegion_inputGroup;
			setReprocessable(false);
			return applyRegion_stop;
		}
		String regionString = "";
		List<IArray> axisList = ((NcGroup) applyRegion_inputGroup).getAxesArrayList();
		IArray xAxis = axisList.get(axisList.size() - 1);
		double xReference = xAxis.getArrayMath().getMinimum();
		double xRange = xAxis.getArrayMath().getMaximum() - xReference;
		IGroup newRegionSet = applyRegion_region.clone();
		List<Region> regionList;
		if (newRegionSet instanceof RegionSet)
			regionList = ((RegionSet) newRegionSet).getRegionList();
		else{
			regionList = RegionUtils.getRegionFromGroup(newRegionSet);
			newRegionSet = RegionFactory.createRegionSet(Factory.createGroup("region_root"), 
					newRegionSet.getShortName());
			for (Region region : regionList)
				newRegionSet.addSubgroup(region);
		}
		for (Region region : regionList){
			if (region instanceof RectilinearRegion){
				RectilinearRegion recRegion = (RectilinearRegion) region;
				int rank = recRegion.getRank();
				if (rank >= 3){
					double[] zSection = recRegion.getPhysicalSection(rank - 3);
					regionString += "z in [" + zSection[0] + "," + zSection[1] + "]; ";
				}
				double[] ySection = recRegion.getPhysicalSection(rank - 2);
				double[] xSection = recRegion.getPhysicalSection(rank - 1);
				regionString += "y in [" + ySection[0] + "," + ySection[1] + "]; ";
				if (ignoreXLimits){
					recRegion.setPhysicalReference(new double[]{ySection[0], xReference});
					recRegion.setPhysicalRange(new double[]{ySection[1] - ySection[0], xRange});
					regionString += "x in [" + xReference + "," + (xReference + xRange) + "]; ";
				}else{
					regionString += "x in [" + xSection[0] + "," + xSection[1] + "]; ";
				}
			}
		}
		applyRegion_outputGroup = RegionUtils.applyRegionToGroup(applyRegion_inputGroup, 
				newRegionSet);
		if (applyRegion_outputGroup instanceof Plot){
			if (regionString.trim().length() > 0)
				((Plot) applyRegion_outputGroup).addProcessingLog("apply mask : " + regionString);
//			long memorySize = ((Plot) applyRegion_inputGroup).calculateMemorySize();
//			if (memorySize > Register.REPROCESSABLE_THRESHOLD){
//				((Plot) applyRegion_inputGroup).clearData();
//				applyRegion_sourceGroup = applyRegion_outputGroup;
//				setReprocessable(false);
//			}else
//				setReprocessable(true);
		}
		applyRegion_inputGroup.getGroupList().clear();
		return applyRegion_stop;
	}
	/**
	 * @return the applyRegion_outputGroup
	 */
	public IGroup getApplyRegion_outputGroup() {
		return applyRegion_outputGroup;
	}
	/**
	 * @param applyRegion_region the applyRegion_region to set
	 */
	public void setApplyRegion_region(IGroup applyRegion_region) {
		this.applyRegion_region = applyRegion_region;
	}
	/**
	 * @param applyRegion_inputGroup the applyRegion_inputGroup to set
	 */
	public void setApplyRegion_inputGroup(IGroup applyRegion_inputGroup) {
		this.applyRegion_inputGroup = applyRegion_inputGroup;
	}
	/**
	 * @param applyRegion_skip the applyRegion_skip to set
	 */
	public void setApplyRegion_skip(Boolean applyRegion_skip) {
		this.applyRegion_skip = applyRegion_skip;
	}
	/**
	 * @param applyRegion_stop the applyRegion_stop to set
	 */
	public void setApplyRegion_stop(Boolean applyRegion_stop) {
		this.applyRegion_stop = applyRegion_stop;
	}
	/**
	 * @return the applyRegion_sourceGroup
	 */
	public IGroup getApplyRegion_sourceGroup() {
		return applyRegion_sourceGroup;
	}
	/**
	 * @param ignoreXLimits the ignoreXLimits to set
	 */
	public void setIgnoreXLimits(Boolean ignoreXLimits) {
		this.ignoreXLimits = ignoreXLimits;
	}
	/**
	 * @param ignoreYLimits the ignoreYLimits to set
	 */
	public void setIgnoreYLimits(Boolean ignoreYLimits) {
		this.ignoreYLimits = ignoreYLimits;
	}

}
