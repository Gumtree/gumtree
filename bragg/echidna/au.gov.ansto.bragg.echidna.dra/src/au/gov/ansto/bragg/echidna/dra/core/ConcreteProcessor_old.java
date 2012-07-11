package au.gov.ansto.bragg.echidna.dra.core;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataDimensionType;
import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;

/**
 * The abstract concreate processor.
 * @author nxi
 *
 */
public interface ConcreteProcessor_old {

//	public List<Object> process(Object ...objects );
	public Boolean process() throws Exception;
	public DataStructureType getDataStructureType();
	
	public DataDimensionType getDataDimensionType();
}
