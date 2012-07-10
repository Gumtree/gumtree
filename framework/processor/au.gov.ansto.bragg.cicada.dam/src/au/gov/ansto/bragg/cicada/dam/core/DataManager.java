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
package au.gov.ansto.bragg.cicada.dam.core;

import java.io.File;
import java.lang.management.MemoryUsage;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.gumtree.data.Factory;
import org.gumtree.data.ansto.io.IgorImporter;
import org.gumtree.data.ansto.io.NcHdfWriter;
import org.gumtree.data.exception.BackupException;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.interfaces.IAttribute;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IDimension;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.utils.Register;
import org.gumtree.data.utils.Register.RegisterListener;
import org.gumtree.data.utils.Utilities;
import org.gumtree.data.utils.Utilities.ModelType;

import au.gov.ansto.bragg.cicada.dam.core.exception.NullDataObjectException;
import au.gov.ansto.bragg.cicada.dam.internal.Activator;
import au.gov.ansto.bragg.datastructures.util.ConverterLib;

/**
 * @author nxi
 *
 */
public class DataManager {

	protected static Map<URI, PhantomReference> staticDataMap;
	protected static ReferenceQueue queue;
	private String dictionaryPath = null;
	protected List<URI> addressList = null;
	private static Random randomGenerator;
	private static RegisterListener REGISTER_LISTENER;
	public DataManager(){
		if (staticDataMap == null) staticDataMap = new HashMap<URI, PhantomReference>();
		if (queue == null) queue = new ReferenceQueue();
//		if (REGISTER_LISTENER == null)
//			addRegisterListener();
		addressList = new LinkedList<URI>();
		if (randomGenerator == null) randomGenerator = new Random();
	}

