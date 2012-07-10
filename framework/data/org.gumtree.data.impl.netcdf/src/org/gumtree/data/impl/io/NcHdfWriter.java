/*******************************************************************************
 * Copyright (c) 2010 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *    Norman Xiong (nxi@Bragg Institute) - initial API and implementation
 ******************************************************************************/
package org.gumtree.data.impl.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

import org.gumtree.data.exception.ItemExistException;
import org.gumtree.data.exception.WriterException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.io.IWriter;
import org.slf4j.LoggerFactory;

/**
 * An HDF implementation of IWriter. Export GDM model objects into HDF files.
 * @author nxi
 * @see org.gumtree.data.io.IWriter
 */
public class NcHdfWriter implements IWriter {

	/**
	 * The ZIP level. 1 means lowest, 9 means highest.
	 */
	protected static final int ZIP_LEVEL = 9;
	/**
	 * The minimum size of an array to be zipped.
	 */
	protected static final int ZIP_MINIMUM_SIZE = 10;
	/**
	 * The native data size for the HDF data type.
	 */
	private static final int NATIVE_DATA_SIZE = 4;
	/**
	 * File instance of the storage.
	 */
	private File file;
	/**
	 * NCSA file handler.
	 */
	private FileFormat fileHandler;
	/**
	 * Is open or not.
	 */
	private boolean isOpen = false;

	/**
	 * Default constructor.
	 */
	public NcHdfWriter() {
	}

	/**
	 * Constructor from a java file instance.
	 * 
	 * @param file
	 *            java File instance
	 */
	public NcHdfWriter(final File file) {
		this.file = file;
	}

