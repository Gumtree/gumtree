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

import java.util.Iterator;
import java.util.Map;

import org.gumtree.util.collection.ParameterMap;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class ParameterMapConverter extends MapConverter {

	private static final String ATTRIBUTE_KEY = "key";
	
	public ParameterMapConverter(Mapper mapper) {
        super(mapper);
    }

	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) {
		return type.equals(ParameterMap.class);
	}
//	public boolean canConvert(Class type) {
//		return type.equals(HashMap.class)
//				|| type.equals(Hashtable.class)
//				|| type.equals(ParameterMap.class)
//				|| (JVM.is14() && type.getName().equals(
//						"java.util.LinkedHashMap"));
//	}

	@SuppressWarnings("rawtypes")
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Map map = (Map) source;
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            writer.startNode(mapper().serializedClass(Map.Entry.class));
            writer.addAttribute(ATTRIBUTE_KEY, entry.getKey().toString());
            writer.setValue(entry.getValue().toString());
            writer.endNode();
        }
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map map = (Map) createCollection(context.getRequiredType());
        while (reader.hasMoreChildren()) {
        	reader.moveDown();
        	Object key = reader.getAttribute(ATTRIBUTE_KEY);
        	Object value = reader.getValue();
            map.put(key, value);
            reader.moveUp();
        }
        return map;
	}
	
}
