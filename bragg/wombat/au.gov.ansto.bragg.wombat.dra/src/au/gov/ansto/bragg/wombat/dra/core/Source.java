package au.gov.ansto.bragg.wombat.dra.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.Factory;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDimension;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;
import au.gov.ansto.bragg.datastructures.core.plot.PlotFactory;
import au.gov.ansto.bragg.datastructures.core.plot.PlotUtil;

/* We convert a source datafile into a databag for input to the algorithm chain.  We
 * add a 'variance' dataitem to the signal databag, which is initially identical to
 * the data themselves, that is, we assume counting statistics.  We also make sure that
 * we are using Double data rather than integer counts as we will soon be efficiency correcting 
 * which will involve multiplication by real numbers.
 * 
 * Other preparations we make include:
 * 1. Removing any axes of dimension 1
 */

public class Source implements ConcreteProcessor{
	IGroup source_groupData = null;
	String source_dataName = "data";
	IGroup source_scanData = null;
	IArray error_data;
	String source_dataStructureType = "plot";
	String source_dataDimensionType = "map";
	private IGroup source_asgroup = null;          //For holding our data before we turn it into a Plot object
	// Fields for interaction with visualisation components
	
	@SuppressWarnings("static-access")
	public Boolean process() throws Exception{
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

		// Set our data structure type and dimension variables from the object's fields

		DataStructureType structureType = null;
		try{
			structureType = DataStructureType.valueOf(source_dataStructureType);
		}catch (Exception e) {
			structureType = DataStructureType.undefined;
		}
		DataDimensionType dimensionType = null;
		try{
			dimensionType = DataDimensionType.valueOf(source_dataDimensionType);
		}catch (Exception e) {
			dimensionType = DataDimensionType.undefined;
		}
		// Convert to double
		// Check array dimensions
		// Add unit information
		// Add error information
		IArray rawdata = ((NcGroup) source_asgroup).findSignal().getData();
		IGroup replaced_group = null;                          //Where our updated data goes, if necessary
		int [] data_shape = rawdata.getShape();
		boolean need_to_replace = false;                      //Flag that we need to make
		//an updated Group
		// Some axes may have dimension one, e.g. a one-point scan  or the time of flight
		// axis for Echidna and Wombat.  We remove all such axes, which essentially means
		// that Wombat and Echidna data may appear to be 2 or 3D depending on the scan
		// type. 
		// Remove axes that have dimension one.  The original shape is stored in data_shape,
		// which we will use when removing the axes from the output group.
		rawdata = rawdata.getArrayUtils().reduce().getArray();
		if(rawdata.getRank()<data_shape.length) need_to_replace=true;
		/*if(rawdata.getElementType()!=float.class){   // Need to convert to double
			/* Note that the following actually alters the contents of a Group.  This is technically
			 * not allowed in Cicada algorithms, as a Cicada client may be holding a reference to the
			 * item that we are changing or deleting.  Because the change in this case will only occur the first
			 * time a raw data object is processed, and before any sink has grabbed a reference to the
			 * group, it should be safe.  A better alternative would be to 'clone' the group.  
			 *
			need_to_replace=true;        //we will replace the data
			// Create a new double array and fill it with the data
			int [] rawdata_shape = rawdata.getShape();
			Array doubledata = Factory.createArray(float.class, rawdata_shape);
			ArrayIterator raw_iter = rawdata.getIterator();
			ArrayIterator dbl_iter = doubledata.getIterator();
			while(raw_iter.hasNext()) {
				dbl_iter.setFloatNext(raw_iter.getFloatNext());
			}
			System.out.println("Finished converting to doubles");
			rawdata = doubledata;                 //Going into the next section of the program our transformed data are in rawdata
		}*/
		if(need_to_replace) {
			//  A new way of replacing the old data: we copy the entire group, except for the data.
			replaced_group = Factory.createGroup(source_asgroup.getParentGroup(), source_asgroup.getShortName(), false);
			for (Object group : source_asgroup.getGroupList())
				replaced_group.addSubgroup((IGroup) group);
			for (Object attribute : source_asgroup.getAttributeList())
				replaced_group.addOneAttribute((IAttribute) attribute);
			for (Object dimension : source_asgroup.getDimensionList())
				replaced_group.addOneDimension((IDimension) dimension);

			// now replace the old data.  Using removeDataItem is not possible as this call
			// call will attempt to calculate hashcodes of the Variables that it is passed in order
			// to verify equality,
			// and so the Variables need both non-null Dimensions and dataTypes.  So instead we create a
			// new group object by copying only those items that are unchanged, and inserting changed versions
			IDataItem old_signal = ((NcGroup) source_asgroup).findSignal();
			String name_to_replace = old_signal.getShortName();
			// Work out which of axes we want to ditch as well from the final group. Note that NeXuS numbers axes
			// from 1, not zero, whereas we start at zero.  Note that if we rerun this processor
			// on previously processed data, the axes will start from zero and there shouldn't
			// be any non-reduced axes.
			List<IDataItem> axis_list = ((NcGroup) source_asgroup).findAxes();
			ArrayList<String> axes_to_ditch = new ArrayList<String>();
			for (IDataItem di : axis_list) {
				String axis_no = di.getAttribute("axis").getStringValue();
				System.out.printf("Found axis %s = %s%n", di.getShortName(),axis_no);
				int bad_cand = Integer.parseInt(di.getAttribute("axis").getStringValue());
				if(bad_cand>0 && data_shape[bad_cand-1]==1) axes_to_ditch.add(di.getShortName());
			}
			List<?> old_atts = old_signal.getAttributeList();
			IDataItem new_signal = Factory.createDataItem(replaced_group, name_to_replace, rawdata);
			for (Object di : source_asgroup.getDataItemList())
				if(((IDataItem) di).getShortName()!=name_to_replace && !(axes_to_ditch.contains(((IDataItem) di).getShortName())))
					replaced_group.addDataItem((IDataItem) di);
			/* The following iterator is deliberately non-parameterised: how does one
			 * specify that it is a list of type Attribute?
			 */
			for (Iterator j = old_atts.iterator(); j.hasNext();) {  
				new_signal.addOneAttribute((IAttribute) j.next());
			}
			// Now add in the new signal which should have all the right attributes, so we add it as a dataitem rather
			// than trying to build a result group
			replaced_group.addDataItem(new_signal);
			//Set axis dimension information: we might have removed one of the dimensions, so we need to lower the dimension of
			//any succeeding axes.  For each axis still present, we reduce its axis number
			//by the number of preceding deleted axes, which we count each time through
			//the loop
			List<IDataItem> data_axes = ((NcGroup) replaced_group).findAxes();
			for (IDataItem oneaxis: data_axes)  {
				int axis_no = Integer.parseInt(oneaxis.getAttribute("axis").getStringValue());
				int new_axis_no = axis_no;
				for (int old_axis=0;old_axis<axis_no;old_axis++ ) 
					if (data_shape[old_axis]==1) new_axis_no--;
				IAttribute dim_attr = Factory.createAttribute("axis", String.format("%d",new_axis_no));
				oneaxis.addOneAttribute(dim_attr);
			}
		} else {
			replaced_group = source_asgroup;
		}
		// Make sure our output Group is plottable, that is, of type Plot.  The plottable version of our group is
		// stored in a dataitem called item_name+'plot_copy'
		if (structureType == DataStructureType.plot){
			if (PlotUtil.getDataStructureType(replaced_group) != DataStructureType.plot){
				IGroup plotCopy = replaced_group.findGroup(replaced_group.getShortName() + "plot_copy");
				if (plotCopy == null) 
					source_scanData = PlotFactory.copyToPlot(replaced_group, 
							replaced_group.getShortName() + "plot_copy", dimensionType);
				else source_scanData = plotCopy;
			}
			//Add variance information using proper API
			try{
				PlotFactory.addDataVarianceToPlot(source_scanData,"dvariance",rawdata);
			}catch (Exception e) {
				// TODO: handle exception
			}
		} else {       //non-plottable data; simply set the appropriate attributes and pass forward
			source_scanData.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, 
					structureType.name());
			source_scanData.addStringAttribute(StaticDefinition.DATA_DIMENSION_TYPE, 
					dimensionType.name());
		}
		return false;
	}
	
	public IGroup getSource_groupData() {
		return source_groupData;
	}
	public void setSource_dataName(String source_dataName) {
		this.source_dataName = source_dataName;
	}
	public void setSource_scanData(IGroup source_scanData) {
		this.source_scanData = source_scanData;
	}
	public void setSource_groupData(IGroup source_groupData) {
		this.source_groupData = source_groupData;
	}
	public IGroup getSource_scanData() {
		return source_scanData;
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
