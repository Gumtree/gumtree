package org.gumtree.msw.schedule.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScheduleWalker {
	// fields
	private final AtomicBoolean busy = new AtomicBoolean(false);
	private final List<IScheduleWalkerListener> listeners = new ArrayList<>();
		
	// properties
	public boolean isBusy() {
		return busy.get();
	}
	
	// methods
	public boolean walk(IScheduleProvider provider, IScheduleExecuter executer) {
		if (!busy.compareAndSet(false, true))
			return false;

		try {
			raiseOnBeginSchedule();
			try {
				if (!provider.initiate())
					return false;

				Summary initializationSummary = executer.initiate();
				raiseOnInitialized(initializationSummary);
				if (!initializationSummary.getInterrupted()) {
					for (ScheduleStep step = provider.firstStep(); step != null; step = provider.nextStep()) {
						boolean interrupted = false;
						
						raiseOnBeginStep(step);
						if (step.isEnabled()) {
							// parameters
							if (step.hasParameters()) {
								raiseOnBeginChangeParameters(step);
								
								ParameterChangeSummary summary = executer.setParameters(
										step.getElementPath().getElementName(),
										step.getParameters());
				
								raiseOnEndChangeParameters(step, summary);
								interrupted |= summary.getInterrupted();
							}
				
							// acquisition
							if (step.isAcquisition()) {
								// pre-acquisition
								if (!interrupted) {
									raiseOnBeginPreAcquisition(step);
	
									Summary summary = executer.preAcquisition();
	
									raiseOnEndPreAcquisition(step, summary);
									interrupted |= summary.getInterrupted();
								}
								// do-acquisition
								if (!interrupted) {
									raiseOnBeginDoAcquisition(step);
									
									AcquisitionSummary summary = executer.doAcquisition(step.getParameters());
									
									raiseOnEndDoAcquisition(step, summary);
									interrupted |= summary.getInterrupted();
								}
								// post-acquisition
								if (!interrupted) {
									raiseOnBeginPostAcquisition(step);
	
									Summary summary = executer.postAcquisition();
	
									raiseOnEndPostAcquisition(step, summary);
									interrupted |= summary.getInterrupted();
								}
							}
						}
						raiseOnEndStep(step);
						
						if (interrupted)
							return false;
					}
				}
			}
			finally {
				try {
					try {
						provider.cleanUp();
					}
					finally {
						Summary summary = executer.cleanUp();
						raiseOnCleanedUp(summary);
					}
				}
				finally {
					raiseOnEndSchedule();
				}
			}
			return true;
		}
		finally {
			busy.set(false);
		}
	}

	// listeners
	public synchronized void addListener(IScheduleWalkerListener listener) {
		if (listeners.contains(listener))
			throw new Error("listener already exists");
		
		listeners.add(listener);
	}
	public synchronized boolean removeListener(IScheduleWalkerListener listener) {
		return listeners.remove(listener);
	}
	// schedule
	private synchronized void raiseOnBeginSchedule() {
		for (IScheduleWalkerListener listener : listeners)
			listener.onBeginSchedule();
	}
	private synchronized void raiseOnEndSchedule() {
		for (IScheduleWalkerListener listener : listeners)
			listener.onEndSchedule();
	}
	// initialization
	private synchronized void raiseOnInitialized(Summary summary) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onInitialized(summary);
	}
	private synchronized void raiseOnCleanedUp(Summary summary) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onCleanedUp(summary);
	}
	// step
	private synchronized void raiseOnBeginStep(ScheduleStep step) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onBeginStep(step);
	}
	private synchronized void raiseOnEndStep(ScheduleStep step) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onEndStep(step);
	}
	// parameters
	private synchronized void raiseOnBeginChangeParameters(ScheduleStep step) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onBeginChangeParameter(step);
	}
	private synchronized void raiseOnEndChangeParameters(ScheduleStep step, ParameterChangeSummary summary) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onEndChangeParameters(step, summary);
	}
	// acquisition
	private synchronized void raiseOnBeginPreAcquisition(ScheduleStep step) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onBeginPreAcquisition(step);
	}
	private synchronized void raiseOnEndPreAcquisition(ScheduleStep step, Summary summary) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onEndPreAcquisition(step, summary);
	}
	private synchronized void raiseOnBeginDoAcquisition(ScheduleStep step) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onBeginDoAcquisition(step);
	}
	private synchronized void raiseOnEndDoAcquisition(ScheduleStep step, AcquisitionSummary summary) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onEndDoAcquisition(step, summary);
	}
	private synchronized void raiseOnBeginPostAcquisition(ScheduleStep step) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onBeginPostAcquisition(step);
	}
	private synchronized void raiseOnEndPostAcquisition(ScheduleStep step, Summary summary) {
		for (IScheduleWalkerListener listener : listeners)
			listener.onEndPostAcquisition(step, summary);
	}
}
