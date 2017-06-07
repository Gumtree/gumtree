package org.gumtree.msw.model;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.msw.RefId;

class SerializationIdMapper implements IRefIdMapper {
	// fields
	private final Map<RefId, RefId> modelTofile;
	private long objectId;
	
	// construction
	public SerializationIdMapper() {
		modelTofile = new HashMap<>();
		objectId = 1;
	}
	
	// methods
	@Override
	public synchronized RefId map(RefId modelId) {
		RefId result = modelTofile.get(modelId);
		
		if (result == null) {
			result = new RefId(RefId.XML_ID, objectId++);
			modelTofile.put(modelId, result);
		}
		
		return result;
	}
}
