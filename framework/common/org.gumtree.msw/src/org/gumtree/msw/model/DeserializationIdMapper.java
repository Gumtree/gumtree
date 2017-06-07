package org.gumtree.msw.model;

import java.util.HashMap;
import java.util.Map;

import org.gumtree.msw.IRefIdProvider;
import org.gumtree.msw.RefId;

public class DeserializationIdMapper implements IRefIdMapper {
	// fields
	private final Map<RefId, RefId> modelTofile;
	private IRefIdProvider idProvider;

	// construction
	public DeserializationIdMapper(IRefIdProvider idProvider) {
		this.modelTofile = new HashMap<>();
		this.idProvider = idProvider;
	}
	
	@Override
	public RefId map(RefId modelId) {
		RefId result = modelTofile.get(modelId);
		
		if (result == null) {
			result = idProvider.nextId();
			modelTofile.put(modelId, result);
		}
		
		return result;
	}
}
