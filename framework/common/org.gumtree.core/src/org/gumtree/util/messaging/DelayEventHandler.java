package org.gumtree.util.messaging;

import org.osgi.service.event.Event;

public abstract class DelayEventHandler extends EventHandler {

	private IDelayEventExecutor delayEventExecutor;

	public DelayEventHandler(String topic, IDelayEventExecutor delayExecutor) {
		super(topic);
		this.delayEventExecutor = delayExecutor;
	}

	public DelayEventHandler(String topic, String filterKey,
			String filterValue, IDelayEventExecutor delayExecutor) {
		super(topic, filterKey, filterValue);
		this.delayEventExecutor = delayExecutor;
	}

	public DelayEventHandler(String topic, String filer,
			IDelayEventExecutor delayExecutor) {
		super(topic, filer);
		this.delayEventExecutor = delayExecutor;
	}

	@Override
	public void handleEvent(Event event) {
		// Delay the event dispatch if the executor is available,
		// otherwise it handles it straight away
		if (getDelayEventExecutor() != null) {
			getDelayEventExecutor().queueEvent(event, this);
		} else {
			handleDelayEvent(event);
		}
	}

	public abstract void handleDelayEvent(Event event);

	public IDelayEventExecutor getDelayEventExecutor() {
		return delayEventExecutor;
	}

	public void setDelayEventExecutor(IDelayEventExecutor delayExecutor) {
		this.delayEventExecutor = delayExecutor;
	}

}
