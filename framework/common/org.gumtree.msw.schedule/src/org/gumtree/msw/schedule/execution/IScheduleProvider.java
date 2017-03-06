package org.gumtree.msw.schedule.execution;

public interface IScheduleProvider {
	// methods
	public boolean initiate();
	public void cleanUp();
	// steps
	public ScheduleStep firstStep();
	public ScheduleStep nextStep();
}
