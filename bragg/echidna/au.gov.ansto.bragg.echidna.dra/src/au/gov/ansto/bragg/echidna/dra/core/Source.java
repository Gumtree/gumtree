package au.gov.ansto.bragg.echidna.dra.core;

import java.util.List;

import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.Util;
import au.gov.ansto.bragg.datastructures.core.plot.Plot;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.process.processor.ConcreteProcessor;

/* We convert a source datafile into a databag for input to the algorithm chain.  We
 * add a 'variance' dataitem to the signal databag, which is initially identical to
 * the data themselves, that is, we assume counting statistics.  We also make sure that
 * we are using Double data rather than integer counts as we will soon be normalising
 * and efficiency correcting which will involve multiplication by real numbers.
 */

public class Source extends ConcreteProcessor{
	IGroup source_groupData = null;
	String source_dataName = "data";
	Plot source_outdata = null;
	IArray error_data;
	String source_dataStructureType = "plot";
	String source_dataDimensionType = "map";
	Boolean source_stop = false;
	/**
	 * @param source_stop the source_stop to set
	 */
	public void setSource_stop(Boolean source_stop) {
		this.source_stop = source_stop;
	}

	private IGroup source_asgroup = null;          //For holding our data before we turn it into a Plot object
	// Fields for interaction with visualisation components
	
	@SuppressWarnings("static-access")
	public Boolean process() throws Exception{
		DataDimensionType dimensionType = null;
		try{
			dimensionType = DataDimensionType.valueOf(source_dataDimensionType);
		}catch (Exception e) {
			dimensionType = DataDimensionType.undefined;
		}

		DataStructureType structureType = null;
		// Set our data structure type and dimension variables from the object's fields	
		structureType = Util.getDataStructureType(source_groupData);
		int index = 0;
		
		/* (Comment added by JRH) A combined data structure type contains a number of groups of data, for example,
		 * each originating from a different NeXuS file. The following code section sums these groups together,
		 * but before doing that it needs to capture the metadata, which will come from the first subgroup.
		 * The following code section therefore transfers metadata group(s) from the first subgroup of the combined
		 * object to the top level of the
		 * combined group.  In this code, metadata groups are those that appear on the same level as
		 * a group containing the group name "data" *could* appear.  Any group called "data" is ignored.
		 * This inclusion of the metadata groups at the top level appears to be necessary in order for the copyToPlot
		 * method to succeed - note the assumption that the metadata for the first group will apply to the remaining
		 * groups.
		 */
		if (structureType == DataStructureType.combined){
			List<?> subGroup = source_groupData.getGroupList();
			Plot plot = null;
			boolean hasMetaData = false;
			for (Object object : subGroup){   // run through subgroups
				if (object instanceof IGroup){
					IGroup group = (IGroup) object;
					if (!hasMetaData){
						List<?> subGroupList = group.getGroupList();
						for (Object metaGroup : subGroupList){
							if (metaGroup instanceof IGroup)
								if (!((IGroup) metaGroup).getName().equals("data"))
									source_groupData.addSubgroup((IGroup) metaGroup);
						}
						hasMetaData = true;
						source_groupData.getParentGroup().addSubgroup(group);
					}
					if (plot == null)
						plot = PlotFactory.copyToPlot(group.findGroup(source_dataName), "CombinedPlot" + index++, dimensionType);
					else
						plot = plot.add(PlotFactory.copyToPlot(group.findGroup(source_dataName), "CombinedPlot" + index++, dimensionType));
				}
			}
			if (plot != null){
				source_asgroup = plot;
				((Plot) source_asgroup).reduce();
			}
			
		}else {
			// First try to locate the data in our databag.  The heuristic is to try for a group named in source_dataname;
			// if this is not there, we look for a dataitem with this name and take its parent; if this also fails, we
			// bring out the big guns and search for a dataitem flagged as the signal, and take its parent.
			try {
				source_asgroup = source_groupData.getGroup(source_dataName);
			} catch (Exception e) {
				// TODO: handle exception
				source_asgroup = source_groupData.getDataItem(source_dataName).getParentGroup();
			}
			if (source_asgroup == null) source_asgroup = ((NcGroup) source_groupData).findSignal().getParentGroup();
		}
		structureType = Util.getDataStructureType(source_asgroup);
		source_outdata = PlotFactory.copyToPlot(source_asgroup, source_asgroup.getShortName()+"plot_copy", dimensionType);
		IArray rawdata = ((NcGroup) source_asgroup).findSignal().getData();
		PlotFactory.addDataVarianceToPlot(source_outdata, "dvariance", rawdata.copy());
		source_outdata.reduce();  //remove any bad axes
		return source_stop;
	}

	public IGroup getSource_groupData() {
		return source_groupData;
	}
	public void setSource_dataName(String source_dataName) {
		this.source_dataName = source_dataName;
	}
	public void setSource_groupData(IGroup source_groupData) {
		this.source_groupData = source_groupData;
	}
	public Plot getSource_outdata() {
		return source_outdata;
	}
	
	public DataStructureType getDataStructureType() {
		return DataStructureType.valueOf(source_dataStructureType);
	}

	public DataDimensionType getDataDimensionType() {
		return DataDimensionType.valueOf(source_dataDimensionType);
	}


	public void setSource_dataStructureType(String source_dataStructureType) {
		this.source_dataStructureType = source_dataStructureType;
	}
	
	public void setSource_dataDimensionType(String source_dataDimensionType) {
		this.source_dataDimensionType = source_dataDimensionType;
	}


	
}
