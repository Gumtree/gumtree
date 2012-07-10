/*******************************************************************************
 * Copyright (c) 2007 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tony Lam (Bragg Institute) - initial API and implementation
 *******************************************************************************/

package org.gumtree.workflow.ui.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.gumtree.workflow.ui.util.ITaskDescriptor;
import org.gumtree.workflow.ui.util.ITaskRegistry;

public class TaskRegistry implements ITaskRegistry {

	// All unique desc
	private Map<String, ITaskDescriptor> descriptors;
	
	// Tag mapping
	private Map<String, List<ITaskDescriptor>> descriptorLookup;
	
	private volatile TaskRegistryReader reader;
	
	public TaskRegistry() {
		descriptors = new HashMap<String, ITaskDescriptor>();
		descriptorLookup = new HashMap<String, List<ITaskDescriptor>>();
	}
	
	protected void addTaskDescriptor(ITaskDescriptor desc) {
		// Caches descriptor
		descriptors.put(desc.getClassname(), desc);
		// Caches tag
		for (String tag : desc.getTags()) {
			List<ITaskDescriptor> descs = descriptorLookup.get(tag);
			if (descs == null) {
				descs = new ArrayList<ITaskDescriptor>(2);
				descriptorLookup.put(tag, descs);
			}
			descs.add(desc);
		}
	}
	
	public ITaskDescriptor[] getAllTaskDescriptors() {
		checkReader();
		Collection<ITaskDescriptor> descs = descriptors.values();
		return descs.toArray(new ITaskDescriptor[descs.size()]);
	}

	public String[] getAvailableTags() {
		checkReader();
		Set<String> keyset = new TreeSet<String>(descriptorLookup.keySet());
		return keyset.toArray(new String[keyset.size()]);
	}

	public ITaskDescriptor getTaskDescriptorById(String id) {
		checkReader();
		return descriptors.get(id);
	}

	public ITaskDescriptor[] getTaskDescriptorsByTag(String tag) {
		checkReader();
		List<ITaskDescriptor> descs = descriptorLookup.get(tag);
		if (descs != null) {
			return descs.toArray(new ITaskDescriptor[descs.size()]);
		}
		return new ITaskDescriptor[0];
	}
	
	private void checkReader() {
		if (reader == null) {
			synchronized (this) {
				if (reader == null) {
					reader = new TaskRegistryReader(this);
					reader.readTasks();
				}
			}
		}
	}
	
}
