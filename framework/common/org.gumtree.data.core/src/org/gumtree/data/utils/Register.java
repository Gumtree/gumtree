/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation,
 * Synchrotron SOLEIL and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Norman XIONG (Bragg Institute) - initial API and implementation
 *     Clément RODRIGUEZ (SOLEIL) - initial API and implementation
 *     Tony LAM (Bragg Institute) - implementation
 ******************************************************************************/

package org.gumtree.data.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gumtree.data.DataType;
import org.gumtree.data.Factory;
import org.gumtree.data.exception.BackupException;
import org.gumtree.data.interfaces.IArray;
import org.gumtree.data.interfaces.IDataItem;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.io.IWriter;

/**
 * @brief A register class for CDMA Arrays.
 * 
 * This class has the logic to control memory
 * usage of arrays. If too many data are loaded at the same time which is likely
 * to use up the JVM memory, it triggers the logic to dump some data into
 * physical storage such as hard drive. So that the memory will be cleaned up.
 * When the data is accessed in the future time, they will be loaded back.
 * 
 * @author nxi
 * @version 0.9 Beta (still under construction, not fully performing yet)
 */
public class Register {

	private final static Map<Long, WeakReference<IArray>> ARRAY_REGISTRY = new HashMap<Long, WeakReference<IArray>>();
	private final static Map<Long, WeakReference<IGroup>> GROUP_REGISTRY = new HashMap<Long, WeakReference<IGroup>>();

	private final static ReferenceQueue<IArray> ARRAY_QUEUE = new ReferenceQueue<IArray>();
	private final static ReferenceQueue<IGroup> GROUP_QUEUE = new ReferenceQueue<IGroup>();
	private final static List<RegisterListener> registerListeners = new ArrayList<RegisterListener>();
	public final static int LOCK_TIMEOUT = 3000;
	public final static long MINIMUM_ARRAY_SIZE = (long) 1E7;
	public final static long CLEAN_THRESHOLD = (long) 2E8;
	public final static long REPROCESSABLE_THRESHOLD = (long) 2E7;
	public static String DATA_GROUP_NAME = "storage";
	private static IWriter backupWriter;
	private static IDataset backupReader;
	private static Register instance;
	private boolean isLocked = false;

	public Register() {
		super();
	}

	protected boolean isLocked() {
		return isLocked;
	}
	
	protected IDataset getBackupReader() {
		return backupReader;
	}
	
	protected static boolean isInstantiated() {
		return (instance != null);
	}
	
	public static Register getInstance() {
		if (instance == null)
			instance = new Register();
		return instance;
	}

	public void setFileHandler(IWriter hdfWriter, IDataset dataset) {
		backupWriter = hdfWriter;
		backupReader = dataset;
	}

	public long getArrayRegisterId(IArray array) {
		long memorySize = getMemorySize(array);
		if (memorySize > CLEAN_THRESHOLD) {
			// clean();
		}
		long registerId = 0;
		if (memorySize > MINIMUM_ARRAY_SIZE) {
			registerId = getRegisterId();
		}
		return registerId;
	}

	public void registerNewArray(IArray array) {
		while (isLocked) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		WeakReference<IArray> arrayReference = new WeakReference<IArray>(array,
				ARRAY_QUEUE);
		long registerId = array.getRegisterId();
		ARRAY_REGISTRY.put(registerId, arrayReference);
		fireArrayRegistered();
	}

	public long registerNewGroup(IGroup group) {
		WeakReference<IGroup> groupReference = new WeakReference<IGroup>(group,
				GROUP_QUEUE);
		long registerId = getRegisterId();
		GROUP_REGISTRY.put(registerId, groupReference);
		fireGroupRegistered();
		return registerId;
	}

