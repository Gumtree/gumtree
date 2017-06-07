package org.gumtree.msw;

public interface INotificationLock extends AutoCloseable {
	// methods
	@Override
	public void close();
}