	private void addRegisterListener() {
		String tempdir = System.getProperty("java.io.tmpdir");
		tempdir = tempdir.replace('\\', '/');
		tempdir += "gumtree/" + Activator.getDefault().PLUGIN_ID + "/";
		File folder = new File(tempdir);
		if (!folder.exists())
			folder.mkdirs();
		else{
			File[] files = folder.listFiles();
			for (File file : files)
				file.delete();
		}
		
		String filename = System.currentTimeMillis() + ".hdf";
		URI tempUri = null;
//		tempUri = URI.create("file:/" + tempdir + filename);
		try {
			tempUri = ConverterLib.path2URI(tempdir + filename);
		} catch (FileAccessException e2) {
			e2.printStackTrace();
			tempUri = URI.create("file:/" + tempdir + filename);
		}
		
		IDataset reader = null;
		NcHdfWriter writer = null;
		try {
			writer = new NcHdfWriter(new File(tempdir + filename));
			reader = Factory.createDatasetInstance(tempUri);
			IGroup dataGroup = Factory.createGroup(reader.getRootGroup(), Register.DATA_GROUP_NAME, true);
			writer.writeToRoot(dataGroup);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (reader == null || writer == null)
			return;
		
		final Register register = Register.getInstance();
		register.setFileHandler(writer, reader);
		
		REGISTER_LISTENER = new RegisterListener(){

			public void arrayAdded() {
				MemoryUsage memory = Register.getHeapMemoryUsage();
				if (memory.getUsed() > memory.getMax() * 0.5){
					System.gc();
				}
				memory = Register.getHeapMemoryUsage();
				if (memory.getUsed() > memory.getMax() * 0.5){
					try {
						register.backupToFile();
					} catch (BackupException e) {
						e.printStackTrace();
					}
					System.gc();
				}

			}

			public void groupAdded() {

			}
		};
		register.addListener(REGISTER_LISTENER);
	}

	public DataManager(String dictionaryPath){
		this();
		this.dictionaryPath = dictionaryPath;
	}

	// [ANSTO][Tony] DataType has been replaced by ModelType in CDM 
	public ModelType checkDataType(URI uri){ 
		Object dataObject = null;
		try {
			dataObject = getObject(uri);
		} catch (FileAccessException e) {
			return null;
		}
		if (dataObject != null){
			return Utilities.checkModelType(dataObject);
		}
		return null;
	}

	public IDataItem getDataItem(URI uri) throws FileAccessException, NullDataObjectException{
		Object dataObject = getObject(uri);
		if (dataObject != null){
			try {
				return (IDataItem) dataObject;
			} catch (Exception e) {
				throw new NullDataObjectException(e);
			}
		}
		return null;
	}

	public IGroup getGroup(URI uri) throws FileAccessException, NullDataObjectException{
		Object dataObject = getObject(uri);
		if (dataObject != null){
			if (dataObject instanceof IGroup) return (IGroup) dataObject;
			else throw new NullDataObjectException("Failed to convert object into Group");
//			try {
//			return (Group) dataObject;
//			} catch (Exception e) {
//			throw new NullDataObjectException(e);
//			}
		}
		return null;	
	}

	public IAttribute getAttribute(URI uri) throws FileAccessException, NullDataObjectException{
		Object dataObject = getObject(uri);
		if (dataObject != null){
			try {
				return (IAttribute) dataObject;
			} catch (Exception e) {
				throw new NullDataObjectException(e);
			}
		}
		return null;
	}

	public IDimension getDimension(URI uri) throws FileAccessException, NullDataObjectException{
		Object dataObject = getObject(uri);
		if (dataObject != null){
			try {
				return (IDimension) dataObject;
			} catch (Exception e) {
				throw new NullDataObjectException(e);
			}
		}
		return null;	
	}

	public IDataset getDataset(URI uri) throws FileAccessException, NullDataObjectException{
		Object dataObject = getObject(uri);
		if (dataObject != null){
			try {
				return (IDataset) dataObject;
			} catch (Exception e) {
				throw new NullDataObjectException(e);
			}
		}
		return null;	
	}

	public Object getObject(URI uri) throws FileAccessException{
		if (staticDataMap.containsKey(uri)) {
			PhantomReference<?> object = staticDataMap.get(uri);
			Object target = object.get();
			if (target != null)
				return target;
		}
		String path = uri.getPath();
		if (path.contains(".ASC")){
			try {
				return importIgorData(uri, dictionaryPath);
			} catch (Exception e) {
				e.printStackTrace();
				throw new FileAccessException(e);
			}
		}
//		System.runFinalization();
//		System.gc();
//		System.runFinalization();
//		if (dataObject != null) staticDataMap.put(uri, new PhantomReference<Object>(dataObject, queue));
//		new PhantomReference<Object>(dataObject, queue);
//		MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
////		List<MemoryPoolMXBean> mempoolsmbeans = ManagementFactory.getMemoryPoolMXBeans();
////		List<GarbageCollectorMXBean> gcmbeans = ManagementFactory.getGarbageCollectorMXBeans();
//		MemoryUsage usage = memorymbean.getHeapMemoryUsage();
//		long usedMemory = usage.getUsed();
//		if (usedMemory > 8E8)
//			System.gc();
		Object dataObject = Utilities.findObject(uri, dictionaryPath);
		return dataObject;
	}

	public String getDictionaryPath() {
		return dictionaryPath;
	}

	public static void register(Object signal){
//		DataType dataType = Factory.checkDataType(signal);
//		switch(dataType){
//		case DataType.Group
//		}
	}

	public static IDataset createTempDataset() throws Exception{
		String tempdir = System.getProperty("java.io.tmpdir");
		if ( !(tempdir.endsWith("/") || tempdir.endsWith("\\")) )
			tempdir = tempdir + System.getProperty("file.separator");
		String filename = "temp" + randomGenerator.nextInt(10000) + ".hdf";
		URI tempUri = null;
		try {
			tempUri = new URI("file:/" + tempdir + filename);
		} catch (URISyntaxException e) {
//			e.printStackTrace();

		}
		IDataset dataset = Factory.createDatasetInstance(tempUri);
		return dataset;
	}

	public IGroup importIgorData(URI uri, String importHeaderFile) throws Exception{
		IgorImporter igorImporter = new IgorImporter();
		IGroup rootGroup = igorImporter.importData(uri.getPath(), importHeaderFile);
		if (dictionaryPath != null) {
			IDictionary dictionary = Factory.createDictionary();
			dictionary.readEntries(dictionaryPath);
			rootGroup.setDictionary(dictionary);
		}
		return rootGroup;
	}
	
}