	public static void clean() {
		// [SOLEIL][clement] TODO Shall we remove this GC call ? It's seen as a major violation by SONAR: our code analyzer and may cause performance leak 
		System.gc();
		for (Entry<Long, WeakReference<IArray>> referenceEntry : ARRAY_REGISTRY
				.entrySet())
			if (referenceEntry.getValue().get() == null)
				ARRAY_REGISTRY.remove(referenceEntry.getKey());
		for (Entry<Long, WeakReference<IGroup>> referenceEntry : GROUP_REGISTRY
				.entrySet())
			if (referenceEntry.getValue().get() == null)
				ARRAY_REGISTRY.remove(referenceEntry.getKey());
	}

	public void addListener(RegisterListener listener) {
		registerListeners.add(listener);
	}

	public void removeListener(RegisterListener listener) {
		registerListeners.remove(listener);
	}

	private void fireArrayRegistered() {
		for (RegisterListener listener : registerListeners)
			listener.arrayAdded();
	}

	private void fireGroupRegistered() {
		for (RegisterListener listener : registerListeners)
			listener.groupAdded();
	}

	public interface RegisterListener {
		public void arrayAdded();

		public void groupAdded();
	}

	public static long getMemorySize(IArray array) {
		long size = array.getSize();
		Class<?> type = array.getElementType();
		DataType dataType = DataType.getType(type);
		return size * dataType.getSize();
	}

	private long getRegisterId() {
		return System.nanoTime();
	}

	public void backupToFile() throws BackupException {
		if (isLocked)
			return;
		isLocked = true;
		// storage.addDataItem(v);
		if (backupWriter == null || backupReader == null)
			return;
		List<Long> toBeRemovedId = new ArrayList<Long>();
		for (Entry<Long, WeakReference<IArray>> arrayEntry : ARRAY_REGISTRY
				.entrySet()) {
			IArray array = arrayEntry.getValue().get();
			if (array != null)
				try {
					array.releaseStorage();
				} catch (Exception e) {
					isLocked = false;
					e.printStackTrace();
				}
			else {
				try {
					String stringId = String.valueOf(arrayEntry.getKey());
					IDataItem item = backupReader.getRootGroup().getGroup(
							DATA_GROUP_NAME).getDataItem(stringId);
					if (item != null)
						backupWriter.removeDataItem("/" + DATA_GROUP_NAME + "/"
								+ stringId);
					// ARRAY_REGISTRY.remove(arrayEntry.getKey());
					toBeRemovedId.add(arrayEntry.getKey());
				} catch (Exception e) {
					// isLocked = false;
					e.printStackTrace();
				}
			}
		}
		for (Long registryID : toBeRemovedId) {
			ARRAY_REGISTRY.remove(registryID);
		}
		isLocked = false;
	}

	public boolean backupArray(long registerId, IArray array)
			throws BackupException {
		if (backupWriter == null || backupReader == null)
			throw new BackupException("backup file handler does not exist");
		try {
			String itemName = String.valueOf(registerId);
			IDataItem item = backupReader.getRootGroup().getGroup(
					DATA_GROUP_NAME).getDataItem(itemName);
			array.lock();
			if (item == null) {
				item = Factory.createDataItem(backupReader.getRootGroup(),
						itemName, array);
				backupReader.getRootGroup().getGroup(DATA_GROUP_NAME)
						.addDataItem(item);
				// backupWriter.addRootGroup(backupReader.getRootGroup());
				backupWriter.writeDataItem("/" + DATA_GROUP_NAME, item);
			} else {
				if (array.isDirty()) {
					item.setCachedData(array, false);
					// backupWriter.addRootGroup(backupReader.getRootGroup());
					// backupWriter.replaceVariable(DATA_GROUP_NAME, item);
					backupWriter.writeDataItem("/" + DATA_GROUP_NAME, item);
				}
			}
			array.unlock();
			item.invalidateCache();
		} catch (Exception e) {
			array.unlock();
			throw new BackupException("failed to backup the data", e);
		}
		return true;
	}

	public static MemoryUsage getHeapMemoryUsage() {
		MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
		return memorymbean.getHeapMemoryUsage();
	}
}
