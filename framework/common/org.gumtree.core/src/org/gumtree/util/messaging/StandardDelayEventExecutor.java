package org.gumtree.util.messaging;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.event.Event;

public class StandardDelayEventExecutor implements IDelayEventExecutor {

	private long delay;

	private List<EventWrapper> eventWrappers;

	private Job job;

	public StandardDelayEventExecutor() {
		this(-1);
	}

	public StandardDelayEventExecutor(long delay) {
		this.delay = delay;
		eventWrappers = new ArrayList<StandardDelayEventExecutor.EventWrapper>(2);
		job = new Job("Delay event executor") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				synchronized (eventWrappers) {
					for (EventWrapper wrapper : eventWrappers) {
						wrapper.getDelayEventHandler().handleDelayEvent(
								wrapper.getEvent());
					}
					eventWrappers.clear();
				}
				if (getDelay() >= 0) {
					job.schedule(getDelay());
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
	}

	@Override
	public StandardDelayEventExecutor activate() {
		if (getDelay() >= 0) {
			job.schedule(getDelay());
		}
		return this;
	}

	@Override
	public StandardDelayEventExecutor deactivate() {
		job.cancel();
		synchronized (eventWrappers) {
			eventWrappers.clear();
		}
		return this;
	}

	@Override
	public void queueEvent(Event event, DelayEventHandler eventHandler) {
		EventWrapper eventWrapper = new EventWrapper(event, eventHandler);
		if (eventWrappers != null) {
			synchronized (eventWrappers) {
				eventWrappers.add(eventWrapper);
			}
		}
	}

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}
	
	class EventWrapper {
		private Event event;

		private DelayEventHandler eventHandler;

		private EventWrapper(Event event, DelayEventHandler eventHandler) {
			this.event = event;
			this.eventHandler = eventHandler;
		}

		public Event getEvent() {
			return event;
		}

		public DelayEventHandler getDelayEventHandler() {
			return eventHandler;
		}
	}

}
