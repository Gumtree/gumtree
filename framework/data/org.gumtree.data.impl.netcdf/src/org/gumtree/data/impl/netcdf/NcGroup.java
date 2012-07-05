/****************************************************************************** 
 * Copyright (c) 2008 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong - initial API and implementation
 *    Paul Hathaway
 ******************************************************************************/
package org.gumtree.data.impl.netcdf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gumtree.data.Factory;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.exception.InvalidArrayTypeException;
import org.gumtree.data.exception.NoResultException;
import org.gumtree.data.exception.SignalNotAvailableException;
import org.gumtree.data.exception.WriterException;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.impl.io.NcHdfWriter;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IDimension;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.interfaces.IKey;
import org.gumtree.data.utils.Utilities.ModelType;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.dataset.VariableDS;

/**
 * Implementation of IGroup using Netcdf classes.
 * 
 * @see IGroup
 * @author nxi Created on 24/03/2009
 */
public class NcGroup extends ucar.nc2.Group implements IGroup {

	/**
	 * Parent dataset.
	 */
	protected NcDataset dataset;
	/**
	 * Dictionary that holding key-path pairs. Only the root group has this
	 * field initialised.
	 */
	protected IDictionary dictionary;

	@Override
	public ModelType getModelType() {
		return ModelType.Group;
	}
	
	@Override
	public void setDictionary(final IDictionary dictionary) {
		if (isRoot()) {
			this.dictionary = dictionary;
		} else {
			getRootGroup().setDictionary(dictionary);
		}
	}

	/**
	 * Generic constructor that set fields.
	 * 
	 * @param dataset
	 *            NcDataset object
	 * @param parent
	 *            NcGroup object
	 * @param shortName
	 *            String value
	 * @param updateParent
	 *            if the parent will get updated (knows this group as a child)
	 */
	public NcGroup(final NcDataset dataset, final NcGroup parent,
			final String shortName, final boolean updateParent) {
		super(dataset == null ? null : dataset.getNetcdfDataset(), parent,
				shortName);
		if (updateParent && parent != null
				&& !parent.getGroups().contains(this)) {
			insertGroup(parent, shortName);
		}
		this.dataset = dataset;
	}

	/**
	 * Construct a NcGroup from a Netcdf Group object.
	 * 
	 * @param from
	 *            Netcdf Group object
	 * @param dataset
	 *            NcDataset object
	 */
	public NcGroup(final ucar.nc2.Group from, final NcDataset dataset) {
		this(dataset, null, from.getShortName(), false);
		for (Variable variable : from.getVariables()) {
			if (variable instanceof VariableDS) {
				NcDataItem cachedVariable = new NcDataItem(
						(VariableDS) variable);
				cachedVariable.setParent(this);
				cachedVariable.setDataset(dataset);
				variables.add(cachedVariable);
			}
		}
		for (Dimension d : from.getDimensions()) {
			NcDimension dimension = new NcDimension(d.getName(), d);
			dimension.setGroup(this);
			dimensions.add(dimension);
		}
		for (Group g : from.getGroups()) {
			NcGroup group = new NcGroup(g, dataset);
			group.setParent(this);
			groups.add(group);
		}
		for (Attribute a : from.getAttributes()) {
			NcAttribute attribute = new NcAttribute(a);
			attributes.add(attribute);
		}
		if (from.isRoot()) {
			this.parent = null;
		}
	}

	/**
	 * Copy constructor. Copy an IGroup object into NcGroup object.
	 * 
	 * @param from
	 *            IGroup object
	 */
	public NcGroup(final IGroup from) {
		this((NcDataset) from.getDataset(), (NcGroup) from.getParentGroup(),
				from.getShortName(), true);
		for (IDataItem dataItem : from.getDataItemList()) {
			addDataItem(new NcDataItem((NcDataItem) dataItem));
		}
		for (IDimension dimension : from.getDimensionList()) {
			if (dimension instanceof NcDimension) {
				addDimension((NcDimension) dimension);
			}
		}
		for (IGroup group : from.getGroupList()) {
			addGroup(group.clone());
		}
		for (IAttribute attribute : from.getAttributeList()) {
			addAttribute(new NcAttribute((NcAttribute) attribute));
		}
		if (isRoot()) {
			this.parent = null;
		}
	}

	@Override
	public void addDataItem(final IDataItem item) {
		IDataItem oldItem = getDataItem(item.getShortName());
		if (oldItem != null) {
			removeDataItem(oldItem);
		}
		if (item instanceof NcDataItem) {
			addVariable((NcDataItem) item);
		}
	}

