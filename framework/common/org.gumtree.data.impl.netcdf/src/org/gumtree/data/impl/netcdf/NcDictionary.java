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
package org.gumtree.data.impl.netcdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.IPath;
import org.gumtree.data.exception.FileAccessException;
import org.gumtree.data.impl.NcFactory;
import org.gumtree.data.interfaces.IDictionary;
import org.gumtree.data.interfaces.IKey;

/**
 * The implementation of IDictionary interface. The dictionary resolves paths
 * from a given key name.
 * 
 * @author nxi
 * 
 */
public class NcDictionary implements IDictionary {

	/**
	 * The default delimit to separate multiple paths in a single String value.
	 */
	public static final String DEFAULT_DELIMIT = ";";

	/**
	 * The dictionary table holder.
	 */
	private Map<IKey, IPath> itemMap = new TreeMap<IKey, IPath>();

	/**
	 * 
	 */
	public NcDictionary() {
	}

	@Override
	public IPath getPath(IKey key) {
		IPath path = itemMap.get(key);
		if (path != null && path.getValue().contains(DEFAULT_DELIMIT)) {
			String[] pathArray = path.getValue().split(DEFAULT_DELIMIT);
			for (int i = 0; i < pathArray.length; i++) {
				if (pathArray[i].trim().length() > 0) {
					return Factory.getFactory(getFactoryName()).createPath(pathArray[i]);
				}
			}
			return null;
		}
		return path;
	}

	@Override
	public List<IKey> getAllKeys() {
		return new ArrayList<IKey>(itemMap.keySet());
	}

	@Override
	public List<IPath> getAllPaths(IKey key) {
		IPath path = itemMap.get(key);
		if (path == null) {
			return null;
		}
		String[] pathArray = path.getValue().split(DEFAULT_DELIMIT);
		List<IPath> results = new ArrayList<IPath>();
		IFactory factory = Factory.getFactory(getFactoryName());
		for (String pathString : pathArray) {
			results.add(factory.createPath(pathString));
		}
		return results;
	}

	@Override
	public void addEntry(String key, String path) {
		addEntry(Factory.getFactory(getFactoryName()).createKey(key), Factory.getFactory(getFactoryName()).createPath(path));
	}
	
	@Override
	public void addEntry(String key, IPath path) {
		addEntry(Factory.getFactory(getFactoryName()).createKey(key), path);
	}
	
	public void addEntry(IKey key, IPath path) {
		IPath oldPath = itemMap.get(key);
		if (oldPath == null) {
			itemMap.put(key, path);
		} else {
			itemMap.put(
					key,
					Factory.getFactory(getFactoryName()).createPath(
							oldPath.getValue() + DEFAULT_DELIMIT
									+ path.getValue()));
		}
	}

	@Override
	public void readEntries(final URI uri) throws FileAccessException {
		File dicFile = new File(uri);
		if (!dicFile.exists()) {
			throw new FileAccessException(
					"the target dictionary file does not exist");
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(dicFile));
			while (br.ready()) {
				String line = br.readLine().trim();
				if (!line.startsWith("#") && !line.isEmpty()) {
					String[] temp = line.split("=");
					if (0<(temp[0].length())) {
						addEntry(temp[0].trim(), temp[1].trim());
					}

				}
			}
		} catch (Exception ex) {
			throw new FileAccessException("failed to open the dictionary file",
					ex);
		}
	}

	@Override
	public void removeEntry(String key, String path) {
		IKey keyObject = Factory.getFactory(getFactoryName()).createKey(key);
		IPath oldPath = itemMap.get(keyObject);
		if (oldPath != null) {
			String pathString = oldPath.getValue();
			if (pathString.contains(DEFAULT_DELIMIT)) {
				String[] paths = pathString.split(DEFAULT_DELIMIT);
				boolean isChanged = false;
				for (int i = 0; i < paths.length; i++) {
					if (paths[i].equals(path)) {
						paths[i] = null;
						isChanged = true;
					}
				}
				if (isChanged) {
					itemMap.put(keyObject, makePathString(paths));
				}
			} else {
				if (pathString.equals(path)) {
					itemMap.remove(key);
				}
			}
		}
	}

	/**
	 * Make a path string from give array of paths.
	 * 
	 * @param paths
	 *            array of Strings
	 * @return new String object
	 */
	private IPath makePathString(final String[] paths) {
		String newString = "";
		for (int i = 0; i < paths.length; i++) {
			if (paths[i] != null && paths[i].trim().length() > 0) {
				newString += paths[i];
			}
		}
		return Factory.getFactory(getFactoryName()).createPath(newString);
	}

	@Override
	public void removeEntry(final String key) {
		itemMap.remove(Factory.getFactory(getFactoryName()).createKey(key));
	}

	@Override
	public boolean containsKey(final String key) {
		return itemMap.containsKey(Factory.getFactory(getFactoryName()).createKey(key));
	}

	@Override
	public void readEntries(final String path) throws FileAccessException {
		File dicFile = new File(path);
		if (!dicFile.exists()) {
			throw new FileAccessException(
					"the target dictionar file does not exist");
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dicFile));
			while (br.ready()) {
				String line = br.readLine().trim();
				if (!line.startsWith("#") && !line.isEmpty()) {
					String[] temp = line.split("=");
					if (0<(temp[0].length())) {
						addEntry(temp[0].trim(), temp[1].trim());
					}

				}
			}
			br.close();
		} catch (Exception ex) {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			throw new FileAccessException("failed to open the dictionary file",
					ex);
		}
	}

	@Override
	public IDictionary clone() throws CloneNotSupportedException {
		NcDictionary dictionary = new NcDictionary();
		for (Entry<IKey, IPath> entry : itemMap.entrySet()) {
			dictionary.addEntry(entry.getKey(), entry.getValue());
		}
		return dictionary;
	}
	
	@Override
	public String getFactoryName() {
		return NcFactory.NAME;
	}
	
}
