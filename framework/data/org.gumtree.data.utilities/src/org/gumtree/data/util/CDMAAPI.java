package org.gumtree.data.util;

import java.io.File;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;

/**
 * A class used to manage and access dataSet by using CDMA.
 * 
 * @author saintin
 */
public class CDMAAPI {

    // Read the dataPath value in the index group

    private static Map<String, IDataset> dataSetMap = new HashMap<String, IDataset>();
    private static Map<String, IGroup> rootGroupMap = new HashMap<String, IGroup>();
    private static Map<String, IDataItem> dataItemMap = new HashMap<String, IDataItem>();
    private static Map<String, IArray> arrayMap = new HashMap<String, IArray>();
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");

    /**
     * Read a DataSet from a sourcePath and with a factoryId
     * 
     * @param sourcePath, the path of the source file
     * @param factoryId, the factory Id
     * @return IDataset
     * @see IDataset
     */
    public static IDataset readDataSet(String sourcePath, String factoryId) throws Exception {
        IDataset dataSet = null;
        if (sourcePath != null && !sourcePath.isEmpty()) {
            if (dataSetMap.containsKey(sourcePath)) {
                dataSet = dataSetMap.get(sourcePath);
            }
            else {
                IFactory factory = Factory.getFactory(factoryId);
                dataSet = factory.createDatasetInstance(new File(sourcePath).toURI());
                if (dataSet != null) {
                    dataSetMap.put(sourcePath, dataSet);
                }
            }

        }
        return dataSet;
    }

    /**
     * Read a the root Group from a sourcePath and with a factoryId
     * 
     * @param sourcePath, the path of the source file
     * @param factoryId, the factory Id
     * @return IGroup
     * @see IGroup
     */
    public static IGroup readRootGoup(String sourcePath, String factoryId) throws Exception {
        IGroup rootGroup = null;
        if (sourcePath != null && !sourcePath.isEmpty()) {
            if (rootGroupMap.containsKey(sourcePath)) {
                rootGroup = rootGroupMap.get(sourcePath);
            }
            else {
                IDataset dataSet = readDataSet(sourcePath, factoryId);
                if (dataSet != null) {
                    dataSet.open();
                    rootGroup = dataSet.getRootGroup();
                    dataSet.close();
                    if (rootGroup != null) {
                        rootGroupMap.put(sourcePath, rootGroup);
                    }
                }
            }
        }
        return rootGroup;
    }

    /**
     * Read an item storage from a sourcePath and with a factoryId, the dataPath and the index
     * position of the group
     * 
     * @param sourcePath, the path of the source file
     * @param factoryId, the factory Id
     * @param dataPath, the dataPath in the source file
     * @param indexGroup, the index of the group in the file
     * @return Object, the storage contained in the dataPath
     */
    // If we dont know the NXEntry we can read the first group
    public static Object readItemStorage(String sourcePath, String factoryId, String dataPath,
            int indexGroup) throws Exception {
        Object storage = null;
        IGroup rootGroup = readRootGoup(sourcePath, factoryId);
        if (rootGroup != null) {
            List<IGroup> groupList = rootGroup.getGroupList();
            if (groupList != null && !groupList.isEmpty() && indexGroup < groupList.size()) {
                IGroup tmpGroup = groupList.get(indexGroup);
                String groupName = tmpGroup.getName();
                storage = readItemStorage(sourcePath, factoryId, groupName + dataPath);
            }
        }
        return storage;
    }

    /**
     * Read an item storage from a sourcePath and with a factoryId, the dataPath
     * 
     * @param sourcePath, the path of the source file
     * @param factoryId, the factory Id
     * @param dataPath, the dataPath in the source file
     * @return Object, the storage contained in the dataPath
     */
    public static Object readItemStorage(String sourcePath, String factoryId, String dataPath)
            throws Exception {
        Object storage = null;
        IArray array = readArray(sourcePath, factoryId, dataPath);
        if (array != null) {
            storage = array.getStorage();
        }
        return storage;

    }

    /**
     * Read an item date from a sourcePath and with a factoryId, the dataPath and the index position
     * of the group
     * 
     * @param sourcePath, the path of the source file
     * @param factoryId, the factory Id
     * @param dataPath, the dataPath in the source file
     * @param indexGroup, the index of the group in the file
     * @return Date, the date of the Item
     */
    // If we dont know the NXEntry we can read the first group
    public static Date readItemDate(String sourcePath, String factoryId, String dataPath,
            int indexGroup) throws Exception {
        Date value = null;
        IGroup rootGroup = readRootGoup(sourcePath, factoryId);
        if (rootGroup != null) {
            if (rootGroup != null) {
                List<IGroup> groupList = rootGroup.getGroupList();
                if (groupList != null && !groupList.isEmpty() && indexGroup < groupList.size()) {
                    IGroup tmpGroup = groupList.get(indexGroup);
                    String groupName = tmpGroup.getName();
                    value = readItemDate(sourcePath, factoryId, groupName + dataPath);

                }
            }
        }

        return value;

    }