	/**
	 * Overwriting addGroup from Netcdf implementation.
	 * 
	 * @param group
	 *            GDM Group object
	 */
	public void addGroup(final IGroup group) {
		if (group instanceof NcGroup) {
			NcGroup groupData = (NcGroup) group;
			super.addGroup(groupData);
			groupData.ncfile = this.ncfile;
			// updateDictionary(group.getShortName(), group.getName());
		}
	}

	// private void updateDictionary(String shortName, String path) {
	// dictionary.addEntry(shortName, path);
	// }

	// public void addLog(String newLog){
	// String parentLog = ((NcGroup) getParentGroup()).getLog();
	// if (parentLog != null) newLog = parentLog + "\n" + newLog;
	// else newLog = "Source data is from file\n" + newLog;
	// // Attribute logAttribute = new Attribute("log", newLog);
	// NcAttribute logAttribute = new NcAttribute("log", newLog);
	// addAttribute(logAttribute);
	// }

	/**
	 * Convenience method which writes the contents of md_value straight to a
	 * Dataitem. md_value will almost always be a character array.
	 * 
	 * @param mdStandard String value
	 * @param mdTag String value
	 * @param mdValue GDM Array
	 * @throws InvalidArrayTypeException invalid type
	 * @author jrh
	 */
	private void addMetadata(final String mdStandard, final String mdTag,
			final IArray mdValue) throws InvalidArrayTypeException {
		// add group
		IGroup mdGroup = getGroupWithAttribute("metadata", mdStandard);
		if (mdGroup == null) {
			mdGroup = new NcGroup(null, this, "metadata_store_" + mdStandard,
					false);
			mdGroup.addStringAttribute("metadata", mdStandard);
			addSubgroup(mdGroup);
		}
		IDataItem mdItem = Factory.createDataItem(mdGroup, mdTag, mdValue);
		mdGroup.addDataItem(mdItem);
	}

	/**
	 * @param mdStandard String value
	 * @param mdTag String value
	 * @param mdValue String value
	 * @param replace true or false
	 * @throws InvalidArrayTypeException invalid type
	 */
	public void addMetadata(final String mdStandard, final String mdTag,
			final String mdValue, final boolean replace)
			throws InvalidArrayTypeException {
		/*
		 * We implement non-replacement of the metadata by reading the current
		 * value and appending to it
		 */
		// Convert string to an Array
		IArray stringArray = Factory.createArray(mdValue.toCharArray());
		if (replace) {
			addMetadata(mdStandard, mdTag, stringArray);
		} else {
			Map<String, String> currentMetadata = null;
			try {
				currentMetadata = harvestMetadata(mdStandard);
			} catch (IOException e) {
				e.printStackTrace();
				// Assume old is corrupted
				addMetadata(mdStandard, mdTag, stringArray);
				return;
			}
			String oldValue = currentMetadata.get(mdTag);
			String newValue = "";
			if (oldValue != null) {
				newValue = oldValue + "\n" + mdValue;
			} else {
				newValue = mdValue;
			}
			addMetadata(mdStandard, mdTag, newValue);
		}
	}

	/**
	 * Short form for addMetadata with default of replacing previous. values for
	 * md_tag
	 * 
	 * @param mdStandard String value
	 * @param mdTag String value
	 * @param mdValue String value
	 * @throws InvalidArrayTypeException invalid type
	 */
	public void addMetadata(final String mdStandard, final String mdTag,
			final String mdValue) throws InvalidArrayTypeException {
		addMetadata(mdStandard, mdTag, mdValue, true);
	}

	/**
	 * @param mdStandard String value
	 * @return Map
	 * @throws IOException I/O error
	 */
	@Override
	public Map<String, String> harvestMetadata(final String mdStandard)
			throws IOException {
		Map<String, String> finalMetadata = new HashMap<String, String>();
		if (!isRoot()) {
			finalMetadata.putAll(getParentGroup().harvestMetadata(mdStandard));
		}
		IGroup mdGroup = getGroupWithAttribute("metadata", mdStandard);
		if (mdGroup == null) {
			return finalMetadata;
		} else {
			for (IDataItem ditem : mdGroup.getDataItemList()) {
				IDataItem mdItem = ditem;
				finalMetadata.put(mdItem.getShortName(), mdItem.getData()
						.toString());
			}
		}
		return finalMetadata;
	}

	@Override
	public void addOneAttribute(final IAttribute attribute) {
		if (attribute instanceof NcAttribute) {
			addAttribute((NcAttribute) attribute);
		}
	}

	@Override
	public void addOneDimension(final IDimension d) {
		if (d instanceof NcDimension) {
			addDimension((NcDimension) d);
		}
	}

	@Override
	public void addSubgroup(final IGroup g) {
		addGroup(g);
	}

