package org.gumtree.util.messaging;

import org.osgi.service.event.Event;

public interface IDelayEventExecutor {

	public void queueEvent(Event event, DelayEventHandler eventHandler);

	public IDelayEventExecutor activate();

	public IDelayEventExecutor deactivate();

}
