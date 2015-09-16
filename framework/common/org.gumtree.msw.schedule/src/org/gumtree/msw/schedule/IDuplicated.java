package org.gumtree.msw.schedule;

interface IDuplicated<T> {
	// methods
    public T getOriginal();
    public T getDuplicate();
}