	// /**
	// * Create the result group content with unlimited number of
	// * CachedVariables. The first
	// * variable in the argument list is the signal argument.
	// * @param signal
	// * @param axes
	// */
	// public void buildResultGroup(IDataItem signal, IDataItem ...axes ){
	// NcDataItem signalVariable = null;
	// if (signal instanceof NcDataItem) signalVariable = (NcDataItem) signal;
	// NcAttribute attribute = new NcAttribute("signal", "1");
	// signalVariable.addAttribute(attribute);
	// addVariable(signalVariable);
	// String axesString = "";
	// for (IDataItem item : axes){
	// NcDataItem variable = (NcDataItem) item;
	// if (axesString.length() > 0) axesString += ":";
	// axesString += variable.getShortName();
	// addVariable(variable);
	// }
	// attribute = new NcAttribute("axes", axesString);
	// signalVariable.addAttribute(attribute);
	// attribute = new NcAttribute("signal", signalVariable.getShortName());
	// addAttribute(attribute);
	// }

	// private void removeDuplicatedVariable(final NcDataItem variable){
	// for (Iterator<?> iter = variables.iterator(); iter.hasNext();){
	// NcDataItem variableItem = (NcDataItem) iter.next();
	// if (variableItem.getShortName().equals(variable.getShortName()))
	// variables.remove(variableItem);
	// }
	// }

	/**
	 * Remove a group with a given name under a parent group.
	 * 
	 * @param g
	 *            parent group
	 * @param s
	 *            String value
	 */
	private void removeDuplicatedGroup(final IGroup g, final String s) {
		List<IGroup> al = g.getGroupList();
		for (int i = 0; i < al.size(); i++) {
			NcGroup group = (NcGroup) al.get(i);
			String temp = group.getShortName();
			boolean b = checkUp(temp, s);
			if (b) {
				g.removeGroup(g.getGroup(temp));
				break;
			}
		}
	}

	/**
	 * Check if two String are equal.
	 * 
	 * @param s1
	 *            String value
	 * @param s2
	 *            String value
	 * @return true or false
	 */
	private boolean checkUp(final String s1, final String s2) {
		return (s1.equals(s2));
	}

	@Override
	public NcAttribute getAttribute(final String name) {
		return (NcAttribute) super.findAttribute(name);
	}

	@Override
	public NcDataItem getDataItem(final String shortName) {
		return (NcDataItem) findVariable(shortName);
	}

	@Override
	public NcDimension getDimension(final String name) {
		return (NcDimension) super.findDimension(name);
	}

	@Override
	public NcGroup getGroup(final String shortName) {
		Group group = super.findGroup(shortName);
		if (group instanceof NcGroup) {
			return (NcGroup) group;
		}
		return null;
	}

	@Override
	public IContainer getContainer(final String shortName) {
		if (shortName != null && shortName.equals("")) {
			return this;
		}
		NcGroup resultGroupItem = getGroup(shortName);
		if (resultGroupItem != null) {
			return resultGroupItem;
		}
		NcDataItem resultVariableItem = getDataItem(shortName);
		if (resultVariableItem != null) {
			return resultVariableItem;
		}
		// NcAttribute resultAttributeItem = findAttribute(shortName);
		// if (resultAttributeItem != null) return resultAttributeItem;
		return null;
	}

	// public NetcdfFile getFile(){
	// return ncfile;
	// }

	/**
	 * Find the main signal that referred in the "signal" attribute of the
	 * GroupData.
	 * 
	 * @return the signal as a CachedVariable instance
	 */
	public NcDataItem findSignal() {
		String signalName = null;
		NcAttribute signalAttribute = this.getAttribute("signal");
		NcDataItem signal = null;
		if (signalAttribute != null) {
			signalName = signalAttribute.getStringValue();
			try {
				signal = this.getDataItem(signalName);
			} catch (Exception e) {
				signal = null;
			}
		}
		if (signal == null) {
			String signalValue = null;
			for (Object item : getVariables()) {
				NcDataItem variable = (NcDataItem) item;
				signalAttribute = variable.getAttribute("signal");
				if (signalAttribute != null) {
					signalValue = signalAttribute.getStringValue();
					if (signalValue.equals("1")) {
						signal = variable;
					}
				}
			}
		}
		if (signal == null) {
			try {
				signal = getGroup("data").findSignal();
			} catch (Exception e) {
				return null;
			}
		}
		return signal;
	}

	/**
	 * Find the axes information if the group is an signal group, which contains
	 * main signal variable and axes variables. Return null if no axes are
	 * found.
	 * 
	 * @return a list of variables that has axes data
	 */
	public List<IDataItem> findAxes() {
		NcDataItem signal = findSignal();
		if (signal == null) {
			return null;
		}
		IAttribute axesAttribute = signal.getAttribute("axes");
		if (axesAttribute == null) {
			return null;
		}
		String[] axesNames = axesAttribute.getStringValue().split(":");
		List<IDataItem> axes = new ArrayList<IDataItem>();
		for (int i = 0; i < axesNames.length; i++) {
			NcDataItem axis = (NcDataItem) findVariable(axesNames[i]);
			if (axis != null) {
				axes.add(axis);
			}
		}
		return axes;
	}

