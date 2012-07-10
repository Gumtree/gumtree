package org.gumtree.util.messaging;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.event.Event;

public class ReducedDelayEventExecutor implements IDelayEventExecutor {

	private long delay;

	private List<EventWrapper> eventWrappers;

	private Job job;

	public ReducedDelayEventExecutor() {
		this(-1);
	}

	public ReducedDelayEventExecutor(long delay) {
		this.delay = delay;
		eventWrappers = new ArrayList<ReducedDelayEventExecutor.EventWrapper>(2);
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
	public ReducedDelayEventExecutor activate() {
		if (getDelay() >= 0) {
			job.schedule(getDelay());
		}
		return this;
	}

	@Override
	public ReducedDelayEventExecutor deactivate() {
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
				// Update by removing old entry first
				eventWrappers.remove(eventWrapper);
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

		@Override
		public boolean equals(Object object) {
			// We only compare topics
			String topic1 = getEvent().getTopic();
			String topic2 = ((EventWrapper) object).getEvent().getTopic();
			EventHandler eventHandler1 = getDelayEventHandler();
			EventHandler eventHandler2 = ((EventWrapper) object)
					.getDelayEventHandler();
			return topic1.equals(topic2) && (eventHandler1 == eventHandler2);
		}

		public Event getEvent() {
			return event;
		}

		public DelayEventHandler getDelayEventHandler() {
			return eventHandler;
		}
	}

}
