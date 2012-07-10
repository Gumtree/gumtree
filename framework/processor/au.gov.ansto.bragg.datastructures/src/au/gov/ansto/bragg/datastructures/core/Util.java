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
package au.gov.ansto.bragg.datastructures.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IGroup;

import au.gov.ansto.bragg.datastructures.core.StaticDefinition.DataStructureType;

/**
 * @author nxi
 * Created on 12/03/2008
 */
public class Util {

	public final static String NEXUS_CLASS_ATTRIBUTE_NAME = "NX_class";
	public final static String NEXUS_ENTRY_ATTRIBUTE_VALUE = "NXentry";
	public final static String NEXUS_DATA_ATTRIBUTE_VALUE = "NXdata";
	public final static String NEXUS_VERSION_NAME = "NeXus_version";
	public final static String NEXUS_SIGNAL_ATTRIBUTE_NAME = "signal";
	public final static String NEXUS_AXES_ATTRIBUTE_NAME = "axes";
	
	public final static String PLOT_METADATA_ATTRIBUTE_NAME = "metadata";
	/**
	 * Check if the group data is in the specific data type.
	 * @param group GDM Group object
	 * @param type DataStructureType enum type
	 * @return boolean
	 * Created on 19/03/2008
	 */
	public static boolean matchStructureType(IGroup group, 
			StaticDefinition.DataStructureType type){
//		Attribute typeAttribute = group.findAttribute(StaticDefination.DATA_STRUCTURE_TYPE);
//		if (typeAttribute != null) {
//			String typeValue = typeAttribute.getStringValue();
//			if (typeValue.matches(type.name())) return true;
//		}
//		return false;
		return hasAttribute(group, StaticDefinition.DATA_STRUCTURE_TYPE, type.name());
	}

	/**
	 * Return the list of entries of the root group. Condition is the group has to be a
	 * root group. The entries of the group are also in GDM Group type.
	 * @param rootGroup GDM Group type, must be a root group
	 * @param type DataStructureType type
	 * @return a List of Group objects
	 * Created on 19/03/2008
	 */
	public static List<IGroup> getEntryList(IGroup rootGroup, DataStructureType type){
		List<IGroup> entryList = new ArrayList<IGroup>();
		if (!rootGroup.isRoot()) return entryList;
		List<IGroup> subGroupList = rootGroup.getGroupList();
		switch (type) {
		case nexusRoot: 
			for (Iterator<?> iterator = subGroupList.iterator(); iterator.hasNext();) {
				IGroup subGroup = (IGroup) iterator.next();
				if (hasAttribute(subGroup, NEXUS_CLASS_ATTRIBUTE_NAME, NEXUS_ENTRY_ATTRIBUTE_VALUE))
					entryList.add(subGroup);
			}
			break;
		case undefined: 
			for (Iterator<?> iterator = subGroupList.iterator(); iterator.hasNext();) {
				IGroup subGroup = (IGroup) iterator.next();
				if (hasAttribute(subGroup, NEXUS_CLASS_ATTRIBUTE_NAME, NEXUS_ENTRY_ATTRIBUTE_VALUE))
					continue;
				IAttribute attribute = subGroup.getAttribute(StaticDefinition.DATA_STRUCTURE_TYPE);
				if (attribute == null) {
					entryList.add(subGroup);
					continue;
				}
				if (hasAttribute(subGroup, StaticDefinition.DATA_STRUCTURE_TYPE, type.name())){
					entryList.add(subGroup);
					continue;
				}
			}
			break;
		default: 
			for (Iterator<?> iterator = subGroupList.iterator(); iterator.hasNext();) {
				IGroup subGroup = (IGroup) iterator.next();
				if (hasAttribute(subGroup, StaticDefinition.DATA_STRUCTURE_TYPE, type.name()))
					entryList.add(subGroup);
			}			
			break;
		}
		return entryList;
	}

	/**
	 * Find the data structure type of the Group data. It will find the attribute with name of
	 * 'dataStructureType', and convert the value to enum type. 
	 * @param group in Group type
	 * @return DataStructureType enum type
	 * Created on 19/03/2008
	 */
	public static DataStructureType getDataStructureType(IGroup group){
		IAttribute dataStructureAttribute = group.getAttribute(StaticDefinition.DATA_STRUCTURE_TYPE);
		if (dataStructureAttribute == null) {
			if (group.isRoot()){
				if (group.getAttribute("NeXus_version") != null)
					return DataStructureType.nexusRoot;
				for (IGroup subGroup : group.getGroupList()){
					if (subGroup.hasAttribute(NEXUS_CLASS_ATTRIBUTE_NAME, NEXUS_ENTRY_ATTRIBUTE_VALUE))
						return DataStructureType.nexusRoot;
				}
			}
			if (group.hasAttribute(NEXUS_CLASS_ATTRIBUTE_NAME, NEXUS_ENTRY_ATTRIBUTE_VALUE))
				return DataStructureType.nexusEntry;
			if (group.hasAttribute(NEXUS_CLASS_ATTRIBUTE_NAME, NEXUS_DATA_ATTRIBUTE_VALUE))
				return DataStructureType.nexusData;
			return DataStructureType.undefined;
		}
		String dataStructureTypeName = dataStructureAttribute.getStringValue();
		DataStructureType type = DataStructureType.valueOf(dataStructureTypeName);
		if (type == null) return DataStructureType.undefined;
		return type;
	}
	
	/**
	 * Check if the group data has an attribute with the given name and attribute value.
	 * @param group GDM Group object
	 * @param attributeName String type
	 * @param attributeValue String type
	 * @return boolean
	 * Created on 19/03/2008
	 */
	public static boolean hasAttribute(IGroup group,
			String attributeName, String attributeValue) {
		// TODO Auto-generated method stub
		IAttribute attribute = group.getAttribute(attributeName);
		if (attribute == null) return false;
		String value = attribute.getStringValue();
		if (value == null) return false;
		if (value.equals(attributeValue)) return true;
		return false;
	}
	
	/**
	 * Set the data structure attribute of the group. If the group does not has 
	 * such an attribute, add a new attribute. Otherwise change the attribute value
	 * to the given one. 
	 * @param group GDM Group object
	 * @param type DataStructureType value
	 * Created on 28/08/2008
	 */
	public static void setDataStructure(IGroup group, DataStructureType type){
		group.addStringAttribute(StaticDefinition.DATA_STRUCTURE_TYPE, type.name());
	}
}