	@Override
	public NcDataItem findDataItem(final String vName) {
		IContainer resultVariable = findContainer(vName);
		if (resultVariable instanceof NcDataItem) {
			return (NcDataItem) resultVariable;
		}
		return null;
	}

	@Override
	public NcDataset getDataset() {
		return dataset;
	}

	/**
	 * @return IDictionary object
	 * @see org.gumtree.data.interfaces.IGroup#findDictionary()
	 */
	@Override
	public IDictionary findDictionary() {
		if (isRoot()) {
			return dictionary;
		} else {
			return getRootGroup().findDictionary();
		}
	}

	/**
	 * Find all entries as a list.
	 * 
	 * @return List of IGroup objects
	 */
	public List<IGroup> getEntries() {
		if (isRoot()) {
			List<IGroup> entryList = new ArrayList<IGroup>();
			List<IGroup> subGroups = getGroupList();
			for (IGroup group : subGroups) {
				// NcGroup entry = (NcGroup) iter.next();
				// NcAttribute nxAttribute = entry.getAttribute("NX_class");
				// if (nxAttribute != null) {
				// if (nxAttribute.getStringValue().equals("NXentry")) {
				// entryList.add(entry);
				// }
				// }
				if (group.isEntry()) {
					entryList.add(group);
				}
			}
			return entryList;
		}
		return getRootGroup().getEntries();
	}

	/**
	 * Find the first entry of the root.
	 * 
	 * @return GDM Group object
	 */
	public NcGroup getFirstEntryAccess() {
		if (isRoot()) {
			List<IGroup> entries = getEntries();
			if (entries.size() > 0) {
				return (NcGroup) entries.get(0);
			} else {
				return null;
			}
		}
		if (isEntry()) {
			return this;
		} else {
			return getParentGroup().getFirstEntryAccess();
		}
	}

	@Override
	public NcGroup findGroup(final String groupName) {
		IContainer resultGroup = findContainer(groupName);
		if (resultGroup instanceof NcGroup) {
			return (NcGroup) resultGroup;
		}
		return null;
	}

	@Override
	public String getLocation() {
		return getDataset().getLocation();
	}

	/**
	 * Get the log of the GDM group.
	 * 
	 * @return String value
	 */
	public String getLog() {
		NcAttribute historyLog = this.getAttribute("log");
		if (historyLog != null) {
			return historyLog.getStringValue();
		}
		return null;
	}

	/**
	 * Find the IObject reference by the key.
	 * 
	 * @param shortName
	 *            String value
	 * @return IObject instance
	 * @see org.gumtree.data.interfaces.IGroup#findObject(java.lang.String)
	 */
	@Override
	public IContainer findContainer(String shortName) {
		IDictionary dictionary = findDictionary();
		if (dictionary != null) {
			IKey key = Factory.getFactory(getFactoryName()).createKey(shortName);
			List<IPath> pathList = dictionary.getAllPaths(key);
			// [ANSTO][TONY][2011-05-04] TODO: Avoid getAllKeys returns null, and use empty list instead
			if (pathList != null) {
				for (IPath path : pathList) {
					if (path != null && path.getValue().trim().length() > 0) {
						IContainer object = findContainerByPath(path.getValue());
						if (object != null) {
							return object;
						}
					}
				}
			}
		}
		return getContainer(shortName);
	}

	@Override
	public IContainer findContainerByPath(String path) {
		String[] pathItems = parsePath(path);
		NcGroup branchGroup = null;
		if (isRootItem(path)) {
			branchGroup = getRootGroup();
		} else {
			branchGroup = this;
		}
		for (int i = 0; i < pathItems.length - 1; i++) {
			String item = pathItems[i];
			if (item.equals(Constants.ENTRY_LABEL)) {
				branchGroup = findCurrentEntry();
			} else {
				branchGroup = branchGroup.getGroup(item);
			}
			if (branchGroup == null) {
				return null;
			}
		}
		String leafPathItem = pathItems[pathItems.length - 1];
		return branchGroup.getContainer(leafPathItem);
	}

	/**
	 * Find the current entry.
	 * 
	 * @return GDM Group object
	 */
	public NcGroup findCurrentEntry() {
		if (isEntry()) {
			return this;
		}
		if (isRoot()) {
			return getFirstEntryAccess();
		}
		return getParentGroup().findCurrentEntry();
	}

