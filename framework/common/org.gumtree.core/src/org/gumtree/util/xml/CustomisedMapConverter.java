/*******************************************************************************
 * Copyright (c) 2012 Australian Nuclear Science and Technology Organisation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bragg Institute - initial API and implementation
 ******************************************************************************/

package org.gumtree.util.xml;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.gumtree.util.collection.ParameterMap;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

// This is basically copied from the original xstream map converter.
// Changes: map key is now treated as attribute, not element
public class CustomisedMapConverter extends MapConverter {

	private static final String ATTRIBUTE_KEY = "key";
	
	public CustomisedMapConverter(Mapper mapper) {
		super(mapper);
	}

	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) {
		return (type.equals(HashMap.class)
				|| type.equals(Hashtable.class)
				|| (JVM.is14() && type.getName().equals(
						"java.util.LinkedHashMap")))
				&& !type.equals(ParameterMap.class);
	}

	@SuppressWarnings("rawtypes")
	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		Map map = (Map) source;
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			writer.startNode(mapper().serializedClass(Map.Entry.class));

//			writeItem(entry.getKey(), context, writer);
			writer.addAttribute(ATTRIBUTE_KEY, entry.getKey().toString());
			writeItem(entry.getValue(), context, writer);

			writer.endNode();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void populateMap(HierarchicalStreamReader reader,
			UnmarshallingContext context, Map map) {
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			
			Object key = reader.getAttribute(ATTRIBUTE_KEY);
//			reader.moveDown();
//			Object key = readItem(reader, context, map);
//			reader.moveUp();

			reader.moveDown();
			Object value = readItem(reader, context, map);
			reader.moveUp();

			map.put(key, value);

			reader.moveUp();
		}
	}

}