	public void open() throws WriterException {
		if (file == null) {
			throw new WriterException("can not open the file : null");
		}
		// try {
		// System.setProperty(H5.H5PATH_PROPERTY_KEY, "ncsa.hdf/jhdf5.dll");
		// H5.loadH5Lib();
		// Class<?> fileclass = Class.forName("ncsa.hdf.object.h5.H5File");
		// FileFormat fileformat = (FileFormat)fileclass.newInstance();
		// if (fileformat != null) {
		// FileFormat.addFileFormat(FileFormat.FILE_TYPE_HDF5,
		// fileformat);
		// }
		// } catch (Throwable err ) {err.printStackTrace();}
		try {
			if (fileHandler == null) {
				FileFormat fileFormat = FileFormat
						.getFileFormat(FileFormat.FILE_TYPE_HDF5);
				if (!file.exists()) {
					fileHandler = fileFormat.createFile(file.getAbsolutePath(),
							FileFormat.WRITE);
					// fileHandler = fileFormat.create(file.getAbsolutePath());
					fileHandler.open();
					// fileHandler.close();
					Object object = fileHandler.getRootNode();
//					System.out.println(object);
				} else {
					fileHandler = fileFormat.createInstance(file
							.getAbsolutePath(), FileFormat.WRITE);
					// fileHandler = fileFormat.open(file.getAbsolutePath(),
					// FileFormat.WRITE);
					fileHandler.open();
				}
			} else {
				fileHandler.open();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (file.exists()) {
				throw new WriterException("can not open the file for "
						+ "writing, please check if it is locked");
			} else {
				throw new WriterException("can not create the file");
			}
		}
		isOpen = true;
	}

	public void close() {
		if (isOpen()) {
			try {
				fileHandler.close();
			} catch (Exception e) {
				LoggerFactory.getLogger(this.getClass()).error(e.getMessage());
			}
			isOpen = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.data.io.IWriter#isOpen()
	 */
	public boolean isOpen() {
		return fileHandler != null && isOpen;
	}

	// protected void checkOpenStatus() throws HdfException{
	// if (!isOpen)
	// throw new HdfException("file is not open. please open the
	// file before writing");
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.gumtree.data.io.IWriter#writeToRoot(org.gumtree.data.IGroup)
	 */
	public void writeToRoot(final IGroup group) throws WriterException {
		if (group.isRoot()) {
			for (IAttribute attribute : group.getAttributeList()) {
				writeToRoot(attribute, true);
			}
			for (IDataItem dataItem : group.getDataItemList()) {
				writeToRoot(dataItem, true);
			}
			for (IGroup subGroup : group.getGroupList()) {
				writeToRoot(subGroup, true);
			}
		} else {
			writeGroup("/", group, true);
		}
	}

	public void writeToRoot(final IGroup group, final boolean force)
			throws WriterException {
		if (group.isRoot()) {
			for (IAttribute attribute : group.getAttributeList()) {
				writeToRoot(attribute, force);
			}
			for (IDataItem dataItem : group.getDataItemList()) {
				writeToRoot(dataItem, force);
			}
			for (IGroup subGroup : group.getGroupList()) {
				writeToRoot(subGroup, force);
			}
		} else {
			writeGroup("/", group, force);
		}
	}

	public void writeToRoot(final IDataItem dataItem) 
	throws WriterException {
		writeDataItem("/", dataItem);
	}

	public void writeToRoot(final IDataItem dataItem, final boolean force)
			throws WriterException {
		writeDataItem("/", dataItem, force);
	}

	public void writeToRoot(final IAttribute attribute)
			throws WriterException {
		writeAttribute("/", attribute);
	}

	public void writeToRoot(final IAttribute attribute, final boolean force)
			throws WriterException {
		writeAttribute("/", attribute, force);
	}

	public void writeGroup(final String parentPath, final IGroup group)
			throws WriterException {
		writeGroup(parentPath, group, false);
	}

	public void writeGroup(final String parentPath, final IGroup group,
			final boolean force) throws WriterException {
		Group parentGroup = findHdfGroup(parentPath);
		if (parentGroup == null) {
			throw new WriterException("the parent group does not exist: "
					+ parentPath);
		}
		writeGroup(parentGroup, group, force);
	}

	/**
	 * Write a group under a parent HDF group.
	 * 
	 * @param parent
	 *            HDF Group
	 * @param group
	 *            GDM Group
	 * @param force
	 *            if to overwrite
	 * @throws WriterException
	 *             failed to write
	 */
	protected void writeGroup(final Group parent, final IGroup group,
			final boolean force) throws WriterException {
		if (group == null) {
			return;
		}
		Group newGroup = null;
		newGroup = findHdfGroup(parent, group.getShortName());
		if (newGroup == null) {
			try {
				newGroup = fileHandler
						.createGroup(group.getShortName(), parent);
			} catch (Exception e) {
				throw new WriterException("Can not create empty group", e);
			}
		}
		for (IAttribute attribute : group.getAttributeList()) {
			writeAttribute(newGroup, attribute, force);
		}
		for (IDataItem dataItem : group.getDataItemList()) {
			writeDataItem(newGroup, dataItem, force);
		}
		for (IGroup subGroup : group.getGroupList()) {
			writeGroup(newGroup, subGroup, force);
		}
	}

	public void writeDataItem(final String parentPath, final IDataItem dataItem)
			throws WriterException {
		writeDataItem(parentPath, dataItem, false);
	}

	public void writeDataItem(final String parentPath,
			final IDataItem dataItem, final boolean force)
			throws WriterException {
		Group parent = findHdfGroup(parentPath);
		if (parent == null) {
			throw new WriterException("the parent group does not exist: "
					+ parentPath);
		}
		writeDataItem(parent, dataItem, force);
	}

	/**
	 * @param parent
	 *            GDM group
	 * @param dataItem
	 *            GDM DataItem object
	 * @param force
	 *            if overwrite
	 * @throws WriterException
	 *             failed to write
	 */
	protected void writeDataItem(final Group parent, final IDataItem dataItem,
			final boolean force) throws WriterException {
		if (parent == null) {
			return;
		}
		if (isDataItemExist(parent, dataItem.getShortName())) {
			if (force) {
				removeDataItem(parent, dataItem.getShortName());
			} else {
				throw new ItemExistException("the data item with the same "
						+ "name already exists, can not overwrite");
			}
		}
		Dataset newDataset = writeHdfDataset(parent, dataItem);
		for (IAttribute attribute : dataItem.getAttributeList()) {
			writeAttribute(newDataset, attribute, force);
		}
	}

	/**
	 * @param parent
	 *            GDM Group object
	 * @param dataItem
	 *            GDM data item
	 * @return NCSA dataset item
	 * @throws WriterException
	 *             failed to write
	 */
	protected Dataset writeHdfDataset(final Group parent,
			final IDataItem dataItem) throws WriterException {
		int[] dimension = dataItem.getShape();
		Datatype dataType = null;
		try {
			dataType = getDataType(dataItem.getData());
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new WriterException("failed to read data from data item: "
					+ dataItem.getShortName(), e1);
		}
		Object data = null;
		try {
			data = getData(dataItem.getData());
		} catch (IOException e) {
			throw new WriterException("failed to read data from data item: "
					+ dataItem.getShortName(), e);
		}
		return writerHdfDataset(parent, dimension, dataType, dataItem
				.getShortName(), data);
	}

	/**
	 * Get the data from an GDM array.
	 * 
	 * @param array
	 *            GDM Array
	 * @return generic object
	 */
	private Object getData(final IArray array) {
		Class<?> type = array.getElementType();
		if (type.equals(float.class) || type.equals(int.class)
				|| type.equals(boolean.class) || type.equals(byte.class)) {
			return array.getStorage();
		} else if (type.equals(double.class)) {
			double[] storage = (double[]) array.getStorage();
			float[] floatData = new float[storage.length];
			for (int i = 0; i < floatData.length; i++) {
				floatData[i] = (float) storage[i];
			}
			return floatData;
		} else if (type.equals(String.class) || type.equals(char.class)) {
			return new String[] { array.toString() };
		} else {
			return array.toString();
		}
	}

	/**
	 * Create a data type of the Array. The type will be generated according to
	 * the GDM data type of the array.
	 * 
	 * @param array
	 *            GDM Array
	 * @return HDF DataType
	 * @throws WriterException
	 *             failed to access
	 */
	protected Datatype getDataType(final IArray array)
			throws WriterException {
		Class<?> type = array.getElementType();
		int typeInteger;
		int dataSize;
		if (type.equals(double.class) || type.equals(float.class)) {
			typeInteger = Datatype.CLASS_FLOAT;
			dataSize = NATIVE_DATA_SIZE;
		} else if (type.equals(int.class)) {
			typeInteger = Datatype.CLASS_INTEGER;
			dataSize = NATIVE_DATA_SIZE;
		} else if (type.equals(char.class)) {
			typeInteger = Datatype.CLASS_STRING;
			dataSize = (int) array.getSize();
		} else if (type.equals(boolean.class)) {
			typeInteger = Datatype.CLASS_INTEGER;
			dataSize = NATIVE_DATA_SIZE;
		} else if (type.equals(String.class)) {
			typeInteger = Datatype.CLASS_STRING;
			String arrayString = array.toString();
			// if (arrayString.charAt(arrayString.length() - 1) == ' ')
			// dataSize = arrayString.length();
			// else
			dataSize = arrayString.length() + 1;
		} else if (type.equals(byte.class)) {
			typeInteger = Datatype.CLASS_BITFIELD;
			dataSize = NATIVE_DATA_SIZE;
		} else {
			typeInteger = Datatype.CLASS_NO_CLASS;
			dataSize = 1;
		}
		try {
			return fileHandler.createDatatype(typeInteger, dataSize,
					Datatype.NATIVE, Datatype.NATIVE);
		} catch (Exception e) {
			throw new WriterException("can not create data type "
					+ type.getName());
		}
	}

	/**
	 * Write the HDF dataset.
	 * 
	 * @param parent
	 *            HDF Group
	 * @param dimension
	 *            array of integer
	 * @param dtype
	 *            HDF Datatype
	 * @param shortname
	 *            String value
	 * @param o
	 *            java array
	 * @return HDF Dataset
	 * @throws WriterException
	 *             Failed to write
	 */
	private Dataset writerHdfDataset(final Group parent, final int[] dimension,
			final Datatype dtype, final String shortname, final Object o)
			throws WriterException {
		int zipLevel = 0;
		long size = 1;
		long[] chunks = null;
		long[] longDimension = null;
		if (dtype.getDatatypeClass() == Datatype.CLASS_STRING) {
			// int length = 1;
			// for (int i = 0; i < dimension.length; i++) {
			// length *= dimension[i];
			// }
			// longDimension = new long[]{length};
			longDimension = new long[] { 1 };
		} else {
			longDimension = new long[dimension.length];
			for (int i = 0; i < longDimension.length; i++) {
				longDimension[i] = (long) dimension[i];
			}
		}
		if (dimension.length > 1) {
			chunks = new long[dimension.length];
			for (int i = 0; i < dimension.length; i++) {
				size *= dimension[i];
				if (i == 0) {
					chunks[i] = 1;
				} else {
					chunks[i] = dimension[i];
				}
			}
		}
		if (dtype.getDatatypeClass() != Datatype.CLASS_STRING
				&& size > ZIP_MINIMUM_SIZE) {
			zipLevel = ZIP_LEVEL;
		}
		Dataset newDataset = null;
		try {
			newDataset = fileHandler.createScalarDS(shortname, parent, dtype,
					longDimension, null, chunks, zipLevel, o);
		} catch (Exception e) {
			throw new WriterException("failed to write data item "
					+ shortname, e);
		}
		return newDataset;
	}

	public void writeAttribute(final String parentPath,
			final IAttribute attribute) throws WriterException {
		writeAttribute(parentPath, attribute, false);
	}

	public void writeAttribute(final String parentPath,
			final IAttribute attribute, final boolean force)
			throws WriterException {
		HObject parent = findHdfGroup(parentPath);
		if (parent == null) {
			parent = findHdfDataset(parentPath);
		}
		if (parent == null) {
			throw new WriterException("the parent group does not exist: "
					+ parentPath);
		}
		// if (isAttributeExist(parent, attribute.getName())){
		// if (force)
		// removeAttribute(parent, attribute.getName());
		// else
		// throw new HdfItemExistException("a " + attribute.getName()
		// + " attribute already exis");
		// }
		writeAttribute(parent, attribute, force);
	}

	/**
	 * Write an Attribute to the HDF file.
	 * 
	 * @param parent
	 *            HDF Object
	 * @param attribute
	 *            GDM Attribute
	 * @param force
	 *            if to overwrite
	 * @throws WriterException
	 *             failed to write
	 */
	protected void writeAttribute(final HObject parent,
			final IAttribute attribute, final boolean force)
			throws WriterException {
		if (parent == null) {
			throw new WriterException("the parent group does not exist");
		}
		if (isAttributeExist(parent, attribute.getName())) {
			if (force) {
				removeAttribute(parent, attribute.getName());
			} else {
				throw new ItemExistException("an attribute already exis : "
						+ attribute.getName());
			}
		}
		Datatype datatype = getDataType(attribute.getValue());
		IArray array = attribute.getValue();
		int[] shape = array.getShape();
		long[] dimension = new long[shape.length];
		if (attribute.isString()) {
			dimension = new long[] { 1 };
			// dimension = new long[]{attribute.getStringValue().length()};
		} else {
			dimension = new long[] { datatype.getDatatypeSize() };
		}
		// for (int i = 0; i < dimension.length; i++) {
		// dimension[i] = shape[i];
		// }
		Object data = null;
		try {
			if (attribute.isString()) {
				data = new String[] { attribute.getStringValue() };
			} else {
				data = getData(array);
			}
		} catch (Exception e) {
			throw new WriterException("failed to read data from attribute: "
					+ attribute.getName(), e);
		}
		// Attribute newAttribute = new Attribute(attribute.getName(), datatype,
		// dimension, data);
		Attribute newAttribute = new Attribute(attribute.getName(), datatype,
				dimension);
		newAttribute.setValue(data);
		try {
			parent.writeMetadata(newAttribute);
		} catch (Exception e) {
			e.printStackTrace();
			throw new WriterException("failed to write data to "
					+ parent.getName(), e);
		}
	}

	public void writeEmptyGroup(final String xPath, final String groupName,
			final boolean force) throws WriterException {
		Group parent = findHdfGroup(xPath);
		if (parent == null) {
			throw new WriterException("the parent group does not exist: "
					+ xPath);
		}
		if (isGroupExist(parent, groupName)) {
			if (force) {
				removeGroup(parent, groupName);
			} else {
				throw new ItemExistException("the data item with the same "
						+ "name already exists, can not overwrite");
			}
		}
		try {
			fileHandler.createGroup(groupName, parent);
		} catch (Exception e) {
			throw new WriterException("Can not create empty group", e);
		}
	}

	public void removeGroup(final String groupPath) {
		Group parent = findHdfGroup(getParentPath(groupPath));
		removeGroup(parent, getShortName(groupPath));
	}

	/**
	 * Remove a HDF group from its parent group with a given name.
	 * 
	 * @param parent
	 *            HDF Group
	 * @param groupName
	 *            String value
	 */
	protected void removeGroup(final Group parent, final String groupName) {
		if (parent != null) {
			List<?> members = parent.getMemberList();
			for (Object object : members) {
				if (object instanceof Group) {
					if (((Group) object).getName().equals(groupName)) {
						parent.removeFromMemberList((Group) object);
					}
				}
			}
		}
	}

	/**
	 * Remove a data item from its parent.
	 * 
	 * @param parent
	 *            HDF group hdf library
	 * @param dataItemName
	 *            String value
	 * @throws WriterException
	 *             faild to write
	 */
	protected void removeDataItem(final Group parent, final String dataItemName)
			throws WriterException {
		if (parent != null) {
			List<?> members = parent.getMemberList();
			Dataset toRemove = null;
			for (Object object : members) {
				if (object instanceof Dataset) {
					if (((Dataset) object).getName().equals(dataItemName)) {
						toRemove = (Dataset) object;
						break;
					}
				}
			}
			if (toRemove != null) {
				try {
					fileHandler.delete(toRemove);
				} catch (Exception e) {
					throw new WriterException(
							"failed to remove data item : " + dataItemName, e);
				}
				// parent.removeFromMemberList(toRemove);
			}

		}
	}

	public void removeDataItem(final String dataItemPath)
			throws WriterException {
		Group parent = findHdfGroup(getParentPath(dataItemPath));
		removeDataItem(parent, getShortName(dataItemPath));
	}

	public void removeAttribute(final String attributePath)
			throws WriterException {
		String parentPath = getParentPath(attributePath);
		HObject parent = findHdfGroup(parentPath);
		if (parent == null) {
			parent = findHdfDataset(parentPath);
		}
		removeAttribute(parent, getShortName(attributePath));
	}

	/**
	 * Remove an Attribute from an HObject.
	 * 
	 * @param parent
	 *            HDF Hobject
	 * @param attributeName
	 *            String value
	 * @throws WriterException
	 *             failed to remove.
	 */
	protected void removeAttribute(final HObject parent,
			final String attributeName) throws WriterException {
		if (parent != null) {
			List<?> members = null;
			try {
				members = parent.getMetadata();
			} catch (Exception e) {
				throw new WriterException("failed to access metadata field "
						+ "of " + parent.getName(), e);
			}
			Attribute toRemove = null;
			for (Object object : members) {
				if (object instanceof Attribute) {
					if (((Attribute) object).getName().equals(attributeName)) {
						toRemove = (Attribute) object;
					}
				}
			}
			if (toRemove != null) {
				try {
					parent.removeMetadata(toRemove);
				} catch (Exception e) {
					throw new WriterException("failed to remove attribute "
							+ attributeName, e);
				}
			}
		}
	}

	public boolean isGroupExist(final String xPath) {
		return isGroupExist(getParentPath(xPath), getShortName(xPath));
	}

	public boolean isGroupExist(final String parentPath, 
			final String groupName) {
		return isGroupExist(findHdfGroup(parentPath), groupName);
	}

	/**
	 * Is the parent group exists.
	 * 
	 * @param parent
	 *            HDF Group
	 * @param groupName
	 *            short name for the group
	 * @return true or false
	 */
	protected boolean isGroupExist(final Group parent, final String groupName) {
		if (findHdfGroup(parent, groupName) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Find a HDF group with a given name under a parent group.
	 * 
	 * @param parent
	 *            HDF Group
	 * @param groupName
	 *            String value
	 * @return HDF Group
	 */
	protected Group findHdfGroup(final Group parent, final String groupName) {
		if (parent == null) {
			return null;
		}
		List<?> members = parent.getMemberList();
		for (Object object : members) {
			if (object instanceof Group) {
				if (((Group) object).getName().equals(groupName)) {
					return (Group) object;
				}
			}
		}
		return null;
	}

	public boolean isDataItemExist(final String xPath) {
		return isDataItemExist(getParentPath(xPath), getShortName(xPath));
	}

	public boolean isDataItemExist(final String parentPath,
			final String dataItemName) {
		return isDataItemExist(findHdfGroup(parentPath), dataItemName);
	}

	/**
	 * Check if a HDF dataset exist under a parent HDF group.
	 * 
	 * @param parent
	 *            HDF Group
	 * @param dataItemName
	 *            String value
	 * @return true or false
	 */
	protected boolean isDataItemExist(final Group parent,
			final String dataItemName) {
		if (parent == null) {
			return false;
		}
		List<?> members = parent.getMemberList();
		for (Object object : members) {
			if (object instanceof Dataset) {
				if (((Dataset) object).getName().equals(dataItemName)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAttributeExist(final String xPath) {
		return isAttributeExist(getParentPath(xPath), getShortName(xPath));
	}

	public boolean isAttributeExist(final String parentPath,
			final String attributeName) {
		Group group = findHdfGroup(parentPath);
		if (group != null) {
			return isAttributeExist(group, attributeName);
		} else {
			Dataset dataset = findHdfDataset(parentPath);
			return isAttributeExist(dataset, attributeName);
		}
	}

	/**
	 * Check if an Attribute with a given name exists under a parent HDF object.
	 * 
	 * @param parent
	 *            HDF object
	 * @param attributeName
	 *            String value
	 * @return true or false
	 */
	protected boolean isAttributeExist(final HObject parent,
			final String attributeName) {
		if (parent == null) {
			return false;
		}
		try {
			List<?> attributes = parent.getMetadata();
			for (Object object : attributes) {
				if (object instanceof Attribute) {
					if (((Attribute) object).getName().equals(attributeName)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(this.getClass()).error(
					e.getLocalizedMessage());
		}
		return false;
	}

	// protected boolean isAttributeExist(Group parent, String attributeName){
	// if (parent == null)
	// return false;
	// try {
	// List<?> attributes = parent.getMetadata();
	// for (Object object : attributes){
	// if (object instanceof Attribute){
	// if (((Attribute) object).getName().equals(attributeName))
	// return true;
	// }
	// }
	// } catch (Exception e) {
	// }
	// return false;
	// }
	//	
	/**
	 * @param xPath
	 *            String value
	 * @return new String
	 */
	public static String standardise(final String xPath) {
		String path = xPath.replace("\\", "/");
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		for (int i = path.length() - 1; i > 0; i++) {
			if (path.charAt(i) == '/') {
				path = path.substring(0, path.length() - 1);
			} else {
				break;
			}
		}
		return path;
	}

	/**
	 * Get the parent path of the given xpath. If the xpath is already a root
	 * path, return itself.
	 * 
	 * @param xPath
	 *            String value
	 * @return new String
	 */
	public static String getParentPath(final String xPath) {
		String path = standardise(xPath);
		path = path.substring(0, path.lastIndexOf("/") + 1);
		path = standardise(path);
		return path;
	}

	/**
	 * Find the short name of an object from a given xpath.
	 * 
	 * @param xPath
	 *            String value
	 * @return name in String type
	 */
	public static String getShortName(final String xPath) {
		String path = standardise(xPath);
		int lastIndex = path.lastIndexOf("/");
		if (path.length() > lastIndex + 1) {
			return path.substring(lastIndex + 1, path.length());
		} else {
			return "";
		}
	}

	/**
	 * Find a HDF group at a given xpath.
	 * 
	 * @param xPath
	 *            String value
	 * @return HDF Group
	 */
	protected Group findHdfGroup(final String xPath) {
		Group parent = (Group) ((javax.swing.tree.DefaultMutableTreeNode) 
				fileHandler.getRootNode()).getUserObject();
		String path = standardise(xPath);
		if (xPath.equals("/")) {
			return parent;
		}
		String[] sections = path.split("/");
		for (int i = 0; i < sections.length; i++) {
			if (sections[i].length() == 0) {
				continue;
			}
			List<?> members = parent.getMemberList();
			boolean isFound = false;
			for (Object object : members) {
				if (object instanceof Group) {
					if (((Group) object).getName().equals(sections[i])) {
						parent = (Group) object;
						isFound = true;
					}
				}
			}
			if (!isFound) {
				return null;
			}
		}
		return parent;
	}

	/**
	 * Find the HDF dataset at a given xpath.
	 * 
	 * @param xPath
	 *            String value
	 * @return HDF Dataset
	 */
	protected Dataset findHdfDataset(final String xPath) {
		Group parent = findHdfGroup(getParentPath(xPath));
		if (parent == null) {
			return null;
		}
		List<?> members = parent.getMemberList();
		for (Object object : members) {
			if (object instanceof Dataset) {
				if (((Dataset) object).getName().equals(getShortName(xPath))) {
					return (Dataset) object;
				}
			}
		}
		return null;
	}

}
