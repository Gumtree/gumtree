package org.gumtree.msw.dummy;

import java.util.concurrent.atomic.AtomicLong;

import org.gumtree.msw.IRefIdProvider;
import org.gumtree.msw.RefId;

public class DummyRefIdProvider implements IRefIdProvider {
	// finals
	public static final DummyRefIdProvider DEFAULT = new DummyRefIdProvider(RefId.SERVER_ID);
	
	// fields
	private final int sourceId;
	private final AtomicLong objectId;

	// construction
	public DummyRefIdProvider(int sourceId) {
		this.sourceId = sourceId;
		this.objectId = new AtomicLong();
	}

	// properties
	@Override
	public int getSourceId() {
		return sourceId;
	}
	
	// methods
	@Override
	public RefId nextId() {
		return new RefId(sourceId, objectId.incrementAndGet());
	}
}
