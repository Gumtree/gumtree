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
import java.util.Map.Entry;

import org.gumtree.util.collection.Parameters;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

public class ParametersConverter extends MapConverter {
	
	public ParametersConverter(Mapper mapper) {
        super(mapper);
    }
	
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) {
		return type.equals(Parameters.class);
	}

	@SuppressWarnings("unchecked")
	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        Map<String, Object> map = (Map<String, Object>) source;
        for (Iterator<Entry<String, Object>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Entry<String, Object> entry = (Entry<String, Object>) iterator.next();
            // Use key as node name
            writer.startNode(entry.getKey());
            if (entry.getValue() instanceof String) {
                // Support short (no tag) for string
            	writer.setValue(entry.getValue().toString());
            } else {
                // Otherwise treat this as normal object
            	writeItem(entry.getValue(), context, writer);
            }
            writer.endNode();
        }
	}
	
	@SuppressWarnings("unchecked")
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        Map<String, Object> map = (Map<String, Object>) createCollection(context.getRequiredType());
        while (reader.hasMoreChildren()) {
        	reader.moveDown();
        	// Use key as node name
        	String key = reader.getNodeName();
        	String value = reader.getValue();
        	if (value != null && value.trim().length() != 0) {
        		// Read shortcut string
        		map.put(key, value);
        	} else {
        		// Read normal object
        		reader.moveDown();
        		Object object = readItem(reader, context, map);
        		map.put(key, object);
        		reader.moveUp();
        	}
            reader.moveUp();
        }
        return map;
	}
	
}
