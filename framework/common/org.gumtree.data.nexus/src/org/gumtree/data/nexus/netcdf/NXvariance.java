/**
 * 
 */
package org.gumtree.data.nexus.netcdf;

import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.impl.netcdf.NcDataset;
import org.gumtree.data.impl.netcdf.NcGroup;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.nexus.IVariance;

import ucar.nc2.dataset.VariableDS;

/**
 * @author nxi
 *
 */
public class NXvariance extends NXDataItem implements IVariance {

	/**
	 * @param from
	 */
	public NXvariance(VariableDS from) {
		super(from);
		if (!hasAttribute(NXConstants.GLOBAL_SIGNAL_CLASS_LABEL, 
				NXConstants.VARANCE_CLASS_VALUE)) {
			addStringAttribute(NXConstants.GLOBAL_SIGNAL_CLASS_LABEL, 
				NXConstants.VARANCE_CLASS_VALUE);
		}
	}

	/**
	 * @param group
	 * @param shortName
	 * @param array
	 * @throws InvalidArrayTypeException
	 */
	public NXvariance(NcGroup group, String shortName, IArray array)
			throws InvalidArrayTypeException {
		super(group, shortName, array);
		if (!hasAttribute(NXConstants.GLOBAL_SIGNAL_CLASS_LABEL, 
				NXConstants.VARANCE_CLASS_VALUE)) {
			addStringAttribute(NXConstants.GLOBAL_SIGNAL_CLASS_LABEL, 
				NXConstants.VARANCE_CLASS_VALUE);
		}
	}

	/**
	 * @param dataset
	 * @param group
	 * @param shortName
	 * @param array
	 * @throws InvalidArrayTypeException
	 */
	public NXvariance(NcDataset dataset, NcGroup group, String shortName,
			IArray array) throws InvalidArrayTypeException {
		super(dataset, group, shortName, array);
		if (!hasAttribute(NXConstants.GLOBAL_SIGNAL_CLASS_LABEL, 
				NXConstants.VARANCE_CLASS_VALUE)) {
			addStringAttribute(NXConstants.GLOBAL_SIGNAL_CLASS_LABEL, 
				NXConstants.VARANCE_CLASS_VALUE);
		}
	}

}