	@Override
	public NcGroup getParentGroup() {
		if (parent == null) {
			return null;
		}
		if (parent instanceof NcGroup) {
			return (NcGroup) parent;
		} else {
			return null;
		}
	}

	@Override
	public NcGroup getRootGroup() {
		if (isRoot()) {
			return this;
		}
		if (getParentGroup() == null) {
			return this;
		} else {
			return getParentGroup().getRootGroup();
		}
	}

	// public void initialiseDictionary(String dicFile) throws
	// FileAccessException{
	// if (dicFile == null)
	// return;
	// try{
	// BufferedReader br = new BufferedReader(new FileReader(
	// new File(dicFile)));
	// if (br==null)
	// throw new FileAccessException(
	// "Dictionary file cannot be open!");
	// while(br.ready()){
	// String[] temp = br.readLine().split("=");
	// if (0<(temp[0].length())) {
	// dictionary.put(temp[0], temp[1]);
	// }
	// }
	// }
	// catch (Exception ex){
	// throw new FileAccessException(ex.getMessage());
	// }
	// }

	/**
	 * Add this group as a child group to the parent.
	 * 
	 * @param p
	 *            parent Group object
	 * @param s
	 *            String value
	 */
	private void insertGroup(final IGroup p, final String s) {
		removeDuplicatedGroup(p, s);
		p.addSubgroup(this);
		// updateDictionary(shortName, p.getName());
	}

	// private void insertVariable(final NcDataItem variable){
	// removeDuplicatedVariable(variable);
	// variables.add(variable);
	// // updateDictionary(variable.getShortName(), variable.getName());
	// }

	@Override
	public boolean isEntry() {
		// NcAttribute nxAttribute = getAttribute("NX_class");
		// if (nxAttribute != null) {
		// if (nxAttribute.getStringValue().equals("NXentry"))
		// return true;
		// }
		// return false;
		if (isRoot()) {
			return false;
		}
		return parent.isRoot();
	}

	/**
	 * Check if the group at the given xpath is an entry group.
	 * 
	 * @param path
	 *            xpath String value
	 * @return true or false
	 */
	protected boolean isEntryItem(final String path) {
		if (path.startsWith("$")) {
			return true;
		}
		return false;
	}

	/**
	 * Check if a xpath originates from root.
	 * 
	 * @param path
	 *            String value
	 * @return true or false
	 */
	protected boolean isRootItem(final String path) {
		if (path.startsWith("/")) {
			return true;
		}
		return false;
	}

	/**
	 * Parse the path into array of strings.
	 * 
	 * @param path
	 *            xpath
	 * @return array of String
	 */
	protected String[] parsePath(final String path) {
		String pathItem = null;
		if (isRootItem(path)) {
			pathItem = path.substring(1, path.length());
			// else if(isEntryItem(path))
			// pathItem = path.split("/", 2)[1];
		} else {
			pathItem = path;
		}
		return pathItem.split("/");
	}

	@Override
	public boolean removeAttribute(final IAttribute attribute) {
		if (attribute instanceof NcAttribute) {
			return remove((NcAttribute) attribute);
		}
		return false;
	}

	@Override
	public boolean removeDataItem(final IDataItem item) {
		if (item instanceof NcDataItem) {
			return remove((NcDataItem) item);
		}
		return false;
	}

	/**
	 * remove a Variable using its (short) name, in this group only.
	 * 
	 * @param varName
	 *            Variable name.
	 * @return true if Variable found and removed
	 */
	@Override
	public boolean removeDataItem(final String varName) {
		return removeVariable(varName);
	}

	@Override
	public boolean removeGroup(final IGroup group) {
		if (group instanceof NcGroup) {
			return remove((NcGroup) group);
		}
		return false;
	}

	@Override
	public boolean removeGroup(final String shortName) {
		return remove(getGroup(shortName));
	}

	@Override
	public boolean removeDimension(final IDimension dimension) {
		if (dimension instanceof NcDimension) {
			remove((NcDimension) dimension);
		}
		return false;
	}

	@Override
	public void setShortName(final String shortName) {
		this.shortName = shortName;
	}

	// private void updateDictionary(String fullname, String shortname){
	// NcGroup temp = this;
	// while(!(temp.isRoot()))
	// {
	// NcGroup gc = temp.getParentGroup();
	// temp.setParent(gc);
	// temp = gc;
	// }
	// temp.getDictionary().put(shortname, fullname);
	// }

	@Override
	public String toString() {
		String result = "<Group>" + getShortName() + "\n";
		List<?> attributeList = getAttributes();
		for (Iterator<?> iterator = attributeList.iterator(); iterator
				.hasNext();) {
			NcAttribute attribute = (NcAttribute) iterator.next();
			result += attribute.toString() + "\n";
		}
		result += "</Group>\n";
		return result;
	}