    /**
     * Read an item date from a sourcePath and with a factoryId, the dataPath
     * 
     * @param sourcePath, the path of the source file
     * @param factoryId, the factory Id
     * @param dataPath, the dataPath in the source file
     * @return Date, the date of the Item
     */
    public static Date readItemDate(String sourcePath, String factoryId, String dataPath)
            throws Exception {
        Date date = null;
        IDataItem item = readDateItem(sourcePath, factoryId, dataPath);
        if (item != null) {
            IAttribute attribute = item.getAttribute("_lastModified");
            date = parseValue(attribute.getStringValue());
        }
        return date;

    }

    /**
     * Read an IDataItem from a sourcePath and with a factoryId, the dataPath
     * 
     * @param sourcePath, the path of the source file
     * @param factoryId, the factory Id
     * @param dataPath, the dataPath in the source file
     * @return IDataItem
     * @see IDataItem
     */
    public static IDataItem readDateItem(String sourcePath, String factoryId, String dataPath)
            throws Exception {
        IDataItem item = null;

        if (sourcePath != null && !sourcePath.isEmpty() && dataPath != null && !dataPath.isEmpty()) {
            String key = sourcePath + dataPath;
            if (dataItemMap.containsKey(key)) {
                item = dataItemMap.get(key);
            }
            if (item == null) {
                IDataset dataSet = readDataSet(sourcePath, factoryId);
                if (dataSet != null) {
                    dataSet.open();
                    IGroup rootGroup = dataSet.getRootGroup();
                    if (rootGroup != null) {
                        item = (IDataItem) rootGroup.findContainerByPath(dataPath);
                    }
                    dataSet.close();
                    if (item != null) {
                        dataItemMap.put(key, item);
                    }
                }

            }
        }
        return item;

    }

    /**
     * Read an IArray from a sourcePath and with a factoryId, the dataPath
     * 
     * @param sourcePath, the path of the source file
     * @param factoryId, the factory Id
     * @param dataPath, the dataPath in the source file
     * @return IArray
     * @see IArray
     */
    public static IArray readArray(String sourcePath, String factoryId, String dataPath)
            throws Exception {
        IArray array = null;
        if (sourcePath != null && !sourcePath.isEmpty() && dataPath != null && !dataPath.isEmpty()) {
            String key = sourcePath + dataPath;
            if (arrayMap.containsKey(key)) {
                array = arrayMap.get(key);
            }
            if (array == null) {
                IDataset dataSet = readDataSet(sourcePath, factoryId);
                if (dataSet != null) {
                    dataSet.open();
                    IGroup rootGroup = dataSet.getRootGroup();
                    if (rootGroup != null) {
                        IDataItem item = (IDataItem) rootGroup.findContainerByPath(dataPath);
                        if (item != null) {
                            array = item.getData();
                            if (array != null) {
                                arrayMap.put(key, array);
                            }
                        }
                    }
                    dataSet.close();
                }
            }
        }
        return array;
    }

    private static Date parseValue(String value) {
        Date date = null;
        if (value != null) {
            // Replace T
            String dateValue = value.replaceFirst("T", " ");
            // Replace Z
            dateValue = dateValue.replaceFirst("Z", " ");
            try {
                date = simpleDateFormat.parse(dateValue);
            }
            catch (ParseException e) {
            }
        }
        return date;
    }

    /**
     * parse a Storage on a String format
     * 
     * @param storage, the storage
     * @return String, the string format of the storage
     */
    public static String parseStorage(Object storage) {
        int index = 0;
        Character oneChar = (Character) Array.get(storage, index);
        Vector<Character> vector = new Vector<Character>();
        while (oneChar != null) {
            vector.add(oneChar);
            index++;
            try {
                oneChar = (Character) Array.get(storage, index);
            }
            catch (Exception e) {
                oneChar = null;
            }
        }

        Iterator<Character> iterator = vector.iterator();
        char[] charArray = new char[vector.size()];
        index = 0;
        while (iterator.hasNext()) {
            charArray[index] = iterator.next().charValue();
            index++;
        }

        String readValue = "";
        if (charArray != null) {
            readValue = String.valueOf(charArray);
        }
        return readValue;
    }

}
