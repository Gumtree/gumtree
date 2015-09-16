package org.gumtree.msw;

public interface IRefIdProvider {
	// properties
	public int getSourceId();
	
	// methods
	public RefId nextId();
}