	/**
	 * Save the Group in the target Dataset location. This method is not in the
	 * IGroup interface, which means it is not called
	 * 
	 * @throws WriterException
	 *             failed to write
	 */
	public void save() throws WriterException {
		String location = getLocation();
		if (location == null || location.trim().length() == 0) {
			throw new WriterException("failed to write to null file");
		}
		NcHdfWriter hdfWriter = new NcHdfWriter(new File(getLocation()));
		hdfWriter.open();
		if (isRoot()) {
			hdfWriter.writeToRoot(this, true);
		} else {
			hdfWriter.writeGroup(getParentGroup().getName(), this, true);
		}
	}

	/**
	 * Find the axes as list of Arrays.
	 * 
	 * @return List of Array
	 * @throws SignalNotAvailableException
	 *             no valid signal
	 */
	public List<IArray> getAxesArrayList() throws SignalNotAvailableException {
		List<IArray> arrayList = new ArrayList<IArray>();
		List<IDataItem> axesDataItems = findAxes();
		if (axesDataItems.size() > 0) {
			for (Iterator<?> iterator = axesDataItems.iterator(); iterator
					.hasNext();) {
				try {
					IArray axesArray = ((IDataItem) iterator.next()).getData();
					arrayList.add(axesArray);
				} catch (IOException e) {
					throw new SignalNotAvailableException(
							"axes array is not reachable " + e.getMessage());
				}
			}
		}
		return arrayList;
	}

	/**
	 * Find the signal of as Array object.
	 * 
	 * @return IArray object
	 * @throws SignalNotAvailableException
	 *             no valid signal
	 */
	public IArray getSignalArray() throws SignalNotAvailableException {
		IArray signalArray = null;
		IDataItem signal = findSignal();
		if (signal != null) {
			try {
				signalArray = signal.getData();
			} catch (IOException e) {
				throw new SignalNotAvailableException(
						"the signal array is not reachable " + e.getMessage());
			}
		}
		return signalArray;
	}

	@Override
	public void addStringAttribute(final String name, final String value) {
		NcAttribute attribute = new NcAttribute(name, value);
		addAttribute(attribute);
	}

	@Override
	public NcDataItem getDataItemWithAttribute(final String attributeName,
			final String value) {
		NcAttribute attribute;
		for (Object item : getVariables()) {
			NcDataItem variable = (NcDataItem) item;
			attribute = variable.getAttribute(attributeName);
			if (attribute != null) {
				String attributeValue = attribute.getStringValue();
				if (attributeValue.equals(value)) {
					return variable;
				}
			}
		}
		return null;
	}

	@Override
	public NcGroup getGroupWithAttribute(final String attributeName,
			final String value) {
		NcAttribute attribute;
		for (Object item : getGroups()) {
			NcGroup group = (NcGroup) item;
			attribute = group.getAttribute(attributeName);
			if (attribute != null) {
				String attributeValue = attribute.getStringValue();
				if (attributeValue.equals(value)) {
					return group;
				}
			}
		}
		return null;
	}

	/**
	 * Enhance the Netcdf group, which in turn enhance every element of the
	 * group.
	 * 
	 * @param parent
	 *            Netcdf Group object
	 * @return enhanced Netcdf Group object
	 */
	protected Group enhance(final Group parent) {
		ucar.nc2.Group enhancedGroup = new ucar.nc2.Group(this.ncfile, parent,
				getShortName());
		for (ucar.nc2.Group group : getGroups()) {
			enhancedGroup.addGroup(((NcGroup) group).enhance(enhancedGroup));
		}
		for (ucar.nc2.Attribute attribute : getAttributes()) {
			enhancedGroup.addAttribute(attribute);
		}
		for (ucar.nc2.Dimension dimension : getDimensions()) {
			enhancedGroup.addDimension(dimension);
		}
		for (ucar.nc2.Variable variable : getVariables()) {
			enhancedGroup.addVariable(((NcDataItem) variable)
					.enhance(enhancedGroup));
		}
		return enhancedGroup;
	}

	@Override
	public boolean hasAttribute(final String name, final String value) {
		IAttribute attribute;
		try {
			attribute = getAttribute(name);
			if (attribute.getStringValue().equals(value)) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	@Override
	public IGroup clone() {
		return new NcGroup(this);
	}

	/**
	 * Set the location of the parent dataset.
	 * 
	 * @param location
	 *            String value
	 * @deprecated use {@link 
	 * 		org.gumtree.data.interfaces.IDataset#setLocation(java.lang.String)}
	 */
	@Deprecated
	public void setLocation(final String location) {
		getDataset().setLocation(location);
	}

	@Override
	public void updateDataItem(String key, IDataItem dataItem)
			throws SignalNotAvailableException {
		IGroup parentGroup = null;
		try {
			String fullPath = findDictionary().getPath(Factory.getFactory(getFactoryName()).createKey(key)).getValue();
			String nodeName;
			int delimiter = fullPath.lastIndexOf("/");
			if (fullPath.length() == delimiter) {
				// if '/' was last character in string, use
				// supplied key as new dictionary value
				nodeName = key;
			} else { // use dictionary value
				nodeName = fullPath.substring(delimiter + 1);
			}
			dataItem.setName(nodeName);

			String parentPath = fullPath.substring(0, delimiter);
			if (0 < parentPath.trim().length()) {
				parentGroup = (IGroup) findContainerByPath(parentPath);
			}
			if (null == parentGroup) {
				parentGroup = getRootGroup();
			}

			// If dataItem with this name already exists in this Group, replace
			// it.
			parentGroup.removeDataItem(nodeName);
			parentGroup.addDataItem(dataItem);
			dataItem.setParent(parentGroup);
		} catch (Exception e) {
			throw new SignalNotAvailableException(
					"Group.updateDataItem: parent group does not exist");
		}
	}

	/**
	 * Close the parent dataset. The storage becomes unavailable.
	 * 
	 * @throws FileAccessException
	 *             failed to close
	 */
	public void close() throws FileAccessException {
		if (!isRoot()) {
			throw new FileAccessException("failed to close - not a root group");
		}
		if (dataset != null) {
			try {
				dataset.close();
			} catch (IOException e) {
				throw new FileAccessException(e);
			}
		}
	}

	@Override
	public List<IAttribute> getAttributeList() {
		if (getAttributes() == null) {
			return null;
		}
		List<IAttribute> attributeList = new ArrayList<IAttribute>();
		for (Attribute attribute : getAttributes()) {
			attributeList.add((NcAttribute) attribute);
		}
		return attributeList;
	}

	@Override
	public List<IDataItem> getDataItemList() {
		if (getVariables() == null) {
			return null;
		}
		List<IDataItem> dataItemList = new ArrayList<IDataItem>();
		for (Variable variable : getVariables()) {
			dataItemList.add((NcDataItem) variable);
		}
		return dataItemList;
	}

	@Override
	public List<IDimension> getDimensionList() {
		if (getDimensions() == null) {
			return null;
		}
		List<IDimension> dimensionList = new ArrayList<IDimension>();
		for (Dimension dimension : getDimensions()) {
			dimensionList.add((NcDimension) dimension);
		}
		return dimensionList;
	}

	@Override
	public List<IGroup> getGroupList() {
		if (getGroups() == null) {
			return null;
		}
		List<IGroup> groupList = new ArrayList<IGroup>();
		for (Group group : getGroups()) {
			groupList.add((NcGroup) group);
		}
		return groupList;
	}

	@Override
	public void setParent(final IGroup group) {
		super.setParentGroup((NcGroup) group);
	}

	@Override
	public List<IContainer> findAllContainers(IKey key) {
		IDictionary dictionary = findDictionary();
		List<IContainer> objectList = new ArrayList<IContainer>();
		if (dictionary != null) {
			List<IPath> pathList = dictionary.getAllPaths(key);
			if (pathList != null) {
				for (IPath path : pathList) {
					IContainer object = findContainerByPath(path.getValue());
					if (object != null) {
						objectList.add(object);
					}
				}
				return objectList;
			}
		}
		IContainer object = getContainer(key.getName());
		if (object != null) {
			objectList.add(object);
		}
		return objectList;
	}

	@Override
	public List<IContainer> findAllOccurrences(IKey key) {
		List<IContainer> objectList = new ArrayList<IContainer>();
		List<IGroup> entryList = getEntries();
		for (IGroup entry : entryList) {
			objectList.add(entry.findContainer(key.getName()));
		}
		return objectList;
	}

	@Override
	public boolean isRoot() {
		return super.isRoot() || getParentGroup() == null || getShortName().length() == 0;
	}
	
	// TODO [SOLEIL][clement]
	// Bellow methods are added to fit new mechanism of dictionary based 
	// on filtering nodes according keys' filters.
	// TODO END
	@Override
	public IDataItem findDataItem(IKey key) {
        IDataItem item = null;
        List<IContainer> list = new ArrayList<IContainer>();
        list = getObjectByKey(key);
        
        for( IContainer object : list ) {
        	if( object.getModelType().equals(ModelType.DataItem) ) {
        		item = (IDataItem) object;
        		break;
        	}
        }
        return item;
	}

	@Override
	public IDataItem findDataItemWithAttribute(IKey key, String name, String attribute) throws NoResultException {
//        IKeyFilter filter;
//        filter = new NcKeyFilter(FilterLabel.ATTRIBUTE_NAME, name);
//        key.pushFilter(filter);
//        filter = new NcKeyFilter(FilterLabel.ATTRIBUTE_VALUE, attribute);
		// [ANSTO][Tony][2011-05-04] TODO: implement the logic to filter attribute
        return findDataItem(key);
	}

	@Override
	public IGroup findGroupWithAttribute(IKey key, String name, String value) {
        List<IGroup> groups = getGroupList();
        IAttribute attr;
        for( IGroup group : groups ) {
            attr = group.getAttribute(name);
            if( attr.getStringValue().equals(value) )
                return group;
        }
        
        return null;
	}

	@Override
	public IGroup findGroup(IKey key) {
		IGroup group = null;
        List<IContainer> list = new ArrayList<IContainer>();
        list = getObjectByKey(key);

        for( IContainer object : list ) {
        	if( object.getModelType().equals(ModelType.Group) ) {
        		group = (IGroup) object;
        		break;
        	}
        }
        
        return group;
	}
	
	
	/**
	 * Find the object targeted by the key and its filters using dictionary.
	 * 
	 * @param key (IKey) eventually carrying filters (IKeyFilter)  
	 * 
	 * @return list of IObject that matches key's criterias
	 */
    private List<IContainer> getObjectByKey(IKey key) {
    	// [SOLEIL][clement] must complete this method to have a filtered dictionary mechanism
    	// TODO IMPORTANT: main method to get an item according a key containing filters.
    	// To apply them according to the underlying data format, I used another private method (applyKeyFilter)
    	// return null;
		IDictionary dictionary = findDictionary();
		List<IContainer> objectList = new ArrayList<IContainer>();
		if (dictionary != null) {
			List<IPath> pathList = dictionary.getAllPaths(key);
			if (pathList != null) {
				for (IPath path : pathList) {
					IContainer object = findObjectByPath(path);
					if (object != null) {
						objectList.add(object);
					}
				}
				return objectList;
			}
		}
		IContainer object = getContainer(key.getName());
		if (object != null) {
			objectList.add(object);
		}
		return objectList;
    }
    
	public IContainer findObjectByPath(final IPath path) {
		String[] pathItems = parsePath(path.getValue());
		NcGroup branchGroup = null;
		if (isRootItem(path.getValue())) {
			branchGroup = getRootGroup();
		} else {
			branchGroup = this;
		}
		for (int i = 0; i < pathItems.length - 1; i++) {
			String item = pathItems[i];
			if (item.equals(Constants.ENTRY_LABEL)) {
				branchGroup = findCurrentEntry();
			} else {
				branchGroup = branchGroup.getGroup(item);
			}
			if (branchGroup == null) {
				return null;
			}
		}
		String leafPathItem = pathItems[pathItems.length - 1];
		return branchGroup.getContainer(leafPathItem);
	}
	
    /*************************************************************************
     * [ANSTO][Tony] The following methods are transitional and are intended
     * to be removed after the GumTree migration to the new CDM API.
     *************************************************************************/
    
	/**
	 * Create the result group content with unlimited number of CachedVariables. The first
	 * variable in the argument list is the signal argument. 
	 * @param signal
	 * @param axes
	 * @deprecated
	 */
	public void buildResultGroup(IDataItem signal, IDataItem ...axes ){
		NcDataItem signalVariable = null;
		if (signal instanceof NcDataItem) signalVariable = (NcDataItem) signal;
		NcAttribute attribute = new NcAttribute("signal", "1");
		signalVariable.addAttribute(attribute);
		addVariable(signalVariable);
		String axesString = "";
		for (IDataItem item : axes){
			NcDataItem variable = (NcDataItem) item;
			if (axesString.length() > 0) axesString += ":";
			axesString += variable.getShortName();
			addVariable(variable);
		}
		attribute = new NcAttribute("axes", axesString);
		signalVariable.addAttribute(attribute);
		attribute = new NcAttribute("signal", signalVariable.getShortName());
		addAttribute(attribute);
	}
	
	public void addLog(String newLog){
		String parentLog = ((NcGroup) getParentGroup()).getLog();
		if (parentLog != null) newLog = parentLog + "\n" + newLog;
		else newLog = "Source data is from file\n" + newLog;
		//		Attribute logAttribute = new Attribute("log", newLog);
		NcAttribute logAttribute = new NcAttribute("log", newLog);
		addAttribute(logAttribute);
	}
	
	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}

	@Override
	public List<IContainer> findAllContainerByPath(String path) {
		List<IContainer> objectList = new ArrayList<IContainer>();
		List<IGroup> entryList = getEntries();
		for (IGroup entry : entryList) {
			try {
				objectList.add(entry.findContainerByPath(path));
			} catch (Exception e) {
			}
		}
		return objectList;
	}

	
}
